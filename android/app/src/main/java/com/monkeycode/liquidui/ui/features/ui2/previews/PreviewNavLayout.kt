package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/* 03 导航 F13–F18 与 04 布局 F19–F24。 */

@Composable
fun PreviewF13() {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("首页", "发现", "我的")
    Column {
        Box(
            Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) { Text("当前页：${tabs[tab]}", fontWeight = FontWeight.SemiBold) }
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            tabs.forEachIndexed { i, label ->
                NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Text(label.take(1)) }, label = { Text(label) })
            }
        }
    }
}

@Composable
fun PreviewF14() {
    var collapsed by remember { mutableStateOf(false) }
    Column {
        Row(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("‹", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(if (collapsed) "素材库" else "素材库 · UI2 体验馆", fontWeight = FontWeight.Bold, style = if (collapsed) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge)
                if (!collapsed) Text("46 条真实预览", style = MaterialTheme.typography.labelSmall)
            }
            Text("分享  收藏")
        }
        TextButton(onClick = { collapsed = !collapsed }) { Text(if (collapsed) "展开顶栏" else "折叠顶栏") }
    }
}

@Composable
fun PreviewF15() {
    var open by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
        Row {
            AnimatedVisibility(visible = open) {
                Column(
                    Modifier.width(120.dp).background(MaterialTheme.colorScheme.primaryContainer).padding(12.dp).height(150.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("抽屉菜单", fontWeight = FontWeight.Bold)
                    listOf("主页", "收藏", "设置").forEach { Text(it) }
                }
            }
            Column(Modifier.padding(12.dp)) {
                Button(onClick = { open = !open }) { Text(if (open) "关闭抽屉" else "打开抽屉") }
                Text("抽屉从左侧滑出，内容区仍可见。", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun PreviewF16() {
    val full = listOf("首页", "素材库", "导航类", "抽屉")
    var depth by remember { mutableIntStateOf(3) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            full.take(depth + 1).forEachIndexed { i, seg ->
                if (i > 0) Text(" / ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    seg,
                    fontWeight = if (i == depth) FontWeight.Bold else FontWeight.Normal,
                    color = if (i == depth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { depth = i },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { depth = (depth + 1).coerceAtMost(3) }) { Text("深入一层") }
            TextButton(onClick = { depth = 0 }) { Text("回首页") }
        }
        Text("点任意一段可跳回该层级。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PreviewF17() {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("概览", "参数", "出处")
    Column {
        TabRow(selectedTabIndex = tab) {
            tabs.forEachIndexed { i, label -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) }) }
        }
        Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
            Text(listOf("这里是功能概览内容", "关键参数与取值范围", "参考项目与许可证")[tab])
        }
    }
}

@Composable
fun PreviewF18() {
    var sel by remember { mutableIntStateOf(0) }
    val menu = listOf("工作台", "素材", "团队", "设置")
    Row(Modifier.fillMaxWidth().height(140.dp)) {
        Column(
            Modifier.width(92.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            menu.forEachIndexed { i, label ->
                Text(
                    label,
                    fontWeight = if (sel == i) FontWeight.Bold else FontWeight.Normal,
                    color = if (sel == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { sel = i }.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier.weight(1f).height(140.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) { Text("${menu[sel]} 内容区") }
    }
}

@Composable
fun PreviewF19() {
    val tiles = listOf("大横幅" to 2, "开关" to 1, "图表" to 1, "按钮组" to 1, "列表" to 1)
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth().height(190.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tiles) { (label, _) ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) { Text(label) }
            }
        }
    }
}

@Composable
fun PreviewF20() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("外观" to listOf("主题配色", "壁纸背景"), "通用" to listOf("权限管理", "关于本机")).forEach { (group, rows) ->
            Column {
                Text(group, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Card(Modifier.fillMaxWidth()) {
                    Column {
                        rows.forEachIndexed { i, row ->
                            Text(row, Modifier.fillMaxWidth().clickable { }.padding(14.dp))
                            if (i < rows.lastIndex) Spacer(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreviewF21() {
    val heights = listOf(64, 96, 80, 120, 72, 100)
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth().height(210.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
    ) {
        items(heights) { h ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Box(Modifier.fillMaxWidth().height(h.dp), contentAlignment = Alignment.Center) { Text("${h}dp") }
            }
        }
    }
}

@Composable
fun PreviewF22() {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cols = when {
            maxWidth < 200.dp -> 1
            maxWidth < 320.dp -> 2
            else -> 3
        }
        Column {
            Text("当前容器宽 ${maxWidth.value.toInt()}dp → $cols 列", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(cols), modifier = Modifier.height(120.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(6) { i ->
                    Card { Box(Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.Center) { Text("卡片 ${i + 1}") } }
                }
            }
        }
    }
}

@Composable
fun PreviewF23() {
    LazyColumn(Modifier.fillMaxWidth().height(210.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("基础组件", "导航", "反馈").forEach { group ->
            stickyHeader {
                Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(8.dp)) {
                    Text(group, fontWeight = FontWeight.Bold)
                }
            }
            items(3) { i -> Text("· $group 条目 ${i + 1}", Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreviewF24() {
    val insets = WindowInsets.safeDrawing
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("安全区（真实 WindowInsets 读数）", fontWeight = FontWeight.SemiBold)
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.secondaryContainer).padding(16.dp),
        ) {
            Text("本卡已避开状态栏/手势区绘制；刘海与手势条区域不会压住内容。insets 类型：safeDrawing")
        }
        Text("实现要点：根布局用 safeDrawingPadding / statusBarsPadding，底栏叠加导航栏 inset。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
