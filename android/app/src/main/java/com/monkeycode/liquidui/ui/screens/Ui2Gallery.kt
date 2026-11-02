package com.monkeycode.liquidui.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.SectionTitle
import com.monkeycode.liquidui.ui.features.ui2.Ui2Categories
import com.monkeycode.liquidui.ui.features.ui2.Ui2Feature
import com.monkeycode.liquidui.ui.features.ui2.Ui2ExtraFeatures
import com.monkeycode.liquidui.ui.features.ui2.Ui2Features
import com.monkeycode.liquidui.ui.features.ui2.Ui2Preview

/**
 * UI2 体验馆：yz8023/UI2 的 46 条素材（F01–F46，与本模板 F 编号无关）在本 App 内的
 * 真实 Compose 实现目录。每条可点进详情页直接操作预览，并长按复制其提示词。
 */
@Composable
fun Ui2CatalogSection(onOpen: (Ui2Feature) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    val chipRowState1 = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }
    val chipRowState2 = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }
    val filtered = remember(query, category) {
        (Ui2Features + Ui2ExtraFeatures).filter { f ->
            (category == null || f.category == category) &&
                (query.isBlank() || f.name.contains(query) || f.summary.contains(query) || f.id.contains(query, true))
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            placeholder = { Text("搜索 UI2 素材…（如 壁纸 / F08）") }, singleLine = true,
        )
        SectionTitle(text = "分类")
        Row(
            Modifier.fillMaxWidth().horizontalScroll(chipRowState1).padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Ui2Chip("全部", category == null) { category = null }
            (Ui2Categories + listOf("玻璃拆解", "动效案例")).forEach { c -> Ui2Chip(c, category == c) { category = if (category == c) null else c } }
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(chipRowState2).padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("视频专题", "弹层馆", "动效馆", "工具").forEach { c -> Ui2Chip(c, category == c) { category = if (category == c) null else c } }
        }
        Text(
            "共 ${filtered.size} 条 · 点卡片进入真实预览，长按详情页按钮复制提示词",
            Modifier.padding(horizontal = 22.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(4.dp))
    // 卡片列表由外层 LazyColumn 承载时改为普通 Column 渲染（本段在 item 内调用）
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        filtered.forEach { f -> Ui2FeatureCard(f) { onOpen(f) } }
    }
}

@Composable
private fun Ui2Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.combinedClickable(onClick = onClick),
        shape = RoundedCornerShape(percent = 50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
    }
}

@Composable
private fun Ui2FeatureCard(feature: Ui2Feature, onClick: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp).combinedClickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 6.dp),
            ) { Text(feature.id, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelMedium) }
            Spacer(Modifier.padding(horizontal = 6.dp))
            Column(Modifier.weight(1f)) {
                Text(feature.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(feature.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            Text(feature.difficulty, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Ui2DetailScreen(feature: Ui2Feature, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    var copied by remember { mutableStateOf(false) }
    var showPrompt by remember { mutableStateOf(false) }
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("‹ 返回素材库") }
                Text("${feature.id} · ${feature.category} · ${feature.difficulty}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(feature.name, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(feature.summary, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(feature.params, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        item {
            SectionTitle(text = "功能体验（真实实现，可直接操作）")
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Box(Modifier.padding(16.dp)) { Ui2Preview(feature) }
            }
        }
        item {
            SectionTitle(text = "提示词")
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "点按查看全文，长按按钮把整段提示词复制到剪贴板，可直接粘贴给 AI 复现此功能。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box(
                        Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                            .combinedClickable(
                                onClick = { showPrompt = !showPrompt },
                                onLongClick = {
                                    clipboard.setText(AnnotatedString(feature.prompt))
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    copied = true
                                },
                            )
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (copied) "✓ 已复制，去粘贴给 AI 吧" else "长按复制提示词",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (showPrompt) {
                        Text(
                            feature.prompt,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp)).padding(12.dp),
                        )
                    }
                    Text(
                        "来源：yz8023/UI2 素材库同条目提示词；实现方案与出处见仓库 素材库/UI2体验馆/${feature.id}-*/。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
