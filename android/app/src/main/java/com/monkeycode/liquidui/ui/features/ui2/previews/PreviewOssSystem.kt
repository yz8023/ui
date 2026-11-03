package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monkeycode.liquidui.R
import java.util.Locale
import kotlin.math.ln

/**
 * 批 E 开源取材 S13–S14：状态层与海拔叠加 / 关于页开源清单与许可证合规。
 *
 * S13 依据 androidx/androidx（Compose Material3，Apache-2.0）的状态层与暗色海拔叠加规格；
 * S14 依据 mikepenz/AboutLibraries（Apache-2.0）的「从依赖元数据生成库清单 + 许可证归类」思路，
 * 数据是**本仓库真实依赖与批 D/E 真实取材来源**，不是编的样例。
 * 清单与逐项参数见 docs/开源素材清单-批E.md。
 */

// ==================== S13 状态层与海拔叠加 ====================

private val STATE_LAYERS = listOf("hover", "focus", "pressed", "dragged")

private val STATE_LAYER_LABEL = mapOf(
    "hover" to R.string.pv_oss_state_hover,
    "focus" to R.string.pv_oss_state_focus,
    "pressed" to R.string.pv_oss_state_pressed,
    "dragged" to R.string.pv_oss_state_dragged,
)

/** M3 状态层不透明度：hover 8% / focus 12% / pressed 12% / dragged 16%。 */
private val STATE_LAYER_ALPHA = mapOf(
    "hover" to 0.08f,
    "focus" to 0.12f,
    "pressed" to 0.12f,
    "dragged" to 0.16f,
)

private val ELEVATIONS = listOf(0, 1, 2, 3, 4, 5)

/** 暗色海拔叠加的近似式：alpha ≈ 4.5 × ln(elevation + 1)%（近似值，待真机与官方实现比对）。 */
private fun overlayAlpha(elevation: Int): Float = (4.5f * ln(elevation + 1f) / 100f)

@Composable
fun PreviewS13() {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current

    var active by remember { mutableStateOf(setOf<String>()) }
    var visualize by rememberSaveable { mutableStateOf(false) }
    var darkSim by rememberSaveable { mutableStateOf(true) }

    val baseColor = scheme.surfaceVariant
    val onColor = scheme.onSurfaceVariant
    val layerColor = if (visualize) Color.Red.copy(alpha = 0.5f) else scheme.onSurface
    val chipShape = RoundedCornerShape(percent = 50)
    val cardShape = RoundedCornerShape(16.dp)
    val accent = scheme.primary

    // 多层同时命中时按不透明度合成，不是相加：12% + 12% ≠ 24%
    val composite = active.fold(0f) { acc, key ->
        val a = STATE_LAYER_ALPHA[key] ?: 0f
        1f - (1f - acc) * (1f - a)
    }

    Column(
        Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.pv_oss_state_rule),
            style = MaterialTheme.typography.labelSmall,
            color = onColor,
        )

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            STATE_LAYERS.forEach { key ->
                val on = active.contains(key)
                val pct = ((STATE_LAYER_ALPHA.getValue(key) * 100f) + 0.5f).toInt()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        context.getString(STATE_LAYER_LABEL.getValue(key)),
                        Modifier
                            .clip(chipShape)
                            .background(if (on) accent else scheme.surfaceVariant)
                            .clickable {
                                active = if (on) active - key else active + key
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (on) scheme.onPrimary else onColor,
                    )
                    Text(
                        stringResource(R.string.pv_oss_state_pct, pct),
                        style = MaterialTheme.typography.labelSmall,
                        color = onColor,
                    )
                }
            }
        }

        // 被叠层的那块表面：状态层盖在内容之上、圆角之内
        Box(
            Modifier
                .fillMaxWidth()
                .height(96.dp)
                .shadow(2.dp, cardShape)
                .clip(cardShape)
                .background(baseColor)
                .clickable {
                    active = if (active.contains("pressed")) active - "pressed" else active + "pressed"
                }
                .padding(16.dp),
        ) {
            Column(Modifier.align(Alignment.CenterStart)) {
                Text(
                    stringResource(R.string.pv_oss_state_surface_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(
                        R.string.pv_oss_state_composite,
                        String.format(Locale.US, "%.1f", composite * 100f),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                )
            }
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(layerColor.copy(alpha = if (visualize) 0.5f else composite))
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (visualize) stringResource(R.string.pv_oss_state_viz_on)
                else stringResource(R.string.pv_oss_state_viz_off),
                Modifier
                    .clip(chipShape)
                    .background(scheme.surfaceVariant)
                    .clickable {
                        visualize = !visualize
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = onColor,
            )
            Text(
                if (darkSim) stringResource(R.string.pv_oss_state_dark_on)
                else stringResource(R.string.pv_oss_state_dark_off),
                Modifier
                    .clip(chipShape)
                    .background(scheme.surfaceVariant)
                    .clickable {
                        darkSim = !darkSim
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = onColor,
            )
        }

        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.pv_oss_state_elev_title),
            style = MaterialTheme.typography.titleSmall,
            color = scheme.onSurface,
        )
        Text(
            stringResource(R.string.pv_oss_state_elev_note),
            style = MaterialTheme.typography.labelSmall,
            color = onColor,
        )

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ELEVATIONS.forEach { e ->
                val a = overlayAlpha(e)
                // 暗色提亮 = 把 surface 朝 onSurface 方向插值；亮色靠阴影，不改表面色
                val tinted = if (darkSim) lerp(baseColor, scheme.onSurface, a) else baseColor
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(width = 64.dp, height = 48.dp)
                            .shadow(e.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(tinted)
                            .border(1.dp, scheme.outlineVariant, RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.pv_oss_state_elev_dp, e),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurface,
                    )
                    Text(
                        stringResource(R.string.pv_oss_state_pct, (a * 100f + 0.5f).toInt()),
                        style = MaterialTheme.typography.labelSmall,
                        color = onColor,
                    )
                }
            }
        }

        Text(
            stringResource(R.string.pv_oss_state_pitfall),
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(scheme.errorContainer)
                .padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onErrorContainer,
        )
    }
}

// ==================== S14 关于页开源清单与许可证合规 ====================

/** 许可证键是稳定英文标识，文案查资源。 */
private const val LIC_APACHE = "apache_2_0"
private const val LIC_MIT = "mit"
private const val LIC_GPL = "gpl_3_0"
private const val LIC_EPL = "epl_1_0"
private const val LIC_NONE = "none"

private val LIC_ORDER = listOf(LIC_APACHE, LIC_MIT, LIC_EPL, LIC_GPL, LIC_NONE)

private val LIC_LABEL = mapOf(
    LIC_APACHE to R.string.pv_oss_lic_apache,
    LIC_MIT to R.string.pv_oss_lic_mit,
    LIC_EPL to R.string.pv_oss_lic_epl,
    LIC_GPL to R.string.pv_oss_lic_gpl,
    LIC_NONE to R.string.pv_oss_lic_none,
)

/** 用途键：依赖 / 取材 / 反例（被剔除的候选）。 */
private const val USE_DEP = "dependency"
private const val USE_SOURCE = "source"
private const val USE_REJECTED = "rejected"

private val USE_LABEL = mapOf(
    USE_DEP to R.string.pv_oss_lic_use_dep,
    USE_SOURCE to R.string.pv_oss_lic_use_source,
    USE_REJECTED to R.string.pv_oss_lic_use_rejected,
)

/**
 * 数据是**本仓库真实情况**，逐条经 GitHub API 核实（2026-11-04）：
 * 依赖取自 android/app/build.gradle.kts + gradle/libs.versions.toml；
 * 取材来源取自 docs/开源素材清单-批D.md 与 批E.md。
 */
private data class LicEntry(
    val repo: String,
    val url: String,
    val license: String,
    val use: String,
    val tag: String,
)

private val LIC_ENTRIES = listOf(
    // ---- 真实依赖（implementation 会打进 APK）----
    LicEntry("androidx/androidx (core-ktx)", "https://github.com/androidx/androidx", LIC_APACHE, USE_DEP, "implementation"),
    LicEntry("androidx/androidx (lifecycle x3)", "https://github.com/androidx/androidx", LIC_APACHE, USE_DEP, "implementation"),
    LicEntry("androidx/androidx (activity-compose)", "https://github.com/androidx/androidx", LIC_APACHE, USE_DEP, "implementation"),
    LicEntry("Jetpack Compose (BOM 2024.10.01: ui/graphics/material3)", "https://github.com/androidx/androidx", LIC_APACHE, USE_DEP, "implementation"),
    LicEntry("kyant0/backdrop-android", "https://github.com/kyant0/backdrop-android", LIC_APACHE, USE_DEP, "implementation"),
    LicEntry("kyant0/shapes", "https://github.com/kyant0/shapes", LIC_APACHE, USE_DEP, "implementation"),
    // testImplementation 不打进 APK：传染性风险只存在于开发期
    LicEntry("junit/junit", "https://github.com/junit-team/junit4", LIC_EPL, USE_DEP, "testImplementation"),
    // ---- 批 D 取材来源（S01–S08）----
    LicEntry("material-foundation/material-color-utilities", "https://github.com/material-foundation/material-color-utilities", LIC_APACHE, USE_SOURCE, "S01"),
    LicEntry("google/material-design-icons", "https://github.com/google/material-design-icons", LIC_APACHE, USE_SOURCE, "S02"),
    LicEntry("kizitonwose/Calendar", "https://github.com/kizitonwose/Calendar", LIC_MIT, USE_SOURCE, "S03"),
    LicEntry("android/compose-samples", "https://github.com/android/compose-samples", LIC_APACHE, USE_SOURCE, "S04"),
    LicEntry("Calvin-LL/Reorderable", "https://github.com/Calvin-LL/Reorderable", LIC_APACHE, USE_SOURCE, "S05"),
    LicEntry("google/accompanist", "https://github.com/google/accompanist", LIC_APACHE, USE_SOURCE, "S06"),
    LicEntry("microsoft/fluentui-android", "https://github.com/microsoft/fluentui-android", LIC_MIT, USE_SOURCE, "S07"),
    LicEntry("google/Accessibility-Test-Framework-for-Android", "https://github.com/google/Accessibility-Test-Framework-for-Android", LIC_APACHE, USE_SOURCE, "S08"),
    // ---- 批 E 取材来源（S09–S14）----
    LicEntry("saket/cascade", "https://github.com/saket/cascade", LIC_APACHE, USE_SOURCE, "S09"),
    LicEntry("saket/InboxRecyclerView", "https://github.com/saket/InboxRecyclerView", LIC_APACHE, USE_SOURCE, "S10"),
    LicEntry("CanHub/Android-Image-Cropper", "https://github.com/CanHub/Android-Image-Cropper", LIC_APACHE, USE_SOURCE, "S11"),
    LicEntry("MohamedRejeb/compose-rich-editor", "https://github.com/MohamedRejeb/compose-rich-editor", LIC_APACHE, USE_SOURCE, "S12"),
    LicEntry("androidx/androidx (Material3 state layer and elevation spec)", "https://github.com/androidx/androidx", LIC_APACHE, USE_SOURCE, "S13"),
    LicEntry("mikepenz/AboutLibraries", "https://github.com/mikepenz/AboutLibraries", LIC_APACHE, USE_SOURCE, "S14"),
    // ---- 需要显式处理的两类风险 ----
    // 本仓库是 MIT；GPL-3.0 的代码不能复制进来，只能学公开规范
    LicEntry("chenaizhang/KernelSU-Style-UI-Kit", "https://github.com/chenaizhang/KernelSU-Style-UI-Kit", LIC_GPL, USE_SOURCE, "batch B"),
    // 无许可证 = 默认保留所有权利，本轮实测后剔除
    LicEntry("saket/press", "https://github.com/saket/press", LIC_NONE, USE_REJECTED, "no license"),
    LicEntry("hackware1993/MagicIndicator", "https://github.com/hackware1993/MagicIndicator", LIC_NONE, USE_REJECTED, "no license"),
)

private val FILTERS = listOf("all", USE_DEP, USE_SOURCE, "issues")

private val FILTER_LABEL = mapOf(
    "all" to R.string.pv_oss_lic_filter_all,
    USE_DEP to R.string.pv_oss_lic_use_dep,
    USE_SOURCE to R.string.pv_oss_lic_use_source,
    "issues" to R.string.pv_oss_lic_filter_issues,
)

@Composable
fun PreviewS14() {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current

    var filter by rememberSaveable { mutableStateOf("all") }
    var copied by remember { mutableStateOf(false) }

    val chipShape = RoundedCornerShape(percent = 50)
    val cardShape = RoundedCornerShape(12.dp)
    val warnBg = scheme.errorContainer
    val warnText = scheme.onErrorContainer
    val okColor = scheme.primary
    val muted = scheme.onSurfaceVariant

    // 真算，不写死条数
    val byLicense = LIC_ORDER.map { key -> key to LIC_ENTRIES.count { it.license == key } }
    val issues = LIC_ENTRIES.filter { it.license == LIC_NONE || it.license == LIC_GPL }
    val shown = when (filter) {
        USE_DEP -> LIC_ENTRIES.filter { it.use == USE_DEP }
        USE_SOURCE -> LIC_ENTRIES.filter { it.use == USE_SOURCE }
        "issues" -> issues
        else -> LIC_ENTRIES
    }

    Column(
        Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.pv_oss_lic_intro, LIC_ENTRIES.size, issues.size),
            style = MaterialTheme.typography.labelSmall,
            color = muted,
        )

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            byLicense.forEach { pair ->
                val key = pair.first
                val count = pair.second
                if (count > 0) {
                    Text(
                        context.getString(LIC_LABEL.getValue(key)) + " × " + count,
                        Modifier
                            .clip(chipShape)
                            .background(
                                if (key == LIC_NONE || key == LIC_GPL) warnBg else scheme.surfaceVariant
                            )
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (key == LIC_NONE || key == LIC_GPL) warnText else muted,
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FILTERS.forEach { key ->
                val on = key == filter
                Text(
                    context.getString(FILTER_LABEL.getValue(key)),
                    Modifier
                        .clip(chipShape)
                        .background(if (on) okColor else scheme.surfaceVariant)
                        .clickable {
                            filter = key
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (on) scheme.onPrimary else muted,
                )
            }
        }

        shown.forEach { e ->
            val risky = e.license == LIC_NONE || e.license == LIC_GPL
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(if (risky) warnBg else scheme.surfaceVariant)
                    .padding(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        e.repo,
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (risky) warnText else scheme.onSurface,
                    )
                    Text(
                        e.tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (risky) warnText else muted,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    context.getString(LIC_LABEL.getValue(e.license)) + "  ·  " +
                        context.getString(USE_LABEL.getValue(e.use)) + "  ·  " + e.url,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (risky) warnText else muted,
                )
            }
        }

        Text(
            stringResource(R.string.pv_oss_lic_dup_rule),
            Modifier.fillMaxWidth().clip(cardShape).background(scheme.surfaceVariant).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = muted,
        )

        Text(
            if (copied) stringResource(R.string.pv_oss_lic_copied, shown.size)
            else stringResource(R.string.pv_oss_lic_generate, shown.size),
            Modifier
                .clip(chipShape)
                .background(okColor)
                .clickable {
                    // 生成的致谢文本是**给机器/文档用的纯文本**，不是界面文案：
                    // 结构性的 ASCII 括号直接拼，避免为它单开一条带占位符的资源
                    val text = shown.joinToString("\n") { e ->
                        e.repo + " (" + context.getString(LIC_LABEL.getValue(e.license)) + ") " + e.url
                    }
                    clipboard.setText(AnnotatedString(text))
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    copied = true
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onPrimary,
        )

        Text(
            stringResource(R.string.pv_oss_lic_flow),
            style = MaterialTheme.typography.labelSmall,
            color = muted,
        )
    }
}
