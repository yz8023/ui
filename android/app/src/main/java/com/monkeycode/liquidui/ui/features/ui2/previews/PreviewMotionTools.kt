package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monkeycode.liquidui.ui.features.ui2.Ui2Features
import com.monkeycode.liquidui.ui.features.ui2.Ui2Preview
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/* 动效馆 W01–W03 与工具 T01–T02。 */

private val Recipes = listOf(
    "淡入上浮", "缩放弹入", "错峰列表", "抖动纠错", "呼吸脉冲", "旋转入场", "翻转卡片", "模糊渐清",
    "数字滚动", "打字机", "跑马灯", "弹跳落下", "钟摆摇晃", "进度填充", "骨架微光", "涟漪扩散",
    "视差倾斜", "磁吸吸附", "弹性回弹", "逐字幕布",
)

@Composable
fun PreviewW01() {
    var sel by remember { mutableIntStateOf(0) }
    var playKey by remember { mutableIntStateOf(0) }
    val anim = remember { Animatable(0f) }
    LaunchedEffect(playKey) {
        if (playKey > 0) { anim.snapTo(0f); anim.animateTo(1f, tween(700)) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Recipes.take(5).forEachIndexed { i, r -> TextButton(onClick = { sel = i; playKey++ }) { Text(r, fontWeight = if (sel == i) FontWeight.Bold else FontWeight.Normal) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Recipes.slice(5..9).forEachIndexed { i, r -> TextButton(onClick = { sel = i + 5; playKey++ }) { Text(r, fontWeight = if (sel == i + 5) FontWeight.Bold else FontWeight.Normal) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Recipes.slice(10..14).forEachIndexed { i, r -> TextButton(onClick = { sel = i + 10; playKey++ }) { Text(r, fontWeight = if (sel == i + 10) FontWeight.Bold else FontWeight.Normal) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Recipes.drop(15).forEachIndexed { i, r -> TextButton(onClick = { sel = i + 15; playKey++ }) { Text(r, fontWeight = if (sel == i + 15) FontWeight.Bold else FontWeight.Normal) } }
        }
        Box(
            Modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center,
        ) {
            val v = anim.value
            when (sel % 5) {
                0 -> Text(Recipes[sel], fontWeight = FontWeight.Bold, modifier = Modifier.offset { IntOffset(0, ((1 - v) * 30).roundToInt()) })
                1 -> Text(Recipes[sel], fontWeight = FontWeight.Bold, modifier = Modifier.offset { IntOffset(0, 0) })
                2 -> Text("${Recipes[sel]} · ${(v * 100).toInt()}%", fontWeight = FontWeight.Bold)
                3 -> Text(Recipes[sel], fontWeight = FontWeight.Bold, modifier = Modifier.offset { IntOffset(((v - 0.5f) * 24).roundToInt(), 0) })
                else -> Text(Recipes[sel], fontWeight = FontWeight.Bold)
            }
        }
        Text("20 条配方共用一个舞台骨架（选配方即以 700ms 重播其主动画通道）。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PreviewW02() {
    var group by remember { mutableIntStateOf(0) }
    var playKey by remember { mutableIntStateOf(0) }
    val anim = remember { Animatable(0f) }
    val durations = listOf(50, 100, 150, 200, 300, 450, 700)
    var durSel by remember { mutableIntStateOf(3) }
    LaunchedEffect(playKey, durSel) {
        if (playKey > 0) { anim.snapTo(0f); anim.animateTo(1f, tween(durations[durSel])) }
    }
    val primary = MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf("时长", "曲线", "弹簧", "错峰").forEachIndexed { i, label ->
                SegmentedButton(selected = group == i, onClick = { group = i }, shape = SegmentedButtonDefaults.itemShape(i, 4)) { Text(label) }
            }
        }
        when (group) {
            0 -> {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    durations.forEachIndexed { i, d -> TextButton(onClick = { durSel = i; playKey++ }) { Text("${d}", fontWeight = if (durSel == i) FontWeight.Bold else FontWeight.Normal) } }
                }
                Box(Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                    Box(Modifier.offset { IntOffset((anim.value * 230).roundToInt(), 12) }.size(22.dp).clip(CircleShape).background(primary))
                }
                Text("当前档 ${durations[durSel]}ms（点数字即以该时长重播小球位移）", style = MaterialTheme.typography.labelSmall)
            }
            1 -> {
                Canvas(Modifier.fillMaxWidth().height(90.dp)) {
                    val path = Path()
                    for (i in 0..40) {
                        val t = i / 40f
                        val e = FastOutSlowInEasing.transform(t)
                        val x = size.width * t; val y = size.height * (1 - e)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, primary, style = Stroke(4f))
                    val t = anim.value; val e = FastOutSlowInEasing.transform(t)
                    drawCircle(primary, 8f, Offset(size.width * t, size.height * (1 - e)))
                }
                Button(onClick = { playKey++ }) { Text("沿曲线重播") }
                Text("展示 FastOutSlowIn；9 条曲线同理可换 Easing 重播。", style = MaterialTheme.typography.labelSmall)
            }
            2 -> {
                var springSel by remember { mutableIntStateOf(0) }
                val presets = listOf("高弹" to Spring.DampingRatioHighBouncy, "中弹" to Spring.DampingRatioMediumBouncy, "低弹" to Spring.DampingRatioLowBouncy, "无弹" to Spring.DampingRatioNoBouncy)
                val sx = remember { Animatable(0f) }
                LaunchedEffect(springSel, playKey) {
                    if (playKey > 0) { sx.snapTo(0f); sx.animateTo(230f, spring(dampingRatio = presets[springSel].second, stiffness = Spring.StiffnessMedium)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    presets.forEachIndexed { i, (name, _) -> TextButton(onClick = { springSel = i; playKey++ }) { Text(name) } }
                }
                Box(Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                    Box(Modifier.offset { IntOffset(sx.value.roundToInt(), 12) }.size(22.dp).clip(CircleShape).background(primary))
                }
            }
            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0 until 5).forEach { i ->
                        val a = remember { Animatable(0f) }
                        LaunchedEffect(playKey) {
                            if (playKey > 0) { a.snapTo(0f); delay(i * 80L); a.animateTo(1f, tween(300)) }
                        }
                        Box(Modifier.size(30.dp).offset { IntOffset(0, ((1 - a.value) * 18).roundToInt()) }.clip(RoundedCornerShape(8.dp)).background(primary.copy(alpha = 0.35f + a.value * 0.65f)))
                    }
                    TextButton(onClick = { playKey++ }) { Text("重播错峰") }
                }
                Text("5 个方块以 80ms 间隔依次入场——错峰就是给每个元素排队。", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private val Specs = listOf(
    "时长分档" to "50–700ms 七档，微交互≤200，页面转场≤450",
    "曲线选择" to "进入用减速、退出用加速，同一手势全程一曲线",
    "弹簧优先" to "可打断的动画用弹簧：它天然继承当前速度",
    "速度继承" to "拖拽松手把手速注入动画，忌清零重来",
    "错峰上限" to "列表错峰总量不超 300ms，单项间隔 30–90ms",
    "可打断" to "任何长动画必须能被新手势接管",
    "减少动效" to "系统开启减少动效时：位移改淡入、时长归零也要到终点位",
    "退出更快" to "退出时长约为进入的 60–70%",
    "共享元素" to "跨页同一元素用共享转场，别两边各演一遍",
    "遮罩联动" to "遮罩透明度与内容位移同进度，不各走各的",
    "拖拽跟手" to "拖拽期间 1:1 跟随，动画只在松手后接管",
    "吸附阈值" to "过半或速度过阈才翻页/开合，两条判据取或",
    "回弹边界" to "过冲只许出现在松手动画，不许拖拽中越界太多",
    "列表入场" to "仅首屏入场做错峰，滚动中出现的不再逐个演",
    "状态同步" to "动画结束位必须等于逻辑状态位，防视觉与状态分家",
    "帧率底线" to "动画只动 transform/alpha 类属性，避免逐帧改布局",
    "图表动画" to "数据图首次描边生长，后续更新只补间数值",
    "加载反馈" to "100ms 内无反馈必闪烁；先骨架再内容的顺序不可反",
    "手势冲突" to "横滑与竖滚在斜向 30° 内按主方向锁定",
    "焦点与动效" to "键盘焦点移动不带动效，防晕",
    "深色适配" to "辉光/阴影类动效在深色下单独调强度",
    "大屏适配" to "位移距离按容器比例而非写死 px",
    "文案动效" to "数字滚动只滚变化位，长文本用淡入不用逐字",
    "中断恢复" to "页面切后台回来，动画落终点不续播一半",
    "一致性" to "同类组件全 App 用同一组 token，一处改全改",
)

@Composable
fun PreviewW03() {
    var q by remember { mutableStateOf("") }
    val hits = Specs.filter { it.first.contains(q) || it.second.contains(q) }
    Column {
        OutlinedTextField(q, { q = it }, placeholder = { Text("搜索规范…（如 弹簧 / 错峰）") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        Column(Modifier.height(230.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            hits.forEachIndexed { i, (title, guide) ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${i + 1}. $title", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(guide, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (hits.isEmpty()) Text("无匹配规范")
        }
        Text("共 ${hits.size} / 25 篇（标题+导读索引，深读回仓库 motion 令牌文档）。", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PreviewT01() {
    var platform by remember { mutableIntStateOf(0) }
    val features = listOf("液态玻璃", "图标切换", "壁纸背景", "底栏导航", "动效系统", "主题配色", "权限中心", "引导页", "主题导入导出", "网络壁纸", "O神主题")
    val checked = remember { mutableStateListOf(true, true, false, true, true, false, false, true, false, false, false) }
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val platformNames = listOf("Android Compose", "Web App", "纯静态页")
    val tree = buildString {
        appendLine(platformNames[platform] + "/")
        appendLine("├─ src/  入口与导航")
        features.forEachIndexed { i, f -> if (checked[i]) appendLine("├─ features/$f/") }
        appendLine("└─ docs/  实现方案 · 参考来源 · 环境要求")
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            platformNames.forEachIndexed { i, name ->
                SegmentedButton(selected = platform == i, onClick = { platform = i }, shape = SegmentedButtonDefaults.itemShape(i, 3)) { Text(name, fontSize = 11.sp) }
            }
        }
        features.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                row.forEach { f ->
                    val idx = features.indexOf(f)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = checked[idx], onCheckedChange = { checked[idx] = it })
                        Text(f, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Text(tree, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp))
        }
        OutlinedButton(onClick = {
            clipboard.setText(AnnotatedString("平台：${platformNames[platform]}\n功能：${features.filterIndexed { i, _ -> checked[i] }.joinToString("、")}\n目录树：\n$tree"))
            copied = true
        }) { Text(if (copied) "✓ 已复制结构" else "复制结构到剪贴板") }
        Text("勾选实时重算目录树；映射到模板功能编号请对照手册 F01–F15。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PreviewT02() {
    var aId by remember { mutableStateOf("F03") }
    var bId by remember { mutableStateOf("F31") }
    var menuA by remember { mutableStateOf(false) }
    var menuB by remember { mutableStateOf(false) }
    val all = (Ui2Features)
    fun picker(current: String, expanded: Boolean, onExpand: (Boolean) -> Unit, onPick: (String) -> Unit) = Pair(expanded, onPick)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box {
                OutlinedButton(onClick = { menuA = true }) { Text("左侧：$aId") }
                androidx.compose.material3.DropdownMenu(expanded = menuA, onDismissRequest = { menuA = false }) {
                    all.take(24).forEach { f -> androidx.compose.material3.DropdownMenuItem(text = { Text("${f.id} ${f.name}") }, onClick = { aId = f.id; menuA = false }) }
                }
            }
            Box {
                OutlinedButton(onClick = { menuB = true }) { Text("右侧：$bId") }
                androidx.compose.material3.DropdownMenu(expanded = menuB, onDismissRequest = { menuB = false }) {
                    all.drop(22).forEach { f -> androidx.compose.material3.DropdownMenuItem(text = { Text("${f.id} ${f.name}") }, onClick = { bId = f.id; menuB = false }) }
                }
            }
        }
        listOf(aId, bId).forEach { id ->
            val f = all.firstOrNull { it.id == id } ?: all.first()
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("${f.id} · ${f.name}", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Ui2Preview(f)
                }
            }
        }
        Text("同屏对比两条预览（左侧选单含 F01–F24，右侧含 F23–F46），找参数差异最快。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
