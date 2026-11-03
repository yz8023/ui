package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.monkeycode.liquidui.R
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 批 E 开源取材 S09–S12：级联菜单 / 展开式列表→详情 / 图片裁剪框 / 富文本编辑器。
 *
 * 四项都是**按上游公开交互规范独立实现**，零新增依赖：
 * S09 saket/cascade（Apache-2.0）、S10 saket/InboxRecyclerView（Apache-2.0）、
 * S11 CanHub/Android-Image-Cropper（Apache-2.0）、S12 MohamedRejeb/compose-rich-editor（Apache-2.0）。
 * 清单与逐项参数见 docs/开源素材清单-批E.md，来源申明见「关于 → 开源致谢」。
 *
 * 本文件的组合上下文纪律：颜色、文案、尺寸全部在组合体里上提成 val，
 * Canvas / graphicsLayer / pointerInput / remember / LaunchedEffect 内只用 val。
 */

// ==================== S09 级联菜单 ====================

/** 菜单数据全部存稳定英文键，文案查资源：`when`/比较操作数不许用 stringResource。 */
private val CASCADE_ROOT = listOf("file", "edit", "view")

private val CASCADE_CHILD = mapOf(
    "file" to listOf("new_doc", "open_recent", "export"),
    "edit" to listOf("find_replace", "format_cell", "sort"),
    "view" to listOf("zoom", "theme"),
)

private val CASCADE_LEAF = mapOf(
    "export" to listOf("to_pdf", "to_csv", "to_png"),
    "theme" to listOf("light", "dark", "follow_system"),
    "zoom" to listOf("zoom_in", "zoom_out", "zoom_fit", "too_deep"),
)

/** 第 3 级还有子项 = 层级过深，规范要求改设计而不是继续加级。 */
private const val CASCADE_MAX_DEPTH = 3

private val CASCADE_LABEL = mapOf(
    "file" to R.string.pv_oss_cascade_file,
    "edit" to R.string.pv_oss_cascade_edit,
    "view" to R.string.pv_oss_cascade_view,
    "new_doc" to R.string.pv_oss_cascade_new_doc,
    "open_recent" to R.string.pv_oss_cascade_open_recent,
    "export" to R.string.pv_oss_cascade_export,
    "find_replace" to R.string.pv_oss_cascade_find_replace,
    "format_cell" to R.string.pv_oss_cascade_format_cell,
    "sort" to R.string.pv_oss_cascade_sort,
    "zoom" to R.string.pv_oss_cascade_zoom,
    "theme" to R.string.pv_oss_cascade_theme,
    "to_pdf" to R.string.pv_oss_cascade_to_pdf,
    "to_csv" to R.string.pv_oss_cascade_to_csv,
    "to_png" to R.string.pv_oss_cascade_to_png,
    "light" to R.string.pv_oss_cascade_light,
    "dark" to R.string.pv_oss_cascade_dark,
    "follow_system" to R.string.pv_oss_cascade_follow_system,
    "zoom_in" to R.string.pv_oss_cascade_zoom_in,
    "zoom_out" to R.string.pv_oss_cascade_zoom_out,
    "zoom_fit" to R.string.pv_oss_cascade_zoom_fit,
    "too_deep" to R.string.pv_oss_cascade_too_deep,
)

@Composable
fun PreviewS09() {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val labelOf = { key: String -> context.getString(CASCADE_LABEL.getValue(key)) }

    var open by rememberSaveable { mutableStateOf(false) }
    var path by remember { mutableStateOf(listOf<String>()) }
    var forward by remember { mutableStateOf(true) }
    var picked by remember { mutableStateOf(listOf<String>()) }
    var tooDeep by remember { mutableStateOf(false) }

    val menuBg = scheme.surfaceVariant
    val menuContent = scheme.onSurfaceVariant
    val arrowColor = scheme.onSurfaceVariant
    val crumbColor = scheme.primary
    val itemShape = RoundedCornerShape(8.dp)

    // 返回键：有子级先退级，顶级才关闭菜单
    BackHandler(enabled = open) {
        if (path.size > 1) {
            forward = false
            path = path.dropLast(1)
            tooDeep = false
        } else {
            open = false
            path = emptyList()
        }
    }

    val itemsAt: (List<String>) -> List<String> = { current ->
        when (current.size) {
            0 -> CASCADE_ROOT
            1 -> CASCADE_CHILD[current[0]] ?: emptyList()
            2 -> CASCADE_LEAF[current[1]] ?: emptyList()
            else -> emptyList()
        }
    }

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.clickable(onClick = {
                    open = !open
                    if (!open) path = emptyList()
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }),
                shape = RoundedCornerShape(percent = 50),
                color = if (open) scheme.primary else scheme.surfaceVariant,
                contentColor = if (open) scheme.onPrimary else scheme.onSurfaceVariant,
            ) {
                Text(
                    stringResource(R.string.pv_oss_cascade_anchor),
                    Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                if (picked.isEmpty()) stringResource(R.string.pv_oss_cascade_none)
                else picked.joinToString("  ›  ") { labelOf(it) },
                style = MaterialTheme.typography.bodySmall,
                color = if (picked.isEmpty()) scheme.onSurfaceVariant else scheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.pv_oss_cascade_rule, CASCADE_MAX_DEPTH),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )

        // 外层必须是 Column：AnimatedVisibility 在这里会解析成 ColumnScope 的扩展，
        // 套在 Box 里就成了「隐式接收者不可用」（CI 实测报的错），改 Column 后合法且自带纵向展开动画
        Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
            AnimatedVisibility(visible = open) {
                Column {
                    // 面包屑：点任意一级跳回
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 6.dp).horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.pv_oss_cascade_root),
                            Modifier.clip(itemShape).clickable {
                                forward = false; path = emptyList(); tooDeep = false
                            }.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (path.isEmpty()) scheme.onSurfaceVariant else crumbColor,
                        )
                        path.forEachIndexed { i, key ->
                            Text("›", style = MaterialTheme.typography.labelSmall, color = arrowColor)
                            Text(
                                labelOf(key),
                                Modifier.clip(itemShape).clickable {
                                    forward = false; path = path.take(i + 1); tooDeep = false
                                }.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (i == path.lastIndex) scheme.onSurfaceVariant else crumbColor,
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = path,
                        transitionSpec = {
                            val dir = if (forward) 1 else -1
                            (slideInHorizontally(tween(200)) { w -> dir * w } + fadeIn(tween(200))) togetherWith
                                (slideOutHorizontally(tween(200)) { w -> -dir * w } + fadeOut(tween(200)))
                        },
                        label = "cascade",
                    ) { current ->
                        Column(
                            Modifier
                                .width(220.dp)
                                .shadow(8.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(menuBg)
                                .padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            itemsAt(current).forEach { key ->
                                val hasChild = when (current.size) {
                                    0 -> CASCADE_CHILD.containsKey(key)
                                    1 -> CASCADE_LEAF.containsKey(key)
                                    else -> false
                                }
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(itemShape)
                                        .clickable {
                                            if (hasChild && current.size + 1 < CASCADE_MAX_DEPTH) {
                                                forward = true
                                                path = current + key
                                                tooDeep = false
                                            } else if (hasChild) {
                                                // 第 3 级还有子项：规范要求改设计，不继续加级
                                                tooDeep = true
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            } else {
                                                picked = current + key
                                                open = false
                                                path = emptyList()
                                                tooDeep = false
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        labelOf(key),
                                        Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = menuContent,
                                    )
                                    if (hasChild) Text("›", color = arrowColor, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }

                    if (tooDeep) {
                        Text(
                            stringResource(R.string.pv_oss_cascade_too_deep_hint, CASCADE_MAX_DEPTH),
                            Modifier
                                .padding(top = 8.dp)
                                .clip(itemShape)
                                .background(scheme.errorContainer)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onErrorContainer,
                        )
                    }
                }
            }
        }

        // 点外部关闭：菜单开着时铺一层透明可点区
        if (open) {
            Box(
                Modifier.fillMaxWidth().height(1.dp).clickable { open = false; path = emptyList() }
            )
        }
    }
}

// ==================== S10 展开式列表→详情 ====================

private val EXPAND_KEYS = listOf("m1", "m2", "m3", "m4", "m5")

private val EXPAND_TITLE = mapOf(
    "m1" to R.string.pv_oss_expand_title_1,
    "m2" to R.string.pv_oss_expand_title_2,
    "m3" to R.string.pv_oss_expand_title_3,
    "m4" to R.string.pv_oss_expand_title_4,
    "m5" to R.string.pv_oss_expand_title_5,
)

private val EXPAND_BODY = mapOf(
    "m1" to R.string.pv_oss_expand_body_1,
    "m2" to R.string.pv_oss_expand_body_2,
    "m3" to R.string.pv_oss_expand_body_3,
    "m4" to R.string.pv_oss_expand_body_4,
    "m5" to R.string.pv_oss_expand_body_5,
)

private const val EXPAND_DURATION = 320
private const val EXPAND_MIN_ALPHA = 0.2f

@Composable
fun PreviewS10() {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val progress = remember { Animatable(0f) }

    var expandedIndex by remember { mutableIntStateOf(-1) }
    var savedIndex by remember { mutableIntStateOf(0) }
    var savedOffset by remember { mutableIntStateOf(0) }

    val cardColor = scheme.surfaceVariant
    val cardContent = scheme.onSurfaceVariant
    val detailBg = scheme.surface
    val rowH = 72.dp
    val p = progress.value

    // 展开 / 收起：收起动画结束后再还原滚动位置，否则会被动画的 layout pass 打断
    LaunchedEffect(expandedIndex) {
        if (expandedIndex >= 0) {
            progress.animateTo(1f, tween(EXPAND_DURATION, easing = FastOutSlowInEasing))
            listState.animateScrollToItem(expandedIndex + 1)
        } else {
            progress.animateTo(0f, tween(EXPAND_DURATION, easing = FastOutSlowInEasing))
            listState.scrollToItem(savedIndex, savedOffset)
        }
    }

    BackHandler(enabled = expandedIndex >= 0) { expandedIndex = -1 }

    BoxWithConstraints(Modifier.fillMaxWidth().height(360.dp)) {
        val fullH = maxHeight
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.pv_oss_expand_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = cardContent,
                )
            }
            EXPAND_KEYS.forEachIndexed { i, key ->
                item(key = key) {
                    val isTarget = i == expandedIndex
                    val expanding = expandedIndex >= 0
                    // 动画的是**布局高度**，不是 graphicsLayer 的缩放：
                    // 缩放会把详情里的文字一起拉长变形（InboxRecyclerView 的做法是改视图边界）。
                    val thisH = if (isTarget) rowH + (fullH - rowH) * p else rowH
                    val corner = if (isTarget) 14.dp * (1f - p) else 14.dp
                    val rowAlpha = if (!isTarget && expanding) 1f - p * (1f - EXPAND_MIN_ALPHA) else 1f

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(thisH)
                            .alpha(rowAlpha)
                            .clip(RoundedCornerShape(corner))
                            .background(if (isTarget && p > 0.5f) detailBg else cardColor)
                            .clickable {
                                if (expandedIndex >= 0) {
                                    expandedIndex = -1
                                } else {
                                    savedIndex = listState.firstVisibleItemIndex
                                    savedOffset = listState.firstVisibleItemScrollOffset
                                    expandedIndex = i
                                }
                            }
                            .padding(14.dp),
                    ) {
                        if (isTarget && p > 0.5f) {
                            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                                Text(
                                    context.getString(EXPAND_TITLE.getValue(key)),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = scheme.onSurface,
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    context.getString(EXPAND_BODY.getValue(key)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    stringResource(
                                        R.string.pv_oss_expand_param,
                                        String.format(Locale.US, "%.2f", p),
                                        String.format(Locale.US, "%.0f", thisH.value),
                                        String.format(Locale.US, "%.2f", rowAlpha),
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.primary,
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.pv_oss_expand_back),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                                Text(
                                    context.getString(EXPAND_TITLE.getValue(key)),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = scheme.onSurface,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    stringResource(R.string.pv_oss_expand_row_meta, i + 1),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cardContent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== S11 图片裁剪框 ====================

private val CROP_RATIOS = listOf("free", "r_1_1", "r_4_3", "r_16_9")

private val CROP_RATIO_LABEL = mapOf(
    "free" to R.string.pv_oss_crop_free,
    "r_1_1" to R.string.pv_oss_crop_1_1,
    "r_4_3" to R.string.pv_oss_crop_4_3,
    "r_16_9" to R.string.pv_oss_crop_16_9,
)

private val CROP_RATIO_VALUE = mapOf("free" to 0f, "r_1_1" to 1f, "r_4_3" to 4f / 3f, "r_16_9" to 16f / 9f)

private const val CROP_MIN_DP = 48f
private const val CROP_HANDLE_DP = 24f

/** 归一化裁剪矩形（0..1），与容器尺寸无关：旋转、换比例、改宽度都不用重算。 */
private data class CropRect(val l: Float, val t: Float, val r: Float, val b: Float) {
    val w: Float get() = r - l
    val h: Float get() = b - t
}

@Composable
fun PreviewS11() {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current

    var ratioKey by rememberSaveable { mutableStateOf("free") }
    var rotated by rememberSaveable { mutableIntStateOf(0) }
    var crop by remember { mutableStateOf(CropRect(0.10f, 0.14f, 0.90f, 0.86f)) }
    var boxW by remember { mutableFloatStateOf(1f) }
    var boxH by remember { mutableFloatStateOf(1f) }
    var dragMode by remember { mutableIntStateOf(-1) }
    var dragStart by remember { mutableStateOf(CropRect(0.10f, 0.14f, 0.90f, 0.86f)) }
    var dragOrigin by remember { mutableStateOf(Offset.Zero) }

    val minPx = with(density) { CROP_MIN_DP.dp.toPx() }
    val handlePx = with(density) { CROP_HANDLE_DP.dp.toPx() }
    val photoTop = scheme.primary
    val photoBottom = scheme.tertiary
    val gridColor = Color.White.copy(alpha = 0.30f)
    val borderColor = Color.White
    val maskColor = Color.Black.copy(alpha = 0.45f)
    val chipShape = RoundedCornerShape(percent = 50)

    val ratio = CROP_RATIO_VALUE.getValue(ratioKey)
    val landscape = rotated % 2 == 0

    BoxWithConstraints(Modifier.fillMaxWidth().padding(16.dp)) {
        Column {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CROP_RATIOS.forEach { key ->
                    val selected = key == ratioKey
                    Text(
                        context.getString(CROP_RATIO_LABEL.getValue(key)),
                        Modifier
                            .clip(chipShape)
                            .background(if (selected) scheme.primary else scheme.surfaceVariant)
                            .clickable {
                                ratioKey = key
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                    )
                }
                Text(
                    stringResource(R.string.pv_oss_crop_rotate),
                    Modifier
                        .clip(chipShape)
                        .background(scheme.surfaceVariant)
                        .clickable {
                            rotated = (rotated + 1) % 4
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(12.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (landscape) 1.5f else 0.667f)
                    .clip(RoundedCornerShape(12.dp))
                    .onSizeChanged {
                        boxW = it.width.toFloat()
                        boxH = it.height.toFloat()
                    }
                    .pointerInput(ratioKey, boxW, boxH) {
                        detectDragGestures(
                            onDragStart = { down ->
                                dragMode = cropHitTest(down, crop, boxW, boxH, handlePx)
                                dragStart = crop
                                dragOrigin = down
                            },
                            onDragEnd = { dragMode = -1 },
                            onDragCancel = { dragMode = -1 },
                        ) { change, _ ->
                            change.consume()
                            if (dragMode < 0 || boxW <= 0f || boxH <= 0f) return@detectDragGestures
                            // dragAmount 是**相邻两次的增量**，累加会漂；用当前位置减按下位置算总位移
                            val total = change.position - dragOrigin
                            crop = applyCropDrag(dragStart, dragMode, total.x, total.y, ratio, boxW, boxH, minPx)
                        }
                    }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    // 底图现场画：渐变 + 斜纹 + 同心圆，不引图片资源、不联网
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(photoTop, photoBottom),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height),
                        )
                    )
                    val step = size.width / 12f
                    var gx = -size.height
                    while (gx < size.width) {
                        drawLine(
                            color = gridColor,
                            start = Offset(gx, 0f),
                            end = Offset(gx + size.height, size.height),
                            strokeWidth = 2f,
                        )
                        gx += step
                    }
                    val cx = size.width * 0.68f
                    val cy = size.height * 0.38f
                    for (k in 1..4) {
                        drawCircle(
                            color = gridColor,
                            radius = size.width * 0.06f * k,
                            center = Offset(cx, cy),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
                        )
                    }

                    val cl = crop.l * size.width
                    val ct = crop.t * size.height
                    val cr = crop.r * size.width
                    val cb = crop.b * size.height
                    // 遮罩用四条矩形拼，不用 BlendMode.Clear（那个必须配 Offscreen 合成策略才正确）
                    drawRect(maskColor, Offset.Zero, androidx.compose.ui.geometry.Size(size.width, ct))
                    drawRect(maskColor, Offset(0f, cb), androidx.compose.ui.geometry.Size(size.width, size.height - cb))
                    drawRect(maskColor, Offset(0f, ct), androidx.compose.ui.geometry.Size(cl, cb - ct))
                    drawRect(maskColor, Offset(cr, ct), androidx.compose.ui.geometry.Size(size.width - cr, cb - ct))

                    // 三分网格线
                    val w = cr - cl
                    val h = cb - ct
                    for (k in 1..2) {
                        drawLine(gridColor, Offset(cl + w * k / 3f, ct), Offset(cl + w * k / 3f, cb), 1f)
                        drawLine(gridColor, Offset(cl, ct + h * k / 3f), Offset(cr, ct + h * k / 3f), 1f)
                    }
                    drawRect(
                        color = borderColor,
                        topLeft = Offset(cl, ct),
                        size = androidx.compose.ui.geometry.Size(w, h),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
                    )
                    // 四角手柄
                    val arm = 18f
                    listOf(
                        Offset(cl, ct) to Offset(1f, 1f),
                        Offset(cr, ct) to Offset(-1f, 1f),
                        Offset(cr, cb) to Offset(-1f, -1f),
                        Offset(cl, cb) to Offset(1f, -1f),
                    ).forEach { pair ->
                        val c = pair.first
                        val d = pair.second
                        drawLine(borderColor, c, Offset(c.x + arm * d.x, c.y), 4f)
                        drawLine(borderColor, c, Offset(c.x, c.y + arm * d.y), 4f)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            val wDp = with(density) { (crop.w * boxW).toDp().value.roundToInt() }
            val hDp = with(density) { (crop.h * boxH).toDp().value.roundToInt() }
            val outW = (crop.w * 1080f).roundToInt()
            val outH = if (crop.w > 0f && boxW > 0f) {
                (crop.h * boxH / (crop.w * boxW) * outW).roundToInt()
            } else {
                0
            }
            Text(
                stringResource(R.string.pv_oss_crop_readout, wDp, hDp, outW, outH),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.pv_oss_crop_note),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

/** 命中判定：0–3 四角、4–7 四边、8 框内平移、-1 没命中。 */
private fun cropHitTest(down: Offset, crop: CropRect, boxW: Float, boxH: Float, tol: Float): Int {
    if (boxW <= 0f || boxH <= 0f) return -1
    val x = down.x
    val y = down.y
    val l = crop.l * boxW
    val t = crop.t * boxH
    val r = crop.r * boxW
    val b = crop.b * boxH
    val nearL = abs(x - l) <= tol
    val nearR = abs(x - r) <= tol
    val nearT = abs(y - t) <= tol
    val nearB = abs(y - b) <= tol
    val inX = x >= l - tol && x <= r + tol
    val inY = y >= t - tol && y <= b + tol
    return when {
        nearL && nearT -> 0
        nearR && nearT -> 1
        nearR && nearB -> 2
        nearL && nearB -> 3
        nearT && inX -> 4
        nearR && inY -> 5
        nearB && inX -> 6
        nearL && inY -> 7
        x > l && x < r && y > t && y < b -> 8
        else -> -1
    }
}

/**
 * 全程在**像素空间**算，最后再归一化：比例锁定若在归一化空间做，会被容器宽高比带偏。
 * 锚点 = 没被拖动的那条对角边；先夹「不出界」，再夹「不小于最小尺寸」，顺序反了会抖。
 */
private fun applyCropDrag(
    start: CropRect,
    mode: Int,
    dxPx: Float,
    dyPx: Float,
    ratio: Float,
    boxW: Float,
    boxH: Float,
    minPx: Float,
): CropRect {
    if (boxW <= 0f || boxH <= 0f) return start
    var l = start.l * boxW
    var t = start.t * boxH
    var r = start.r * boxW
    var b = start.b * boxH
    when (mode) {
        0 -> { l += dxPx; t += dyPx }
        1 -> { r += dxPx; t += dyPx }
        2 -> { r += dxPx; b += dyPx }
        3 -> { l += dxPx; b += dyPx }
        4 -> t += dyPx
        5 -> r += dxPx
        6 -> b += dyPx
        7 -> l += dxPx
        8 -> {
            val w = r - l
            val h = b - t
            l = (l + dxPx).coerceIn(0f, boxW - w)
            t = (t + dyPx).coerceIn(0f, boxH - h)
            return CropRect(l / boxW, t / boxH, (l + w) / boxW, (t + h) / boxH)
        }
    }
    l = l.coerceIn(0f, boxW)
    t = t.coerceIn(0f, boxH)
    r = r.coerceIn(0f, boxW)
    b = b.coerceIn(0f, boxH)
    if (r - l < minPx) r = l + minPx
    if (b - t < minPx) b = t + minPx
    if (r > boxW) { l = (boxW - (r - l)).coerceAtLeast(0f); r = boxW }
    if (b > boxH) { t = (boxH - (b - t)).coerceAtLeast(0f); b = boxH }

    if (ratio > 0f) {
        val anchorRight = mode == 0 || mode == 3 || mode == 7
        val anchorBottom = mode == 0 || mode == 1 || mode == 4
        var w = r - l
        var h = w / ratio
        val maxH = (if (anchorBottom) b else boxH - t)
        if (h > maxH) { h = maxH; w = h * ratio }
        val maxW = (if (anchorRight) r else boxW - l)
        if (w > maxW) { w = maxW; h = w / ratio }
        if (w < minPx) { w = minPx; h = w / ratio }
        if (h < minPx) { h = minPx; w = h * ratio }
        val nl = if (anchorRight) r - w else l
        val nt = if (anchorBottom) b - h else t
        l = nl.coerceIn(0f, boxW - w)
        t = nt.coerceIn(0f, boxH - h)
        r = l + w
        b = t + h
    }
    return CropRect(l / boxW, t / boxH, r / boxW, b / boxH)
}

// ==================== S12 富文本编辑器 ====================

/** 样式区间：kind 是稳定英文键，用来判断「已加过没有」。 */
private data class RichSpan(val kind: String, val start: Int, val end: Int, val style: SpanStyle)

private val RICH_ACTIONS = listOf("bold", "italic", "underline", "strike", "link", "bullet", "number", "copy")

private val RICH_ACTION_LABEL = mapOf(
    "bold" to R.string.pv_oss_rich_bold,
    "italic" to R.string.pv_oss_rich_italic,
    "underline" to R.string.pv_oss_rich_underline,
    "strike" to R.string.pv_oss_rich_strike,
    "link" to R.string.pv_oss_rich_link,
    "bullet" to R.string.pv_oss_rich_bullet,
    "number" to R.string.pv_oss_rich_number,
    "copy" to R.string.pv_oss_rich_copy,
)

@Composable
fun PreviewS12() {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current

    val seed = stringResource(R.string.pv_oss_rich_seed)
    var text by remember { mutableStateOf(seed) }
    var selection by remember { mutableStateOf(TextRange.Zero) }
    val spans = remember { mutableStateListOf<RichSpan>() }
    var copied by remember { mutableStateOf(false) }

    val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)
    val italicStyle = SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
    val underlineStyle = SpanStyle(textDecoration = TextDecoration.Underline)
    val strikeStyle = SpanStyle(textDecoration = TextDecoration.LineThrough)
    val linkStyle = SpanStyle(color = scheme.primary, textDecoration = TextDecoration.Underline)
    val fieldBg = scheme.surfaceVariant
    val fieldContent = scheme.onSurface
    val barColor = scheme.onSurfaceVariant
    val activeColor = scheme.primary
    val chipShape = RoundedCornerShape(8.dp)

    val styleOf = { kind: String ->
        when (kind) {
            "bold" -> boldStyle
            "italic" -> italicStyle
            "underline" -> underlineStyle
            "strike" -> strikeStyle
            else -> linkStyle
        }
    }

    // 选区为空时作用于当前词，这是编辑器的通行做法
    val effectiveRange: () -> IntRange = {
        val s = selection.min
        val e = selection.max
        if (e > s) {
            s until e
        } else {
            var a = s
            var b = s
            while (a > 0 && !text[a - 1].isWhitespace()) a--
            while (b < text.length && !text[b].isWhitespace()) b++
            a until b
        }
    }

    val rendered = buildAnnotatedString {
        append(text)
        spans.forEach { sp ->
            val st = sp.start.coerceIn(0, text.length)
            val en = sp.end.coerceIn(st, text.length)
            if (en > st) addStyle(sp.style, st, en)
        }
    }

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            stringResource(R.string.pv_oss_rich_note),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            RICH_ACTIONS.forEach { kind ->
                val active = kind != "bullet" && kind != "number" && kind != "copy" &&
                    spans.any { it.kind == kind && it.start <= selection.min && it.end >= selection.max && selection.max > selection.min }
                Text(
                    context.getString(RICH_ACTION_LABEL.getValue(kind)),
                    Modifier
                        .clip(chipShape)
                        .background(if (active) activeColor else fieldBg)
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            when (kind) {
                                "bullet", "number" -> {
                                    val range = effectiveRange()
                                    val lines = text.split("\n")
                                    var acc = 0
                                    var n = 0
                                    val rebuilt = lines.joinToString("\n") { line ->
                                        val lineStart = acc
                                        acc += line.length + 1
                                        val lineEnd = acc - 1
                                        val hit = lineEnd > range.first && lineStart <= range.last
                                        if (!hit) {
                                            line
                                        } else {
                                            n++
                                            if (kind == "bullet") "•  $line" else "$n.  $line"
                                        }
                                    }
                                    text = rebuilt
                                    selection = TextRange(rebuilt.length.coerceAtMost(selection.min))
                                }
                                "copy" -> {
                                    clipboard.setText(AnnotatedString(text))
                                    copied = true
                                }
                                else -> {
                                    val range = effectiveRange()
                                    if (range.last >= range.first) {
                                        val had = spans.any { it.kind == kind && it.start <= range.first && it.end >= range.last + 1 }
                                        val kept = spans.filterNot {
                                            it.kind == kind && it.start < range.last + 1 && it.end > range.first
                                        }
                                        spans.clear()
                                        spans.addAll(kept)
                                        if (!had) {
                                            spans.add(RichSpan(kind, range.first, range.last + 1, styleOf(kind)))
                                        }
                                    }
                                }
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) scheme.onPrimary else barColor,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        BasicTextField(
            value = TextFieldValue(text = text, selection = selection),
            onValueChange = { v ->
                text = v.text
                selection = TextRange(
                    v.selection.min.coerceIn(0, v.text.length),
                    v.selection.max.coerceIn(0, v.text.length),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(fieldBg)
                .padding(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = fieldContent),
        )

        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.pv_oss_rich_preview_label),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Surface(
            Modifier.fillMaxWidth().border(1.dp, scheme.outlineVariant, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = scheme.surface,
        ) {
            Text(
                rendered,
                Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurface,
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            if (copied) stringResource(R.string.pv_oss_rich_copied, text.length)
            else stringResource(R.string.pv_oss_rich_stat, text.length, spans.size),
            style = MaterialTheme.typography.labelSmall,
            color = if (copied) scheme.primary else scheme.onSurfaceVariant,
        )
    }
}
