/*
 * 主题色配置与切换（Theme Configuration）
 *
 * 功能：
 *   - 外观模式：跟随系统 / 浅色 / 深色（FilterChip 胶囊单选）
 *   - 主题色：动态色彩（Android 12+ 壁纸取色）/ 默认蓝色 / 自定义颜色
 *   - 桌面图标切换：activity-alias 动态启用/禁用
 *   - 自定义颜色调色盘：SatVal 方块 + Hue 竖条 + HEX 输入
 *
 * 来源：参考 CYQawa/YunX ThemeScreen.kt / ThemeController.kt / Theme.kt
 * 适用：Jetpack Compose + Material 3 项目
 */

package com.example.theme

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// ============================================================
// 主页面：ThemeScreen
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun 主题配置屏幕(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    var showColorPicker by remember { mutableStateOf(false) }
    var colorSectionExpanded by rememberSaveable { mutableStateOf(true) }
    var iconSectionExpanded  by rememberSaveable { mutableStateOf(true) }

    // 折叠动画（可中断）
    val density = LocalDensity.current
    var colorContentHeightPx by remember { mutableIntStateOf(0) }
    var iconContentHeightPx  by remember { mutableIntStateOf(0) }
    val colorExpandProgress = remember { Animatable(if (colorSectionExpanded) 1f else 0f) }
    val iconExpandProgress  = remember { Animatable(if (iconSectionExpanded)  1f else 0f) }

    LaunchedEffect(colorSectionExpanded) {
        colorExpandProgress.animateTo(
            targetValue = if (colorSectionExpanded) 1f else 0f,
            animationSpec = tween(250, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(iconSectionExpanded) {
        iconExpandProgress.animateTo(
            targetValue = if (iconSectionExpanded) 1f else 0f,
            animationSpec = tween(250, easing = FastOutSlowInEasing)
        )
    }

    // 读取当前设置
    val darkMode     = ThemeController.darkMode
    val colorMode    = ThemeController.colorMode
    val seedColor    = ThemeController.seedColor
    val effectiveColorMode = if (colorMode == 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) 1 else colorMode

    // 图标变体
    val settingsRepo = remember { SettingsRepository(context) }
    var appIconVariant by remember { mutableStateOf(settingsRepo.appIconVariant) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("主题与外观", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // ── 外观模式 ─────────────────────────────────────
            SectionLabel("外观模式")
            AppearanceModeCard(
                darkMode = darkMode,
                onModeSelected = { ThemeController.setDarkMode(context, it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── 主题色 ──────────────────────────────────────
            SectionLabel("主题色")
            CollapsibleColorCard(
                expanded = colorSectionExpanded,
                expandProgress = colorExpandProgress,
                contentHeightPx = colorContentHeightPx,
                onHeightChanged = { colorContentHeightPx = it },
                effectiveColorMode = effectiveColorMode,
                seedColor = seedColor,
                onToggleExpand = { colorSectionExpanded = !colorSectionExpanded },
                onColorModeChange = { ThemeController.setColorMode(context, it) },
                onSeedColorChange = { ThemeController.setSeedColor(context, it) },
                onOpenColorPicker = { showColorPicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── 桌面图标 ────────────────────────────────────
            SectionLabel("桌面图标")
            CollapsibleIconCard(
                expanded = iconSectionExpanded,
                expandProgress = iconExpandProgress,
                contentHeightPx = iconContentHeightPx,
                onHeightChanged = { iconContentHeightPx = it },
                appIconVariant = appIconVariant,
                onToggleExpand = { iconSectionExpanded = !iconSectionExpanded },
                onIconSelected = { variant ->
                    appIconVariant = variant
                    switchAppIcon(context, variant)
                    settingsRepo.appIconVariant = variant
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ── 调色盘弹窗 ────────────────────────────────────────
    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = seedColor,
            onDismiss = { showColorPicker = false },
            onColorSelected = { argb ->
                ThemeController.setSeedColor(context, argb)
                showColorPicker = false
            }
        )
    }
}

// ============================================================
// 外观模式卡片
// ============================================================

@Composable
private fun AppearanceModeCard(
    darkMode: Int,
    onModeSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "选择应用的明暗外观",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modes = listOf("跟随系统", "浅色", "深色")
                modes.forEachIndexed { index, label ->
                    SmoothFilterChip(
                        selected = darkMode == index,
                        label = label,
                        onClick = { onModeSelected(index) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ============================================================
// 主题色可折叠卡片
// ============================================================

@Composable
private fun CollapsibleColorCard(
    expanded: Boolean,
    expandProgress: Animatable<Float, *>,
    contentHeightPx: Int,
    onHeightChanged: (Int) -> Unit,
    effectiveColorMode: Int,
    seedColor: Long,
    onToggleExpand: () -> Unit,
    onColorModeChange: (Int) -> Unit,
    onSeedColorChange: (Long) -> Unit,
    onOpenColorPicker: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column {
            // 头部（始终显示）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("主题色", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    AnimatedVisibility(
                        visible = !expanded,
                        enter = fadeIn(tween(200)) + expandVertically(tween(200), expandFrom = Alignment.Top),
                        exit  = fadeOut(tween(200)) + shrinkVertically(tween(200), shrinkTowards = Alignment.Top)
                    ) {
                        Text(
                            text = when {
                                effectiveColorMode == 0 -> "动态色彩（跟随壁纸）"
                                effectiveColorMode == 2 -> "自定义颜色"
                                else -> "默认蓝色"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = tween(200), label = "arrow"
                )
                Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.rotate(rotation))
            }

            // 可折叠内容
            val animatedHeightDp = with(density) { (contentHeightPx * expandProgress.value).toDp() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (contentHeightPx > 0) Modifier.height(animatedHeightDp) else Modifier)
                    .clipToBounds()
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(unbounded = true)
                        .onSizeChanged { onHeightChanged(it.height) }
                        .graphicsLayer { alpha = expandProgress.value }
                ) {
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                        HorizontalDivider(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))

                        // 动态色彩开关（Android 12+）
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("动态色彩", style = MaterialTheme.typography.bodyMedium)
                                    Text("从系统壁纸自动取色", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = effectiveColorMode == 0,
                                    onCheckedChange = { on -> onColorModeChange(if (on) 0 else 1) }
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        // 颜色选择（动态关闭时显示）
                        AnimatedVisibility(visible = effectiveColorMode != 0) {
                            Column {
                                Text("主题颜色", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(top = 10.dp, bottom = 8.dp)
                                ) {
                                    itemsIndexed(PRESET_COLORS) { _, (name, color) ->
                                        val isSelected = effectiveColorMode == 2 && seedColor == color ||
                                                (effectiveColorMode == 1 && color == DEFAULT_BLUE)
                                        ColorDot(
                                            color = color,
                                            name = name,
                                            isSelected = isSelected,
                                            onClick = { onSeedColorChange(color) }
                                        )
                                    }
                                    item {
                                        CustomColorButton(
                                            isSelected = effectiveColorMode == 2,
                                            seedColor = seedColor,
                                            onClick = onOpenColorPicker
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 桌面图标可折叠卡片
// ============================================================

@Composable
private fun CollapsibleIconCard(
    expanded: Boolean,
    expandProgress: Animatable<Float, *>,
    contentHeightPx: Int,
    onHeightChanged: (Int) -> Unit,
    appIconVariant: Int,
    onToggleExpand: () -> Unit,
    onIconSelected: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.StarOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("桌面图标", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    AnimatedVisibility(
                        visible = !expanded,
                        enter = fadeIn(tween(200)) + expandVertically(tween(200), expandFrom = Alignment.Top),
                        exit  = fadeOut(tween(200)) + shrinkVertically(tween(200), shrinkTowards = Alignment.Top)
                    ) {
                        Text(
                            text = if (appIconVariant == 1) "新图标" else "经典图标",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f, animationSpec = tween(200), label = "iconArrow"
                )
                Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.rotate(rotation))
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) + expandVertically(tween(200), expandFrom = Alignment.Top),
                exit  = fadeOut(tween(150)) + shrinkVertically(tween(150), shrinkTowards = Alignment.Top)
            ) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        AppIconOption(
                            iconRes = R.drawable.ic_launcher,
                            name = "经典图标",
                            isSelected = appIconVariant == 0,
                            onClick = { onIconSelected(0) }
                        )
                        AppIconOption(
                            iconRes = R.drawable.ic_launcher2,
                            name = "新图标",
                            isSelected = appIconVariant == 1,
                            onClick = { onIconSelected(1) }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Android 12+ 立即生效；部分设备需回到桌面或重启启动器后查看。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ============================================================
// 调色盘弹窗
// ============================================================

@Composable
private fun ColorPickerDialog(
    initialColor: Long,
    onDismiss: () -> Unit,
    onColorSelected: (Long) -> Unit
) {
    val initialHsv = colorToHsv(Color(initialColor.toInt()))
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }
    val currentColor = Color.hsv(hue, saturation, value)
    var hexInput by remember(currentColor) {
        mutableStateOf(colorToHex(currentColor).removePrefix("#"))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(Modifier.padding(24.dp).fillMaxWidth()) {
                // HEX 输入 + 预览
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("#", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        BasicTextField(
                            value = hexInput,
                            onValueChange = {
                                val filtered = it.filter { c -> c.isLetterOrDigit() }.take(6).uppercase()
                                hexInput = filtered
                                if (filtered.length == 6) runCatching {
                                    val hsv = colorToHsv(hexToColor(filtered))
                                    hue = hsv[0]; saturation = hsv[1]; value = hsv[2]
                                }
                            },
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                keyboardType = KeyboardType.Ascii
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.width(140.dp)
                        )
                    }
                    Box(
                        Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    ) {
                        Box(Modifier.matchParentSize().background(currentColor))
                    }
                }
                Spacer(Modifier.height(20.dp))

                // SatVal 方块 + Hue 竖条
                Row(Modifier.fillMaxWidth().height(200.dp)) {
                    SatValPanel(
                        hue = hue, saturation = saturation, value = value,
                        onValChange = { s, v -> saturation = s; value = v },
                        modifier = Modifier.weight(1f).fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    VerticalHueSlider(
                        hue = hue,
                        onHueChange = { hue = it },
                        modifier = Modifier.width(24.dp).fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    )
                }
                Spacer(Modifier.height(24.dp))

                // 底部按钮
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onColorSelected(currentColor.toArgb().toLong() and 0xFFFFFFFFL) }) {
                        Text("应用")
                    }
                }
            }
        }
    }
}

// ============================================================
// 子组件：SatValPanel / VerticalHueSlider
// ============================================================

@Composable
private fun SatValPanel(
    hue: Float, saturation: Float, value: Float,
    onValChange: (Float, Float) -> Unit, modifier: Modifier
) {
    Box(modifier) {
        Canvas(
            modifier = Modifier.matchParentSize()
                .pointerInput(Unit) {
                    detectTapGestures { off ->
                        onValChange((off.x / size.width).coerceIn(0f, 1f), 1f - (off.y / size.height).coerceIn(0f, 1f))
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        onValChange((change.position.x / size.width).coerceIn(0f, 1f), 1f - (change.position.y / size.height).coerceIn(0f, 1f))
                    }
                }
        ) {
            drawRect(Color.hsv(hue, 1f, 1f))
            drawRect(brush = Brush.horizontalGradient(listOf(Color.White, Color.Transparent)))
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            val x = saturation * size.width
            val y = (1f - value) * size.height
            val cs = 14f
            drawRect(Color.Black.copy(alpha = 0.5f), topLeft = Offset(x - cs/2, y - cs/2), size = Size(cs, cs), style = Stroke(3f))
            drawRect(Color.White,          topLeft = Offset(x - cs/2, y - cs/2), size = Size(cs, cs), style = Stroke(1.5f))
        }
    }
}

@Composable
private fun VerticalHueSlider(
    hue: Float, onHueChange: (Float) -> Unit, modifier: Modifier
) {
    Box(modifier) {
        Canvas(
            modifier = Modifier.matchParentSize()
                .pointerInput(Unit) {
                    detectTapGestures { off -> onHueChange((off.y / size.height * 360f).coerceIn(0f, 360f)) }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        onHueChange((change.position.y / size.height * 360f).coerceIn(0f, 360f))
                    }
                }
        ) {
            val colors = (0..360 step 10).map { Color.hsv(it.toFloat(), 1f, 1f) }
            drawRect(brush = Brush.verticalGradient(colors))
            val y = (hue / 360f) * size.height
            val bh = 6f
            drawRect(Color.Black.copy(alpha = 0.5f), topLeft = Offset(0f, y - bh/2), size = Size(size.width, bh), style = Stroke(2f))
            drawRect(Color.White,               topLeft = Offset(0f, y - bh/2), size = Size(size.width, bh), style = Stroke(1f))
        }
    }
}

// ============================================================
// 子组件：SmoothFilterChip / ColorDot / CustomColorButton
// ============================================================

@Composable
private fun SmoothFilterChip(
    selected: Boolean, label: String, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val duration = 200
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = duration, easing = LinearEasing), label = "container"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = duration, easing = LinearEasing), label = "content"
    )
    Surface(
        onClick = onClick, modifier = modifier.height(36.dp),
        shape = CircleShape, color = containerColor, contentColor = contentColor,
        border = if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
    ) {
        Box(Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
private fun ColorDot(
    color: Long, name: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(Color(color.toInt()))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(Icons.Outlined.Check, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CustomColorButton(
    isSelected: Boolean, seedColor: Long, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(48.dp).clip(CircleShape)
                .background(if (isSelected) Color(seedColor.toInt()) else MaterialTheme.colorScheme.surfaceContainerHighest)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Palette, contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text("自定义",
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ============================================================
// 子组件：AppIconOption / SectionLabel
// ============================================================

@Composable
private fun AppIconOption(
    iconRes: Int, name: String, isSelected: Boolean, onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(72.dp).clip(RoundedCornerShape(18.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes), contentDescription = name,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp))
            )
            if (isSelected) {
                Box(
                    Modifier.align(Alignment.TopEnd).size(20.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = "已选择",
                        tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text, style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

// ============================================================
// 辅助函数
// ============================================================

/** activity-alias 切换桌面图标 */
private fun switchAppIcon(context: Context, variant: Int) {
    val pm = context.packageManager
    val main   = ComponentName(context, "${context.packageName}.MainActivity")
    val alias  = ComponentName(context, "${context.packageName}.MainActivityIcon2")
    if (variant == 1) {
        pm.setComponentEnabledSetting(alias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,  PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(main,  PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
    } else {
        pm.setComponentEnabledSetting(main,  PackageManager.COMPONENT_ENABLED_STATE_ENABLED,  PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(alias, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
    }
}

private fun colorToHsv(color: Color): FloatArray {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    return hsv
}

private fun colorToHex(color: Color): String =
    "#%02X%02X%02X".format((color.red * 255).toInt(), (color.green * 255).toInt(), (color.blue * 255).toInt())

private fun hexToColor(hex: String): Color {
    val clean = hex.removePrefix("#")
    return Color(
        red   = clean.substring(0, 2).toInt(16) / 255f,
        green = clean.substring(2, 4).toInt(16) / 255f,
        blue  = clean.substring(4, 6).toInt(16) / 255f
    )
}

// ============================================================
// 主题控制器（单例）
// ============================================================

object 主题控制器 {
    /** 深色模式：0=跟随系统，1=浅色，2=深色 */
    var darkMode  by mutableStateOf(0)
        private set

    /** 主题色模式：0=动态色彩，1=默认蓝色，2=自定义颜色 */
    var colorMode by mutableStateOf(0)
        private set

    /** 自定义种子色（ARGB Long） */
    var seedColor by mutableStateOf(DEFAULT_BLUE)
        private set

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val s = SettingsRepository(context)
        darkMode  = s.darkMode
        colorMode = s.themeColorMode
        seedColor = s.themeSeedColor
        initialized = true
    }

    fun setDarkMode(context: Context, value: Int) {
        darkMode = value.coerceIn(0, 2)
        SettingsRepository(context).darkMode = darkMode
    }

    fun setColorMode(context: Context, value: Int) {
        colorMode = value.coerceIn(0, 2)
        SettingsRepository(context).themeColorMode = colorMode
    }

    fun setSeedColor(context: Context, argb: Long) {
        seedColor = argb
        colorMode = 2
        SettingsRepository(context).apply {
            themeSeedColor  = argb
            themeColorMode  = 2
        }
    }
}

// ============================================================
// 预置颜色列表
// ============================================================

private val PRESET_COLORS = listOf(
    "蓝色" to 0xFF415F91L,  "靛蓝" to 0xFF3F51B5L,  "紫色" to 0xFF6750A4L,
    "玫红" to 0xFFC2185BL,  "红色" to 0xFFB3261EL,  "橙色" to 0xFFF4631CL,
    "金黄" to 0xFFF9A825L,  "绿色" to 0xFF38761DL,  "青色" to 0xFF00897BL,
    "天蓝" to 0xFF0288D1L,
)
private const val DEFAULT_BLUE = 0xFF415F91L

// ============================================================
// 设置仓储（SharedPreferences 封装）
// ============================================================

class 设置仓库(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var darkMode: Int
        get() = prefs.getInt("dark_mode", 0)
        set(v) { prefs.edit().putInt("dark_mode", v.coerceIn(0, 2)).apply() }

    var themeColorMode: Int
        get() = prefs.getInt("theme_color_mode", 0)
        set(v) { prefs.edit().putInt("theme_color_mode", v.coerceIn(0, 2)).apply() }

    var themeSeedColor: Long
        get() = prefs.getLong("theme_seed_color", DEFAULT_BLUE)
        set(v) { prefs.edit().putLong("theme_seed_color", v).apply() }

    var appIconVariant: Int
        get() = prefs.getInt("app_icon_variant", 0)
        set(v) { prefs.edit().putInt("app_icon_variant", v.coerceIn(0, 1)).apply() }
}

// ============================================================
// 顶层主题 Composable
// ============================================================

@Composable
fun 应用主题(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    ThemeController.init(context)

    val isDark = when (ThemeController.darkMode) {
        1 -> false
        2 -> true
        else -> darkTheme
    }

    val colorScheme = when {
        ThemeController.colorMode == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        ThemeController.colorMode == 2 ->
            seedColorScheme(ThemeController.seedColor, isDark)
        isDark -> darkColorScheme()
        else   -> lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

// 占位：将 SchemeTonalSpot 逻辑替换为实际 material-color-utilities 调用
@Composable
private fun seedColorScheme(seedArgb: Long, dark: Boolean) =
    if (dark) darkColorScheme() else lightColorScheme()  // TODO: 集成 SchemeTonalSpot
