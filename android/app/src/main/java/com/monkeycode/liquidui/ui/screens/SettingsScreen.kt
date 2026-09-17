package com.monkeycode.liquidui.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.monkeycode.liquidui.ui.components.CompactSwitch
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.GradientBar
import com.monkeycode.liquidui.ui.components.InfoBanner
import com.monkeycode.liquidui.ui.components.PreferenceCard
import com.monkeycode.liquidui.ui.components.PreferenceRow
import com.monkeycode.liquidui.ui.components.PrimaryButton
import com.monkeycode.liquidui.ui.components.SectionTitle
import com.monkeycode.liquidui.ui.components.SegmentedTabs
import com.monkeycode.liquidui.ui.theme.AppChrome
import com.monkeycode.liquidui.ui.theme.AppIcons
import com.monkeycode.liquidui.ui.theme.PaletteId
import com.monkeycode.liquidui.ui.theme.ThemeMode
import com.monkeycode.liquidui.ui.theme.LocalAppChrome
import com.monkeycode.liquidui.ui.theme.VisualMode
import com.monkeycode.liquidui.ui.theme.palettePrimaryLight

/** Permission required to read the home-screen wallpaper for the background layer. */
private fun wallpaperReadPermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

@Composable
fun SettingsScreen(settings: AppSettingsState) {
    val currentChrome = LocalAppChrome.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(top = 20.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "所有调整实时生效。把这份状态类套到你的业务里，换肤能力即刻拥有。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { SectionTitle("风格") }
        item {
            PreferenceCard {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        text = "视觉风格",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "玻璃=液态玻璃面板；普通=实心 Material 布局；MD3=壁纸动态取色（Android 12+）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    SegmentedTabs(
                        labels = VisualMode.entries.map { it.label },
                        selectedIndex = settings.visualMode.ordinal,
                        onSelected = { index ->
                            settings.updateVisualMode(VisualMode.entries[index])
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item { SectionTitle("外观") }
        item {
            PreferenceCard {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        text = "主题配色",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    PaletteChips(settings)
                    if (settings.palette == PaletteId.Custom) {
                        Spacer(Modifier.height(14.dp))
                        CustomPalettePicker(settings)
                    }
                }
            }
        }
        item {
            PreferenceCard {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        text = "主题模式",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    SegmentedTabs(
                        labels = listOf("跟随系统", "浅色", "深色"),
                        selectedIndex = when (settings.themeMode) {
                            ThemeMode.System -> 0
                            ThemeMode.Light -> 1
                            ThemeMode.Dark -> 2
                        },
                        onSelected = { index ->
                            settings.updateThemeMode(
                                when (index) {
                                    0 -> ThemeMode.System
                                    1 -> ThemeMode.Light
                                    else -> ThemeMode.Dark
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item {
            PreferenceCard {
                PreferenceRow(
                    icon = AppIcons.Image,
                    title = "桌面壁纸背景",
                    subtitle = "用手机桌面壁纸做背景层，玻璃面板折射其上（首次开启需授权媒体权限）",
                    trailing = {
                        WallpaperSwitch(settings)
                    }
                )
            }
        }

        item { SectionTitle("动效") }
        item {
            PreferenceCard {
                PreferenceRow(
                    icon = AppIcons.Speed,
                    title = "减少动效",
                    subtitle = "物理动画切换为临界阻尼，内容始终处于终态可见",
                    trailing = {
                        CompactSwitch(
                            checked = settings.reducedMotion,
                            onCheckedChange = settings::updateReducedMotion
                        )
                    }
                )
            }
        }
        item {
            SliderCard(
                title = "动效阻尼",
                subtitle = "全局弹簧阻尼：${((currentChrome.motionDamping.coerceIn(0f, 1f)) * 100).toInt()}%，越低越弹",
                value = settings.motionDamping,
                onValueChange = settings::updateMotionDamping
            )
        }
        item {
            SliderCard(
                title = "透明度",
                subtitle = "玻璃涂层浓淡：${((currentChrome.glassOpacity.coerceIn(0f, 1f)) * 100).toInt()}%",
                value = settings.glassOpacity,
                onValueChange = settings::updateGlassOpacity
            )
        }
        item {
            SliderCard(
                title = "折射率",
                subtitle = "液态透镜强度：${((currentChrome.glassRefraction.coerceIn(0f, 1f)) * 100).toInt()}%",
                value = settings.glassRefraction,
                onValueChange = settings::updateGlassRefraction
            )
        }
        item {
            SliderCard(
                title = "圆角倍率",
                subtitle = "整体圆角比例：${String.format("%.1f", settings.cornerScale)}×",
                value = settings.cornerScale,
                valueRange = 0.8f..1.5f,
                onValueChange = settings::updateCornerScale
            )
        }
        item {
            PreferenceCard {
                PreferenceRow(
                    icon = AppIcons.RotateRight,
                    title = "恢复默认",
                    subtitle = "重置全部外观与动效偏好",
                    onClick = settings::reset
                )
            }
        }

        item { SectionTitle("通知") }
        item {
            PreferenceCard {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        text = "发送测试通知",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "向系统发一条即时通知作为预览，正文使用当前主题主色着色（Android 13+ 首次需授权）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    NotificationPreviewButton(Modifier.fillMaxWidth())
                }
            }
        }

        item { SectionTitle("关于") }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Liquid Motion UI",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "v1.2.0",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Android 液态玻璃设计模板 · 基于 dyparse 视觉语言与 motion 动效体系",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "设计参考 · 开源仓库",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    ReferenceLine("dyparse", "github.com/kd64i/dyparse")
                    ReferenceLine("Miuix", "github.com/chibatching/Miuix")
                    ReferenceLine("motion", "github.com/motiondivision/motion")
                }
            }
        }

        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoBanner(
                    text = "生效中的 chrome：mode=${currentChrome.visualMode.label} · " +
                        "opacity=${String.format("%.2f", currentChrome.glassOpacity)} · " +
                        "refraction=${String.format("%.2f", currentChrome.glassRefraction)} · " +
                        "corner=${String.format("%.2f", currentChrome.cornerScale)} · " +
                        "damping=${(currentChrome.motionDamping * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaletteChips(settings: AppSettingsState) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PaletteId.entries.forEach { palette ->
            val selected = settings.palette == palette
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { settings.updatePalette(palette) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(palettePrimaryLight(palette), CircleShape)
                )
                Text(
                    text = palette.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun WallpaperSwitch(settings: AppSettingsState) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) settings.updateWallpaperBackground(true)
    }
    CompactSwitch(
        checked = settings.wallpaperBackground,
        onCheckedChange = { checked ->
            if (!checked) {
                settings.updateWallpaperBackground(false)
            } else {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    wallpaperReadPermission()
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) settings.updateWallpaperBackground(true)
                else permissionLauncher.launch(wallpaperReadPermission())
            }
        }
    )
}

@Composable
private fun ReferenceLine(name: String, repo: String) {
    Text(
        text = "$name  ·  $repo",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SliderCard(
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        )
    }
}

/** HSV wheel for the free seed colour, plus a depth slider. */
@Composable
private fun CustomPalettePicker(settings: AppSettingsState) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(0.5f) }
    var value by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(settings.customSeed) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(settings.customSeed, hsv)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
    }

    fun commit() {
        settings.updateCustomSeed(
            Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
        )
    }

    Column {
        Text(
            text = "种子色",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        GradientBar(
            colors = (0..24).map {
                Color(android.graphics.Color.HSVToColor(floatArrayOf(it * 15f, 1f, 1f)))
            },
            fraction = hue / 360f,
            onFractionChange = { hue = it * 360f; commit() }
        )
        Spacer(Modifier.height(6.dp))
        GradientBar(
            colors = listOf(
                Color.White,
                Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, value)))
            ),
            fraction = saturation,
            onFractionChange = { saturation = it; commit() }
        )
        Spacer(Modifier.height(6.dp))
        GradientBar(
            colors = listOf(
                Color.Black,
                Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, 1f)))
            ),
            fraction = value,
            onFractionChange = { value = it; commit() }
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "背景深浅",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Slider(
            value = settings.customShade,
            onValueChange = settings::updateCustomShade,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        )
    }
}

/** Fires a themed preview notification. Ensures a channel exists first. */
@Composable
private fun NotificationPreviewButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) sendNotificationPreview(context, primary)
    }
    PrimaryButton(
        onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) sendNotificationPreview(context, primary)
                else permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                sendNotificationPreview(context, primary)
            }
        },
        modifier = modifier
    ) {
        Text("发送")
    }
}

/** Fires a themed preview notification. Ensures a channel exists first. */
private fun sendNotificationPreview(context: Context, accent: Color) {
    val channelId = "liquid_preview"
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "即时预览",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Liquid Motion UI 主题预览通知"
        manager.createNotificationChannel(channel)
    }
    val builder = android.app.Notification.Builder(context, channelId)
        .setContentTitle("Liquid Motion UI")
        .setContentText("当前主题的即时预览 · 玻璃质感")
        .setAutoCancel(true)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        builder.setColor(accent.toArgb())
    }
    manager.notify(1001, builder.build())
}
