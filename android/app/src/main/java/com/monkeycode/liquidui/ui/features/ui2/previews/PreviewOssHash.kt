package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.domain.BlurHash
import com.monkeycode.liquidui.domain.Keyframe
import com.monkeycode.liquidui.domain.KeyframeMode
import com.monkeycode.liquidui.domain.SettingsRules
import com.monkeycode.liquidui.domain.Timeline
import com.monkeycode.liquidui.domain.Track
import kotlinx.coroutines.delay
import kotlin.math.sqrt

/**
 * 批 G 开源取材 S19–S20：BlurHash 真实编解码 / 多轨关键帧时间轴。
 *
 * 两页都不是示意图：
 * S19 现场调用 `domain/BlurHash.kt` 编码再解码，屏幕上那串字符就是真的 hash，
 * 误差表也是当场算的（不是抄来的数字）；
 * S20 现场调用 `domain/KeyframeTrack.kt` 采样，下面那个方块的位置/透明度/缩放
 * 全部由采样值驱动，拖动游标就是真的在 scrub 一条时间轴。
 *
 * 依据：woltapp/blurhash（MIT）的算法规范与参考编码器、airbnb/lottie-android（Apache-2.0）的
 * 多轨关键帧时间轴模型。**都不引依赖**——算法在 `domain/` 里按规范重写，纯 Kotlin、可单测（批 A 覆盖）。
 */

// ==================== S19 BlurHash 真实编解码 ====================

/** 取样尺寸：编码在这一尺寸上做，MAE 也在这一尺寸上比（与单元测试同口径）。 */
private const val HASH_SRC_W = 16
private const val HASH_SRC_H = 12

/** 显示尺寸：解码可以解到任意尺寸，这正是 BlurHash 当占位图的用法。 */
private const val HASH_VIEW_W = 48
private const val HASH_VIEW_H = 36

/** 首次实测日期（写进页面，避免读数被当成长期有效的常量）。 */
private const val HASH_MEASURE_DATE = "2026-11-04"

private val HASH_PATTERNS = listOf("gradient", "solid", "stripes", "radial")

private val HASH_PATTERN_LABEL = mapOf(
    "gradient" to R.string.pv_oss_hash_pat_gradient,
    "solid" to R.string.pv_oss_hash_pat_solid,
    "stripes" to R.string.pv_oss_hash_pat_stripes,
    "radial" to R.string.pv_oss_hash_pat_radial,
)

/** 现场算 MAE 用的分量组合，含规范默认的 4×3。 */
private val HASH_COMP_STEPS = listOf(
    intArrayOf(1, 1), intArrayOf(2, 2), intArrayOf(4, 3), intArrayOf(6, 5), intArrayOf(9, 9),
)

/**
 * 程序化取样：不读 Bitmap、不解码文件，`(x, y) -> ARGB` 直接给。
 * 与 `BlurHash.encode` 的采样器入口同一个形状，所以单元测试和这一页看到的必须是同一个 hash。
 */
private fun hashSample(key: String, x: Int, y: Int, w: Int, h: Int): Int {
    val u = if (w > 1) x.toFloat() / (w - 1) else 0f
    val v = if (h > 1) y.toFloat() / (h - 1) else 0f
    return when (key) {
        // 纯色：本仓库的默认种子色，也是批 A 里那三条「反直觉」断言用的图
        "solid" -> 0xFF3E8FE0.toInt()
        "stripes" -> if (x / 3 % 2 == 0) 0xFF22262B.toInt() else 0xFFF3F5F8.toInt()
        "radial" -> {
            val dx = u - 0.5f
            val dy = v - 0.5f
            val d = (sqrt(dx * dx + dy * dy) * 2f).coerceIn(0f, 1f)
            val c = (255f * (1f - d)).toInt()
            (0xFF shl 24) or (c shl 16) or ((c * 0.6f).toInt() shl 8) or (255 - c)
        }
        else -> {
            // 渐变：左上 #3E8FE0 → 右下 #F2A03E，沿对角线插值
            val t = ((u + v) * 0.5f).coerceIn(0f, 1f)
            val r = (0x3E + (0xF2 - 0x3E) * t).toInt()
            val g = (0x8F + (0xA0 - 0x8F) * t).toInt()
            val b = (0xE0 + (0x3E - 0xE0) * t).toInt()
            (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
    }
}

private fun hashPixels(key: String, w: Int, h: Int): IntArray {
    val out = IntArray(w * h)
    for (y in 0 until h) {
        for (x in 0 until w) out[y * w + x] = hashSample(key, x, y, w, h)
    }
    return out
}

/** 逐像素画一张小图。颜色表在组合期就算好，Canvas 里只查表（探针不允许在绘制块里读主题）。 */
private fun DrawScope.drawPixelGrid(colors: List<Color>, cols: Int) {
    if (colors.isEmpty() || cols <= 0) return
    val cw = size.width / cols
    val ch = size.height / (colors.size / cols)
    for (i in colors.indices) {
        val px = i % cols
        val py = i / cols
        // +0.6f 盖掉相邻像素间的抗锯齿缝，否则小图看起来像网格
        drawRect(colors[i], Offset(px * cw, py * ch), Size(cw + 0.6f, ch + 0.6f))
    }
}

private fun fmt2(v: Float): String = String.format(java.util.Locale.US, "%.2f", v)

@Composable
fun PreviewS19() {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current

    var pattern by rememberSaveable { mutableStateOf("gradient") }
    var xComp by rememberSaveable { mutableIntStateOf(BlurHash.DEFAULT_X_COMPONENTS) }
    var yComp by rememberSaveable { mutableIntStateOf(BlurHash.DEFAULT_Y_COMPONENTS) }

    val srcPixels = remember(pattern) { hashPixels(pattern, HASH_SRC_W, HASH_SRC_H) }
    val hash = remember(srcPixels, xComp, yComp) {
        BlurHash.encode(srcPixels, HASH_SRC_W, HASH_SRC_H, xComp, yComp) ?: ""
    }
    val viewPixels = remember(hash) { BlurHash.decode(hash, HASH_VIEW_W, HASH_VIEW_H) ?: IntArray(0) }
    val srcSizePixels = remember(hash) { BlurHash.decode(hash, HASH_SRC_W, HASH_SRC_H) ?: IntArray(0) }
    val mae = remember(srcPixels, srcSizePixels) { BlurHash.meanAbsoluteError(srcPixels, srcSizePixels) }
    val segments = remember(hash) { BlurHash.segmentsOf(hash) }
    val dcArgb = remember(hash) { BlurHash.dcColor(hash) }
    // ARGB → #AARRGGBB 复用领域层的 SettingsRules.customSeedHex：同一件事不在两处各写一遍
    val dcHex = if (dcArgb != null) SettingsRules.customSeedHex(dcArgb) else "-"

    val srcColors = remember(srcPixels) { srcPixels.map { Color(it) } }
    val viewColors = remember(viewPixels) { viewPixels.map { Color(it) } }

    // 误差表当场算：换图案、换分量数都跟着变，页面上不会出现过期数字
    val maeRows = remember(srcPixels) {
        HASH_COMP_STEPS.map { step ->
            val rowHash = BlurHash.encode(srcPixels, HASH_SRC_W, HASH_SRC_H, step[0], step[1]) ?: ""
            val rowDecoded = BlurHash.decode(rowHash, HASH_SRC_W, HASH_SRC_H) ?: IntArray(0)
            Triple(step[0], step[1], BlurHash.meanAbsoluteError(srcPixels, rowDecoded))
        }
    }
    var bestIndex = 0
    maeRows.forEachIndexed { i, row -> if (row.third < maeRows[bestIndex].third) bestIndex = i }

    val card = RoundedCornerShape(10.dp)
    val pill = RoundedCornerShape(percent = 50)
    val segSizeColor = scheme.primary
    val segMaxColor = scheme.tertiary
    val segDcColor = scheme.secondary
    val segAcColor = scheme.onSurfaceVariant

    Column(
        Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            stringResource(R.string.pv_oss_hash_intro),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )

        // ---- 图案 ----
        Text(
            stringResource(R.string.pv_oss_hash_pattern_title),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurface,
        )
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HASH_PATTERNS.forEach { key ->
                val selected = key == pattern
                Text(
                    context.getString(HASH_PATTERN_LABEL.getValue(key)),
                    Modifier.clip(pill)
                        .background(if (selected) scheme.primaryContainer else scheme.surfaceVariant)
                        .clickable {
                            pattern = key
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                )
            }
        }

        // ---- 分量数 ----
        Text(
            stringResource(R.string.pv_oss_hash_comp_title),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurface,
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(true, false).forEach { isX ->
                val value = if (isX) xComp else yComp
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "-",
                        Modifier.clip(pill).background(scheme.surfaceVariant)
                            .clickable {
                                if (value > 1) {
                                    if (isX) xComp = value - 1 else yComp = value - 1
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.onSurfaceVariant,
                    )
                    Text(
                        "$value",
                        Modifier.width(20.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.onSurface,
                    )
                    Text(
                        "+",
                        Modifier.clip(pill).background(scheme.surfaceVariant)
                            .clickable {
                                if (value < 9) {
                                    if (isX) xComp = value + 1 else yComp = value + 1
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                stringResource(R.string.pv_oss_hash_comp_value, xComp, yComp),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
        }

        // ---- 原图 vs 解码图 ----
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.pv_oss_hash_original, HASH_SRC_W, HASH_SRC_H),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
                Canvas(Modifier.fillMaxWidth().height(96.dp).clip(card)) { drawPixelGrid(srcColors, HASH_SRC_W) }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.pv_oss_hash_decoded, HASH_VIEW_W, HASH_VIEW_H, hash.length),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
                Canvas(Modifier.fillMaxWidth().height(96.dp).clip(card)) { drawPixelGrid(viewColors, HASH_VIEW_W) }
            }
        }

        // ---- hash 逐段着色 ----
        Row(
            Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp)
                .horizontalScroll(rememberScrollState()),
        ) {
            segments.forEach { seg ->
                val roleColor = when (seg[2]) {
                    BlurHash.ROLE_SIZE -> segSizeColor
                    BlurHash.ROLE_MAX_AC -> segMaxColor
                    BlurHash.ROLE_DC -> segDcColor
                    else -> segAcColor
                }
                Text(
                    hash.substring(seg[0], seg[1]),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    color = roleColor,
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(stringResource(R.string.pv_oss_hash_seg_size), style = MaterialTheme.typography.labelSmall, color = segSizeColor)
            Text(stringResource(R.string.pv_oss_hash_seg_max), style = MaterialTheme.typography.labelSmall, color = segMaxColor)
            Text(stringResource(R.string.pv_oss_hash_seg_dc), style = MaterialTheme.typography.labelSmall, color = segDcColor)
            Text(
                stringResource(R.string.pv_oss_hash_seg_ac, xComp * yComp - 1),
                style = MaterialTheme.typography.labelSmall,
                color = segAcColor,
            )
        }
        Text(
            stringResource(R.string.pv_oss_hash_readout, hash.length, xComp, yComp, 4 + 2 * xComp * yComp, dcHex, fmt2(mae)),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurface,
        )

        // ---- 现场实测的 MAE 表 ----
        Text(
            stringResource(R.string.pv_oss_hash_mae_title),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurface,
        )
        maeRows.forEachIndexed { i, row ->
            val best = i == bestIndex
            Row(
                Modifier.fillMaxWidth().clip(card)
                    .background(if (best) scheme.primaryContainer else scheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(R.string.pv_oss_hash_mae_row, row.first, row.second, fmt2(row.third)),
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    color = if (best) scheme.onPrimaryContainer else scheme.onSurface,
                )
                if (best) {
                    Text(
                        stringResource(R.string.pv_oss_hash_mae_best),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onPrimaryContainer,
                    )
                }
            }
        }
        Text(
            stringResource(R.string.pv_oss_hash_mae_verdict),
            Modifier.fillMaxWidth().clip(card).background(scheme.errorContainer).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onErrorContainer,
        )
        Text(
            stringResource(R.string.pv_oss_hash_why),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.pv_oss_hash_mae_note, HASH_MEASURE_DATE),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )

        Text(
            stringResource(R.string.pv_oss_hash_api),
            Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.pv_oss_hash_downgrade),
            Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.pv_oss_hash_cmd),
            Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(0.dp))
    }
}

// ==================== S20 多轨关键帧时间轴 ====================

private val KF_TRACK_KEYS = listOf("translate", "alpha", "scale")

private val KF_TRACK_LABEL = mapOf(
    "translate" to R.string.pv_oss_kf_track_translate,
    "alpha" to R.string.pv_oss_kf_track_alpha,
    "scale" to R.string.pv_oss_kf_track_scale,
)

private val KF_MODE_KEYS = listOf("linear", "step", "smooth")

private val KF_MODE_LABEL = mapOf(
    "linear" to R.string.pv_oss_kf_mode_linear,
    "step" to R.string.pv_oss_kf_mode_step,
    "smooth" to R.string.pv_oss_kf_mode_smooth,
)

/** 画曲线时的归一化范围。写死是为了让三条量纲不同的轨道能在同一张图里比较。 */
private val KF_TRACK_RANGE = mapOf(
    "translate" to Pair(0f, 88f),
    "alpha" to Pair(0f, 1f),
    "scale" to Pair(0.5f, 1.3f),
)

private const val KF_SAMPLES = 72
private const val KF_TICK_MS = 16
private const val KF_OVERFLOW_MS = 500

private fun kfModeOf(key: String): KeyframeMode = when (key) {
    "step" -> KeyframeMode.STEP
    "smooth" -> KeyframeMode.SMOOTH
    else -> KeyframeMode.LINEAR
}

/**
 * 三条轨道的关键帧。时间点与取值是固定的，页面上能改的只有插值方式——
 * 这样「同一段关键帧、三种插值」的差别才看得见。
 */
private fun kfTrack(key: String, modeKey: String): Track {
    val mode = kfModeOf(modeKey)
    val keys = when (key) {
        "translate" -> listOf(
            Keyframe(0, 0f, mode),
            Keyframe(400, 88f, mode),
            Keyframe(900, 18f, mode),
            Keyframe(1400, 88f, mode),
        )
        "alpha" -> listOf(
            Keyframe(0, 0.15f, mode),
            Keyframe(500, 1f, mode),
            Keyframe(1100, 0.4f, mode),
            Keyframe(1400, 1f, mode),
        )
        else -> listOf(
            Keyframe(0, 0.7f, mode),
            Keyframe(700, 1.25f, mode),
            Keyframe(1400, 0.9f, mode),
        )
    }
    return Track(key, keys)
}

/** 屏幕 x → 时间。拖动与点按共用这一条，避免两处算法各写一遍。 */
private fun kfTimeAtX(x: Float, width: Float, timeline: Timeline): Int {
    if (width <= 0f) return timeline.startMs
    val ratio = (x / width).coerceIn(0f, 1f)
    return timeline.clampTime((timeline.startMs + ratio * timeline.durationMs).toInt())
}

@Composable
fun PreviewS20() {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current

    var translateMode by rememberSaveable { mutableStateOf("smooth") }
    var alphaMode by rememberSaveable { mutableStateOf("linear") }
    var scaleMode by rememberSaveable { mutableStateOf("step") }
    var cursorMs by rememberSaveable { mutableIntStateOf(600) }
    var playing by rememberSaveable { mutableStateOf(false) }

    val modeOf = mapOf("translate" to translateMode, "alpha" to alphaMode, "scale" to scaleMode)
    val timeline = remember(translateMode, alphaMode, scaleMode) {
        Timeline(KF_TRACK_KEYS.map { key -> kfTrack(key, modeOf.getValue(key)) })
    }

    // 播放：每帧推进 16ms，到末尾回到起点。key 里有 playing，暂停时协程自动取消
    LaunchedEffect(playing, timeline) {
        while (playing) {
            delay(KF_TICK_MS.toLong())
            val next = cursorMs + KF_TICK_MS
            cursorMs = if (next > timeline.endMs) timeline.startMs else next
        }
    }

    val sample = remember(timeline, cursorMs) { timeline.sampleAt(cursorMs) }
    val txValue = sample["translate"] ?: 0f
    val alphaValue = (sample["alpha"] ?: 1f).coerceIn(0f, 1f)
    val scaleValue = (sample["scale"] ?: 1f).coerceIn(0.3f, 2f)
    val passedCount = remember(timeline, cursorMs) { timeline.passedKeyframesAt(cursorMs) }
    val movingCount = remember(timeline, cursorMs) { timeline.movingTrackCountAt(cursorMs) }
    val timelineProgress = remember(timeline, cursorMs) { timeline.progressAt(cursorMs) }

    val card = RoundedCornerShape(10.dp)
    val pill = RoundedCornerShape(percent = 50)
    val laneBaseColor = scheme.surfaceVariant
    val cursorColor = scheme.error
    val keyColor = scheme.onSurface
    val trackColors = listOf(scheme.primary, scheme.secondary, scheme.tertiary)
    val timelineNow = timeline
    val cursorNow = cursorMs

    Column(
        Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            stringResource(R.string.pv_oss_kf_intro),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )

        // ---- 时间轴：点按定位、横向拖动 scrub ----
        Canvas(
            Modifier.fillMaxWidth().height(180.dp).clip(card)
                .pointerInput(timelineNow) {
                    val widthPx = size.width.toFloat()
                    detectTapGestures { pos -> cursorMs = kfTimeAtX(pos.x, widthPx, timelineNow) }
                }
                .pointerInput(timelineNow) {
                    val widthPx = size.width.toFloat()
                    detectHorizontalDragGestures { change, _ ->
                        cursorMs = kfTimeAtX(change.position.x, widthPx, timelineNow)
                    }
                },
        ) {
            val laneH = size.height / KF_TRACK_KEYS.size
            val w = size.width
            val start = timelineNow.startMs.toFloat()
            val span = timelineNow.durationMs.toFloat().coerceAtLeast(1f)
            for (i in KF_TRACK_KEYS.indices) {
                val track = timelineNow.tracks[i]
                val top = i * laneH
                val range = KF_TRACK_RANGE.getValue(track.key)
                val lo = range.first
                val hi = (range.second - range.first).let { if (it == 0f) 1f else it }
                drawRect(laneBaseColor, Offset(0f, top + laneH - 3f), Size(w, 3f))
                var prevX = 0f
                var prevY = top
                for (s in 0..KF_SAMPLES) {
                    val t = start + span * s / KF_SAMPLES
                    val v = track.valueAt(t.toInt())
                    val nx = w * (t - start) / span
                    val ny = top + laneH * 0.88f - laneH * 0.74f * ((v - lo) / hi).coerceIn(0f, 1f)
                    if (s > 0) drawLine(trackColors[i], Offset(prevX, prevY), Offset(nx, ny), 2.5f)
                    prevX = nx
                    prevY = ny
                }
                track.keys.forEach { kf ->
                    val kx = w * (kf.timeMs - start) / span
                    val ky = top + laneH * 0.88f - laneH * 0.74f * ((kf.value - lo) / hi).coerceIn(0f, 1f)
                    drawRect(keyColor, Offset(kx - 3.5f, ky - 3.5f), Size(7f, 7f))
                }
            }
            // 游标越界时夹在右边缘：读数会显示越界行为，线不会画出画布外
            val cx = (w * (cursorNow - start) / span).coerceIn(0f, w)
            drawLine(cursorColor, Offset(cx, 0f), Offset(cx, size.height), 2.5f)
        }
        Text(
            stringResource(R.string.pv_oss_kf_drag_hint),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )

        // ---- 游标读数 + 播放控制 ----
        val overflowText = if (cursorMs > timeline.endMs) {
            stringResource(R.string.pv_oss_kf_cursor_over)
        } else {
            stringResource(R.string.pv_oss_kf_cursor_in)
        }
        Text(
            stringResource(R.string.pv_oss_kf_cursor, cursorMs, overflowText),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurface,
        )
        Text(
            stringResource(
                R.string.pv_oss_kf_timeline,
                timeline.durationMs,
                "${(timelineProgress * 100).toInt()}%",
                passedCount,
                timeline.keyframeTotal,
                movingCount,
                timeline.trackCount,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val playLabel = if (playing) {
                stringResource(R.string.pv_oss_kf_pause)
            } else {
                stringResource(R.string.pv_oss_kf_play)
            }
            listOf(playLabel to 0, context.getString(R.string.pv_oss_kf_reset) to 1, context.getString(R.string.pv_oss_kf_overflow) to 2)
                .forEach { entry ->
                    Text(
                        entry.first,
                        Modifier.clip(pill).background(scheme.surfaceVariant)
                            .clickable {
                                when (entry.second) {
                                    0 -> playing = !playing
                                    1 -> {
                                        playing = false
                                        cursorMs = timeline.startMs
                                    }
                                    else -> {
                                        playing = false
                                        // 故意越界：这一页要演示的就是「游标超出末尾之后各读数怎么表现」
                                        cursorMs = timeline.endMs + KF_OVERFLOW_MS
                                    }
                                }
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
        }

        // ---- 采样值驱动的实体 ----
        Text(
            stringResource(R.string.pv_oss_kf_live),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Box(
            Modifier.fillMaxWidth().height(92.dp).clip(card).background(scheme.surfaceVariant),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                Modifier.padding(start = 14.dp).size(52.dp)
                    .graphicsLayer {
                        translationX = txValue * 2.4f
                        alpha = alphaValue
                        scaleX = scaleValue
                        scaleY = scaleValue
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .background(scheme.primary),
            )
        }

        // ---- 每条轨道：读数 + 插值方式 ----
        KF_TRACK_KEYS.forEach { key ->
            val track = timeline.trackOf(key)
            val value = track?.valueAt(cursorMs) ?: 0f
            val segIndex = track?.segmentIndexOf(cursorMs) ?: 0
            val segCount = track?.segmentCount ?: 0
            val segProgress = track?.segmentProgressAt(cursorMs) ?: 0f
            val modeKey = modeOf.getValue(key)
            Column(
                Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    stringResource(
                        R.string.pv_oss_kf_row,
                        context.getString(KF_TRACK_LABEL.getValue(key)),
                        fmt2(value),
                        segIndex + 1,
                        segCount,
                        context.getString(KF_MODE_LABEL.getValue(modeKey)),
                        "${(segProgress * 100).toInt()}%",
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    KF_MODE_KEYS.forEach { mk ->
                        val selected = mk == modeKey
                        Text(
                            context.getString(KF_MODE_LABEL.getValue(mk)),
                            Modifier.clip(pill)
                                .background(if (selected) scheme.primaryContainer else scheme.surface)
                                .clickable {
                                    when (key) {
                                        "translate" -> translateMode = mk
                                        "alpha" -> alphaMode = mk
                                        else -> scaleMode = mk
                                    }
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Text(
            stringResource(R.string.pv_oss_kf_smooth_note),
            Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.pv_oss_kf_boundary),
            Modifier.fillMaxWidth().clip(card).background(scheme.errorContainer).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onErrorContainer,
        )
        Text(
            stringResource(R.string.pv_oss_kf_api),
            Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(0.dp))
    }
}
