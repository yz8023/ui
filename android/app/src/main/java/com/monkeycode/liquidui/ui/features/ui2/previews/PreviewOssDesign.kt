package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.SectionTitle
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * 批 D 开源取材·设计两条：S01 色彩令牌与对比度检查、S02 图标网格与四轴规格。
 *
 * 来源申明（详见 `素材库/UI2体验馆/S01-color-contrast/参考来源.md`）：
 *  · S01 参考 material-foundation/material-color-utilities（Apache-2.0）的色调板分档与暗色映射口径；
 *    **对比度与相对亮度是 WCAG 2.x 的精确实现**，色调板明度档用 HSL 插值**近似**——上游的
 *    HCT/CAM16 色空间涉及色适应矩阵，本仓库不复刻，页面上也如实标注「近似」。
 *  · S02 参考 google/material-design-icons（Apache-2.0）的 24dp 视口、2dp 内边距、
 *    关键形状（keyline shapes）与 Material Symbols 的 fill/weight/grade/optical size 四轴。
 * 两者都只借鉴规格与算法口径，未复制任何上游源码；本仓库为 MIT。
 */

// ─── 颜色数学（S01 与 S08 共用）─────────────────────

/** sRGB 通道反伽马。WCAG 2.x 相对亮度的第一步。 */
internal fun srgbChannelToLinear(c: Float): Float =
    if (c <= 0.04045f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)

/** WCAG 2.x 相对亮度，0（黑）–1（白）。 */
internal fun relativeLuminance(c: Color): Float =
    0.2126f * srgbChannelToLinear(c.red) +
        0.7152f * srgbChannelToLinear(c.green) +
        0.0722f * srgbChannelToLinear(c.blue)

/** WCAG 对比度，1.0–21.0。永远把亮的一方放分子，所以参数顺序无所谓。 */
internal fun contrastRatio(a: Color, b: Color): Float {
    val la = relativeLuminance(a)
    val lb = relativeLuminance(b)
    return (max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)
}

/** Color → HSL 三元组（h 0–360、s 0–1、l 0–1）。 */
internal fun colorToHsl(c: Color): FloatArray {
    val r = c.red; val g = c.green; val b = c.blue
    val mx = max(r, max(g, b)); val mn = min(r, min(g, b))
    val d = mx - mn
    val l = (mx + mn) / 2f
    if (d == 0f) return floatArrayOf(0f, 0f, l)
    val s = d / (1f - abs(2f * l - 1f)).coerceAtLeast(0.0001f)
    val h = when (mx) {
        r -> 60f * (((g - b) / d) % 6f)
        g -> 60f * (((b - r) / d) + 2f)
        else -> 60f * (((r - g) / d) + 4f)
    }
    return floatArrayOf((h + 360f) % 360f, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))
}

/** HSL → Color。手写而非借 android.graphics，是为了让明度档真的是 HSL 的 L。 */
internal fun hslToColor(h: Float, s: Float, l: Float): Color {
    val c = (1f - abs(2f * l - 1f)) * s
    val hp = (((h % 360f) + 360f) % 360f) / 60f
    val x = c * (1f - abs(hp % 2f - 1f))
    val idx = hp.toInt().coerceIn(0, 5)
    val rgb = floatArrayOf(
        if (idx == 0 || idx == 5) c else if (idx == 1 || idx == 4) x else 0f,
        if (idx == 1 || idx == 2) c else if (idx == 0 || idx == 3) x else 0f,
        if (idx == 3 || idx == 4) c else if (idx == 2 || idx == 5) x else 0f
    )
    val m = l - c / 2f
    return Color(
        (rgb[0] + m).coerceIn(0f, 1f),
        (rgb[1] + m).coerceIn(0f, 1f),
        (rgb[2] + m).coerceIn(0f, 1f)
    )
}

/**
 * 由种子色生成某一档色调（tone 0–100）。
 *
 * **近似实现**：上游 material-color-utilities 在 HCT（基于 CAM16 的色适应空间）里取等 L* 档，
 * 这里用 HSL 的 L 直接当档位，并把饱和度在明度两极自然收敛（否则 T95 会发灰、T5 会发红）。
 * 页面与文档都写明这一点，不冒充上游算法。
 */
internal fun toneOf(seed: Color, tone: Int): Color {
    val hsl = colorToHsl(seed)
    val l = (tone / 100f).coerceIn(0f, 1f)
    val chromaFall = 1f - abs(2f * l - 1f)
    val s = (hsl[1] * (0.35f + 0.65f * chromaFall)).coerceIn(0f, 1f)
    return hslToColor(hsl[0], s, l)
}

/** #RRGGBB，大写。 */
internal fun Color.toHex(): String {
    val v = toArgb()
    return "#%06X".format(0xFFFFFF and v)
}

/** 对比度分级：AAA(7)、AA(4.5)、AA 大字(3)、不合格。返回稳定英文键，文案在渲染点解析。 */
internal fun contrastGrade(ratio: Float): String = when {
    ratio >= 7f -> "aaa"
    ratio >= 4.5f -> "aa"
    ratio >= 3f -> "aa_large"
    else -> "fail"
}

private val OssToneSteps = listOf(0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 95, 99, 100)

private val OssSeedSwatches = listOf(
    Color(0xFF3482FF), Color(0xFF00A870), Color(0xFFB4690E),
    Color(0xFF8B5CF6), Color(0xFFE5484D), Color(0xFF0F172A)
)

// ─── S01 ──────────────────────────────────────────

@Composable
fun PreviewS01() {
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    val gradeLabels = mapOf(
        "aaa" to stringResource(R.string.pv_oss_color_grade_aaa),
        "aa" to stringResource(R.string.pv_oss_color_grade_aa),
        "aa_large" to stringResource(R.string.pv_oss_color_grade_aa_large),
        "fail" to stringResource(R.string.pv_oss_color_grade_fail)
    )
    var seedIndex by remember { mutableStateOf(0) }
    var hueShift by remember { mutableFloatStateOf(0f) }
    val seedBase = OssSeedSwatches[seedIndex]
    val seedHsl = remember(seedBase) { colorToHsl(seedBase) }
    val seed = remember(seedBase, hueShift) {
        hslToColor((seedHsl[0] + hueShift) % 360f, seedHsl[1], seedHsl[2])
    }
    val palette = remember(seed) { OssToneSteps.map { it to toneOf(seed, it) } }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_color_title))
            Text(
                stringResource(R.string.pv_oss_color_intro),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.pv_oss_color_seed_label),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OssSeedSwatches.forEachIndexed { i, c ->
                    val on = i == seedIndex
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(c)
                            .border(
                                if (on) 3.dp else 1.dp,
                                if (on) MaterialTheme.colorScheme.onSurface else Color(0x33000000),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { seedIndex = i }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.pv_oss_color_hue, hueShift.roundToInt()),
                style = MaterialTheme.typography.labelMedium
            )
            Slider(
                value = hueShift,
                onValueChange = { hueShift = it },
                valueRange = 0f..359f
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(seed)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    seed.toHex(),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable {
                        clipboard.setText(AnnotatedString(seed.toHex()))
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(R.string.pv_oss_color_copy_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_color_palette_title))
            Text(
                stringResource(R.string.pv_oss_color_palette_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            palette.forEach { entry ->
                val tone = entry.first
                val c = entry.second
                val vsWhite = contrastRatio(c, Color.White)
                val vsBlack = contrastRatio(c, Color.Black)
                val useWhite = vsWhite >= vsBlack
                val grade = contrastGrade(if (useWhite) vsWhite else vsBlack)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .width(52.dp)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(c)
                            .clickable {
                                clipboard.setText(AnnotatedString(c.toHex()))
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "T$tone",
                            color = if (useWhite) Color.White else Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        c.toHex(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.width(76.dp)
                    )
                    Text(
                        stringResource(
                            R.string.pv_oss_color_contrast_pair,
                            "%.2f".format(if (useWhite) vsWhite else vsBlack),
                            if (useWhite) stringResource(R.string.pv_oss_color_on_white)
                            else stringResource(R.string.pv_oss_color_on_black)
                        ),
                        fontSize = 12.sp,
                        modifier = Modifier.width(124.dp)
                    )
                    Text(
                        gradeLabels.getValue(grade),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (grade == "fail") MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.pv_oss_color_luminance, "%.4f".format(relativeLuminance(seed))),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_color_mapping_title))
            Text(
                stringResource(R.string.pv_oss_color_mapping_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            OssRoleRows.forEach { row ->
                val lightFg = toneOf(seed, row.lightFg)
                val lightBg = toneOf(seed, row.lightBg)
                val darkFg = toneOf(seed, row.darkFg)
                val darkBg = toneOf(seed, row.darkBg)
                val lr = contrastRatio(lightFg, lightBg)
                val dr = contrastRatio(darkFg, darkBg)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(row.nameRes),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.width(96.dp)
                    )
                    OssSwatchChip(
                        bg = lightBg, fg = lightFg,
                        label = stringResource(
                            R.string.pv_oss_color_tone_pair, row.lightFg, row.lightBg
                        ),
                        ratio = lr
                    )
                    Spacer(Modifier.width(8.dp))
                    OssSwatchChip(
                        bg = darkBg, fg = darkFg,
                        label = stringResource(
                            R.string.pv_oss_color_tone_pair, row.darkFg, row.darkBg
                        ),
                        ratio = dr
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        gradeLabels.getValue(contrastGrade(min(lr, dr))),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (min(lr, dr) < 4.5f) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.pv_oss_color_mapping_rule),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private class OssRoleRow(
    val nameRes: Int,
    val lightFg: Int,
    val lightBg: Int,
    val darkFg: Int,
    val darkBg: Int
)

private val OssRoleRows = listOf(
    OssRoleRow(R.string.pv_oss_color_role_primary, 100, 40, 20, 80),
    OssRoleRow(R.string.pv_oss_color_role_surface, 10, 99, 90, 10),
    OssRoleRow(R.string.pv_oss_color_role_outline, 50, 99, 60, 20),
    OssRoleRow(R.string.pv_oss_color_role_error, 100, 40, 20, 80)
)

@Composable
private fun OssSwatchChip(bg: Color, fg: Color, label: String, ratio: Float) {
    Column(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text("%.1f:1".format(ratio), color = fg, fontSize = 10.sp)
    }
}

// ─── S02 ──────────────────────────────────────────

private val OssKeylineShapes = listOf("circle", "square", "vertical", "horizontal")

@Composable
fun PreviewS02() {
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    var weight by remember { mutableFloatStateOf(400f) }
    var grade by remember { mutableFloatStateOf(0f) }
    var filled by remember { mutableStateOf(false) }
    var optical by remember { mutableStateOf(24) }
    var showGrid by remember { mutableStateOf(true) }
    var showKeyline by remember { mutableStateOf(true) }
    val keylineLabel = mapOf(
        "circle" to stringResource(R.string.pv_oss_icon_key_circle),
        "square" to stringResource(R.string.pv_oss_icon_key_square),
        "vertical" to stringResource(R.string.pv_oss_icon_key_vertical),
        "horizontal" to stringResource(R.string.pv_oss_icon_key_horizontal)
    )
    // weight 100–700 → 线宽；grade 再微调；optical size 越大，同样视觉重量需要越粗的线
    val strokeDp = (0.9f + (weight - 100f) / 600f * 1.9f + grade / 200f * 0.5f) *
        (0.85f + (optical - 20) / 40f * 0.35f)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_icon_title))
            Text(
                stringResource(R.string.pv_oss_icon_intro),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_icon_grid_title))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OssKeylineShapes.forEach { k ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        OssKeylineCanvas(k, showGrid)
                        Text(
                            keylineLabel.getValue(k),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = showGrid, onCheckedChange = { showGrid = it })
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.pv_oss_icon_toggle_grid), fontSize = 12.sp)
                Spacer(Modifier.width(18.dp))
                Switch(checked = showKeyline, onCheckedChange = { showKeyline = it })
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.pv_oss_icon_toggle_keyline), fontSize = 12.sp)
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_icon_axis_title))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OssAxisIcon(filled = filled, strokeDp = strokeDp, viewport = optical, size = 96)
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.pv_oss_icon_axis_weight, weight.roundToInt()),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(value = weight, onValueChange = { weight = it }, valueRange = 100f..700f)
                    Text(
                        stringResource(R.string.pv_oss_icon_axis_grade, grade.roundToInt()),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(value = grade, onValueChange = { grade = it }, valueRange = -50f..200f)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Switch(checked = filled, onCheckedChange = { filled = it })
                        Text(
                            stringResource(
                                if (filled) R.string.pv_oss_icon_axis_fill_on
                                else R.string.pv_oss_icon_axis_fill_off
                            ),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(20, 24, 40).forEach { vp ->
                            val on = vp == optical
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (on) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { optical = vp }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    stringResource(R.string.pv_oss_icon_axis_optical, vp),
                                    fontSize = 11.sp,
                                    color = if (on) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.pv_oss_icon_axis_stroke, "%.2f".format(strokeDp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable {
                    clipboard.setText(AnnotatedString("%.2f".format(strokeDp)))
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
        }

        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_icon_size_title))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                listOf(18, 20, 24, 36, 48).forEach { s ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        OssAxisIcon(filled = filled, strokeDp = strokeDp, viewport = 24, size = s)
                        Text(
                            stringResource(R.string.pv_oss_icon_size_dp, s),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.pv_oss_icon_size_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 24dp 视口 + 2dp 安全区 + 一条关键形状。纯 Canvas 实绘。 */
@Composable
private fun OssKeylineCanvas(shape: String, showGrid: Boolean) {
    val grid = MaterialTheme.colorScheme.outlineVariant
    val line = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(64.dp)) {
        val u = size.width / 24f
        if (showGrid) {
            var i = 0
            while (i <= 24) {
                drawLine(grid, Offset(i * u, 0f), Offset(i * u, size.height), 1f)
                drawLine(grid, Offset(0f, i * u), Offset(size.width, i * u), 1f)
                i += 4
            }
            drawRect(grid, Offset(2 * u, 2 * u), Size(20 * u, 20 * u), style = Stroke(2f))
        }
        val pad = 2 * u
        val live = size.width - pad * 2
        when (shape) {
            "circle" -> drawCircle(line, radius = live / 2f, center = Offset(size.width / 2f, size.height / 2f), style = Stroke(3f))
            "square" -> drawRect(line, Offset(3 * u, 3 * u), Size(18 * u, 18 * u), style = Stroke(3f))
            "vertical" -> drawRect(line, Offset(4 * u, 2 * u), Size(16 * u, 20 * u), style = Stroke(3f))
            else -> drawRect(line, Offset(2 * u, 4 * u), Size(20 * u, 16 * u), style = Stroke(3f))
        }
    }
}

/**
 * 示例图标（铃铛）：按 fill / weight / optical size 三轴重绘。
 * viewport 越大，同一图形在相同线宽下看起来越细，所以线宽要随视口放大——这正是
 * Material Symbols「光学尺寸」轴存在的原因。
 */
@Composable
private fun OssAxisIcon(filled: Boolean, strokeDp: Float, viewport: Int, size: Int) {
    val tint = MaterialTheme.colorScheme.onSurface
    val scale = size.toFloat() / viewport.toFloat()
    val strokePx = with(LocalDensity.current) { (strokeDp * scale).dp.toPx() }
    Canvas(Modifier.size(size.dp)) {
        val u = this.size.width / viewport.toFloat()
        val cx = this.size.width / 2f
        val bodyTop = 5f * u
        val bodyBottom = 16f * u
        val halfW = 5.5f * u
        val path = Path().apply {
            moveTo(cx - halfW, bodyBottom)
            cubicTo(cx - halfW, bodyBottom - 3f * u, cx - halfW + 0.6f * u, bodyTop + 1.5f * u, cx, bodyTop)
            cubicTo(cx + halfW - 0.6f * u, bodyTop + 1.5f * u, cx + halfW, bodyBottom - 3f * u, cx + halfW, bodyBottom)
            close()
        }
        if (filled) {
            drawPath(path, tint)
        } else {
            drawPath(path, tint, style = Stroke(strokePx))
        }
        drawLine(
            tint,
            Offset(cx - 7f * u, bodyBottom),
            Offset(cx + 7f * u, bodyBottom),
            if (filled) 2.2f * u * 0.42f else strokePx,
            cap = StrokeCap.Round
        )
        if (filled) {
            drawCircle(tint, radius = 1.6f * u, center = Offset(cx, bodyBottom + 2.6f * u))
        } else {
            drawArc(
                tint, 0f, 180f, false,
                topLeft = Offset(cx - 1.8f * u, bodyBottom + 0.8f * u),
                size = Size(3.6f * u, 3.6f * u),
                style = Stroke(strokePx)
            )
        }
    }
}
