package com.monkeycode.liquidui.ui.features.ui2.previews

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshot.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.SectionTitle
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 批 D 开源取材·质量一条：S08 无障碍自检器。
 *
 * 来源申明：检查项口径参考 google/Accessibility-Test-Framework-for-Android（Apache-2.0，
 * Accessibility Check Framework）——它把无障碍问题做成一条条可执行的 Check（触摸目标尺寸、
 * 文本对比度、可点击项缺语义标签等）。本条**不引该依赖**（那是 JUnit/Robolectric 体系的库），
 * 只把其中五条能在运行时自查的口径搬到界面上，当场算给你看：
 *  1. 触摸目标 ≥ 48dp —— `onGloballyPositioned` 实测本页 6 个真实控件的尺寸；
 *  2. 文本对比度 ≥ 4.5:1 —— 用 S01 同一套 WCAG 2.x 公式算当前主题的 5 组前景/背景配对；
 *  3. 系统动画开关 —— 读 `Settings.Global.ANIMATOR_DURATION_SCALE`（无需权限）；
 *  4. 字体缩放 —— 读 `LocalDensity.fontScale`；
 *  5. 语义标签 —— **声明式自检**：把本页图标按钮有没有传 contentDescription 逐条列出。
 *     运行时遍历 SemanticsNode 需要 compose-ui-test 依赖，本仓库 0 测试依赖，故不做假遍历。
 */

private class OssA11yProbe(val id: String, val labelRes: Int, val sideDp: Int, val hasContentDesc: Boolean)

/** 6 个被实测的控件：故意混入 3 个不达标的，检查器必须能点出来。 */
private val OssA11yProbes = listOf(
    OssA11yProbe("ok_button", R.string.pv_oss_a11y_probe_ok_button, 52, true),
    OssA11yProbe("small_icon", R.string.pv_oss_a11y_probe_small_icon, 32, false),
    OssA11yProbe("row_target", R.string.pv_oss_a11y_probe_row_target, 48, true),
    OssA11yProbe("chip_target", R.string.pv_oss_a11y_probe_chip_target, 40, true),
    OssA11yProbe("edge_icon", R.string.pv_oss_a11y_probe_edge_icon, 44, false),
    OssA11yProbe("big_target", R.string.pv_oss_a11y_probe_big_target, 64, true)
)

private const val OSS_A11Y_MIN_TARGET_DP = 48
private const val OSS_A11Y_MIN_CONTRAST = 4.5f
private const val OSS_A11Y_FONT_SCALE_WARN = 1.3f

@Composable
fun PreviewS08() {
    val context = LocalContext.current
    val density = LocalDensity.current
    val measured = remember { mutableStateMapOf<String, Int>() }
    var runId by remember { mutableIntStateOf(1) }

    val animatorScale = remember(runId) {
        try {
            Settings.Global.getFloat(
                context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f
            )
        } catch (e: Exception) {
            1f
        }
    }
    val fontScale = density.fontScale
    val scheme = MaterialTheme.colorScheme

    // 五组前景/背景配对，全部当场算
    val pairs = remember(scheme, runId) {
        listOf(
            R.string.pv_oss_a11y_pair_body to (scheme.onSurface to scheme.surface),
            R.string.pv_oss_a11y_pair_muted to (scheme.onSurfaceVariant to scheme.surface),
            R.string.pv_oss_a11y_pair_primary to (scheme.onPrimary to scheme.primary),
            R.string.pv_oss_a11y_pair_outline to (scheme.outline to scheme.surface),
            R.string.pv_oss_a11y_pair_error to (scheme.onError to scheme.error)
        )
    }

    val touchFail = OssA11yProbes.count { (measured[it.id] ?: it.sideDp) < OSS_A11Y_MIN_TARGET_DP }
    val contrastFail = pairs.count { contrastRatio(it.second.first, it.second.second) < OSS_A11Y_MIN_CONTRAST }
    val descMissing = OssA11yProbes.count { !it.hasContentDesc }
    val animOff = animatorScale <= 0f
    val fontBig = fontScale > OSS_A11Y_FONT_SCALE_WARN
    val failTotal = touchFail + contrastFail + descMissing + (if (fontBig) 1 else 0)
    val warnTotal = if (animOff) 1 else 0

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_a11y_title))
            Text(
                stringResource(R.string.pv_oss_a11y_intro),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OssA11yBadge(
                    label = stringResource(R.string.pv_oss_a11y_sum_fail, failTotal),
                    color = MaterialTheme.colorScheme.error
                )
                OssA11yBadge(
                    label = stringResource(R.string.pv_oss_a11y_sum_warn, warnTotal),
                    color = MaterialTheme.colorScheme.tertiary
                )
                OssA11yBadge(
                    label = stringResource(R.string.pv_oss_a11y_sum_run, runId),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { runId += 1 }
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    stringResource(R.string.pv_oss_a11y_rerun),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. 触摸目标实测
        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_a11y_touch_title))
            Text(
                stringResource(R.string.pv_oss_a11y_touch_rule, OSS_A11Y_MIN_TARGET_DP),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OssA11yProbes.forEach { probe ->
                    Box(
                        Modifier
                            .size(probe.sideDp.dp)
                            .clip(if (probe.id == "ok_button") RoundedCornerShape(12.dp) else CircleShape)
                            .background(
                                if ((measured[probe.id] ?: probe.sideDp) < OSS_A11Y_MIN_TARGET_DP) {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.22f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                }
                            )
                            .onSizeProbe(probe.id, measured, density.density)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            OssA11yProbes.forEach { probe ->
                val dp = measured[probe.id] ?: probe.sideDp
                val ok = dp >= OSS_A11Y_MIN_TARGET_DP
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(probe.labelRes),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        stringResource(R.string.pv_oss_a11y_touch_value, dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
            if (touchFail > 0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.pv_oss_a11y_touch_fix, touchFail),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // 2. 对比度实测
        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_a11y_contrast_title))
            Text(
                stringResource(R.string.pv_oss_a11y_contrast_rule, "%.1f".format(OSS_A11Y_MIN_CONTRAST)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            pairs.forEach { entry ->
                val fg = entry.second.first
                val bg = entry.second.second
                val ratio = contrastRatio(fg, bg)
                val ok = ratio >= OSS_A11Y_MIN_CONTRAST
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .width(58.dp)
                            .height(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.pv_oss_a11y_contrast_sample), color = fg, fontSize = 11.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        stringResource(entry.first),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        stringResource(R.string.pv_oss_a11y_contrast_value, "%.2f".format(ratio)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
            if (contrastFail > 0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.pv_oss_a11y_contrast_fix, contrastFail),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // 3/4. 系统动画开关与字体缩放
        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_a11y_system_title))
            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Text(
                    stringResource(R.string.pv_oss_a11y_anim_label),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    stringResource(R.string.pv_oss_a11y_anim_value, "%.2f".format(animatorScale)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                if (animOff) stringResource(R.string.pv_oss_a11y_anim_off)
                else stringResource(R.string.pv_oss_a11y_anim_on),
                style = MaterialTheme.typography.bodySmall,
                color = if (animOff) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Text(
                    stringResource(R.string.pv_oss_a11y_font_label),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    stringResource(R.string.pv_oss_a11y_font_value, "%.2f".format(fontScale)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (fontBig) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
            Text(
                if (fontBig) stringResource(R.string.pv_oss_a11y_font_big)
                else stringResource(R.string.pv_oss_a11y_font_ok),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 5. 语义标签声明自检
        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_a11y_semantics_title))
            Text(
                stringResource(R.string.pv_oss_a11y_semantics_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            OssA11yProbes.forEach { probe ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(probe.labelRes),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (probe.hasContentDesc) stringResource(R.string.pv_oss_a11y_semantics_ok)
                        else stringResource(R.string.pv_oss_a11y_semantics_missing),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (probe.hasContentDesc) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }
            }
            if (descMissing > 0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.pv_oss_a11y_semantics_fix, descMissing),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.pv_oss_a11y_measure_dp, measured.size, OssA11yProbes.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 把实测到的边长（取宽高较小值，dp）写回状态表；值没变就不写，避免布局回环。 */
private fun Modifier.onSizeProbe(id: String, sink: SnapshotStateMap<String, Int>, density: Float): Modifier =
    this.onGloballyPositioned { coords ->
        val dp = min(coords.size.width / density, coords.size.height / density).roundToInt()
        if (sink[id] != dp) sink[id] = dp
    }

@Composable
private fun OssA11yBadge(label: String, color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}
