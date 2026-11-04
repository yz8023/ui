package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monkeycode.liquidui.R
import kotlin.math.abs

/**
 * 批 F 开源取材 S15–S16：图片变换九宫格 / 可缩放看图与占位。
 *
 * S15 依据 wasabeef/glide-transformations（Apache-2.0）的**变换清单与参数口径**，
 * 实现全部走 Compose 的 ColorMatrix／ColorFilter／DrawScope.clipPath／多趟叠加，不引 Glide；
 * S16 依据 saket/telephoto（Apache-2.0）的缩放平移与低清占位思路，不引依赖。
 * 清单见 docs/开源素材清单-批F.md，来源申明见「关于 → 开源致谢」。
 *
 * 底图一律 Canvas 现场画（渐变 + 斜纹 + 同心圆 + 亮块），不引图片资源、不联网。
 */

// ==================== 共用：现场画的底图 ====================

/**
 * @param filter 逐绘制调用传入：ColorFilter 只能挂在 DrawScope 的绘制函数上，
 *               `graphicsLayer` 没有 colorFilter 这个属性（这是 S15 最容易猜错的地方）。
 * @param alpha  多趟叠加做近似模糊时用。
 */
private fun DrawScope.drawSamplePhoto(
    top: Color,
    bottom: Color,
    grid: Color,
    filter: ColorFilter? = null,
    alpha: Float = 1f,
) {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(top, bottom),
            start = Offset.Zero,
            end = Offset(size.width, size.height),
        ),
        alpha = alpha,
        colorFilter = filter,
    )
    val step = size.width / 9f
    var x = -size.height
    while (x < size.width) {
        drawLine(grid, Offset(x, 0f), Offset(x + size.height, size.height), 2f, alpha = alpha, colorFilter = filter)
        x += step
    }
    for (k in 1..3) {
        drawCircle(
            color = grid,
            radius = size.width * 0.075f * k,
            center = Offset(size.width * 0.70f, size.height * 0.34f),
            alpha = alpha,
            style = Stroke(width = 2f),
            colorFilter = filter,
        )
    }
    // 一块纯白与一块纯黑：灰度／反色／对比度有没有生效，看这两块最直观
    drawRect(
        color = Color.White,
        topLeft = Offset(size.width * 0.10f, size.height * 0.62f),
        size = Size(size.width * 0.24f, size.height * 0.22f),
        alpha = alpha,
        colorFilter = filter,
    )
    drawRect(
        color = Color.Black,
        topLeft = Offset(size.width * 0.40f, size.height * 0.62f),
        size = Size(size.width * 0.24f, size.height * 0.22f),
        alpha = alpha * 0.85f,
        colorFilter = filter,
    )
}

private fun grayMatrix(): ColorMatrix = ColorMatrix().apply { setToSaturation(0f) }

private fun invertMatrix(): ColorMatrix = ColorMatrix(
    floatArrayOf(
        -1f, 0f, 0f, 0f, 255f,
        0f, -1f, 0f, 0f, 255f,
        0f, 0f, -1f, 0f, 255f,
        0f, 0f, 0f, 1f, 0f,
    )
)

/** 对比度 1.45 倍并把中点压回去：矩阵对角缩放 + 平移，两个动作缺一不可。 */
private fun contrastMatrix(amount: Float): ColorMatrix {
    val t = (1f - amount) * 127.5f
    return ColorMatrix(
        floatArrayOf(
            amount, 0f, 0f, 0f, t,
            0f, amount, 0f, 0f, t,
            0f, 0f, amount, 0f, t,
            0f, 0f, 0f, 1f, 0f,
        )
    )
}

private fun starPath(w: Float, h: Float): Path {
    val path = Path()
    val cx = w / 2f
    val cy = h / 2f
    val outer = minOf(w, h) * 0.46f
    val inner = outer * 0.42f
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outer else inner
        val a = Math.PI / 5.0 * i - Math.PI / 2.0
        val px = cx + (r * Math.cos(a)).toFloat()
        val py = cy + (r * Math.sin(a)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    return path
}

// ==================== S15 图片变换九宫格 ====================

private val FX_KINDS = listOf(
    "circle", "rounded", "grayscale",
    "invert", "duotone", "contrast",
    "mask", "rotate_flip", "blur",
)

private val FX_LABEL = mapOf(
    "circle" to R.string.pv_oss_fx_circle,
    "rounded" to R.string.pv_oss_fx_rounded,
    "grayscale" to R.string.pv_oss_fx_grayscale,
    "invert" to R.string.pv_oss_fx_invert,
    "duotone" to R.string.pv_oss_fx_duotone,
    "contrast" to R.string.pv_oss_fx_contrast,
    "mask" to R.string.pv_oss_fx_mask,
    "rotate_flip" to R.string.pv_oss_fx_rotate_flip,
    "blur" to R.string.pv_oss_fx_blur,
)

private val FX_NOTE = mapOf(
    "circle" to R.string.pv_oss_fx_note_circle,
    "rounded" to R.string.pv_oss_fx_note_rounded,
    "grayscale" to R.string.pv_oss_fx_note_grayscale,
    "invert" to R.string.pv_oss_fx_note_invert,
    "duotone" to R.string.pv_oss_fx_note_duotone,
    "contrast" to R.string.pv_oss_fx_note_contrast,
    "mask" to R.string.pv_oss_fx_note_mask,
    "rotate_flip" to R.string.pv_oss_fx_note_rotate_flip,
    "blur" to R.string.pv_oss_fx_note_blur,
)

@Composable
fun PreviewS15() {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current

    var selected by rememberSaveable { mutableStateOf("grayscale") }

    val photoTop = scheme.primary
    val photoBottom = scheme.tertiary
    val photoGrid = Color.White.copy(alpha = 0.35f)
    val duoShadow = scheme.primary
    val duoHighlight = scheme.tertiary
    val cellBg = scheme.surfaceVariant
    val chipShape = RoundedCornerShape(10.dp)

    Column(
        Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            stringResource(R.string.pv_oss_fx_intro),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        for (row in 0 until 3) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (col in 0 until 3) {
                    val kind = FX_KINDS[row * 3 + col]
                    val on = kind == selected
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(chipShape)
                            .background(if (on) scheme.primaryContainer else cellBg)
                            .clickable {
                                selected = kind
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val shapeMod = when (kind) {
                            "circle" -> Modifier.clip(CircleShape)
                            "rounded" -> Modifier.clip(RoundedCornerShape(12.dp))
                            else -> Modifier
                        }
                        // 双色调要叠加 BlendMode，必须先把这一格隔离成离屏层，否则会渗透到页面背景
                        val layerMod = if (kind == "duotone") {
                            Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        } else {
                            Modifier
                        }
                        Canvas(Modifier.size(72.dp).then(layerMod).then(shapeMod)) {
                            when (kind) {
                                "grayscale" -> drawSamplePhoto(photoTop, photoBottom, photoGrid, ColorFilter.colorMatrix(grayMatrix()))
                                "invert" -> drawSamplePhoto(photoTop, photoBottom, photoGrid, ColorFilter.colorMatrix(invertMatrix()))
                                "contrast" -> drawSamplePhoto(photoTop, photoBottom, photoGrid, ColorFilter.colorMatrix(contrastMatrix(1.45f)))
                                "duotone" -> {
                                    drawSamplePhoto(photoTop, photoBottom, photoGrid, ColorFilter.colorMatrix(grayMatrix()))
                                    drawRect(color = duoShadow, blendMode = BlendMode.Modulate)
                                    drawRect(color = duoHighlight, blendMode = BlendMode.Lighten, alpha = 0.55f)
                                }
                                "mask" -> clipPath(starPath(size.width, size.height)) {
                                    drawSamplePhoto(photoTop, photoBottom, photoGrid)
                                }
                                "rotate_flip" -> rotate(12f) {
                                    scale(-1f, 1f) { drawSamplePhoto(photoTop, photoBottom, photoGrid) }
                                }
                                "blur" -> {
                                    // 3×3 共 9 趟低透明度偏移叠加：真模糊要 RenderEffect（API 31+），本仓库 minSdk 26
                                    for (dx in -1..1) {
                                        for (dy in -1..1) {
                                            translate(dx * 3.5f, dy * 3.5f) {
                                                drawSamplePhoto(photoTop, photoBottom, photoGrid, alpha = 1f / 9f)
                                            }
                                        }
                                    }
                                }
                                else -> drawSamplePhoto(photoTop, photoBottom, photoGrid)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            context.getString(FX_LABEL.getValue(kind)),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (on) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Text(
            context.getString(FX_NOTE.getValue(selected)),
            Modifier.fillMaxWidth().clip(chipShape).background(cellBg).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.pv_oss_fx_pitfall),
            Modifier
                .fillMaxWidth()
                .clip(chipShape)
                .background(scheme.errorContainer)
                .padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onErrorContainer,
        )
    }
}

// ==================== S16 可缩放看图与占位 ====================

private val ZOOM_PHASES = listOf("placeholder", "loading", "success", "error")

private val ZOOM_PHASE_LABEL = mapOf(
    "placeholder" to R.string.pv_oss_zoom_phase_placeholder,
    "loading" to R.string.pv_oss_zoom_phase_loading,
    "success" to R.string.pv_oss_zoom_phase_success,
    "error" to R.string.pv_oss_zoom_phase_error,
)

private const val ZOOM_MIN = 1f
private const val ZOOM_MAX = 4f
private const val ZOOM_DOUBLE_TAP = 2.5f

@Composable
fun PreviewS16() {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current

    var phase by rememberSaveable { mutableStateOf("success") }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var loadProgress by remember { mutableFloatStateOf(0f) }

    val photoTop = scheme.primary
    val photoBottom = scheme.tertiary
    val photoGrid = Color.White.copy(alpha = 0.35f)
    val errorTint = scheme.error
    val barColor = scheme.primary
    val barTrack = scheme.surfaceVariant
    val chipShape = RoundedCornerShape(percent = 50)

    // 加载中：1.2 秒走完后自动落到成功态，模拟真实图片加载
    LaunchedEffect(phase) {
        if (phase == "loading") {
            loadProgress = 0f
            var t = 0f
            while (t < 1f) {
                t = (t + 0.04f).coerceAtMost(1f)
                loadProgress = t
                kotlinx.coroutines.delay(48L)
            }
            phase = "success"
        }
    }

    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ZOOM_PHASES.forEach { key ->
                val on = key == phase
                Text(
                    context.getString(ZOOM_PHASE_LABEL.getValue(key)),
                    Modifier
                        .clip(chipShape)
                        .background(if (on) scheme.primary else scheme.surfaceVariant)
                        .clickable {
                            phase = key
                            scale = 1f
                            offset = Offset.Zero
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (on) scheme.onPrimary else scheme.onSurfaceVariant,
                )
            }
        }

        BoxWithConstraints(
            Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(14.dp)).background(barTrack)
        ) {
            val wPx = with(density) { maxWidth.toPx() }
            val hPx = with(density) { maxHeight.toPx() }
            val centerX = wPx / 2f
            val centerY = hPx / 2f
            val maxX = (scale - 1f) * wPx / 2f
            val maxY = (scale - 1f) * hPx / 2f
            val touchEdge = maxX > 0f && (abs(offset.x) >= maxX - 0.5f || abs(offset.y) >= maxY - 0.5f)

            Canvas(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    // 缩放手势与双击手势必须挂在两个独立的 pointerInput 上，写在一个里会互相吃事件
                    .pointerInput(phase, wPx, hPx) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            if (phase != "success") return@detectTransformGestures
                            val target = (scale * zoom).coerceIn(ZOOM_MIN, ZOOM_MAX)
                            val z = target / scale
                            // 让质心下的那个点缩放后仍停在质心：o2 = (c-center)(1-z) + o1·z + pan
                            val nx = (centroid.x - centerX) * (1f - z) + offset.x * z + pan.x
                            val ny = (centroid.y - centerY) * (1f - z) + offset.y * z + pan.y
                            val mx = (target - 1f) * wPx / 2f
                            val my = (target - 1f) * hPx / 2f
                            scale = target
                            offset = Offset(nx.coerceIn(-mx, mx), ny.coerceIn(-my, my))
                        }
                    }
                    .pointerInput(phase, wPx, hPx) {
                        detectTapGestures(
                            onDoubleTap = { pos ->
                                if (phase != "success") return@detectTapGestures
                                if (scale > ZOOM_MIN + 0.01f) {
                                    scale = ZOOM_MIN
                                    offset = Offset.Zero
                                } else {
                                    val z = ZOOM_DOUBLE_TAP
                                    val nx = (pos.x - centerX) * (1f - z)
                                    val ny = (pos.y - centerY) * (1f - z)
                                    val mx = (z - 1f) * wPx / 2f
                                    val my = (z - 1f) * hPx / 2f
                                    scale = z
                                    offset = Offset(nx.coerceIn(-mx, mx), ny.coerceIn(-my, my))
                                }
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        )
                    }
            ) {
                when (phase) {
                    // 占位：低清版先顶上（telephoto 的 BlurHash 占位思路，零依赖用多趟叠加近似）
                    "placeholder" -> {
                        for (dx in -1..1) {
                            for (dy in -1..1) {
                                translate(dx * 6f, dy * 6f) {
                                    drawSamplePhoto(photoTop, photoBottom, photoGrid, alpha = 1f / 9f)
                                }
                            }
                        }
                    }
                    "loading" -> {
                        for (dx in -1..1) {
                            for (dy in -1..1) {
                                translate(dx * 6f, dy * 6f) {
                                    drawSamplePhoto(photoTop, photoBottom, photoGrid, alpha = 1f / 9f)
                                }
                            }
                        }
                        drawRect(barTrack, Offset(0f, size.height - 10f), Size(size.width, 10f))
                        drawRect(barColor, Offset(0f, size.height - 10f), Size(size.width * loadProgress, 10f))
                    }
                    "error" -> {
                        drawSamplePhoto(photoTop, photoBottom, photoGrid, alpha = 0.25f)
                        drawRect(errorTint, alpha = 0.18f)
                    }
                    else -> drawSamplePhoto(photoTop, photoBottom, photoGrid)
                }
            }
        }

        // 分支先上提成局部 val：内联 if/else 当实参会被 i18n 探针误判成假属性
        val edgeText = if (touchEdge) {
            stringResource(R.string.pv_oss_zoom_edge_yes)
        } else {
            stringResource(R.string.pv_oss_zoom_edge_no)
        }
        Text(
            stringResource(
                R.string.pv_oss_zoom_readout,
                String.format(java.util.Locale.US, "%.2f", scale),
                String.format(java.util.Locale.US, "%.0f", offset.x),
                String.format(java.util.Locale.US, "%.0f", offset.y),
                edgeText,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.pv_oss_zoom_reset),
                Modifier
                    .clip(chipShape)
                    .background(scheme.surfaceVariant)
                    .clickable {
                        scale = ZOOM_MIN
                        offset = Offset.Zero
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.pv_oss_zoom_reload),
                Modifier
                    .clip(chipShape)
                    .background(scheme.surfaceVariant)
                    .clickable {
                        phase = "loading"
                        scale = ZOOM_MIN
                        offset = Offset.Zero
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
        }
        Text(
            stringResource(
                R.string.pv_oss_zoom_note,
                String.format(java.util.Locale.US, "%.0f", ZOOM_MIN),
                String.format(java.util.Locale.US, "%.0f", ZOOM_MAX),
                String.format(java.util.Locale.US, "%.1f", ZOOM_DOUBLE_TAP),
            ),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.pv_oss_zoom_pitfall),
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(scheme.errorContainer).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onErrorContainer,
        )
        Spacer(Modifier.width(0.dp))
    }
}
