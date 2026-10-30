package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/* 05 反馈 F25–F31 与 06 数据展示 F32–F37。 */

@Composable
fun PreviewF25() {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(show) { if (show) { delay(2200); show = false } }
    Column {
        Button(onClick = { show = true }) { Text("弹出 Toast") }
        Spacer(Modifier.height(10.dp))
        AnimatedVisibility(visible = show) {
            Box(
                Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.inverseSurface).padding(horizontal = 16.dp, vertical = 10.dp),
            ) { Text("✓ 已复制到剪贴板", color = MaterialTheme.colorScheme.inverseOnSurface) }
        }
    }
}

@Composable
fun PreviewF26() {
    var open by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("未操作") }
    Column {
        Button(onClick = { open = true }) { Text("打开对话框") }
        Text("结果：$result", style = MaterialTheme.typography.bodySmall)
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false; result = "点外部关闭" },
            title = { Text("清除缓存壁纸？") },
            text = { Text("清除后将回退到极光背景，网络原图不会受影响。") },
            confirmButton = { TextButton(onClick = { open = false; result = "确认清除" }) { Text("清除") } },
            dismissButton = { TextButton(onClick = { open = false; result = "取消" }) { Text("取消") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewF27() {
    var open by remember { mutableStateOf(false) }
    var picked by remember { mutableStateOf("未选择") }
    Column {
        Button(onClick = { open = true }) { Text("打开底部弹层") }
        Text("已选：$picked", style = MaterialTheme.typography.bodySmall)
    }
    if (open) {
        ModalBottomSheet(onDismissRequest = { open = false }) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("壁纸来源", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                listOf("系统壁纸", "本地图片", "网络链接").forEach {
                    TextButton(onClick = { picked = it; open = false }) { Text(it) }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PreviewF28() {
    var loaded by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(0.35f, 0.85f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "a")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!loaded) {
            Row {
                Box(Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)))
                Spacer(Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.width(140.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)))
                    Box(Modifier.width(90.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)))
                }
            }
        } else {
            Text("内容已加载：真实数据替换骨架（同一布局位置）。", fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(onClick = { loaded = !loaded }) { Text(if (loaded) "回到骨架屏" else "模拟加载完成") }
    }
}

@Composable
fun PreviewF29() {
    var hasData by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (!hasData) {
            Text("暂无收藏", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("点过的素材会在这里聚合，方便下次直接体验。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { hasData = true }) { Text("添加一条演示数据") }
        } else {
            Text("已有一条演示收藏（点下方可清空回空态）")
            TextButton(onClick = { hasData = false }) { Text("清空") }
        }
    }
}

@Composable
fun PreviewF30() {
    var step by remember { mutableIntStateOf(1) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LinearProgressIndicator(progress = { step / 3f }, modifier = Modifier.fillMaxWidth())
        Text("步骤 $step / 3：${listOf("填资料", "选壁纸", "完成")[step - 1]}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { step = (step - 1).coerceAtLeast(1) }) { Text("上一步") }
            Button(onClick = { step = (step + 1).coerceAtMost(3) }) { Text("下一步") }
            CircularProgressIndicator(progress = { step / 3f }, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun PreviewF31() {
    var state by remember { mutableIntStateOf(0) } // 0 idle 1 loading 2 done
    LaunchedEffect(state) {
        if (state == 1) { delay(1400); state = 2 }
        if (state == 2) { delay(1600); state = 0 }
    }
    Button(onClick = { if (state == 0) state = 1 }, enabled = state == 0, modifier = Modifier.fillMaxWidth()) {
        when (state) {
            0 -> Text("提交")
            1 -> { CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary); Spacer(Modifier.width(8.dp)); Text("提交中…") }
            else -> Text("✓ 成功")
        }
    }
}

@Composable
fun PreviewF32() {
    var alt by remember { mutableStateOf(false) }
    val data = if (alt) listOf(3f, 5f, 4f, 8f, 6f, 9f) else listOf(2f, 4f, 7f, 5f, 8f, 6f)
    val primary = MaterialTheme.colorScheme.primary
    Column {
        Canvas(Modifier.fillMaxWidth().height(110.dp)) {
            val max = 10f; val stepX = size.width / (data.size - 1)
            var prev: Offset? = null
            data.forEachIndexed { i, v ->
                val p = Offset(i * stepX, size.height - (v / max) * size.height)
                prev?.let { drawLine(primary, it, p, strokeWidth = 5f, cap = StrokeCap.Round) }
                drawCircle(primary, 6f, p); prev = p
            }
        }
        TextButton(onClick = { alt = !alt }) { Text("切换数据集（真实重绘）") }
    }
}

@Composable
fun PreviewF33() {
    val data = listOf(0.5f, 0.8f, 0.35f, 0.9f, 0.6f)
    var selected by remember { mutableIntStateOf(-1) }
    val primary = MaterialTheme.colorScheme.primary
    val variant = MaterialTheme.colorScheme.tertiary
    Column {
        Canvas(Modifier.fillMaxWidth().height(110.dp)) {
            val barW = size.width / (data.size * 2 - 1)
            data.forEachIndexed { i, v ->
                drawRect(
                    color = if (i == selected) variant else primary,
                    topLeft = Offset(i * barW * 2, size.height * (1 - v)),
                    size = Size(barW, size.height * v),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            data.indices.forEach { i -> TextButton(onClick = { selected = i }) { Text("柱${i + 1}") } }
        }
        Text(if (selected >= 0) "已选中柱 ${selected + 1}：${(data[selected] * 100).toInt()}%" else "点按钮高亮对应柱", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PreviewF34() {
    val parts = listOf(0.45f to Color(0xFF3482FF), 0.3f to Color(0xFF34C759), 0.25f to Color(0xFFFF9F0A))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(110.dp)) {
            var start = -90f
            parts.forEach { (frac, color) ->
                drawArc(color, start, frac * 360f, useCenter = false, style = Stroke(width = 26f, cap = StrokeCap.Butt))
                start += frac * 360f
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("玻璃 45%", "动效 30%", "导航 25%").forEachIndexed { i, label ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(parts[i].second)); Spacer(Modifier.width(6.dp)); Text(label, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun PreviewF35() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        listOf("今日步数" to "8,432" to "+12%", "专注时长" to "3.2h" to "+5%").forEach { (label, value, delta) ->
            Card(Modifier.weight(1f)) {
                Column(Modifier.padding(14.dp)) {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(delta, color = Color(0xFF34C759), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun PreviewF36() {
    val rows = listOf(
        listOf("功能", "状态", "版本"),
        listOf("液态玻璃", "已接入", "2.4"),
        listOf("系统壁纸", "已修复", "2.4.2"),
        listOf("UI2体验馆", "新增", "2.5"),
    )
    Column {
        rows.forEachIndexed { ri, row ->
            Row(
                Modifier.fillMaxWidth()
                    .background(if (ri == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEachIndexed { ci, cell ->
                    Text(cell, Modifier.weight(if (ci == 0) 1.4f else 1f), fontWeight = if (ri == 0) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun PreviewF37() {
    val events = listOf("09:00" to "提交需求", "10:30" to "CI 构建通过", "11:15" to "真机验收", "14:00" to "发布 v2.5")
    var done by remember { mutableIntStateOf(2) }
    Column {
        events.forEachIndexed { i, (time, label) ->
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(14.dp).clip(CircleShape)
                            .background(if (i < done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                    )
                    if (i < events.lastIndex) Box(Modifier.width(2.dp).height(26.dp).background(MaterialTheme.colorScheme.outlineVariant))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(label, fontWeight = if (i < done) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
        TextButton(onClick = { done = (done + 1) % 5 }) { Text("推进时间线") }
    }
}
