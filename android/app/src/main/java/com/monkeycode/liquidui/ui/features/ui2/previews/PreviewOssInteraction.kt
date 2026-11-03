package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.SectionTitle
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 批 D 开源取材·交互三条：S05 长按拖拽排序、S06 下拉刷新、S07 教练引导气泡。
 *
 * 来源申明：
 *  · S05 参考 Calvin-LL/Reorderable（Apache-2.0）的长按激活、按指针落点换位、落位动画与拖动抬升；
 *  · S06 参考 google/accompanist（Apache-2.0）swiperefresh 的阈值触发、指示器位移-旋转映射与回弹，
 *    该库上游已把能力让给 Material 3 的 PullToRefresh，这里按同一口径用 `nestedScroll` 自绘；
 *  · S07 参考 microsoft/fluentui-android（MIT）TeachingCallout 的箭头指向、多步序列与「不再显示」。
 * 三条都用已有依赖手写（零新增依赖），未复制任何上游源码；本仓库为 MIT。
 */

// ─── S05 长按拖拽排序 ───────────────────────────────

private val OssReorderSeed = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

@Composable
fun PreviewS05() {
    val haptics = LocalHapticFeedback.current
    val rows = remember { OssReorderSeed.toMutableStateList() }
    val listState = rememberLazyListState()
    var draggingKey by remember { mutableStateOf<Int?>(null) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    var moveCount by remember { mutableIntStateOf(0) }

    // 拖到视口上下边缘时自动滚动（长列表里必需，否则最后一项永远拖不上来）
    LaunchedEffect(draggingKey) {
        while (draggingKey != null) {
            val key = draggingKey
            if (key != null) {
                val info = listState.layoutInfo
                val item = info.visibleItemsInfo.firstOrNull { it.key == key }
                if (item != null) {
                    val top = item.offset + dragOffsetPx.roundToInt()
                    val bottom = top + item.size
                    val edge = (info.viewportSize.height * 0.12f).roundToInt()
                    if (top < edge) listState.scrollBy(-16f)
                    else if (bottom > info.viewportEndOffset - edge) listState.scrollBy(16f)
                }
            }
            withFrameNanos { }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_reorder_title))
            Text(
                stringResource(R.string.pv_oss_reorder_intro),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(rows, key = { it }) { key ->
                        val dragging = key == draggingKey
                        val index = rows.indexOf(key)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .graphicsLayer {
                                    translationY = if (dragging) dragOffsetPx else 0f
                                    scaleX = if (dragging) 1.03f else 1f
                                    scaleY = if (dragging) 1.03f else 1f
                                    shadowElevation = if (dragging) 12f else 0f
                                }
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (dragging) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                )
                                .pointerInput(rows.size) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggingKey = key
                                            dragOffsetPx = 0f
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDragEnd = {
                                            draggingKey = null
                                            dragOffsetPx = 0f
                                        },
                                        onDragCancel = {
                                            draggingKey = null
                                            dragOffsetPx = 0f
                                        },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            dragOffsetPx += amount.y
                                            val info = listState.layoutInfo
                                            val self = info.visibleItemsInfo.firstOrNull { it.key == key }
                                            if (self != null) {
                                                val center = self.offset + self.size / 2 +
                                                    dragOffsetPx.roundToInt()
                                                val hit = info.visibleItemsInfo.firstOrNull { other ->
                                                    other.key != key &&
                                                        center in other.offset..(other.offset + other.size)
                                                }
                                                val hitKey = hit?.key
                                                if (hitKey is Int) {
                                                    val from = rows.indexOf(key)
                                                    val to = rows.indexOf(hitKey)
                                                    if (from >= 0 && to >= 0 && from != to) {
                                                        rows.add(to, rows.removeAt(from))
                                                        // 换位后布局 offset 变了，补偿掉差值，指针下的视觉位置不跳
                                                        dragOffsetPx += (self.offset - hit.offset).toFloat()
                                                        moveCount += 1
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    (index + 1).toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                stringResource(R.string.pv_oss_reorder_item, key),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                stringResource(R.string.pv_oss_reorder_handle),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.pv_oss_reorder_moves, moveCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OssGhostButton(
                    labelRes = R.string.pv_oss_reorder_reset,
                    onClick = {
                        rows.clear()
                        rows.addAll(OssReorderSeed)
                        moveCount = 0
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.pv_oss_reorder_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OssGhostButton(labelRes: Int, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            stringResource(labelRes),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── S06 下拉刷新 ───────────────────────────────────

private val OssRefreshRows = listOf(1, 2, 3, 4, 5, 6, 7, 8)

@Composable
fun PreviewS06() {
    val density = LocalDensity.current
    val thresholdPx = with(density) { 72.dp.toPx() }
    val holdPx = with(density) { 48.dp.toPx() }
    var overscroll by remember { mutableFloatStateOf(0f) }
    var refreshing by remember { mutableStateOf(false) }
    var refreshCount by remember { mutableIntStateOf(0) }
    var lastCost by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(900)
            refreshing = false
            overscroll = 0f
            refreshCount += 1
            lastCost = 900
        }
    }

    val connection = remember(thresholdPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (refreshing) return Offset.Zero
                // 向上滑：先把已经拉出来的量收回去，再让列表滚
                if (available.y < 0f && overscroll > 0f) {
                    val consumed = max(available.y, -overscroll)
                    overscroll += consumed
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (refreshing) return Offset.Zero
                // 列表已到顶还有向下的余量 → 变成下拉位移
                if (available.y > 0f) {
                    overscroll = min(overscroll + available.y, thresholdPx * 1.6f)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (!refreshing && overscroll >= thresholdPx) {
                    refreshing = true
                    overscroll = 0f
                } else if (!refreshing) {
                    overscroll = 0f
                }
                return Velocity.Zero
            }
        }
    }

    val progress = (overscroll / thresholdPx).coerceIn(0f, 1.6f)
    val indicatorDp by animateFloatAsState(
        targetValue = if (refreshing) holdPx else overscroll,
        animationSpec = tween(durationMillis = if (refreshing) 160 else 0),
        label = "oss_refresh_indicator"
    )
    val spin = rememberInfiniteTransition(label = "oss_refresh_spin")
    val spinAngle by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Restart),
        label = "oss_refresh_angle"
    )
    val rotation = if (refreshing) spinAngle else progress * 300f

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_refresh_title))
            Text(
                stringResource(R.string.pv_oss_refresh_intro),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .nestedScroll(connection)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(OssRefreshRows) { n ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.pv_oss_refresh_item, n),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                // 指示器：位移 0→72dp 映射旋转 0→300°，达阈值变实色
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset { IntOffset(0, (indicatorDp - holdPx).roundToInt().coerceAtLeast(0)) }
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .graphicsLayer {
                            alpha = if (refreshing) 1f else progress.coerceIn(0f, 1f)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // MaterialTheme.colorScheme 是 @Composable getter，DrawScope 里取不到 → 上提成 val
                    val arcActive = MaterialTheme.colorScheme.primary
                    val arcIdle = MaterialTheme.colorScheme.outline
                    Canvas(Modifier.size(20.dp)) {
                        drawArc(
                            color = if (progress >= 1f || refreshing) arcActive else arcIdle,
                            startAngle = rotation - 90f,
                            sweepAngle = if (refreshing) 260f else 40f + progress * 220f,
                            useCenter = false,
                            style = Stroke(width = size.minDimension / 9f, cap = StrokeCap.Round)
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(
                    R.string.pv_oss_refresh_state,
                    if (refreshing) stringResource(R.string.pv_oss_refresh_state_busy)
                    else if (progress >= 1f) stringResource(R.string.pv_oss_refresh_state_ready)
                    else stringResource(R.string.pv_oss_refresh_state_idle),
                    "%.0f".format(progress * 100f)
                ),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.pv_oss_refresh_count, refreshCount, lastCost),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.pv_oss_refresh_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── S07 教练引导气泡 ───────────────────────────────

private class OssCoachStep(val titleRes: Int, val bodyRes: Int)

private val OssCoachSteps = listOf(
    OssCoachStep(R.string.pv_oss_coach_step1_title, R.string.pv_oss_coach_step1_body),
    OssCoachStep(R.string.pv_oss_coach_step2_title, R.string.pv_oss_coach_step2_body),
    OssCoachStep(R.string.pv_oss_coach_step3_title, R.string.pv_oss_coach_step3_body),
    OssCoachStep(R.string.pv_oss_coach_step4_title, R.string.pv_oss_coach_step4_body)
)

@Composable
fun PreviewS07() {
    val density = LocalDensity.current
    var step by rememberSaveable { mutableStateOf(0) }
    var dismissed by rememberSaveable { mutableStateOf(false) }
    var containerPos by remember { mutableStateOf(Offset.Zero) }
    var targetPos by remember { mutableStateOf(Offset.Zero) }
    var targetSize by remember { mutableStateOf(Size.Zero) }
    var measured by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_coach_title))
            Text(
                stringResource(R.string.pv_oss_coach_intro),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))
                    .onGloballyPositioned {
                        containerPos = it.positionInRoot()
                    }
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OssCoachTarget(
                        labelRes = R.string.pv_oss_coach_target1,
                        isCurrent = step == 0 && !dismissed,
                        capture = step == 0,
                        onMeasure = { p, s -> targetPos = p; targetSize = s; measured = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OssCoachTarget(
                        labelRes = R.string.pv_oss_coach_target2,
                        isCurrent = step == 1 && !dismissed,
                        capture = step == 1,
                        onMeasure = { p, s -> targetPos = p; targetSize = s; measured = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OssCoachTarget(
                            labelRes = R.string.pv_oss_coach_target3,
                            isCurrent = step == 2 && !dismissed,
                            capture = step == 2,
                            onMeasure = { p, s -> targetPos = p; targetSize = s; measured = true },
                            modifier = Modifier.weight(1f)
                        )
                        OssCoachTarget(
                            labelRes = R.string.pv_oss_coach_target4,
                            isCurrent = step == 3 && !dismissed,
                            capture = step == 3,
                            onMeasure = { p, s -> targetPos = p; targetSize = s; measured = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        stringResource(R.string.pv_oss_coach_page_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!dismissed && measured) {
                    val rel = Offset(targetPos.x - containerPos.x, targetPos.y - containerPos.y)
                    val leftDp = with(density) { rel.x.toDp() }
                    val topDp = with(density) { rel.y.toDp() }
                    val wDp = with(density) { targetSize.width.toDp() }
                    val hDp = with(density) { targetSize.height.toDp() }
                    // 遮罩挖孔：不用 BlendMode.Clear（要配 CompositingStrategy.Offscreen 才对），
                    // 直接用四块矩形围出孔洞，视觉一致且零 API 风险。
                    val holePad = 6.dp
                    val hl = leftDp - holePad
                    val ht = topDp - holePad
                    val hw = wDp + holePad * 2
                    val hh = hDp + holePad * 2
                    val scrim = Color(0x99000000)
                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.fillMaxWidth().height(ht.coerceAtLeast(0.dp)).background(scrim))
                        Row(Modifier.fillMaxWidth().height(hh.coerceAtLeast(0.dp))) {
                            Box(Modifier.width(hl.coerceAtLeast(0.dp)).fillMaxSize().background(scrim))
                            Box(Modifier.width(hw.coerceAtLeast(0.dp)).fillMaxSize())
                            Box(Modifier.weight(1f).fillMaxSize().background(scrim))
                        }
                        Box(Modifier.weight(1f).fillMaxWidth().background(scrim))
                    }
                    OssCoachBubble(
                        step = step,
                        anchorLeft = hl,
                        anchorTop = ht,
                        anchorWidth = hw,
                        anchorHeight = hh,
                        onPrev = { if (step > 0) { step -= 1; measured = false } },
                        onNext = {
                            if (step < OssCoachSteps.size - 1) {
                                step += 1
                                measured = false
                            } else {
                                dismissed = true
                            }
                        },
                        onSkip = { dismissed = true }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OssGhostButton(
                    labelRes = if (dismissed) R.string.pv_oss_coach_replay else R.string.pv_oss_coach_restart,
                    onClick = {
                        dismissed = false
                        step = 0
                        measured = false
                    }
                )
                Text(
                    stringResource(
                        R.string.pv_oss_coach_progress,
                        if (dismissed) OssCoachSteps.size else step + 1,
                        OssCoachSteps.size
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.pv_oss_coach_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OssCoachTarget(
    labelRes: Int,
    isCurrent: Boolean,
    capture: Boolean,
    onMeasure: (Offset, Size) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCurrent) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            )
            .then(
                if (capture) {
                    Modifier.onGloballyPositioned {
                        val root = it.positionInRoot()
                        onMeasure(root, Size(it.size.width.toFloat(), it.size.height.toFloat()))
                    }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 14.dp, vertical = 16.dp)
    ) {
        Text(
            stringResource(labelRes),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCurrent) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

/** 气泡：默认在目标下方，下方放不下就翻到上方；箭头跟着翻转。 */
@Composable
private fun OssCoachBubble(
    step: Int,
    anchorLeft: Dp,
    anchorTop: Dp,
    anchorWidth: Dp,
    anchorHeight: Dp,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val info = OssCoachSteps[step]
    val below = anchorTop + anchorHeight + 120.dp < 400.dp
    val bubbleTop = if (below) anchorTop + anchorHeight + 10.dp else anchorTop - 118.dp
    val arrowUp = !below
    Column(
        Modifier
            .offset(x = anchorLeft.coerceAtLeast(8.dp), y = bubbleTop.coerceAtLeast(4.dp))
            .width(minOf(anchorWidth + 60.dp, 300.dp))
    ) {
        if (arrowUp) OssCoachArrow(pointUp = true, tint = MaterialTheme.colorScheme.inverseSurface)
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.inverseSurface)
                .padding(12.dp)
        ) {
            Column {
                Text(
                    stringResource(info.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(info.bodyRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OssBubbleButton(labelRes = R.string.pv_oss_coach_skip, onClick = onSkip, filled = false)
                    if (step > 0) {
                        OssBubbleButton(labelRes = R.string.pv_oss_coach_prev, onClick = onPrev, filled = false)
                    }
                    OssBubbleButton(
                        labelRes = if (step == OssCoachSteps.size - 1) R.string.pv_oss_coach_done
                        else R.string.pv_oss_coach_next,
                        onClick = onNext,
                        filled = true
                    )
                }
            }
        }
        if (!arrowUp) OssCoachArrow(pointUp = false, tint = MaterialTheme.colorScheme.inverseSurface)
    }
}

@Composable
private fun OssCoachArrow(pointUp: Boolean, tint: Color) {
    Canvas(Modifier.size(width = 16.dp, height = 8.dp).padding(start = 12.dp)) {
        val path = Path().apply {
            if (pointUp) {
                moveTo(0f, size.height)
                lineTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
            } else {
                moveTo(0f, 0f)
                lineTo(size.width / 2f, size.height)
                lineTo(size.width, 0f)
            }
            close()
        }
        drawPath(path, tint)
    }
}

@Composable
private fun OssBubbleButton(labelRes: Int, onClick: () -> Unit, filled: Boolean) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (filled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.18f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            stringResource(labelRes),
            fontSize = 12.sp,
            color = if (filled) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.inverseOnSurface
        )
    }
}

