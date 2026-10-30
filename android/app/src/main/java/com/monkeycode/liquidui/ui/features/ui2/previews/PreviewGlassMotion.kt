package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/* 玻璃拆解 G01–G06 与动效案例 M01–M07（对照 UI2 glass-demos / motion-demos 机制重写）。 */

@Composable
fun PreviewG01() {
    var ox by remember { mutableFloatStateOf(60f) }
    var oy by remember { mutableFloatStateOf(10f) }
    Column {
        Box(
            Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        ) {
            Text(
                "液态玻璃不是贴图：透镜边缘把背后的字掰弯，中心保持清晰，边缘带一点 RGB 色散。拖动透镜看放大与色散边。",
                Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall,
            )
            Box(
                Modifier.offset { IntOffset(ox.roundToInt(), oy.roundToInt()) }.size(84.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                    .border(2.dp, Color(0x66FF3B30), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, drag ->
                            change.consume()
                            ox = (ox + drag.x).coerceIn(-10f, 260f)
                            oy = (oy + drag.y).coerceIn(-10f, 60f)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) { Text("×1.4", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
        }
        Text("透镜可拖（色散红边为近似示意，真实折射需 backdrop 采样）。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PreviewG02() {
    var angle by remember { mutableFloatStateOf(35f) }
    val glare = (angle / 90f).coerceIn(0f, 1f)
    Column {
        Box(
            Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border((1 + (glare * 2).roundToInt()).dp, Color.White.copy(alpha = 0.25f + glare * 0.6f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("视角 ${angle.toInt()}° · 眩光 ${(glare * 100).toInt()}%", fontWeight = FontWeight.Bold)
        }
        Slider(value = angle, onValueChange = { angle = it }, valueRange = 0f..90f)
        Text("菲涅尔：越斜着看，边缘反光越强——玻璃与塑料的分界线。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PreviewG03() {
    var n by remember { mutableFloatStateOf(4f) }
    var gap by remember { mutableFloatStateOf(70f) }
    val primary = MaterialTheme.colorScheme.primary
    Column {
        Canvas(Modifier.fillMaxWidth().height(120.dp)) {
            // superellipse blob left
            val cx = size.width * 0.28f; val cy = size.height / 2; val r = 42f
            val path = Path()
            for (i in 0..60) {
                val t = (i / 60f) * 2 * PI.toFloat()
                val ex = 2f / n
                val x = cx + r * Math.signum(cos(t)) * Math.pow(kotlin.math.abs(cos(t).toDouble()), ex.toDouble()).toFloat()
                val y = cy + r * Math.signum(sin(t)) * Math.pow(kotlin.math.abs(sin(t).toDouble()), ex.toDouble()).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, primary.copy(alpha = 0.75f))
            // two merging blobs right
            val bx = size.width * 0.68f
            drawCircle(primary.copy(alpha = 0.6f), 30f, Offset(bx - gap / 2, cy))
            drawCircle(primary.copy(alpha = 0.6f), 30f, Offset(bx + gap / 2, cy))
            if (gap < 62f) drawCircle(primary.copy(alpha = 0.6f), (62f - gap) / 2 + 6f, Offset(bx, cy))
        }
        Text("超椭圆指数 n=${"%.1f".format(n)}（2=椭圆，越大越方）", style = MaterialTheme.typography.labelSmall)
        Slider(value = n, onValueChange = { n = it }, valueRange = 2f..10f)
        Text("两滴间距 ${gap.toInt()}dp（小于 62 开始融合）", style = MaterialTheme.typography.labelSmall)
        Slider(value = gap, onValueChange = { gap = it }, valueRange = 20f..130f)
    }
}

@Composable
fun PreviewG04() {
    var stage by remember { mutableIntStateOf(0) }
    val stages = listOf("0 原图", "1 横向模糊", "2 纵向模糊", "3 合成 + 高光")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .then(if (stage >= 1) Modifier.graphicsLayer { alpha = 0.92f } else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("第 ${stage} 步：${stages[stage].substringAfter(' ')}", fontWeight = FontWeight.Bold)
                Text("模糊必须分横/纵两趟（可分离卷积），一次二维卷积贵 4 倍。", style = MaterialTheme.typography.labelSmall)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            stages.forEachIndexed { i, label ->
                TextButton(onClick = { stage = i }) { Text(label, fontWeight = if (stage == i) FontWeight.Bold else FontWeight.Normal) }
            }
        }
    }
}

@Composable
fun PreviewG05() {
    val x = remember { Animatable(30f) }
    val y = remember { Animatable(30f) }
    val scope = rememberCoroutineScope()
    Box(
        Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    scope.launch {
                        launch { x.animateTo(change.position.x - 70f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)) }
                        launch { y.animateTo(change.position.y - 70f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)) }
                    }
                }
            },
    ) {
        Box(
            Modifier.offset { IntOffset(x.value.roundToInt(), y.value.roundToInt()) }.size(56.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center,
        ) { Text("玻璃", color = Color.White, style = MaterialTheme.typography.labelSmall) }
        Text("在区域内拖动：玻璃用弹簧追你，永远慢半拍", Modifier.padding(10.dp), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PreviewG06() {
    val passes = listOf("采样 backdrop", "双向模糊", "折射位移", "染色+高光描边")
    val on = remember { mutableStateOf(listOf(true, true, true, true)) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        passes.forEachIndexed { i, label ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${i + 1}. $label", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Switch(checked = on.value[i], onCheckedChange = { v ->
                    on.value = on.value.toMutableList().also { it[i] = v }
                })
            }
        }
        Text(
            "当前效果：" + when {
                !on.value[0] -> "无采样 → 只能退化成半透明蒙层"
                !on.value[1] -> "边缘锯齿、无磨砂"
                !on.value[2] -> "是磨砂玻璃，但没有液态感"
                !on.value[3] -> "发灰发闷，像塑料片"
                else -> "完整液态玻璃"
            },
            color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun PreviewM01() {
    val text = "Hang it, then part it."
    var play by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(play) { if (play > 0) { progress.snapTo(0f); progress.animateTo(1f, tween(900)) } }
    Column {
        Row {
            text.forEachIndexed { i, ch ->
                val local = ((progress.value * text.length) - i).coerceIn(0f, 1f)
                Text(
                    ch.toString(),
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer { alpha = local; translationY = (1f - local) * 24f },
                )
            }
        }
        Button(onClick = { play++ }) { Text("拉开幕布（重播）") }
    }
}

@Composable
fun PreviewM02() {
    var plucks by remember { mutableIntStateOf(0) }
    val vibes = remember { List(7) { Animatable(0f) } }
    val scope = rememberCoroutineScope()
    val stringColor = MaterialTheme.colorScheme.primary
    Column {
        Canvas(Modifier.fillMaxWidth().height(110.dp)) {
            for (i in 0 until 7) {
                val x = size.width * (0.12f + i * 0.126f)
                val v = vibes[i].value * 10f
                drawLine(
                    stringColor, Offset(x + v, 8f), Offset(x - v, size.height - 8f),
                    strokeWidth = 3f,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..7).forEach { i ->
                TextButton(onClick = {
                    plucks++
                    scope.launch { vibes[i - 1].snapTo(1f); vibes[i - 1].animateTo(0f, tween(700)) }
                }) { Text("弦$i") }
            }
        }
        Text("已拨弦 $plucks 次（点弦看振动衰减）", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PreviewM03() {
    var seconds by remember { mutableIntStateOf(0) }
    val hand = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            seconds++
            hand.animateTo(seconds * 6f, spring(dampingRatio = 0.35f, stiffness = 260f))
        }
    }
    val primary = MaterialTheme.colorScheme.primary
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        val faceColor = MaterialTheme.colorScheme.surfaceVariant
        Canvas(Modifier.size(120.dp)) {
            drawCircle(faceColor, style = Stroke(4f))
            for (i in 0 until 12) {
                val a = i * 30f * PI.toFloat() / 180f
                drawLine(
                    Color.Gray,
                    Offset(center.x + cos(a) * 46f, center.y + sin(a) * 46f),
                    Offset(center.x + cos(a) * 54f, center.y + sin(a) * 54f), 3f,
                )
            }
            val a = (hand.value - 90f) * PI.toFloat() / 180f
            drawLine(primary, center, Offset(center.x + cos(a) * 48f, center.y + sin(a) * 48f), 5f)
            drawCircle(primary, 6f, center)
        }
        Text("秒针每秒弹跳带过冲：已走 $seconds 秒", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PreviewM04() {
    LazyColumn(Modifier.fillMaxWidth().height(190.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("第一屏", "第二屏", "第三屏").forEach { title ->
            stickyHeader {
                Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(10.dp)) {
                    Text(title, fontWeight = FontWeight.Bold)
                }
            }
            items(3) { i -> Text("· $title 内容 ${i + 1}（滚到顶会被下一屏压住）", Modifier.padding(8.dp)) }
        }
    }
}

@Composable
fun PreviewM05() {
    var sel by remember { mutableIntStateOf(4) }
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            (0..8).forEach { i ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 5.dp).clickable { sel = i }) {
                    Box(
                        Modifier.width(if (sel == i) 4.dp else 2.dp).height(if (sel == i) 34.dp else 22.dp)
                            .background(if (sel == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp)),
                    )
                    Text("${i + 1}", style = MaterialTheme.typography.labelSmall, fontWeight = if (sel == i) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        Text("当前刻度 ${sel + 1}（点刻度吸附选中）", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun PreviewM06() {
    var frame by remember { mutableFloatStateOf(0f) }
    Column {
        Box(
            Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.secondaryContainer)
                .graphicsLayer { rotationY = (frame * 180f); cameraDistance = 12f * density },
            contentAlignment = Alignment.Center,
        ) {
            Text(if ((frame * 2).toInt() % 2 == 0) "第 ${(frame * 10).toInt() + 1} 帧 · 正面" else "背面", fontWeight = FontWeight.Bold)
        }
        Slider(value = frame, onValueChange = { frame = it }, valueRange = 0f..1f)
        Text("拖动翻页：角度驱动帧，过了 90° 看到背面。", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PreviewM07() {
    var rotating by remember { mutableStateOf(true) }
    val angle = remember { Animatable(0f) }
    LaunchedEffect(rotating) {
        if (rotating) angle.animateTo(360f, tween(6000, easing = { it }))
    }
    val primary = MaterialTheme.colorScheme.primary
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Canvas(Modifier.size(130.dp)) {
            val rad = angle.value * PI.toFloat() / 180f
            val pts3d = listOf(
                Triple(-1f, -1f, -1f), Triple(1f, -1f, -1f), Triple(1f, 1f, -1f), Triple(-1f, 1f, -1f),
                Triple(-1f, -1f, 1f), Triple(1f, -1f, 1f), Triple(1f, 1f, 1f), Triple(-1f, 1f, 1f),
            )
            fun proj(p: Triple<Float, Float, Float>): Offset {
                val x = p.first * cos(rad) - p.third * sin(rad)
                val z = p.first * sin(rad) + p.third * cos(rad)
                val s = 34f * (1f + z * 0.12f)
                return Offset(center.x + x * s, center.y + p.second * s)
            }
            val pts = pts3d.map { proj(it) }
            val edges = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 0, 4 to 5, 5 to 6, 6 to 7, 7 to 4, 0 to 4, 1 to 5, 2 to 6, 3 to 7)
            edges.forEach { (a, b) -> drawLine(primary, pts[a], pts[b], 3f) }
        }
        TextButton(onClick = {
            rotating = !rotating
            if (!rotating) { /* pause at current angle */ }
        }) { Text(if (rotating) "暂停自转" else "继续自转（从当前角度重新转一圈）") }
    }
}
