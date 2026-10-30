package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/* 视频专题 V01–V09 与弹层馆 X01–X04。 */

@Composable
fun PreviewV01() {
    var kind by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Dialog", "Sheet", "Toast", "Popover", "Drawer").forEach { k ->
                TextButton(onClick = {
                    when (k) {
                        "Dialog" -> dialog = true
                        "Sheet" -> sheet = true
                        else -> kind = k
                    }
                }) { Text(k) }
            }
        }
        AnimatedVisibility(visible = kind == "Toast") {
            Box(Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.inverseSurface).padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text("Toast：不打断，2 秒自动走", color = MaterialTheme.colorScheme.inverseOnSurface, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (kind == "Popover") Text("Popover：依附触发点，指回来源（如按钮下方菜单）。", style = MaterialTheme.typography.bodySmall)
        if (kind == "Drawer") Text("Drawer：从边缘进，占半屏以内，是导航不是确认。", style = MaterialTheme.typography.bodySmall)
        Text("选型口诀：要确认用 Dialog，要选东西用 Sheet，只通知用 Toast。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (dialog) AlertDialog(onDismissRequest = { dialog = false }, title = { Text("Dialog") }, text = { Text("模态确认，中断当前流程。") }, confirmButton = { TextButton(onClick = { dialog = false }) { Text("好") } })
    if (sheet) ModalBottomSheet(onDismissRequest = { sheet = false }) {
        Text("Sheet：底部列表/表单，可拖关。", Modifier.padding(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewV02() {
    var active by remember { mutableStateOf(0) }
    val types = listOf("非模态条", "悬浮球", "边缘坞", "横幅", "迷你窗", "灵动岛")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            types.forEachIndexed { i, t -> TextButton(onClick = { active = i }) { Text(t, fontWeight = if (active == i) FontWeight.Bold else FontWeight.Normal) } }
        }
        Box(
            Modifier.fillMaxWidth().height(84.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                listOf(
                    "顶部非模态条：不挡操作，自动消失",
                    "悬浮球：全局入口，可拖",
                    "边缘坞：贴边收起，滑出展开",
                    "横幅：图+文+按钮，强运营位",
                    "迷你窗：小窗播放/导航，可拖动",
                    "灵动岛：状态胶囊，点开扩展",
                )[active],
                Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun PreviewV03() {
    val tricks = listOf(
        "发丝线（1px 真线）" to "分隔不用阴影，用最细的线",
        "连续曲率圆角" to "角不是圆弧拼接，是连续过渡",
        "动态背景模糊" to "浮层身后实时模糊，不是灰蒙版",
        "物理弹簧" to "动画带过冲与速度继承",
        "触觉反馈" to "关键动作配轻震，眼睛耳朵手指一起确认",
    )
    val on = remember { mutableStateListOf(true, true, false, true, false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        tricks.forEachIndexed { i, (name, desc) ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = on[i], onCheckedChange = { on[i] = it })
            }
        }
        Text("已启用 ${on.count { it }}/5 招——质感是开关的叠加，不是单个特效。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun PreviewV04() {
    val full = "流式输出时，文字像打字一样逐段到达；思考时有三点脉冲；随时能停；不满意可重新生成；长按可复制——五个细节缺一不可。"
    var shown by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(running) {
        if (running) {
            shown = 0
            while (shown < full.length && running) { delay(45); shown++ }
            running = false
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Text(if (shown == 0 && !running) "点「生成」看流式输出…" else full.take(shown), Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { running = true }) { Text(if (shown in 1 until full.length) "重新生成" else "生成") }
            OutlinedButton(onClick = { running = false }, enabled = running) { Text("停止") }
            if (shown == full.length) Text("✓ 已完成（可长按复制）", color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.CenterVertically))
        }
    }
}

@Composable
fun PreviewV05() {
    var dd by remember { mutableStateOf(false) }
    var ddVal by remember { mutableStateOf("上海") }
    var selectOpen by remember { mutableStateOf(false) }
    var selVal by remember { mutableStateOf("上海") }
    val cities = listOf("北京", "上海", "广州", "深圳")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Dropdown（菜单）", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Box {
                    OutlinedButton(onClick = { dd = true }) { Text("$ddVal ▾") }
                    DropdownMenu(expanded = dd, onDismissRequest = { dd = false }) {
                        cities.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { ddVal = c; dd = false }) }
                    }
                }
                Text("轻量、就近、选项少时用", style = MaterialTheme.typography.labelSmall)
            }
            Column(Modifier.weight(1f)) {
                Text("Select（选择器）", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { selectOpen = true }) { Text(selVal) }
                Text("表单字段、要校验/回显时用", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
    if (selectOpen) {
        AlertDialog(
            onDismissRequest = { selectOpen = false },
            title = { Text("选择城市") },
            text = {
                Column { cities.forEach { c -> Text(c, Modifier.fillMaxWidth().clickable { selVal = c; selectOpen = false }.padding(10.dp)) } }
            },
            confirmButton = { TextButton(onClick = { selectOpen = false }) { Text("取消") } },
        )
    }
}

@Composable
fun PreviewV06() {
    var showZones by remember { mutableStateOf(true) }
    var hits by remember { mutableIntStateOf(0) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("显示热区透视", Modifier.weight(1f))
            Switch(checked = showZones, onCheckedChange = { showZones = it })
        }
        Box(Modifier.fillMaxWidth().height(90.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(44.dp)
                    .then(if (showZones) Modifier.border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(6.dp)) else Modifier)
                    .clickable { hits++ },
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(20.dp).clip(RoundedCornerShape(5.dp)).background(MaterialTheme.colorScheme.primary))
            }
        }
        Text("红框=44dp 真实热区，蓝块=20dp 视觉。已命中 $hits 次——点在框内任意处都算中。", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PreviewV07() {
    var lang by remember { mutableIntStateOf(0) }
    val langs = listOf(
        "中文" to ("保存" to false),
        "English" to ("Save changes" to false),
        "Deutsch" to ("Änderungen speichern" to false),
        "العربية" to ("حفظ التغييرات" to true),
    )
    val (label, rtl) = langs[lang].second
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            langs.forEachIndexed { i, (name, _) -> TextButton(onClick = { lang = i }) { Text(name, fontWeight = if (lang == i) FontWeight.Bold else FontWeight.Normal) } }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = if (rtl) Arrangement.End else Arrangement.Start,
        ) {
            Button(onClick = {}) { Text(label) }
        }
        Text(
            when (lang) {
                0 -> "中文最短，按钮游刃有余"
                1 -> "英文变长，固定宽按钮开始挤"
                2 -> "德语最长——写死宽度的按钮必爆，这就是要弹性布局的原因"
                else -> "阿拉伯语 RTL：布局镜像，起点在右侧"
            },
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun PreviewV08() { RhythmGame(thresholdMs = 1600, label = "虎先锋 · 慢速判定条") }

@Composable
fun PreviewV09() { RhythmGame(thresholdMs = 950, label = "寅虎 · 快速判定条 + 连击") }

@Composable
private fun RhythmGame(thresholdMs: Int, label: String) {
    var pos by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var verdict by remember { mutableStateOf("点开始后，在光标进入金色完美区时点判定") }
    LaunchedEffect(running) {
        if (running) {
            pos = 0
            val start = System.currentTimeMillis()
            while (running && pos < 100) {
                pos = ((System.currentTimeMillis() - start) * 100 / thresholdMs).toInt().coerceAtMost(100)
                delay(16)
            }
            if (pos >= 100 && running) { verdict = "过头了——节奏点已错过"; combo = 0; running = false }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        Box(Modifier.fillMaxWidth().height(26.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(Modifier.fillMaxWidth(0.16f).offset(x = 168.dp).height(26.dp).background(Color(0xFFFFC107).copy(alpha = 0.8f)))
            Box(Modifier.width(4.dp).height(26.dp).offset(x = (pos * 2.2f).dp).background(MaterialTheme.colorScheme.error))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { running = true; verdict = "判定条行进中…" }) { Text("开始") }
            Button(onClick = {
                if (running) {
                    running = false
                    if (pos in 76..92) { score += 100 + combo * 10; combo++; verdict = "完美！+${100 + (combo - 1) * 10} 分" }
                    else { combo = 0; verdict = if (pos < 76) "早了——抢拍" else "晚了——拖拍" }
                }
            }) { Text("判定！") }
            Text("分 $score · 连击 $combo", fontWeight = FontWeight.Bold)
        }
        Text(verdict, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

/* ------- X01–X04 弹层馆 ------- */

private val PopupTypes = listOf("确认", "输入", "加载", "中间列表", "底部列表", "底部勾选", "抽屉", "图片", "依附", "气泡")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewX01() {
    var active by remember { mutableStateOf<String?>(null) }
    var input by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("点任意一型，立刻真弹（点外部/返回可关）：", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PopupTypes.take(5).forEach { t -> TextButton(onClick = { active = t }) { Text(t) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PopupTypes.drop(5).forEach { t -> TextButton(onClick = { active = t }) { Text(t) } }
        }
    }
    when (active) {
        "确认" -> AlertDialog(onDismissRequest = { active = null }, title = { Text("确认弹窗") }, text = { Text("asConfirm 的原生等价：模态确认。") }, confirmButton = { TextButton(onClick = { active = null }) { Text("确认") } }, dismissButton = { TextButton(onClick = { active = null }) { Text("取消") } })
        "输入" -> AlertDialog(onDismissRequest = { active = null }, title = { Text("输入弹窗") }, text = { OutlinedTextField(input, { input = it }, placeholder = { Text("输入内容") }) }, confirmButton = { TextButton(onClick = { active = null }) { Text("提交") } })
        "加载" -> AlertDialog(onDismissRequest = { active = null }, title = { Text("加载中…") }, text = { Row { androidx.compose.material3.CircularProgressIndicator(Modifier.size(22.dp)); Spacer(Modifier.width(12.dp)); Text("请稍候") } }, confirmButton = { TextButton(onClick = { active = null }) { Text("取消") } })
        "中间列表", "依附", "气泡" -> AlertDialog(onDismissRequest = { active = null }, title = { Text("$active") }, text = { Column { listOf("选项一", "选项二", "选项三").forEach { Text(it, Modifier.fillMaxWidth().clickable { active = null }.padding(10.dp)) } } }, confirmButton = { TextButton(onClick = { active = null }) { Text("关闭") } })
        "底部列表", "底部勾选", "抽屉", "图片" -> ModalBottomSheet(onDismissRequest = { active = null }) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$active（底部形态）", fontWeight = FontWeight.Bold)
                listOf("条目一", "条目二", "条目三").forEach { Text(it, Modifier.fillMaxWidth().clickable { active = null }.padding(8.dp)) }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun PreviewX02() {
    val anims = listOf("淡入", "缩放", "上滑", "下滑", "左滑入", "右滑入", "缩放+淡入", "无动画")
    var sel by remember { mutableIntStateOf(0) }
    var show by remember { mutableStateOf(true) }
    LaunchedEffect(sel, show) { }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            anims.take(4).forEachIndexed { i, a -> TextButton(onClick = { sel = i; show = false }) { Text(a) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            anims.drop(4).forEachIndexed { i, a -> TextButton(onClick = { sel = i + 4; show = false }) { Text(a) } }
        }
        Box(Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visible = show,
                enter = when (sel) {
                    0 -> fadeIn()
                    1 -> scaleIn()
                    2 -> slideInVertically { it } + fadeIn()
                    3 -> slideInVertically { -it } + fadeIn()
                    4 -> slideInHorizontally { -it } + fadeIn()
                    5 -> slideInHorizontally { it } + fadeIn()
                    6 -> scaleIn() + fadeIn()
                    else -> fadeIn(tweenless())
                },
                exit = fadeOut(),
            ) {
                Card { Text("舞台卡片 · ${anims[sel]}", Modifier.padding(18.dp), fontWeight = FontWeight.Bold) }
            }
            if (!show) {
                LaunchedEffect(Unit) { delay(60); show = true }
            }
        }
        Text("XPopup 的 20 个 PopupAnimation 在此映射为 8 类过渡族（原生等价）。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun tweenless() = androidx.compose.animation.core.tween<Float>(0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewX03() {
    var dismissOutside by remember { mutableStateOf(true) }
    var darkPopup by remember { mutableStateOf(false) }
    var open by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("点外部可关闭", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Switch(checked = dismissOutside, onCheckedChange = { dismissOutside = it })
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("深色弹窗", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Switch(checked = darkPopup, onCheckedChange = { darkPopup = it })
        }
        Button(onClick = { open = true }) { Text("用当前设置弹一个") }
        if (note.isNotEmpty()) Text(note, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
    if (open) {
        AlertDialog(
            onDismissRequest = {
                if (dismissOutside) { open = false; note = "已通过点外部关闭" } else note = "点外部被拦截（设置生效中）"
            },
            title = { Text(if (darkPopup) "深色弹窗" else "浅色弹窗") },
            text = { Text("这就是设置直连运行时的效果。") },
            confirmButton = { TextButton(onClick = { open = false; note = "已确认关闭" }) { Text("确认") } },
            containerColor = if (darkPopup) Color(0xFF242424) else MaterialTheme.colorScheme.surface,
            titleContentColor = if (darkPopup) Color.White else MaterialTheme.colorScheme.onSurface,
            textContentColor = if (darkPopup) Color.White else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun PreviewX04() {
    var stage by remember { mutableIntStateOf(1) }
    val offsetY = remember { Animatable(80f) }
    val scope = rememberCoroutineScope()
    val stages = listOf(20f, 80f, 150f)
    Column {
        Box(
            Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        ) {
            Card(
                Modifier.fillMaxWidth().offset { IntOffset(0, offsetY.value.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                val target = stages.minByOrNull { kotlin.math.abs(it - offsetY.value) } ?: 80f
                                stage = stages.indexOf(target)
                                scope.launch { offsetY.animateTo(target, androidx.compose.animation.core.spring()) }
                            },
                        ) { change, drag ->
                            change.consume()
                            scope.launch { offsetY.snapTo((offsetY.value + drag.y).coerceIn(0f, 170f)) }
                        }
                    },
            ) {
                Column(Modifier.padding(16.dp)) {
                    Box(Modifier.width(36.dp).height(4.dp).align(Alignment.CenterHorizontally).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant))
                    Spacer(Modifier.height(10.dp))
                    Text("拖我上下移动，松手吸附到最近档位", fontWeight = FontWeight.Bold)
                    Text("当前档位 ${stage + 1} / 3（收起 · 半开 · 全开）", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
