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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.SectionTitle
import com.monkeycode.liquidui.ui.features.toolkit.ToolkitCategories
import com.monkeycode.liquidui.ui.features.toolkit.ToolkitCategoryLabels
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
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    val chipRowState = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }
    val filtered = remember(query, category) {
        ToolkitEntries.filter { e ->
            (category == null || e.category == category) &&
                (query.isBlank() || context.getString(e.nameRes).contains(query) || e.en.contains(query, true) ||
                    context.getString(e.summaryRes).contains(query) || e.id.contains(query, true))
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        androidx.compose.material3.OutlinedTextField(
            value = query, onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            placeholder = { Text("搜索工具…（如 更新 / 日志 / U03）") }, singleLine = true,
        )
        SectionTitle(text = stringResource(R.string.common_category))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(chipRowState).padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TkChip(stringResource(R.string.common_all), category == null) { category = null }
            ToolkitCategories.forEach { key ->
                TkChip(stringResource(ToolkitCategoryLabels.getValue(key)), category == key) {
                    category = if (category == key) null else key
                }
            }
        }
        GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.toolkit_text_1), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.toolkit_text_2) +
                        stringResource(R.string.toolkit_text_3) +
                        stringResource(R.string.toolkit_text_4),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.toolkit_text_5) +
                        stringResource(R.string.toolkit_text_6),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Text(
            stringResource(R.string.common_shared_12, filtered.size),
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
                Text(stringResource(entry.nameRes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(stringResource(entry.summaryRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
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
    val context = LocalContext.current
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
                TextButton(onClick = onBack) { Text(stringResource(R.string.common_back_to_library)) }
                Text(
                    "${entry.id} · ${stringResource(ToolkitCategoryLabels.getValue(entry.category))} · ${entry.difficulty}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(stringResource(entry.nameRes), Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(entry.en, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(entry.summaryRes), Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            SectionTitle(text = stringResource(R.string.toolkit_text_7))
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { Text(stringResource(entry.whyRes), style = MaterialTheme.typography.bodySmall) }
        }
        item {
            SectionTitle(text = stringResource(R.string.toolkit_text_8))
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { Text(stringResource(entry.keyImplRes), style = MaterialTheme.typography.bodySmall) }
        }
        item {
            SectionTitle(text = stringResource(R.string.toolkit_text_9))
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(stringResource(entry.pitfallsRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
        item {
            SectionTitle(text = stringResource(R.string.toolkit_text_10))
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { Text(stringResource(entry.paramsRes), style = MaterialTheme.typography.bodySmall) }
        }
        item {
            SectionTitle(text = stringResource(R.string.common_demo_section_hint))
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(vertical = 4.dp)) { ToolkitPreview(entry) }
            }
        }
        item {
            SectionTitle(text = stringResource(R.string.common_prompt))
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.toolkit_text_11) +
                            stringResource(R.string.toolkit_text_12),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box(
                        Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                            .combinedClickable(
                                onClick = { showPrompt = !showPrompt },
                                onLongClick = {
                                    clipboard.setText(AnnotatedString(context.getString(entry.promptRes)))
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    copied = true
                                },
                            )
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (copied) stringResource(R.string.common_copied_hint) else stringResource(R.string.common_long_press_to_copy),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (showPrompt) {
                        Text(
                            stringResource(entry.promptRes),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                        )
                    }
                    Text(
                        stringResource(R.string.toolkit_text_13) +
                            stringResource(R.string.toolkit_text_14),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
