package com.monkeycode.liquidui.ui.features.ui2.previews

import com.monkeycode.liquidui.R

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
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/* 视频专题 V01–V09 与弹层馆 X01–X04。 */

@OptIn(ExperimentalMaterial3Api::class)
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
    val types = listOf(stringResource(R.string.pv_video_text_1), stringResource(R.string.pv_video_text_2), stringResource(R.string.pv_video_text_3), stringResource(R.string.pv_video_text_4), stringResource(R.string.pv_video_text_5), stringResource(R.string.pv_video_text_6))
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
                    stringResource(R.string.pv_video_text_7),
                    stringResource(R.string.pv_video_text_8),
                    stringResource(R.string.pv_video_text_9),
                    stringResource(R.string.pv_video_text_10),
                    stringResource(R.string.pv_video_text_11),
                    stringResource(R.string.pv_video_text_12),
                )[active],
                Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun PreviewV03() {
    val tricks = listOf(
        "发丝线（1px 真线）" to stringResource(R.string.pv_video_text_13),
        "连续曲率圆角" to stringResource(R.string.pv_video_text_14),
        "动态背景模糊" to stringResource(R.string.pv_video_text_15),
        "物理弹簧" to stringResource(R.string.pv_video_text_16),
        "触觉反馈" to stringResource(R.string.pv_video_text_17),
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
        Text(stringResource(R.string.pv_video_text_71, on.count { it }), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun PreviewV04() {
    val full = stringResource(R.string.pv_video_text_18)
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
            Text(if (shown == 0 && !running) stringResource(R.string.pv_video_text_19) else full.take(shown), Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { running = true }) { Text(if (shown in 1 until full.length) stringResource(R.string.pv_video_text_20) else stringResource(R.string.pv_video_text_21)) }
            OutlinedButton(onClick = { running = false }, enabled = running) { Text(stringResource(R.string.pv_video_text_22)) }
            if (shown == full.length) Text(stringResource(R.string.pv_video_text_23), color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.CenterVertically))
        }
    }
}

@Composable
fun PreviewV05() {
    var dd by remember { mutableStateOf(false) }
    var ddVal by remember { mutableStateOf("上海") }
    var selectOpen by remember { mutableStateOf(false) }
    var selVal by remember { mutableStateOf("上海") }
    val cities = listOf(stringResource(R.string.pv_video_text_24), stringResource(R.string.pv_video_text_25), stringResource(R.string.pv_video_text_26), stringResource(R.string.pv_video_text_27))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.pv_video_text_28), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Box {
                    OutlinedButton(onClick = { dd = true }) { Text("$ddVal ▾") }
                    DropdownMenu(expanded = dd, onDismissRequest = { dd = false }) {
                        cities.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { ddVal = c; dd = false }) }
                    }
                }
                Text(stringResource(R.string.pv_video_text_29), style = MaterialTheme.typography.labelSmall)
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.pv_video_text_30), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { selectOpen = true }) { Text(selVal) }
                Text(stringResource(R.string.pv_video_text_31), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
    if (selectOpen) {
        AlertDialog(
            onDismissRequest = { selectOpen = false },
            title = { Text(stringResource(R.string.pv_video_text_32)) },
            text = {
                Column { cities.forEach { c -> Text(c, Modifier.fillMaxWidth().clickable { selVal = c; selectOpen = false }.padding(10.dp)) } }
            },
            confirmButton = { TextButton(onClick = { selectOpen = false }) { Text(stringResource(R.string.common_cancel)) } },
        )
    }
}

@Composable
fun PreviewV06() {
    var showZones by remember { mutableStateOf(true) }
    var hits by remember { mutableIntStateOf(0) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.pv_video_text_33), Modifier.weight(1f))
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
        Text(stringResource(R.string.pv_video_text_72, hits), style = MaterialTheme.typography.labelSmall)
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
                0 -> stringResource(R.string.pv_video_text_34)
                1 -> stringResource(R.string.pv_video_text_35)
                2 -> stringResource(R.string.pv_video_text_36)
                else -> stringResource(R.string.pv_video_text_37)
            },
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun PreviewV08() { RhythmGame(thresholdMs = 1600, label = stringResource(R.string.pv_video_label_1)) }

@Composable
fun PreviewV09() { RhythmGame(thresholdMs = 950, label = stringResource(R.string.pv_video_label_2)) }

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
            Button(onClick = { running = true; verdict = "判定条行进中…" }) { Text(stringResource(R.string.aq_demo_text_3)) }
            Button(onClick = {
                if (running) {
                    running = false
                    if (pos in 76..92) { score += 100 + combo * 10; combo++; verdict = "完美！+${100 + (combo - 1) * 10} 分" }
                    else { combo = 0; verdict = if (pos < 76) "早了——抢拍" else "晚了——拖拍" }
                }
            }) { Text(stringResource(R.string.pv_video_text_38)) }
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
        Text(stringResource(R.string.pv_video_text_39), style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PopupTypes.take(5).forEach { t -> TextButton(onClick = { active = t }) { Text(t) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PopupTypes.drop(5).forEach { t -> TextButton(onClick = { active = t }) { Text(t) } }
        }
    }
    when (active) {
        "确认" -> AlertDialog(onDismissRequest = { active = null }, title = { Text(stringResource(R.string.pv_video_text_40)) }, text = { Text(stringResource(R.string.pv_video_text_41)) }, confirmButton = { TextButton(onClick = { active = null }) { Text(stringResource(R.string.components_confirm_1)) } }, dismissButton = { TextButton(onClick = { active = null }) { Text(stringResource(R.string.common_cancel)) } })
        "输入" -> AlertDialog(onDismissRequest = { active = null }, title = { Text(stringResource(R.string.pv_video_text_42)) }, text = { OutlinedTextField(input, { input = it }, placeholder = { Text(stringResource(R.string.components_placeholder_1)) }) }, confirmButton = { TextButton(onClick = { active = null }) { Text(stringResource(R.string.common_shared_8)) } })
        "加载" -> AlertDialog(onDismissRequest = { active = null }, title = { Text(stringResource(R.string.pv_video_text_43)) }, text = { Row { androidx.compose.material3.CircularProgressIndicator(Modifier.size(22.dp)); Spacer(Modifier.width(12.dp)); Text(stringResource(R.string.pv_video_text_44)) } }, confirmButton = { TextButton(onClick = { active = null }) { Text(stringResource(R.string.common_cancel)) } })
        stringResource(R.string.pv_video_text_45), stringResource(R.string.pv_video_text_46), "气泡" -> AlertDialog(onDismissRequest = { active = null }, title = { Text("$active") }, text = { Column { listOf(stringResource(R.string.pv_video_text_47), stringResource(R.string.pv_video_text_48), stringResource(R.string.pv_video_text_49)).forEach { Text(it, Modifier.fillMaxWidth().clickable { active = null }.padding(10.dp)) } } }, confirmButton = { TextButton(onClick = { active = null }) { Text(stringResource(R.string.pv_video_text_50)) } })
        stringResource(R.string.pv_video_text_51), stringResource(R.string.pv_video_text_52), stringResource(R.string.common_shared_9), "图片" -> ModalBottomSheet(onDismissRequest = { active = null }) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$active（底部形态）", fontWeight = FontWeight.Bold)
                listOf(stringResource(R.string.pv_video_text_53), stringResource(R.string.pv_video_text_54), stringResource(R.string.pv_video_text_55)).forEach { Text(it, Modifier.fillMaxWidth().clickable { active = null }.padding(8.dp)) }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun PreviewX02() {
    val anims = listOf(stringResource(R.string.pv_video_text_56), stringResource(R.string.pv_video_text_57), stringResource(R.string.pv_video_text_58), stringResource(R.string.pv_video_text_59), stringResource(R.string.pv_video_text_60), stringResource(R.string.pv_video_text_61), stringResource(R.string.pv_video_text_62), stringResource(R.string.pv_video_text_63))
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
          Column {
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
                Card { Text(stringResource(R.string.pv_video_text_73, anims[sel]), Modifier.padding(18.dp), fontWeight = FontWeight.Bold) }
            }
          }
            if (!show) {
                LaunchedEffect(Unit) { delay(60); show = true }
            }
        }
        Text(stringResource(R.string.pv_video_text_64), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Text(stringResource(R.string.pv_video_text_65), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Switch(checked = dismissOutside, onCheckedChange = { dismissOutside = it })
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.pv_video_text_66), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Switch(checked = darkPopup, onCheckedChange = { darkPopup = it })
        }
        Button(onClick = { open = true }) { Text(stringResource(R.string.pv_video_text_67)) }
        if (note.isNotEmpty()) Text(note, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
    if (open) {
        AlertDialog(
            onDismissRequest = {
                if (dismissOutside) { open = false; note = "已通过点外部关闭" } else note = "点外部被拦截（设置生效中）"
            },
            title = { Text(if (darkPopup) stringResource(R.string.pv_video_text_66) else stringResource(R.string.pv_video_text_68)) },
            text = { Text(stringResource(R.string.pv_video_text_69)) },
            confirmButton = { TextButton(onClick = { open = false; note = "已确认关闭" }) { Text(stringResource(R.string.components_confirm_1)) } },
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
                    Text(stringResource(R.string.pv_video_text_70), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.pv_video_text_74, stage + 1), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
