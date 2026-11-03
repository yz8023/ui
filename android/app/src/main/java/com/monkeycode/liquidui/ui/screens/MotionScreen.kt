package com.monkeycode.liquidui.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.InfoBanner
import com.monkeycode.liquidui.ui.components.Meter
import com.monkeycode.liquidui.ui.components.PrimaryButton
import com.monkeycode.liquidui.ui.components.SecondaryButton
import com.monkeycode.liquidui.ui.components.SectionTitle
import com.monkeycode.liquidui.ui.components.SegmentedTabs
import com.monkeycode.liquidui.ui.components.liquidGlass
import com.monkeycode.liquidui.ui.motion.Motion
import com.monkeycode.liquidui.ui.motion.impactShake
import com.monkeycode.liquidui.ui.motion.idleBreathing
import com.monkeycode.liquidui.ui.theme.AuroraAmber
import com.monkeycode.liquidui.ui.theme.AuroraMint
import com.monkeycode.liquidui.ui.theme.AuroraRose
import com.monkeycode.liquidui.ui.theme.AuroraViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val springPresetLabels = listOf("gentle", "standard", "snappy", "bouncy", "heavy")

private fun springPreset(index: Int): FiniteAnimationSpec<Float> = when (index) {
    0 -> Motion.gentle(0.001f)
    1 -> Motion.standard(0.001f)
    2 -> Motion.snappy(0.001f)
    3 -> Motion.bouncy(0.001f)
    else -> Motion.heavy(0.001f)
}

@Composable
fun MotionScreen(settings: AppSettingsState) {
    val reduced = settings.reducedMotion

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
                    text = stringResource(R.string.motion_text_1),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.motion_text_2),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { SectionTitle(stringResource(R.string.motion_section_title_1), Modifier.padding(top = 8.dp)) }
        item { SpringPlayground(reduced) }

        item { SectionTitle(stringResource(R.string.motion_section_title_2)) }
        item { StaggerDemo(reduced) }

        item { SectionTitle(stringResource(R.string.motion_section_title_3)) }
        item { ShakeDemo(reduced) }

        item { SectionTitle(stringResource(R.string.motion_section_title_4)) }
        item { BreathingDemo(reduced) }

        item {
            InfoBanner(
                text = if (reduced) {
                    stringResource(R.string.motion_text_3)
                } else {
                    stringResource(R.string.motion_text_4)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun SpringPlayground(reduced: Boolean) {
    var presetIndex by remember { mutableIntStateOf(3) }
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var trackWidth by remember { mutableFloatStateOf(0f) }
    var readout by remember { mutableFloatStateOf(0f) }
    var demoRun by remember { mutableIntStateOf(0) }
    var demoDir by remember { mutableFloatStateOf(1f) }
    val trackWidthState = androidx.compose.runtime.rememberUpdatedState(trackWidth)
    val damping = Motion.motionDamping.floatValue
    val density = androidx.compose.ui.platform.LocalDensity.current

    fun runDemo() {
        val w = trackWidthState.value
        if (w <= 0f) return
        val maxOffset = with(density) {
            (w / 2f) - 56.dp.toPx()
        }
        val target = demoDir * maxOffset
        scope.launch {
            offset.animateTo(
                target,
                if (reduced) spring(1f, 900f) else springPreset(presetIndex)
            )
        }
        demoDir = -demoDir
    }

    LaunchedEffect(demoRun, damping, reduced) { runDemo() }

    GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        SegmentedTabs(
            labels = springPresetLabels,
            selectedIndex = presetIndex,
            onSelected = {
                presetIndex = it
                demoRun++
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .onGloballyPositioned { trackWidth = it.size.width.toFloat() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            )
            Box(
                Modifier
                    .offset { IntOffset(offset.value.roundToInt(), 0) }
                    .size(56.dp)
                    .pointerInput(reduced) {
                        val maxOffset = with(density) {
                            (trackWidthState.value / 2f) - 56.dp.toPx()
                        }
                        detectDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    offset.animateTo(
                                        0f,
                                        if (reduced) spring(1f, 900f) else springPreset(presetIndex)
                                    )
                                }
                            },
                            onDragCancel = {
                                scope.launch { offset.animateTo(0f, spring(1f, 900f)) }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                val next = (offset.value + dragAmount.x)
                                    .coerceIn(-maxOffset, maxOffset)
                                offset.snapTo(next)
                                readout = next
                            }
                        }
                    }
                    .liquidGlass(shape = CircleShape, elevation = 3.dp, borderAlpha = 0.6f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.motion_text_5),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "token: spring-${springPresetLabels[presetIndex]} · " +
                stringResource(R.string.motion_text_14, (damping * 100).toInt()) +
                stringResource(R.string.motion_text_15, readout.roundToInt()),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StaggerDemo(reduced: Boolean) {
    var runId by remember { mutableIntStateOf(0) }
    GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.motion_text_6),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "token: stagger.standard-list",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SecondaryButton(onClick = { runId++ }) { Text(stringResource(R.string.motion_text_7)) }
        }
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(6) { index ->
                RevealRow(runId = runId, index = index, enabled = !reduced)
            }
        }
    }
}

@Composable
private fun RevealRow(runId: Int, index: Int, enabled: Boolean) {
    val progress = remember { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(runId, enabled) {
        if (!enabled) {
            progress.snapTo(1f)
            return@LaunchedEffect
        }
        progress.snapTo(0f)
        delay((index * Motion.Stagger.StandardList).toLong())
        progress.animateTo(1f, Motion.standard(0.01f))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = progress.value
                translationY = (1f - progress.value) * 26f
            }
            .liquidGlass(shape = RoundedCornerShape(14.dp), elevation = 0.dp, borderAlpha = 0.26f)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                    when (index % 4) {
                        0 -> AuroraMint
                        1 -> AuroraViolet
                        2 -> AuroraAmber
                        else -> AuroraRose
                    }.copy(alpha = 0.85f)
                )
        )
        Text(
            text = stringResource(R.string.motion_text_16, index + 1),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "${index * Motion.Stagger.StandardList}ms",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ShakeDemo(reduced: Boolean) {
    var trigger by remember { mutableIntStateOf(0) }
    var amplitude by remember { mutableFloatStateOf(8f) }
    var label by remember { mutableStateOf("待机") }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .impactShake(
                trigger = if (reduced) null else trigger,
                amplitude = amplitude
            )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.motion_text_8),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(onClick = {
                amplitude = 4f
                label = "拾取 · 0.10/0.22s"
                trigger++
            }) { Text(stringResource(R.string.motion_text_9)) }
            PrimaryButton(onClick = {
                amplitude = 16f
                label = "碰撞 · 0.35/0.35s"
                trigger++
            }) { Text(stringResource(R.string.motion_text_10)) }
            SecondaryButton(onClick = {
                amplitude = 2.5f
                label = "落地 · 0.07/0.14s"
                trigger++
            }) { Text(stringResource(R.string.motion_text_11)) }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.motion_text_12),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BreathingDemo(reduced: Boolean) {
    val colors = listOf(AuroraMint, AuroraViolet, AuroraAmber)
    GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.motion_text_13),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .then(
                            if (reduced) Modifier else Modifier.idleBreathing(
                                periodMs = 5200 + index * 900,
                                amplitudeX = 0.02f + index * 0.01f,
                                amplitudeY = 0.03f + index * 0.01f
                            )
                        )
                        .liquidGlass(shape = RoundedCornerShape(18.dp), elevation = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Meter(progress = 0.68f)
    }
}
