package com.monkeycode.liquidui.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.monkeycode.liquidui.ui.motion.运动
import com.monkeycode.liquidui.ui.theme.AppShape
import kotlinx.coroutines.launch

// ============================================================
// Slider
// ============================================================

/**
 * Default Material slider restyled to match the reference: 4dp rounded track,
 * primary active fill, 22dp thumb, optional value label above the thumb while
 * dragging.
 */
@Composable
fun 玻璃滑块(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    showValueLabel: Boolean = false,
    labelFormatter: (Float) -> String = { "${it.toInt()}" }
) {
    var dragging by remember { mutableStateOf(false) }
    val animatedThumbColor by animateColorAsState(
        targetValue = if (dragging) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        animationSpec = tween(运动.Duration.Fast),
        label = "thumbColor"
    )
    Column(modifier = modifier) {
        if (showValueLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = labelFormatter(value),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val pct = ((value - valueRange.start) /
                    (valueRange.endInclusive - valueRange.start) * 100f).toInt()
                Text(
                    text = "$pct %",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Slider(
            value = value,
            onValueChange = {
                dragging = true
                onValueChange(it)
            },
            onValueChangeFinished = { dragging = false },
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = animatedThumbColor,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledActiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledInactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            ),
            thumb = {
    val lowerFrac = ((lower - valueRange.start) /
        (valueRange.endInclusive - valueRange.start)).toFloat()
    val upperFrac = ((upper - valueRange.start) /
        (valueRange.endInclusive - valueRange.start)).toFloat()
    val fillWidth = upperFrac - lowerFrac
    val lowerOffsetX = with(LocalDensity.current) { (lowerFrac * boxWidthPx - 11f).toDp() }
    val upperOffsetX = with(LocalDensity.current) { (upperFrac * boxWidthPx - 11f).toDp() }
    val fillOffsetX = with(LocalDensity.current) { (lowerFrac * boxWidthPx).toDp() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .onSizeChanged { boxWidthPx = it.width }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        activeThumb = nearestThumb(
                            positionX = it.x,
                            boxWidth = boxWidthPx,
                            lower = lower,
                            upper = upper,
                            valueRange = valueRange
                        )
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val fraction = (change.position.x / boxWidthPx.toFloat()).coerceIn(0f, 1f)
                        val raw = valueRange.start +
                            fraction * (valueRange.endInclusive - valueRange.start)
                        when (activeThumb) {
                            0 -> lower = raw.coerceAtMost(upper - 0.01f)
                            1 -> upper = raw.coerceAtLeast(lower + 0.01f)
                        }
                    },
                    onDragStopped = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    activeThumb = nearestThumb(
                        positionX = down.position.x,
                        boxWidth = boxWidthPx,
                        lower = lower,
                        upper = upper,
                        valueRange = valueRange
                    )
                    horizontalDrag(down.id) { change ->
                        change.consume()
                        val fraction = (change.position.x / boxWidthPx.toFloat()).coerceIn(0f, 1f)
                        val raw = valueRange.start +
                            fraction * (valueRange.endInclusive - valueRange.start)
                        when (activeThumb) {
                            0 -> lower = raw.coerceAtMost(upper - 0.01f)
                            1 -> upper = raw.coerceAtLeast(lower + 0.01f)
                        }
                    }
                    onDragEnd()
                }
            }
    ) {
        // Inactive track.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(2.dp))
                .background(trackColour)
        )
        // Active fill between thumbs.
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = fillWidth)
                .offset(x = fillOffsetX)
                .height(4.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(2.dp))
                .background(activeColour)
        )
        // Lower thumb.
        Box(
            modifier = Modifier
                .offset(x = lowerOffsetX)
                .size(22.dp)
                .clip(CircleShape)
                .background(thumbColour)
                .align(Alignment.Center)
        )
        // Upper thumb.
        Box(
            modifier = Modifier
                .offset(x = upperOffsetX)
                .size(22.dp)
                .clip(CircleShape)
                .background(thumbColour)
                .align(Alignment.Center)
        )
    }
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        )
    }
}

// ============================================================
// Range Slider
// ============================================================

/**
 * Two-thumb range slider. Each thumb is an independent draggable pip; the
 * filled track sits between them. Tap anywhere on the track to jump the
 * nearest thumb.
 */
@Composable
fun 玻璃范围滑块(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    labelFormatter: (Float) -> String = { "${it.toInt()}" }
) {
    var lower by remember(value.start) { mutableFloatStateOf(value.start) }
    var upper by remember(value.endInclusive) { mutableFloatStateOf(value.endInclusive) }
    var activeThumb by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(value) {
        if (value.start != lower) lower = value.start
        if (value.endInclusive != upper) upper = value.endInclusive
    }

    val onDragEnd: () -> Unit = {
        activeThumb = null
        onValueChange(lower..upper)
    }

    val trackColour = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val activeColour = MaterialTheme.colorScheme.primary
    val thumbColour = MaterialTheme.colorScheme.primary
    var boxWidthPx by remember { mutableIntStateOf(0) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = labelFormatter(lower),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = labelFormatter(upper),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .onSizeChanged { boxWidthPx = it.width }
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            activeThumb = nearestThumb(
                                positionX = it.x,
                                boxWidth = boxWidthPx,
                                lower = lower,
                                upper = upper,
                                valueRange = valueRange
                            )
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val fraction = (change.position.x / boxWidthPx.toFloat()).coerceIn(0f, 1f)
                            val raw = valueRange.start +
                                fraction * (valueRange.endInclusive - valueRange.start)
                            when (activeThumb) {
                                0 -> lower = raw.coerceAtMost(upper - 0.01f)
                                1 -> upper = raw.coerceAtLeast(lower + 0.01f)
                            }
                        },
                        onDragStopped = { onDragEnd() },
                        onDragCancel = { onDragEnd() }
                    )
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        activeThumb = nearestThumb(
                            positionX = down.position.x,
                            boxWidth = boxWidthPx,
                            lower = lower,
                            upper = upper,
                            valueRange = valueRange
                        )
                        horizontalDrag(down.id) { change ->
                            change.consume()
                            val fraction = (change.position.x / boxWidthPx.toFloat()).coerceIn(0f, 1f)
                            val raw = valueRange.start +
                                fraction * (valueRange.endInclusive - valueRange.start)
                            when (activeThumb) {
                                0 -> lower = raw.coerceAtMost(upper - 0.01f)
                                1 -> upper = raw.coerceAtLeast(lower + 0.01f)
                            }
                        }
                        onDragEnd()
                    }
                }
        ) {
            // Inactive track.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(2.dp))
                    .background(trackColour)
            )
            // Active fill between thumbs.
            val lowerFrac = ((lower - valueRange.start) /
                (valueRange.endInclusive - valueRange.start)).toFloat()
            val upperFrac = ((upper - valueRange.start) /
                (valueRange.endInclusive - valueRange.start)).toFloat()
            val fillWidth = upperFrac - lowerFrac
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = fillWidth)
                    .offset {
                        DpOffset(
                            x = with(LocalDensity.current) { (lowerFrac * boxWidthPx).toDp() },
                            y = 0.dp
                        )
                    }
                    .height(4.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(2.dp))
                    .background(activeColour)
            )
            // Lower thumb.
            Box(
                modifier = Modifier
                    .offset {
                        DpOffset(
                            x = with(LocalDensity.current) { (lowerFrac * boxWidthPx - 11f).toDp() },
                            y = 0.dp
                        )
                    }
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(thumbColour)
                    .align(Alignment.Center)
            )
            // Upper thumb.
            Box(
                modifier = Modifier
                    .offset {
                        DpOffset(
                            x = with(LocalDensity.current) { (upperFrac * boxWidthPx - 11f).toDp() },
                            y = 0.dp
                        )
                    }
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(thumbColour)
                    .align(Alignment.Center)
            )
        }
    }
}

/** Returns 0 for lower thumb, 1 for upper thumb, based on which is closest. */
private fun nearestThumb(
    positionX: Float,
    boxWidth: Int,
    lower: Float,
    upper: Float,
    valueRange: ClosedFloatingPointRange<Float>
): Int {
    if (boxWidth == 0) return 0
    val frac = (positionX / boxWidth.toFloat()).coerceIn(0f, 1f)
    val raw = valueRange.start + frac * (valueRange.endInclusive - valueRange.start)
    return if (kotlin.math.abs(raw - lower) <= kotlin.math.abs(raw - upper)) 0 else 1
}

// ============================================================
// Segmented / Discrete value control
// ============================================================

/**
 * A glass-styled segmented control. The sliding thumb tracks a draggable float;
 * tap a segment to jump, or drag across the whole track and release to snap.
 */
@Composable
fun 玻璃分段标签(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var widthPx by remember { mutableIntStateOf(0) }
    val fraction = remember { Animatable(selectedIndex.toFloat()) }
    val dragging = remember { mutableStateOf(false) }

    LaunchedEffect(selectedIndex) {
        if (!dragging.value) {
            fraction.animateTo(selectedIndex.toFloat(), 运动.snappy(0.001f))
        }
    }

    Row(
        modifier = modifier
            .onSizeChanged { widthPx = it.width }
            .liquidGlass(shape = AppShape.control, elevation = 0.dp, borderAlpha = 0.28f)
            .pointerInput(options.size, selectedIndex) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        dragging.value = true
                        scope.launch { fraction.stop() }
                    },
                    onDragEnd = {
                        dragging.value = false
                        val target = fraction.value.roundToInt().coerceIn(0, options.lastIndex)
                        scope.launch {
                            fraction.animateTo(target.toFloat(), 运动.snappy(0.001f))
                        }
                        if (target != selectedIndex) onSelected(target)
                    },
                    onDragCancel = {
                        dragging.value = false
                        scope.launch {
                            fraction.animateTo(selectedIndex.toFloat(), 运动.snappy(0.001f))
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val step = if (widthPx > 0) widthPx / options.size.toFloat() else 1f
                        scope.launch {
                            fraction.snapTo(
                                (fraction.value + dragAmount / step).coerceIn(0f, (options.size - 1).toFloat())
                            )
                        }
                    }
                )
            }
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val distance = kotlin.math.abs(fraction.value - index).coerceIn(0f, 1f)
            val selectedness = 1f - distance
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(AppShape.pill)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = selectedness * 0.85f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelected(index) }
                    )
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selectedness > 0.5f) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selectedness > 0.5f) FontWeight.SemiBold else FontWeight.Normal,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// ============================================================
// Defaults
// ============================================================

/** Shared visual tokens for every slider variant in this file. */
object 玻璃滑块Defaults {
    val thumbSize = 22.dp
    val trackHeight = 4.dp
    val cornerRadius = 2.dp
}
