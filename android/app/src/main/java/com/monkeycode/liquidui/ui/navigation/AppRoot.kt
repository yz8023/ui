package com.monkeycode.liquidui.ui.navigation

import android.app.WallpaperManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
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
import com.monkeycode.liquidui.ui.screens.AppSettingsState
import com.monkeycode.liquidui.ui.screens.ComponentsScreen
import com.monkeycode.liquidui.ui.screens.HomeScreen
import com.monkeycode.liquidui.ui.screens.MotionScreen
import com.monkeycode.liquidui.ui.screens.SettingsScreen
import com.monkeycode.liquidui.ui.theme.LocalAppChrome
import kotlin.math.max

/**
 * App shell: background layer, one screen, bottom bar, dialog overlay stack.
 *
 * Two recorded [com.kyant.backdrop.Backdrop] layers power the glass:
 *  - `auroraBackdrop` records the decoration (aurora blobs or the device
 *    wallpaper when enabled); every in-page glass panel samples it.
 *  - `contentBackdrop` records the screens; the liquid tab bar samples a
 *    combined layer so content scrolling beneath it refracts through it.
 *
 * When the master `glassEnabled` chrome knob is off, the shell drops the layer
 * machinery and renders a plain Material layout (solid cards + [NavigationBar]).
 */
@Composable
fun AppRoot(settings: AppSettingsState) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val screens = Screen.entries
    val chrome = LocalAppChrome.current
    val glassEnabled = chrome.glassEnabled
    val wallpaperBackground = chrome.wallpaperBackground
    val surfaceColor = MaterialTheme.colorScheme.background

    val context = LocalContext.current
    val wallpaperDrawable = remember(wallpaperBackground, context) {
        if (wallpaperBackground) {
            try {
                // Reading the wallpaper requires a media permission on API 31+;
                // degrade to the aurora layer when missing or unavailable.
                WallpaperManager.getInstance(context).drawable
            } catch (e: SecurityException) {
                null
            } catch (e: RuntimeException) {
                null
            }
        } else null
    }

    val auroraBackdrop = rememberLayerBackdrop {
        when {
            wallpaperDrawable != null -> drawWallpaperCover(wallpaperDrawable)
            else -> drawRect(surfaceColor)
        }
        drawContent()
    }
    val contentBackdrop = rememberLayerBackdrop { drawContent() }
    val fullSceneBackdrop = if (glassEnabled) {
        rememberCombinedBackdrop(auroraBackdrop, contentBackdrop)
    } else null
    val dialogHost = remember { GlassDialogHost() }

    val screenContent: @Composable () -> Unit = {
        when (screens[selectedIndex]) {
            Screen.Home -> HomeScreen(
                settings = settings,
                onOpenComponents = { selectedIndex = Screen.Components.ordinal },
                onOpenMotion = { selectedIndex = Screen.Motion.ordinal }
            )
            Screen.Components -> ComponentsScreen()
            Screen.Motion -> MotionScreen(settings = settings)
            Screen.Settings -> SettingsScreen(settings = settings)
        }
    }

    CompositionLocalProvider(
        LocalGlassBackdrop provides (if (glassEnabled) auroraBackdrop else null),
        LocalGlassContentBackdrop provides fullSceneBackdrop,
        LocalGlassDialogHost provides dialogHost
    ) {
        Box(Modifier.fillMaxSize().background(surfaceColor)) {
            if (glassEnabled || wallpaperBackground) {
                Box(Modifier.matchParentSize().layerBackdrop(auroraBackdrop)) {
                    if (glassEnabled && wallpaperDrawable == null) {
                        AuroraBackground(Modifier.matchParentSize()) {}
                    }
                }
                Box(Modifier.matchParentSize().layerBackdrop(contentBackdrop)) {
                    Box(Modifier.matchParentSize()) { screenContent() }
                }
            } else {
                Box(Modifier.matchParentSize()) { screenContent() }
            }

            if (glassEnabled) {
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
            } else {
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
    if (iw <= 0 || ih <= 0) return
    val scale = max(size.width / iw, size.height / ih)
    val w = iw * scale
    val h = ih * scale
    val left = (size.width - w) / 2f
    val top = (size.height - h) / 2f
    drawable.setBounds(left.toInt(), top.toInt(), (left + w).toInt(), (top + h).toInt())
    drawable.draw(drawContext.canvas.nativeCanvas)
}
