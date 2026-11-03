package com.monkeycode.liquidui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape






import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.compose.ui.platform.LocalContext
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.features.adminquery.AdminQueryNorms
import com.monkeycode.liquidui.ui.features.overlayterms.OverlayTerms
import com.monkeycode.liquidui.ui.features.toolkit.ToolkitEntries
import com.monkeycode.liquidui.ui.features.ui2.Ui2ExtraFeatures
import com.monkeycode.liquidui.ui.features.ui2.Ui2Features
import com.monkeycode.liquidui.ui.components.InfoBanner
import com.monkeycode.liquidui.ui.components.SectionTitle
import com.monkeycode.liquidui.ui.components.Tag
import com.monkeycode.liquidui.ui.theme.AppIcons
import com.monkeycode.liquidui.ui.theme.AppShape

/**
 * 素材浏览器（MaterialBrowser）— 功能点目录页
 *
 * 展示素材库中每个功能模块的元数据（名称、分类、难度、依赖、文件数），
 * 支持搜索和分类筛选，点击卡片可查看接入说明。
 */
@Composable
fun MaterialBrowser(onOpenFeature: (FeatureTarget) -> Unit) {
    val context = LocalContext.current
    val materialLabel1 = stringResource(R.string.material_label_1)
    // 状态一律提到详情分支之前、并可跨组合保存：点卡片查看（UI2 详情或跳转功能页）后
    // 返回时，列表滚动位置（条目+像素偏移）、搜索词、分类筛选都停在原处，不回到顶部。
    var section by rememberSaveable { mutableStateOf(0) } // 0=模板素材 1=UI2体验馆 2=数据查询规范 3=弹窗提示术语 4=工具与基建
    // 详情选中态存 id 而非对象：可跨组合保存，返回列表后仍停在原处（见上方注释）
    var ui2SelectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var adminSelectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var overlaySelectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var toolkitSelectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategoryName by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedCategory = FeatureCategory.entries.firstOrNull { it.name == selectedCategoryName }
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val ui2Selected = ui2SelectedId?.let { id -> (Ui2Features + Ui2ExtraFeatures).firstOrNull { it.id == id } }
    if (ui2Selected != null) {
        Ui2DetailScreen(feature = ui2Selected, onBack = { ui2SelectedId = null })
        return
    }
    val adminSelected = adminSelectedId?.let { id -> AdminQueryNorms.firstOrNull { it.id == id } }
    if (adminSelected != null) {
        AdminQueryDetailScreen(norm = adminSelected, onBack = { adminSelectedId = null })
        return
    }
    val overlaySelected = overlaySelectedId?.let { id -> OverlayTerms.firstOrNull { it.id == id } }
    if (overlaySelected != null) {
        OverlayTermsDetailScreen(term = overlaySelected, onBack = { overlaySelectedId = null })
        return
    }
    val toolkitSelected = toolkitSelectedId?.let { id -> ToolkitEntries.firstOrNull { it.id == id } }
    if (toolkitSelected != null) {
        ToolkitDetailScreen(entry = toolkitSelected, onBack = { toolkitSelectedId = null })
        return
    }

    val filtered = remember(query, selectedCategory) {
        allFeatureItems().asSequence()
            .filter { feature ->
                selectedCategory == null || feature.category == selectedCategory
            }
            .filter { feature ->
                query.isBlank() ||
                    // remember{} 的谓词不是组合上下文，取文案只能用 context
                    context.getString(feature.nameRes).contains(query, ignoreCase = true) ||
                    context.getString(feature.descriptionRes).contains(query, ignoreCase = true)
            }
            .toList()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(top = 20.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.pv_nav_text_3),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.material_text_1),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(label = materialLabel1, selected = section == 0, onClick = { section = 0 })
                CategoryChip(label = stringResource(R.string.mb_section_ui2, Ui2Features.size + Ui2ExtraFeatures.size), selected = section == 1, onClick = { section = 1 })
                CategoryChip(label = stringResource(R.string.mb_section_admin_query, AdminQueryNorms.size), selected = section == 2, onClick = { section = 2 })
                CategoryChip(label = stringResource(R.string.mb_section_overlay_terms, OverlayTerms.size), selected = section == 3, onClick = { section = 3 })
                CategoryChip(label = stringResource(R.string.mb_section_toolkit, ToolkitEntries.size), selected = section == 4, onClick = { section = 4 })
            }
        }

        if (section == 1) {
            item { Ui2CatalogSection(onOpen = { ui2SelectedId = it.id }) }
            item { Spacer(Modifier.height(24.dp)) }
        }

        if (section == 2) {
            item { AdminQueryCatalogSection(onOpen = { adminSelectedId = it.id }) }
            item { Spacer(Modifier.height(24.dp)) }
        }

        if (section == 3) {
            item { OverlayTermsCatalogSection(onOpen = { overlaySelectedId = it.id }) }
            item { Spacer(Modifier.height(24.dp)) }
        }

        if (section == 4) {
            item { ToolkitCatalogSection(onOpen = { toolkitSelectedId = it.id }) }
            item { Spacer(Modifier.height(24.dp)) }
        }

        if (section == 0) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                placeholder = { Text(stringResource(R.string.material_text_2)) },
                leadingIcon = {
                    Icon(AppIcons.BlurOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                shape = AppShape.control,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.40f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
                singleLine = true
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            SectionTitle(text = stringResource(R.string.material_text_3))
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(
                    label = stringResource(R.string.common_all),
                    selected = selectedCategory == null,
                    onClick = { selectedCategoryName = null }
                )
                FeatureCategory.entries.forEach { category ->
                    CategoryChip(
                        label = context.getString(category.labelRes),
                        selected = selectedCategory == category,
                        onClick = { selectedCategoryName = if (selectedCategory == category) null else category.name }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(filtered) { feature ->
            FeatureCard(
                feature = feature,
                onClick = { onOpenFeature(feature.target) }
            )
        }
        }
    }


}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(percent = 50),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun FeatureCard(
    feature: FeatureItem,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(feature.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stringResource(feature.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Tag(
                    text = stringResource(feature.category.labelRes),
                    accent = when (feature.category) {
                        FeatureCategory.Appearance -> Color(0xFF4CAF50)
                        FeatureCategory.Motion -> Color(0xFF2196F3)
                        FeatureCategory.Navigation -> Color(0xFFFF9800)
                        FeatureCategory.Settings -> Color(0xFF9C27B0)
                        FeatureCategory.Components -> Color(0xFF00BCD4)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(feature.difficultyRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── 数据模型 ───────────────────────────────────────────────────────────────

enum class FeatureTarget { Components, Motion, Settings, Permissions, About, Onboarding }

enum class FeatureCategory(@StringRes val labelRes: Int) {
    Appearance(R.string.feature_category_appearance),
    Motion(R.string.feature_category_motion),
    Navigation(R.string.feature_category_navigation),
    Settings(R.string.feature_category_settings),
    Components(R.string.feature_category_components)
}

/**
 * 素材库目录的一条。显示字段（名字/简介/难度）是 @StringRes，文案在 strings.xml 的
 * `mb_feature_*` 与 `mb_difficulty_*`；`category` 是 enum（自带 labelRes，见阶段 5a），
 * `icon`/`target` 不是文案。
 *
 * 原先还有 `instructions`／`codeDir`／`fileCount` 三个字段，已删除：全仓 **0 处属性访问**
 * （点卡片是 `onOpenFeature(feature.target)` 直接跳真实功能页，没有详情面板），
 * 而且 `instructions` 里写的「复制 素材库/玻璃常用组件/代码/控件.kt」在批 C 把中文文件名
 * 改成英文后已经是过期路径（现在叫 `Controls.kt`）。接入步骤的权威在各
 * `素材库` 各分类目录下的 `实现方案.md`，代码里不留一份会跟着漂移的副本。
 */
data class FeatureItem(
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val category: FeatureCategory,
    @StringRes val difficultyRes: Int,
    val icon: ImageVector,
    val target: FeatureTarget,
)

/** 扫描素材库，返回所有功能模块。 */
private fun allFeatureItems(): List<FeatureItem> = listOf(
    FeatureItem(
        nameRes = R.string.mb_feature_01_name,
        descriptionRes = R.string.mb_feature_01_description,
        category = FeatureCategory.Appearance,
        difficultyRes = R.string.mb_difficulty_beginner,
        icon = AppIcons.Palette,
        target = FeatureTarget.Settings
    ),
    FeatureItem(
        nameRes = R.string.mb_feature_02_name,
        descriptionRes = R.string.mb_feature_02_description,
        category = FeatureCategory.Components,
        difficultyRes = R.string.mb_difficulty_beginner,
        icon = AppIcons.Widgets,
        target = FeatureTarget.Components
    ),
    FeatureItem(
        nameRes = R.string.mb_feature_03_name,
        descriptionRes = R.string.mb_feature_03_description,
        category = FeatureCategory.Motion,
        difficultyRes = R.string.mb_difficulty_advanced,
        icon = AppIcons.Animation,
        target = FeatureTarget.Motion
    ),
    FeatureItem(
        nameRes = R.string.mb_feature_04_name,
        descriptionRes = R.string.mb_feature_04_description,
        category = FeatureCategory.Navigation,
        difficultyRes = R.string.mb_difficulty_advanced,
        icon = AppIcons.Widgets,
        target = FeatureTarget.Components
    ),
    FeatureItem(
        nameRes = R.string.mb_feature_05_name,
        descriptionRes = R.string.mb_feature_05_description,
        category = FeatureCategory.Settings,
        difficultyRes = R.string.mb_difficulty_beginner,
        icon = AppIcons.Palette,
        target = FeatureTarget.Settings
    ),
    FeatureItem(
        nameRes = R.string.mb_feature_06_name,
        descriptionRes = R.string.mb_feature_06_description,
        category = FeatureCategory.Settings,
        difficultyRes = R.string.mb_difficulty_advanced,
        icon = AppIcons.Settings,
        target = FeatureTarget.Settings
    ),
    FeatureItem(
        nameRes = R.string.mb_feature_07_name,
        descriptionRes = R.string.mb_feature_07_description,
        category = FeatureCategory.Settings,
        difficultyRes = R.string.mb_difficulty_advanced,
        icon = AppIcons.Settings,
        target = FeatureTarget.Settings
    ),
    FeatureItem(
        nameRes = R.string.mb_feature_08_name,
        descriptionRes = R.string.mb_feature_08_description,
        category = FeatureCategory.Navigation,
        difficultyRes = R.string.mb_difficulty_beginner,
        icon = AppIcons.Speed,
        target = FeatureTarget.Onboarding
    ),
    FeatureItem(
        nameRes = R.string.mb_feature_09_name,
        descriptionRes = R.string.mb_feature_09_description,
        category = FeatureCategory.Components,
        difficultyRes = R.string.mb_difficulty_beginner,
        icon = AppIcons.Notifications,
        target = FeatureTarget.Components
    ),
    FeatureItem(
        nameRes = R.string.mb_feature_10_name,
        descriptionRes = R.string.mb_feature_10_description,
        category = FeatureCategory.Settings,
        difficultyRes = R.string.mb_difficulty_beginner,
        icon = AppIcons.Shield,
        target = FeatureTarget.Permissions
    ),
    FeatureItem(
        nameRes = R.string.mb_feature_11_name,
        descriptionRes = R.string.mb_feature_11_description,
        category = FeatureCategory.Settings,
        difficultyRes = R.string.mb_difficulty_beginner,
        icon = AppIcons.RotateRight,
        target = FeatureTarget.Settings
    ),
    FeatureItem(
        nameRes = R.string.mb_feature_12_name,
        descriptionRes = R.string.mb_feature_12_description,
        category = FeatureCategory.Settings,
        difficultyRes = R.string.mb_difficulty_beginner,
        icon = AppIcons.Shield,
        target = FeatureTarget.About
    )
)
