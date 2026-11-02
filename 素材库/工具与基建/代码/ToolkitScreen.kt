package com.monkeycode.liquidui.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.monkeycode.liquidui.ui.features.toolkit.ToolkitCategories
import com.monkeycode.liquidui.ui.features.toolkit.ToolkitEntries
import com.monkeycode.liquidui.ui.features.toolkit.ToolkitEntry
import com.monkeycode.liquidui.ui.features.toolkit.ToolkitPreview

/**
 * 工具与基建（手册 F18–F21，条目 U01–U04）：更新检查、日志分享、Markdown 渲染、自适应导航。
 * 数据与提示词见 `ui/features/toolkit/ToolkitSpec.kt`，演示见 `ToolkitDemo.kt`，
 * 真实实现见同包的 `AppUpdateChecker.kt` / `AppLog.kt` / `MarkdownText.kt` / `AdaptiveNav.kt`。
 * 列表与详情滚动位置均可保存，返回不回顶部。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolkitCatalogSection(onOpen: (ToolkitEntry) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    val chipRowState = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }
    val filtered = remember(query, category) {
        ToolkitEntries.filter { e ->
            (category == null || e.category == category) &&
                (query.isBlank() || e.name.contains(query) || e.en.contains(query, true) ||
                    e.summary.contains(query) || e.id.contains(query, true))
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        androidx.compose.material3.OutlinedTextField(
            value = query, onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            placeholder = { Text("搜索工具…（如 更新 / 日志 / U03）") }, singleLine = true,
        )
        SectionTitle(text = "分类")
        Row(
            Modifier.fillMaxWidth().horizontalScroll(chipRowState).padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TkChip("全部", category == null) { category = null }
            ToolkitCategories.forEach { c -> TkChip(c, category == c) { category = if (category == c) null else c } }
        }
        GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("这四条是「基建」，不是样式", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    "更新检查、日志分享、Markdown 渲染、大屏适配——做完一个 App 迟早都要写，" +
                        "而且写法一旦错了就是线上事故（拿不到日志、装不上更新、大屏布局塌）。" +
                        "四条都是真实可运行代码：真的发请求、真的下载、真的分享、真的切布局，不是示意图。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "示例 App 自己也用上了：设置页有真实的「检查更新」与「运行日志」，" +
                        "更新检查打的就是本模板仓库的 GitHub Releases。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Text(
            "共 ${filtered.size} 条 · 点卡片进入可操作演示，长按详情页按钮复制提示词",
            Modifier.padding(horizontal = 22.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(4.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        filtered.forEach { e -> ToolkitCard(e) { onOpen(e) } }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TkChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.combinedClickable(onClick = onClick),
        shape = RoundedCornerShape(percent = 50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ToolkitCard(entry: ToolkitEntry, onClick: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp).combinedClickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Text(entry.id, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.padding(horizontal = 6.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(entry.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            Text(entry.difficulty, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolkitDetailScreen(entry: ToolkitEntry, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    var copied by remember { mutableStateOf(false) }
    var showPrompt by remember { mutableStateOf(false) }
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("‹ 返回素材库") }
                Text(
                    "${entry.id} · ${entry.category} · ${entry.difficulty}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(entry.name, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(entry.en, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(entry.summary, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            SectionTitle(text = "为什么需要")
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { Text(entry.why, style = MaterialTheme.typography.bodySmall) }
        }
        item {
            SectionTitle(text = "关键实现（照抄要点）")
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { Text(entry.keyImpl, style = MaterialTheme.typography.bodySmall) }
        }
        item {
            SectionTitle(text = "坑与反例")
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(entry.pitfalls, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
        item {
            SectionTitle(text = "参数与默认值")
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { Text(entry.params, style = MaterialTheme.typography.bodySmall) }
        }
        item {
            SectionTitle(text = "功能体验（真实实现，可直接操作）")
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(vertical = 4.dp)) { ToolkitPreview(entry) }
            }
        }
        item {
            SectionTitle(text = "提示词")
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "点按查看全文，长按按钮把整段提示词复制到剪贴板。整段可以直接发给 AI，" +
                            "里面的接口名、超时值、权限项、失败分支都已写死，不用你再补细节。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box(
                        Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                            .combinedClickable(
                                onClick = { showPrompt = !showPrompt },
                                onLongClick = {
                                    clipboard.setText(AnnotatedString(entry.prompt))
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
                            entry.prompt,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                        )
                    }
                    Text(
                        "实现全部在本仓库 `ui/features/toolkit/` 内（AppUpdateChecker / AppLog / MarkdownText / AdaptiveNav），" +
                            "零第三方依赖；接入与替换说明见 素材库/工具与基建/实现方案.md。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
