package com.monkeycode.liquidui.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.components.CompactSwitch
import com.monkeycode.liquidui.ui.components.ColorWheel
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.GlassDialog
import com.monkeycode.liquidui.ui.components.GradientBar
import com.monkeycode.liquidui.ui.components.InfoBanner
import com.monkeycode.liquidui.ui.components.PreferenceCard
import com.monkeycode.liquidui.ui.components.PreferenceRow
import com.monkeycode.liquidui.ui.components.PrimaryButton
import com.monkeycode.liquidui.ui.components.SecondaryButton
import com.monkeycode.liquidui.ui.components.SectionTitle
import com.monkeycode.liquidui.ui.components.SegmentedTabs
import com.monkeycode.liquidui.ui.features.AppIconPicker
import com.monkeycode.liquidui.ui.features.toolkit.AppLog
import com.monkeycode.liquidui.ui.features.toolkit.AppLogLevel
import com.monkeycode.liquidui.ui.features.toolkit.AppUpdateChecker
import com.monkeycode.liquidui.ui.features.toolkit.MarkdownText
import com.monkeycode.liquidui.ui.features.toolkit.UpdateCheckResult
import com.monkeycode.liquidui.ui.features.toolkit.UpdateState
import com.monkeycode.liquidui.ui.theme.AppChrome
import com.monkeycode.liquidui.ui.theme.AppIcons
import com.monkeycode.liquidui.ui.theme.PaletteId
import com.monkeycode.liquidui.ui.theme.ThemeMode
import com.monkeycode.liquidui.ui.theme.LocalAppChrome
import com.monkeycode.liquidui.ui.theme.VisualMode
import com.monkeycode.liquidui.ui.theme.palettePrimaryLight

@Composable
fun SettingsScreen(
    settings: AppSettingsState,
    onOpenAbout: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenPermissions: () -> Unit,
) {
    val currentChrome = LocalAppChrome.current
    val context = LocalContext.current

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
                        text = "玻璃=液态玻璃面板；普通=实心 Material 布局；MD3=壁纸动态取色（Android 12+）；O神=Miuix 分组卡片+翻页+悬浮底栏",
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
        item { SectionTitle("主题配置") }
        item { ThemeConfigTransferCard(settings) }
        item { WallpaperSettingsCard(settings) }

        item { SectionTitle("首次体验") }
        item {
            PreferenceCard {
                PreferenceRow(
                    icon = AppIcons.BlurOn,
                    title = "重新查看欢迎引导",
                    subtitle = "再次打开首次启动说明，适合给使用者演示项目能力",
                    onClick = { settings.updateOnboardingCompleted(false) }
                )
            }
        }

        item { SectionTitle("应用图标") }
        item {
            PreferenceCard {
                AppIconPicker(Modifier.padding(horizontal = 16.dp, vertical = 14.dp))
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
                title = "模糊强度",
                subtitle = "背景采样模糊：${settings.glassBlurRadiusDp.toInt()}dp",
                value = settings.glassBlurRadiusDp,
                valueRange = 0f..40f,
                onValueChange = settings::updateGlassBlurRadiusDp
            )
        }
        item {
            SliderCard(
                title = "折射高度",
                subtitle = "液态透镜边缘高度：${settings.glassRefractionHeightDp.toInt()}dp",
                value = settings.glassRefractionHeightDp,
                valueRange = 0f..40f,
                onValueChange = settings::updateGlassRefractionHeightDp
            )
        }
        item {
            SliderCard(
                title = "折射幅度",
                subtitle = "壁纸内容偏移幅度：${settings.glassRefractionAmountDp.toInt()}dp",
                value = settings.glassRefractionAmountDp,
                valueRange = 0f..60f,
                onValueChange = settings::updateGlassRefractionAmountDp
            )
        }
        item {
            SliderCard(
                title = "玻璃染色",
                subtitle = "白色玻璃涂层：${(settings.glassTintAlpha * 100).toInt()}%",
                value = settings.glassTintAlpha,
                valueRange = 0f..0.35f,
                onValueChange = settings::updateGlassTintAlpha
            )
        }
        item {
            SliderCard(
                title = "高光边框",
                subtitle = "玻璃边缘强度：${(settings.glassBorderAlpha * 100).toInt()}%",
                value = settings.glassBorderAlpha,
                valueRange = 0f..0.45f,
                onValueChange = settings::updateGlassBorderAlpha
            )
        }
        item {
            PreferenceCard {
                PreferenceRow(
                    icon = AppIcons.WaterDrop,
                    title = "增强色彩层次",
                    subtitle = "提升玻璃后的壁纸色彩通透感",
                    trailing = {
                        CompactSwitch(settings.glassVibrancyEnabled, settings::updateGlassVibrancyEnabled)
                    }
                )
            }
        }
        item {
            PreferenceCard {
                PreferenceRow(
                    icon = AppIcons.WaterDrop,
                    title = "色差折射",
                    subtitle = "在液态透镜边缘显示更明显的色散",
                    trailing = {
                        CompactSwitch(settings.glassChromaticAberration, settings::updateGlassChromaticAberration)
                    }
                )
            }
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

        item { SectionTitle("更新与日志") }
        item {
            PreferenceCard { UpdateCheckPanel() }
        }
        item {
            PreferenceCard { AppLogPanel() }
        }

        item { SectionTitle("帮助与合规") }
        item {
            PreferenceCard {
                PreferenceRow(
                    icon = AppIcons.Shield,
                    title = "关于与开源致谢",
                    subtitle = "完整查看参考项目、作者、用途、链接与许可证",
                    onClick = onOpenAbout
                )
            }
        }
        item {
            PreferenceCard {
                PreferenceRow(
                    icon = AppIcons.Shield,
                    title = "隐私说明",
                    subtitle = "查看本地数据、权限用途和网络行为的完整说明",
                    onClick = onOpenPrivacy
                )
            }
        }
        item {
            PreferenceCard {
                PreferenceRow(
                    icon = AppIcons.Shield,
                    title = "权限管理",
                    subtitle = "真实请求权限、检查状态，拒绝后可进入系统设置恢复",
                    onClick = onOpenPermissions
                )
            }
        }
        item {
            PreferenceCard {
                PreferenceRow(
                    icon = AppIcons.BlurOn,
                    title = "项目主页",
                    subtitle = "打开 GitHub 仓库，查看源码、文档和构建记录",
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/yz8023/ui")
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        runCatching { context.startActivity(intent) }
                    }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoBanner(
                    text = "生效中的 chrome：mode=${currentChrome.visualMode.label} · " +
                        "blur=${settings.glassBlurRadiusDp.toInt()}dp · " +
                        "lens=${settings.glassRefractionHeightDp.toInt()}/${settings.glassRefractionAmountDp.toInt()}dp · " +
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

@Composable
private fun ThemeConfigTransferCard(settings: AppSettingsState) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    PreferenceCard {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = "导入 / 导出主题",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "把当前外观、玻璃参数和动效偏好保存为 JSON；可复制给别人或从剪贴板恢复。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SecondaryButton(
                    onClick = {
                        val json = settings.exportThemeJson()
                        clipboard.setPrimaryClip(ClipData.newPlainText("LiquidUI theme", json))
                        Toast.makeText(context, "主题 JSON 已复制", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("导出") }
                PrimaryButton(
                    onClick = {
                        val json = clipboard.primaryClip
                            ?.takeIf { it.itemCount > 0 }
                            ?.getItemAt(0)
                            ?.coerceToText(context)
                            ?.toString()
                            .orEmpty()
                        val ok = settings.importThemeJson(json)
                        Toast.makeText(
                            context,
                            if (ok) "主题已导入" else "剪贴板不是有效主题 JSON",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("导入") }
            }
        }
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
            text = "种子色 · 在色盘上点击/拖动取色，右侧滑条调深浅",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        ColorWheel(
            hue = hue,
            saturation = saturation,
            value = value,
            onColorChange = { h, s, v ->
                hue = h
                saturation = s
                value = v
                commit()
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
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
        AppLog.i(
            "Notification",
            if (granted) "POST_NOTIFICATIONS 已授权，发送预览通知" else "POST_NOTIFICATIONS 被拒绝，跳过发送"
        )
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
        channel.description = "Android 素材库示例主题预览通知"
        manager.createNotificationChannel(channel)
    }
    val builder = android.app.Notification.Builder(context, channelId)
        .setContentTitle("Android 素材库示例")
        .setContentText("当前主题的即时预览 · 玻璃质感")
        .setSmallIcon(R.drawable.ic_stat_launcher)
        .setAutoCancel(true)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        builder.setColor(accent.toArgb())
    }
    manager.notify(1001, builder.build())
    AppLog.i("Notification", "已发送预览通知 id=1001 channel=$channelId accent=#${accent.toArgb().toUInt().toString(16)}")
}

/**
 * 手册 F18 / 条目 U01 的真实接入：检查本模板仓库自己的 GitHub Release。
 * 宿主把 [AppUpdateChecker.DEFAULT_REPO] 换成自己的 `owner/repo` 即可。
 */
@Composable
private fun UpdateCheckPanel() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<UpdateCheckResult?>(null) }
    var downloading by remember { mutableStateOf(false) }
    var written by remember { mutableStateOf(0L) }
    var total by remember { mutableStateOf(-1L) }
    var apkPath by remember { mutableStateOf<String?>(null) }
    var needPermission by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }
    var job by remember { mutableStateOf<Job?>(null) }
    val current = remember(context) { AppUpdateChecker.currentVersionName(context) }

    Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text("检查更新", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(
            "真实请求 api.github.com 比对版本；检查目标是本模板仓库 ${AppUpdateChecker.DEFAULT_REPO}。" +
                "当前安装版本 $current。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        PrimaryButton(
            onClick = {
                checking = true
                note = null
                AppLog.i("Update", "设置页触发检查")
                job = scope.launch {
                    val r = withContext(Dispatchers.IO) { AppUpdateChecker.check(context) }
                    result = r
                    checking = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (checking) "检查中…" else "检查更新")
        }

        val r = result
        if (r != null) {
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))
            Text(
                when (r.state) {
                    UpdateState.UPDATE_AVAILABLE -> "发现新版本"
                    UpdateState.UP_TO_DATE -> "已是最新"
                    UpdateState.FAILED -> "检查失败"
                } + " · ${r.message} · 耗时 ${r.elapsedMillis}ms",
                style = MaterialTheme.typography.bodySmall,
                color = if (r.state == UpdateState.FAILED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            val latest = r.latest
            if (latest != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "${latest.tag} · ${latest.publishedAt} · " +
                        (latest.apkName ?: "无 APK 附件") + " · " + AppUpdateChecker.humanSize(latest.apkBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (latest.body.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text("更新日志（Markdown 实时渲染，见 F20/U03）", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(6.dp))
                    MarkdownText(
                        latest.body,
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    )
                }
            }

            val url = latest?.apkUrl
            if (r.state == UpdateState.UPDATE_AVAILABLE && url != null) {
                Spacer(Modifier.height(12.dp))
                if (downloading) {
                    val t = total
                    val w = written
                    if (t > 0) {
                        val ratio = (w.toFloat() / t.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${(ratio * 100).toInt()}% · ${AppUpdateChecker.humanSize(w)} / ${AppUpdateChecker.humanSize(t)}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        Text("下载中 · ${AppUpdateChecker.humanSize(w)}", style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton(
                        onClick = {
                            job?.cancel()
                            downloading = false
                            note = "已取消下载"
                            AppLog.w("Update", "用户取消下载")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("取消下载") }
                } else {
                    PrimaryButton(
                        onClick = {
                            downloading = true
                            written = 0
                            total = latest.apkBytes
                            note = null
                            needPermission = false
                            job = scope.launch {
                                val outcome = runCatching {
                                    withContext(Dispatchers.IO) {
                                        AppUpdateChecker.download(context, url, latest.apkName ?: "update.apk") { w, t ->
                                            written = w
                                            total = if (t > 0) t else -1
                                        }
                                    }
                                }
                                downloading = false
                                outcome.onSuccess { file ->
                                    apkPath = file.absolutePath
                                    val launched = AppUpdateChecker.installApk(context, file)
                                    needPermission = !launched
                                    note = if (launched) "已调起系统安装器" else "已下载，但系统未授权「安装未知应用」"
                                }.onFailure { th ->
                                    note = "下载失败：${th.javaClass.simpleName} ${th.message}"
                                    AppLog.e("Update", "下载失败", th)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (apkPath != null) "重新下载并安装" else "下载 APK 并安装") }
                }
            }

            if (needPermission) {
                Spacer(Modifier.height(8.dp))
                SecondaryButton(
                    onClick = { AppUpdateChecker.openUnknownSourcesSettings(context) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("去授权「安装未知应用」") }
                Spacer(Modifier.height(8.dp))
                SecondaryButton(
                    onClick = { url?.let { AppUpdateChecker.openInBrowser(context, it) } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("改用浏览器下载") }
            }
            if (r.state == UpdateState.FAILED) {
                Spacer(Modifier.height(8.dp))
                SecondaryButton(
                    onClick = {
                        checking = true
                        note = null
                        job = scope.launch {
                            val again = withContext(Dispatchers.IO) { AppUpdateChecker.check(context) }
                            result = again
                            checking = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("重试") }
            }
            apkPath?.let {
                Spacer(Modifier.height(6.dp))
                Text("已落盘：$it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        note?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/**
 * 手册 F19 / 条目 U02 的真实接入：查看、过滤、复制、分享应用自己的运行日志。
 * 日志来自真实调用点（更新检查、通知发送、权限结果），不是面板自己造的数据。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppLogPanel() {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    var showSheet by remember { mutableStateOf(false) }
    var level by remember { mutableStateOf<AppLogLevel?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }
    val entries = AppLog.entries
    val picked = entries.filter { level == null || it.level == level }

    PreferenceRow(
        icon = AppIcons.Widgets,
        title = "运行日志",
        subtitle = "内存缓冲 ${entries.size}/${AppLog.MAX_ENTRIES} 条 · 可过滤、复制、系统分享",
        onClick = {
            showSheet = true
            AppLog.d("Log", "打开运行日志面板")
        }
    )

    if (showSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(onDismissRequest = { showSheet = false }, sheetState = sheetState) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text("运行日志", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "只存内存不落盘，进程被杀即清空，因此不需要存储权限。分享前请自行确认内容不含隐私。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = level == null, onClick = { level = null }, label = { Text("全部") })
                    AppLogLevel.entries.forEach { l ->
                        FilterChip(
                            selected = level == l,
                            onClick = { level = if (level == l) null else l },
                            label = { Text(l.label) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("命中 ${picked.size} 条（最新在上）", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (picked.isEmpty()) {
                        Text("（暂无日志：去点几次「检查更新」或「发送通知」就有了）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        picked.asReversed().forEach { e ->
                            Text(
                                "#${e.seq} ${e.level.label}/${e.tag}: ${e.message.replace("\n", " ⏎ ")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (e.level == AppLogLevel.ERROR) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                PrimaryButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(AppLog.dump(level)))
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        toast = "已复制 ${picked.size} 条"
                        Toast.makeText(context, "已复制 ${picked.size} 条日志", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("复制全文") }
                Spacer(Modifier.height(8.dp))
                SecondaryButton(
                    onClick = {
                        val intent = AppLog.buildShareIntent(context, level)
                        val ok = runCatching {
                            context.startActivity(Intent.createChooser(intent, "分享日志"))
                        }.isSuccess
                        AppLog.i("Log", if (ok) "调起分享面板" else "无可接收分享的应用")
                        Toast.makeText(
                            context,
                            if (ok) "已调起系统分享" else "没有可接收的应用，请改用复制",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("系统分享") }
                Spacer(Modifier.height(8.dp))
                SecondaryButton(
                    onClick = {
                        val n = entries.size
                        AppLog.clear()
                        AppLog.i("Log", "已清空缓冲（原 $n 条）")
                        Toast.makeText(context, "已清空 $n 条", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("清空") }
                toast?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}
