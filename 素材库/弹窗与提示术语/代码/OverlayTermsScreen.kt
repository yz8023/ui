package com.monkeycode.liquidui.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.SectionTitle
import com.monkeycode.liquidui.ui.features.overlayterms.OverlayCategories
import com.monkeycode.liquidui.ui.features.overlayterms.OverlayTerm
import com.monkeycode.liquidui.ui.features.overlayterms.OverlayTerms
import com.monkeycode.liquidui.ui.features.overlayterms.OverlayTermsPreview

/**
 * 弹窗与提示术语（手册 F17）：12 个「突然冒出来的东西」各叫什么、什么时候用哪个、
 * 与邻居的区别，以及在 Android / Compose 里的真实可操作实现。
 * 条目 P01–P12；数据与提示词见 `ui/features/overlayterms/OverlayTermsSpec.kt`，
 * 演示见 `OverlayTermsDemo.kt`。列表与详情滚动位置均可保存，返回不回顶部。
 */
@Composable
fun OverlayTermsCatalogSection(onOpen: (OverlayTerm) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    val chipRowState = rememberSaveable(saver = androidx.compose.foundation.ScrollState.Saver) { androidx.compose.foundation.ScrollState(0) }
    val filtered = remember(query, category) {
        OverlayTerms.filter { t ->
            (category == null || t.category == category) &&
                (query.isBlank() || t.name.contains(query) || t.en.contains(query, true) || t.summary.contains(query) || t.id.contains(query, true))
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            placeholder = { Text(stringResource(R.string.overlaytermsscreen_text_1)) }, singleLine = true,
        )
        SectionTitle(text = stringResource(R.string.common_category))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(chipRowState).padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OverlayChip(stringResource(R.string.common_all), category == null) { category = null }
            OverlayCategories.forEach { c -> OverlayChip(c, category == c) { category = if (category == c) null else c } }
        }
        GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.overlaytermsscreen_text_2), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.overlaytermsscreen_text_3) +
                        stringResource(R.string.overlaytermsscreen_text_4) +
                        stringResource(R.string.overlaytermsscreen_text_5),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.overlaytermsscreen_text_6),
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
        filtered.forEach { t -> OverlayCard(t) { onOpen(t) } }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OverlayChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
private fun OverlayCard(term: OverlayTerm, onClick: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp).combinedClickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Text(term.id, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.padding(horizontal = 6.dp))
            Column(Modifier.weight(1f)) {
                Text("${term.name}（${term.en}）", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(term.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            Text(term.difficulty, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OverlayTermsDetailScreen(term: OverlayTerm, onBack: () -> Unit) {
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
                TextButton(onClick = onBack) { Text(stringResource(R.string.common_back_to_library)) }
                Text(
                    "${term.id} · ${term.category} · ${term.difficulty}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(term.name, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(term.en, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(term.summary, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(term.params, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        item {
            SectionTitle(text = stringResource(R.string.overlaytermsscreen_text_7))
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { Text(term.definition, style = MaterialTheme.typography.bodySmall) }
        }
        item {
            SectionTitle(text = stringResource(R.string.overlaytermsscreen_text_8))
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { Text(term.whenToUse, style = MaterialTheme.typography.bodySmall) }
        }
        item {
            SectionTitle(text = stringResource(R.string.overlaytermsscreen_text_9))
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { Text(term.vsNeighbor, style = MaterialTheme.typography.bodySmall) }
        }
        item {
            SectionTitle(text = stringResource(R.string.overlaytermsscreen_text_10))
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { Text(term.androidImpl, style = MaterialTheme.typography.bodySmall) }
        }
        item {
            SectionTitle(text = stringResource(R.string.common_anti_pattern))
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(term.badCase, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
        item {
            SectionTitle(text = stringResource(R.string.common_demo_section_hint))
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(vertical = 4.dp)) { OverlayTermsPreview(term) }
            }
        }
        item {
            SectionTitle(text = stringResource(R.string.common_prompt))
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.overlaytermsscreen_text_11),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box(
                        Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                            .combinedClickable(
                                onClick = { showPrompt = !showPrompt },
                                onLongClick = {
                                    clipboard.setText(AnnotatedString(term.prompt))
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
                            term.prompt,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                        )
                    }
                    Text(
                        stringResource(R.string.overlaytermsscreen_text_12),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
