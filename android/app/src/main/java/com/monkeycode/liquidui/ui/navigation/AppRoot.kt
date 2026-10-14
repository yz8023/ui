package com.monkeycode.liquidui.ui.navigation

import android.os.Build
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.monkeycode.liquidui.ui.components.AuroraBackground
import com.monkeycode.liquidui.ui.components.GlassDialogHost
import com.monkeycode.liquidui.ui.components.LiquidBottomTab
import com.monkeycode.liquidui.ui.components.LiquidBottomTabs
import com.monkeycode.liquidui.ui.components.LocalGlassBackdrop
import com.monkeycode.liquidui.ui.components.LocalGlassContentBackdrop
import com.monkeycode.liquidui.ui.components.LocalGlassDialogHost
import com.monkeycode.liquidui.ui.features.rememberSystemWallpaper
import com.monkeycode.liquidui.ui.motion.运动
import com.monkeycode.liquidui.ui.screens.AboutScreen
import com.monkeycode.liquidui.ui.screens.AppSettingsState
import com.monkeycode.liquidui.ui.screens.PermissionCenterScreen
import com.monkeycode.liquidui.ui.screens.PrivacyScreen
import com.monkeycode.liquidui.ui.screens.WallpaperMode
import com.monkeycode.liquidui.ui.screens.ComponentsScreen
import com.monkeycode.liquidui.ui.screens.HomeScreen
import com.monkeycode.liquidui.ui.screens.FeatureTarget
import com.monkeycode.liquidui.ui.screens.运动屏幕
import com.monkeycode.liquidui.ui.screens.SettingsScreen
import com.monkeycode.liquidui.ui.screens.素材浏览器
import com.monkeycode.liquidui.ui.theme.LocalAppChrome
import com.monkeycode.liquidui.ui.theme.OShinTokens
import com.monkeycode.liquidui.ui.theme.VisualMode
import kotlin.math.max
import kotlinx.coroutines.launch

private enum class InfoDestination { About, Privacy, Permissions }

/**
 * App shell: background layer, one screen, bottom bar, dialog overlay stack.
 *
 * Two recorded [com.kyant.backdrop.Backdrop] layers power the glass:
 *  - `auroraBackdrop` records the decoration (aurora blobs or the device
 *    wallpaper when enabled); every in-page glass panel samples it.
 *  - `contentBackdrop` records the screens; the liquid tab bar samples a
 *    combined layer so content scrolling beneath it refracts through it.
 *
 * When the master chrome knob is off (`Normal` or `Md3`), the shell drops the
 * layer machinery and renders a plain Material layout (solid cards + [NavigationBar]).
 */
@Composable
fun AppRoot(settings: AppSettingsState) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var infoDestination by remember { mutableStateOf<InfoDestination?>(null) }
    val screens = Screen.entries
    val chrome = LocalAppChrome.current
    val glassEnabled = chrome.visualMode == VisualMode.Glass
    val oshinEnabled = chrome.visualMode == VisualMode.OShin
    val wallpaperBackground = chrome.wallpaperBackground
    val wallpaperMode = settings.wallpaperMode
    val cachedWallpaperPath = settings.cachedWallpaperPath
    val wallpaperRevision = settings.wallpaperRevision
    val surfaceColor = MaterialTheme.colorScheme.background

    val context = LocalContext.current
    // System wallpaper: direct WallpaperManager read with graceful fallback
    // (see features/SystemWallpaper.kt — no permission gate; the holder
    // re-reads on every foreground return so a changed home-screen wallpaper
    // is picked up live). Always composed; only selected in System mode.
    val systemWallpaper = rememberSystemWallpaper()
    val cachedDrawable = remember(cachedWallpaperPath, wallpaperRevision, context) {
        cachedWallpaperPath
            ?.let(BitmapFactory::decodeFile)
            ?.let { BitmapDrawable(context.resources, it) }
    }
    val wallpaperDrawable = when (wallpaperMode) {
        WallpaperMode.Aurora -> null
        WallpaperMode.Cached -> cachedDrawable
        WallpaperMode.System -> systemWallpaper
    }

    val auroraBackdrop = rememberLayerBackdrop(graphicsLayer = rememberGraphicsLayer()) {
        when {
            wallpaperDrawable != null -> drawWallpaperCover(wallpaperDrawable)
            else -> drawRect(surfaceColor)
        }
        drawContent()
    }
    val contentBackdrop = rememberLayerBackdrop(graphicsLayer = rememberGraphicsLayer()) { drawContent() }
    val fullSceneBackdrop = if (glassEnabled) {
        rememberCombinedBackdrop(auroraBackdrop, contentBackdrop)
    } else null
    val dialogHost = remember { GlassDialogHost() }

    val screenContent: @Composable () -> Unit = {
        val currentChrome = LocalAppChrome.current
        val bounceSpec = if (currentChrome.reducedMotion) {
            tween<Float>(运动.Duration.Fast)
        } else {
            运动.bouncy(0.001f)
        }
        AnimatedContent(
            targetState = selectedIndex to infoDestination,
            transitionSpec = {
                (scaleIn(initialScale = 0.94f, animationSpec = bounceSpec) +
                    fadeIn(animationSpec = tween(运动.Duration.Fast))) togetherWith
                    (scaleOut(targetScale = 0.97f, animationSpec = tween(运动.Duration.Fast)) +
                        fadeOut(animationSpec = tween(运动.Duration.Fast)))
            },
            label = "screenSwitch"
        ) { destination ->
            val info = destination.second
            if (info != null) {
                when (info) {
                    InfoDestination.About -> AboutScreen { infoDestination = null }
                    InfoDestination.Privacy -> PrivacyScreen { infoDestination = null }
                    InfoDestination.Permissions -> PermissionCenterScreen { infoDestination = null }
                }
                return@AnimatedContent
            }
            TemplatePage(
                index = destination.first,
                settings = settings,
                onNavigate = { selectedIndex = it },
                onOpenInfo = { infoDestination = it }
            )
        }
    }

    // O神壳层内容：左右翻页（HorizontalPager）+ 每页原有大标题，
    // 与玻璃模式的 AnimatedContent 缩放切换刻意不同 —— 这是布局级切换的一部分。
    val oshinPagerState = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = { screens.size }
    )
    val oshinScope = rememberCoroutineScope()
    androidx.compose.runtime.LaunchedEffect(oshinPagerState, oshinEnabled) {
        if (!oshinEnabled) return@LaunchedEffect
        snapshotFlow { oshinPagerState.settledPage }.collect { page ->
            if (page != selectedIndex) selectedIndex = page
        }
    }
    androidx.compose.runtime.LaunchedEffect(selectedIndex, oshinEnabled) {
        if (oshinEnabled && oshinPagerState.settledPage != selectedIndex) {
            oshinPagerState.animateScrollToPage(selectedIndex)
        }
    }

    BackHandler(enabled = infoDestination != null) { infoDestination = null }

    CompositionLocalProvider(
        LocalGlassBackdrop provides (if (glassEnabled) auroraBackdrop else null),
        LocalGlassContentBackdrop provides fullSceneBackdrop,
        LocalGlassDialogHost provides dialogHost
    ) {
        Box(Modifier.fillMaxSize().background(surfaceColor)) {
            if (glassEnabled || (wallpaperBackground && !oshinEnabled)) {
                Box(Modifier.matchParentSize().layerBackdrop(auroraBackdrop)) {
                    when {
                        wallpaperDrawable != null -> {
                            // Visible wallpaper: draw it as real content so it shows
                            // on screen AND is captured by the sampled layer above.
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .drawBehind { drawWallpaperCover(wallpaperDrawable) }
                            )
                        }
                        glassEnabled -> AuroraBackground(Modifier.matchParentSize()) {}
                    }
                }
                Box(Modifier.matchParentSize().layerBackdrop(contentBackdrop)) {
                    Box(Modifier.matchParentSize()) { screenContent() }
                }
            } else if (oshinEnabled && infoDestination == null) {
                // O神：翻页布局。页面本身的大标题保留（与 OShin 各页一致），
                // 壳层只负责底色、翻页与悬浮底栏。
                HorizontalPager(
                    state = oshinPagerState,
                    modifier = Modifier.matchParentSize(),
                    beyondViewportPageCount = 1
                ) { page ->
                    // 页间模糊（同 OShin：偏移越大越糊，上限 16dp；API 31+ 才有 RenderEffect）。
                    val pageOffset = runCatching { oshinPagerState.getOffsetDistanceInPages(page) }
                        .getOrDefault(0f)
                    val blurPx = kotlin.math.abs(pageOffset) *
                        with(LocalDensity.current) { 16.dp.toPx() }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurPx > 0.5f) {
                                    renderEffect = BlurEffect(blurPx, blurPx, TileMode.Decal)
                                }
                            }
                    ) {
                        TemplatePage(
                            index = page,
                            settings = settings,
                            onNavigate = { selectedIndex = it },
                            onOpenInfo = { infoDestination = it }
                        )
                    }
                }
            } else {
                Box(Modifier.matchParentSize()) { screenContent() }
            }

            if (infoDestination == null && oshinEnabled) {
                OShinBottomTabs(
                    selectedIndex = selectedIndex,
                    screens = screens,
                    onTabSelected = { index ->
                        selectedIndex = index
                        oshinScope.launch { oshinPagerState.animateScrollToPage(index) }
                    }
                )
            } else if (infoDestination == null && glassEnabled) {
                LiquidBottomTabs(
                    selectedTabIndex = { selectedIndex },
                    onTabSelected = { selectedIndex = it },
                    backdrop = rememberCombinedBackdrop(auroraBackdrop, contentBackdrop),
                    tabsCount = screens.size,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 14.dp)
                ) {
                    screens.forEachIndexed { index, screen ->
                        LiquidBottomTab(
                            onClick = { selectedIndex = index },
                            index = index
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Visible
                            )
                        }
                    }
                }
            } else if (infoDestination == null) {
                SolidBottomTabs(
                    selectedIndex = selectedIndex,
                    onTabSelected = { selectedIndex = it },
                    screens = screens
                )
            }

            dialogHost.items.forEach { overlay ->
                key(overlay) { overlay() }
            }
        }
    }
}

/** One template page body, shared by the glass shell (AnimatedContent) and the OShin pager shell. */
@Composable
private fun TemplatePage(
    index: Int,
    settings: AppSettingsState,
    onNavigate: (Int) -> Unit,
    onOpenInfo: (InfoDestination) -> Unit
) {
    when (Screen.entries[index]) {
        Screen.Home -> HomeScreen(
            settings = settings,
            onOpenComponents = { onNavigate(Screen.Components.ordinal) },
            onOpenMotion = { onNavigate(Screen.Motion.ordinal) }
        )
        Screen.Components -> ComponentsScreen()
        Screen.Motion -> 运动屏幕(settings = settings)
        Screen.Settings -> SettingsScreen(
            settings = settings,
            onOpenAbout = { onOpenInfo(InfoDestination.About) },
            onOpenPrivacy = { onOpenInfo(InfoDestination.Privacy) },
            onOpenPermissions = { onOpenInfo(InfoDestination.Permissions) },
        )
        Screen.Gallery -> 素材浏览器 { target ->
            when (target) {
                FeatureTarget.Components -> onNavigate(Screen.Components.ordinal)
                FeatureTarget.Motion -> onNavigate(Screen.Motion.ordinal)
                FeatureTarget.Settings -> onNavigate(Screen.Settings.ordinal)
                FeatureTarget.Permissions -> onOpenInfo(InfoDestination.Permissions)
                FeatureTarget.About -> onOpenInfo(InfoDestination.About)
                FeatureTarget.Onboarding -> settings.resetOnboarding()
            }
        }
    }
}

/**
 * O神悬浮底栏：左右留 40dp、底部 16dp、高 64dp 的胶囊容器，
 * 图标 26dp + 标签 11sp，选中为系统蓝（浅 #0088FF / 深 #0091FF）。
 * 参数对齐 OShin 的 BottomTabs（其容器另有 backdrop 模糊，本模板在
 * 非玻璃壳层用高不透明度卡面 + 阴影近似，保证内容滚过时仍可读）。
 */
@Composable
private fun BoxScope.OShinBottomTabs(
    selectedIndex: Int,
    screens: List<Screen>,
    onTabSelected: (Int) -> Unit
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accent = if (dark) OShinTokens.BottomAccentDark else OShinTokens.BottomAccentLight
    val container = if (dark) OShinTokens.BottomContainerDark else OShinTokens.BottomContainerLight
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = OShinTokens.BottomBarSidePadding, vertical = OShinTokens.BottomBarBottomPadding)
            .shadow(10.dp, RoundedCornerShape(percent = 50), clip = false)
            .clip(RoundedCornerShape(percent = 50))
            .background(container)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        screens.forEachIndexed { index, screen ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(
                        if (selected) MaterialTheme.colorScheme.onSurface.copy(alpha = if (dark) 0.16f else 0.08f)
                        else androidx.compose.ui.graphics.Color.Transparent
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onTabSelected(index) }
                    )
                    .padding(vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Icon(
                    imageVector = screen.icon,
                    contentDescription = screen.title,
                    tint = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = screen.title,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,
                    color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BoxScope.SolidBottomTabs(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    screens: List<Screen>
) {
    NavigationBar(modifier = Modifier.align(Alignment.BottomCenter)) {
        screens.forEachIndexed { index, screen ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onTabSelected(index) },
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) }
            )
        }
    }
}

/** Cover-draws a [Drawable] (device wallpaper) scaled to fill the layer. */
private fun DrawScope.drawWallpaperCover(drawable: Drawable) {
    val iw = drawable.intrinsicWidth
    val ih = drawable.intrinsicHeight
    if (iw > 0 && ih > 0) {
        val scale = max(size.width / iw, size.height / ih)
        val w = iw * scale
        val h = ih * scale
        val left = (size.width - w) / 2f
        val top = (size.height - h) / 2f
        drawable.setBounds(left.toInt(), top.toInt(), (left + w).toInt(), (top + h).toInt())
    } else {
        drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
    }
    drawable.draw(drawContext.canvas.nativeCanvas)
}
