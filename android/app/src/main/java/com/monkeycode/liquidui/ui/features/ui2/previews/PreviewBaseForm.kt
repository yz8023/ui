package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

/* 01 基础组件 F01–F06 与 02 表单输入 F07–F12 的真实预览（对照 UI2 同名条目规格实现）。 */

@Composable
fun PreviewF01() {
    var loading by remember { mutableStateOf(false) }
    var count by remember { mutableIntStateOf(0) }
    LaunchedEffect(loading) { if (loading) { delay(1600); loading = false } }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PressButton("主要按钮") { count++ }
            OutlinedButton(onClick = { count++ }, shape = RoundedCornerShape(12.dp)) { Text("次要") }
            TextButton(onClick = { count++ }) { Text("幽灵") }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { loading = true }, enabled = !loading, shape = RoundedCornerShape(12.dp)) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp)); Text("提交中…")
                } else Text("加载态演示")
            }
            Text("已点击 $count 次", style = MaterialTheme.typography.bodySmall)
        }
        Text("按压缩放 0.955 + 弹簧回弹（左一）；加载态尺寸不变。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PressButton(text: String, onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.955f else 1f, label = "press")
    Button(onClick = onClick, interactionSource = src, modifier = Modifier.scale(scale), shape = RoundedCornerShape(12.dp)) { Text(text) }
}

@Composable
fun PreviewF02() {
    var value by remember { mutableStateOf("") }
    val max = 24
    val error = value.length > max
    Column {
        OutlinedTextField(
            value = value, onValueChange = { value = it }, label = { Text("昵称") },
            modifier = Modifier.fillMaxWidth(), singleLine = true, isError = error,
            trailingIcon = {
                if (value.isNotEmpty()) Text("✕", Modifier.clickable { value = "" }.padding(8.dp))
            },
            supportingText = {
                Row(Modifier.fillMaxWidth()) {
                    Text(if (error) "超出字数上限" else "浮动标签 + 清除键 + 计数", Modifier.weight(1f))
                    Text("${value.length}/$max", color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        )
    }
}

@Composable
fun PreviewF03() {
    val items = listOf("壁纸背景", "动效", "通知")
    val states = remember { mutableStateListOf(true, false, true) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEachIndexed { i, label ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Switch(checked = states[i], onCheckedChange = { states[i] = it })
            }
        }
        Text("连点开关试试：状态与位置始终同步。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PreviewF04() {
    var v by remember { mutableFloatStateOf(40f) }
    var dragging by remember { mutableStateOf(false) }
    Column {
        Row(Modifier.fillMaxWidth()) {
            Text("音量", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            if (dragging || true) Text("${v.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = v, onValueChange = { v = it; dragging = true }, onValueChangeFinished = { dragging = false }, valueRange = 0f..100f)
        Text("拖动看数值气泡（此处常显数值），两端不越界。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PreviewF05() {
    val checks = remember { mutableStateListOf(true, false, false) }
    var radio by remember { mutableIntStateOf(0) }
    val parent = when {
        checks.all { it } -> ToggleableState.On
        checks.none { it } -> ToggleableState.Off
        else -> ToggleableState.Indeterminate
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TriStateCheckbox(state = parent, onClick = {
                val target = parent != ToggleableState.On
                for (i in checks.indices) checks[i] = target
            })
            Text("全部权限（三态）", fontWeight = FontWeight.SemiBold)
        }
        listOf("读取壁纸", "发送通知", "访问网络").forEachIndexed { i, label ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                Checkbox(checked = checks[i], onCheckedChange = { checks[i] = it }); Text(label)
            }
        }
        Spacer(Modifier.height(6.dp))
        listOf("极光", "系统壁纸", "本地缓存").forEachIndexed { i, label ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = radio == i, onClick = { radio = i }); Text(label)
            }
        }
    }
}

@Composable
fun PreviewF06() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row {
            listOf("张", "李", "王", "+9").forEachIndexed { i, name ->
                Box(
                    Modifier.padding(start = if (i == 0) 0.dp else (-8).dp).size(40.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text(name, style = MaterialTheme.typography.labelMedium) }
            }
            Spacer(Modifier.width(16.dp))
            Box {
                Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) { Text("邮") }
                Box(
                    Modifier.align(Alignment.TopEnd).size(16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.Center,
                ) { Text("3", color = MaterialTheme.colorScheme.onError, style = MaterialTheme.typography.labelSmall) }
            }
        }
        Text("头像组重叠 8dp + 未读徽标；点任意头像无动作，纯展示组件。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PreviewF07() {
    var email by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var touched by remember { mutableStateOf(false) }
    val emailOk = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)
    val pwdOk = pwd.length >= 6
    val confirmOk = confirm.isNotEmpty() && confirm == pwd
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(email, { email = it }, label = { Text("邮箱") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            isError = touched && !emailOk,
            supportingText = { if (touched && !emailOk) Text("邮箱格式不正确") })
        OutlinedTextField(pwd, { pwd = it }, label = { Text("密码（≥6位）") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = touched && !pwdOk, supportingText = { if (touched && !pwdOk) Text("密码至少 6 位") })
        OutlinedTextField(confirm, { confirm = it }, label = { Text("确认密码") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = touched && !confirmOk, supportingText = { if (touched && !confirmOk) Text("两次输入不一致") })
        Button(onClick = { touched = true }, modifier = Modifier.fillMaxWidth()) { Text("注册（演示校验）") }
        if (touched && emailOk && pwdOk && confirmOk) Text("✓ 格式检查通过（演示，未创建账号）", color = MaterialTheme.colorScheme.primary)
        Text("改密码后确认密码会重新判不一致——联动校验。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PreviewF08() {
    var n by remember { mutableIntStateOf(1) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            HoldButton("−", enabled = n > 0) { n = (n - 1).coerceAtLeast(0) }
            Text("$n", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(56.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            HoldButton("+", enabled = n < 9) { n = (n + 1).coerceAtMost(9) }
        }
        Text("单击 ±1，长按连发（到端自动停）；范围 0–9。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HoldButton(label: String, enabled: Boolean, onStep: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape)
            .background(if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(onPress = {
                    onStep()
                    val released = try {
                        withTimeout(400) { awaitRelease() }; true
                    } catch (_: Exception) { false }
                    if (!released) {
                        while (true) { onStep(); if (try { withTimeout(80) { awaitRelease() }; true } catch (_: Exception) { false }) break }
                    }
                })
            },
        contentAlignment = Alignment.Center,
    ) { Text(label, style = MaterialTheme.typography.titleLarge) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewF09() {
    var sel by remember { mutableIntStateOf(0) }
    val opts = listOf("日", "周", "月")
    Column {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            opts.forEachIndexed { i, label ->
                SegmentedButton(selected = sel == i, onClick = { sel = i }, shape = SegmentedButtonDefaults.itemShape(i, opts.size)) { Text(label) }
            }
        }
        Text("当前粒度：${opts[sel]}（切换下方统计口径的演示）", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PreviewF10() {
    var q by remember { mutableStateOf("") }
    val all = listOf("液态玻璃", "系统壁纸", "图标切换", "动效系统", "O神主题", "网络壁纸", "权限中心")
    val hits = all.filter { it.contains(q, ignoreCase = true) }
    Column {
        OutlinedTextField(q, { q = it }, placeholder = { Text("搜索功能…") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            trailingIcon = { if (q.isNotEmpty()) Text("✕", Modifier.clickable { q = "" }.padding(8.dp)) })
        Spacer(Modifier.height(8.dp))
        if (q.isBlank()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("壁纸", "玻璃", "图标").forEach { FilterChip(selected = false, onClick = { q = it }, label = { Text(it) }) }
            }
        }
        hits.forEach { Text("· $it", Modifier.padding(vertical = 3.dp), style = MaterialTheme.typography.bodyMedium) }
        if (hits.isEmpty()) Text("无结果，换个关键词试试", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private data class UploadJob(val name: String, val progress: Float, val done: Boolean)

@Composable
fun PreviewF11() {
    val jobs = remember { mutableStateListOf<UploadJob>() }
    var seq by remember { mutableIntStateOf(0) }
    LaunchedEffect(jobs.size) {
        while (true) {
            delay(120)
            for (i in jobs.indices) if (!jobs[i].done) {
                val p = jobs[i].progress + 0.08f
                jobs[i] = jobs[i].copy(progress = p.coerceAtMost(1f), done = p >= 1f)
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { seq++; jobs.add(UploadJob("壁纸素材_$seq.jpg", 0f, false)) }) { Text("添加图片（模拟上传）") }
        jobs.forEach { job ->
            Column {
                Row(Modifier.fillMaxWidth()) {
                    Text(job.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text(if (job.done) "完成" else "${(job.progress * 100).toInt()}%", color = MaterialTheme.colorScheme.primary)
                }
                LinearProgressIndicator(progress = { job.progress }, modifier = Modifier.fillMaxWidth())
            }
        }
        if (jobs.isEmpty()) Text("队列为空——先添加一张。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewF12() {
    var city by remember { mutableStateOf("上海") }
    var expanded by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box {
            OutlinedButton(onClick = { expanded = true }) { Text("城市：$city ▾") }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("北京", "上海", "广州", "深圳", "新加坡").forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = { city = it; expanded = false })
                }
            }
        }
        OutlinedButton(onClick = { showDate = true }) { Text("选择日期") }
        dateState.selectedDateMillis?.let {
            Text("已选：${java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(it))}", color = MaterialTheme.colorScheme.primary)
        }
        Text("下拉选择器 + 系统日期选择器，均为真实组件。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (showDate) {
        DatePickerDialog(onDismissRequest = { showDate = false }, confirmButton = {
            TextButton(onClick = { showDate = false }) { Text("确定") }
        }) { DatePicker(state = dateState) }
    }
}
