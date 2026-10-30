package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/* 07 动效 F38–F42 与 08 应用模板 F43–F46。 */

@Composable
fun PreviewF38() {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Box(
        Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .size(64.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primary)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            scope.launch {
                                launch { offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
                                launch { offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
                            }
                        },
                    ) { change, drag ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + drag.x)
                            offsetY.snapTo(offsetY.value + drag.y)
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) { Text("拖我", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun PreviewF39() {
    var show by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { show = !show }) { Text(if (show) "重置" else "播放错峰入场") }
        (0 until 5).forEach { i ->
            val anim = remember { androidx.compose.animation.core.Animatable(0f) }
            LaunchedEffect(show) {
                if (show) { delay(i * 90L); anim.animateTo(1f, androidx.compose.animation.core.tween(420)) }
                else anim.snapTo(0f)
            }
            Box(
                Modifier.fillMaxWidth().graphicsLayer {
                    alpha = anim.value
                    translationY = (1f - anim.value) * 40f
                }.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.secondaryContainer).padding(12.dp),
            ) { Text("条目 ${i + 1}（入场延迟 ${i * 90}ms，420ms 渐入上浮）") }
        }
    }
}

@Composable
fun PreviewF40() {
    var dismissed by remember { mutableStateOf(false) }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Column {
        if (!dismissed) {
            Card(
                Modifier.fillMaxWidth().offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                if (offsetX.value > 220f || offsetX.value < -220f) dismissed = true
                                else scope.launch { offsetX.animateTo(0f, spring()) }
                            },
                        ) { change, drag ->
                            change.consume()
                            scope.launch { offsetX.snapTo(offsetX.value + drag.x) }
                        }
                    },
            ) {
                Text("← 左右拖动我，超过阈值就划走 →", Modifier.padding(18.dp), fontWeight = FontWeight.SemiBold)
            }
        } else {
            Text("卡片已划走。")
            TextButton(onClick = { dismissed = false; scope.launch { offsetX.snapTo(0f) } }) { Text("复原") }
        }
    }
}

@Composable
fun PreviewF41() {
    var page by remember { mutableIntStateOf(0) }
    Column {
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
            },
            label = "pageTransition",
        ) { p ->
            Box(
                Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (p % 2 == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center,
            ) { Text("页面 ${p + 1} / 3", fontWeight = FontWeight.Bold) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { page = (page + 2) % 3 }) { Text("上一页") }
            Button(onClick = { page = (page + 1) % 3 }) { Text("下一页") }
        }
    }
}

@Composable
fun PreviewF42() {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val parallax = if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset * 0.5f else 42f
    LazyColumn(state = listState, modifier = Modifier.fillMaxWidth().height(200.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Box(
                Modifier.fillMaxWidth().height(84.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "头图（以 0.5× 速度视差后移）",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer { translationY = parallax },
                )
            }
        }
        items(6) { i ->
            Card(Modifier.fillMaxWidth()) { Text("滚动条目 ${i + 1}", Modifier.padding(14.dp)) }
        }
    }
}

@Composable
fun PreviewF43() {
    val pages = listOf("欢迎使用" to "46 条素材全部可现场体验", "长按复制" to "每条提示词长按即复制给 AI", "开始吧" to "点完成回到素材列表")
    val state = rememberPagerState(pageCount = { pages.size })
    Column {
        HorizontalPager(state = state, modifier = Modifier.fillMaxWidth().height(130.dp)) { p ->
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(18.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(pages[p].first, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(pages[p].second, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.Center) {
            pages.indices.forEach { i ->
                Box(
                    Modifier.padding(horizontal = 3.dp).size(if (state.currentPage == i) 10.dp else 7.dp).clip(CircleShape)
                        .background(if (state.currentPage == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                )
            }
        }
    }
}

@Composable
fun PreviewF44() {
    var account by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("登录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(account, { account = it }, label = { Text("手机号 / 邮箱") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(pwd, { pwd = it }, label = { Text("密码") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation())
        Button(
            onClick = { msg = if (account.isNotBlank() && pwd.length >= 6) "演示登录成功（未连后端）" else "账号不能为空，密码至少 6 位" },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("登录") }
        msg?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("忘记密码", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text("注册账号", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun PreviewF45() {
    val states = remember { mutableStateListOf(true, false, true) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("系统壁纸背景", "减少动效", "通知").forEachIndexed { i, label ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(label, fontWeight = FontWeight.SemiBold)
                    Text("演示行，切换即时生效", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = states[i], onCheckedChange = { states[i] = it })
            }
        }
    }
}

@Composable
fun PreviewF46() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                Text("L", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Liquid 用户", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("已体验 12 / 46 条素材", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("收藏" to "8", "足迹" to "23", "成就" to "5").forEach { (label, v) ->
                Card(Modifier.weight(1f)) {
                    Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(v, fontWeight = FontWeight.Bold)
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        listOf("我的收藏", "浏览历史", "设置").forEach { Text("$it ›", Modifier.fillMaxWidth().padding(vertical = 4.dp)) }
    }
}
