package com.monkeycode.liquidui.ui.features.ui2

import androidx.annotation.StringRes
import com.monkeycode.liquidui.R

/**
 * UI2 体验馆目录（46 条）：元数据与提示词移植自 yz8023/UI2（library/index.json
 * 与 library/prompts/全部提示词.md），文档原件见仓库 `素材库/UI2体验馆/`。
 * 每条的真实 Compose 预览在 previews/ 各分类文件中实现，宿主见 Ui2PreviewHost。
 * UI2 编号与本模板 F01–F15 编号是两套体系，勿混用（id 带 UI2 语义仅作来源标识）。
 *
 * 文案一律走 `strings.xml`（资源名前缀 `ui2_<id 小写>_`），本文件只剩标识符：
 *   · `nameRes` / `summaryRes` / `paramsRes` / `promptRes` = `@StringRes Int`，渲染点解析；
 *   · `category` = 稳定英文键（既是筛选键也进 `rememberSaveable`），文案查 `Ui2CategoryLabels`；
 *   · `id` / `slug` / `difficulty` 不是文案（`Ui2Preview` 按 id 分派、difficulty 是 ★ 符号）。
 * 换语言或改品牌文字都只改 XML，不用碰这里。
 */
data class Ui2Feature(
    val id: String,
    val slug: String,
    @StringRes val nameRes: Int,
    val category: String,
    val difficulty: String,
    @StringRes val summaryRes: Int,
    @StringRes val paramsRes: Int,
    @StringRes val promptRes: Int,
)

/** 主目录分类键（顺序 = 筛选芯片展示顺序）。键是稳定标识，不是显示文案。 */
val Ui2Categories = listOf("basic_components", "form_input", "navigation", "layout", "feedback", "data_display", "motion", "app_templates")

/**
 * 分类键 → 文案资源（含增补目录的 6 类）。渲染：`stringResource(Ui2CategoryLabels.getValue(key))`。
 * 用 `getValue` 而不是 `[]`：键写错要当场崩，不要静默显示空白。
 */
val Ui2CategoryLabels: Map<String, Int> = mapOf(
    "basic_components" to R.string.ui2_category_basic_components,
    "form_input" to R.string.ui2_category_form_input,
    "navigation" to R.string.ui2_category_navigation,
    "layout" to R.string.ui2_category_layout,
    "feedback" to R.string.ui2_category_feedback,
    "data_display" to R.string.ui2_category_data_display,
    "motion" to R.string.ui2_category_motion,
    "app_templates" to R.string.ui2_category_app_templates,
    "glass_breakdown" to R.string.ui2_category_glass_breakdown,
    "motion_cases" to R.string.ui2_category_motion_cases,
    "video_topics" to R.string.ui2_category_video_topics,
    "popup_gallery" to R.string.ui2_category_popup_gallery,
    "motion_gallery" to R.string.ui2_category_motion_gallery,
    "tools" to R.string.ui2_category_tools,
)

val Ui2Features: List<Ui2Feature> = listOf(

    Ui2Feature("F01", "button", R.string.ui2_f01_name, "basic_components", "★☆☆", R.string.ui2_f01_summary, R.string.ui2_f01_params, R.string.ui2_f01_prompt),
    Ui2Feature("F02", "textfield", R.string.ui2_f02_name, "basic_components", "★★☆", R.string.ui2_f02_summary, R.string.ui2_f02_params, R.string.ui2_f02_prompt),
    Ui2Feature("F03", "switch", R.string.ui2_f03_name, "basic_components", "★☆☆", R.string.ui2_f03_summary, R.string.ui2_f03_params, R.string.ui2_f03_prompt),
    Ui2Feature("F04", "slider", R.string.ui2_f04_name, "basic_components", "★★☆", R.string.ui2_f04_summary, R.string.ui2_f04_params, R.string.ui2_f04_prompt),
    Ui2Feature("F05", "checkbox-radio", R.string.ui2_f05_name, "basic_components", "★☆☆", R.string.ui2_f05_summary, R.string.ui2_f05_params, R.string.ui2_f05_prompt),
    Ui2Feature("F06", "avatar-badge", R.string.ui2_f06_name, "basic_components", "★☆☆", R.string.ui2_f06_summary, R.string.ui2_f06_params, R.string.ui2_f06_prompt),
    Ui2Feature("F07", "form-validation", R.string.ui2_f07_name, "form_input", "★★☆", R.string.ui2_f07_summary, R.string.ui2_f07_params, R.string.ui2_f07_prompt),
    Ui2Feature("F08", "stepper", R.string.ui2_f08_name, "form_input", "★☆☆", R.string.ui2_f08_summary, R.string.ui2_f08_params, R.string.ui2_f08_prompt),
    Ui2Feature("F09", "segmented", R.string.ui2_f09_name, "form_input", "★☆☆", R.string.ui2_f09_summary, R.string.ui2_f09_params, R.string.ui2_f09_prompt),
    Ui2Feature("F10", "search", R.string.ui2_f10_name, "form_input", "★★☆", R.string.ui2_f10_summary, R.string.ui2_f10_params, R.string.ui2_f10_prompt),
    Ui2Feature("F11", "upload", R.string.ui2_f11_name, "form_input", "★★☆", R.string.ui2_f11_summary, R.string.ui2_f11_params, R.string.ui2_f11_prompt),
    Ui2Feature("F12", "picker", R.string.ui2_f12_name, "form_input", "★★☆", R.string.ui2_f12_summary, R.string.ui2_f12_params, R.string.ui2_f12_prompt),
    Ui2Feature("F13", "tabbar", R.string.ui2_f13_name, "navigation", "★★☆", R.string.ui2_f13_summary, R.string.ui2_f13_params, R.string.ui2_f13_prompt),
    Ui2Feature("F14", "navbar", R.string.ui2_f14_name, "navigation", "★★☆", R.string.ui2_f14_summary, R.string.ui2_f14_params, R.string.ui2_f14_prompt),
    Ui2Feature("F15", "drawer", R.string.ui2_f15_name, "navigation", "★★☆", R.string.ui2_f15_summary, R.string.ui2_f15_params, R.string.ui2_f15_prompt),
    Ui2Feature("F16", "breadcrumb", R.string.ui2_f16_name, "navigation", "★☆☆", R.string.ui2_f16_summary, R.string.ui2_f16_params, R.string.ui2_f16_prompt),
    Ui2Feature("F17", "tabs", R.string.ui2_f17_name, "navigation", "★★☆", R.string.ui2_f17_summary, R.string.ui2_f17_params, R.string.ui2_f17_prompt),
    Ui2Feature("F18", "sidebar", R.string.ui2_f18_name, "navigation", "★★☆", R.string.ui2_f18_summary, R.string.ui2_f18_params, R.string.ui2_f18_prompt),
    Ui2Feature("F19", "bento-grid", R.string.ui2_f19_name, "layout", "★★☆", R.string.ui2_f19_summary, R.string.ui2_f19_params, R.string.ui2_f19_prompt),
    Ui2Feature("F20", "grouped-list", R.string.ui2_f20_name, "layout", "★☆☆", R.string.ui2_f20_summary, R.string.ui2_f20_params, R.string.ui2_f20_prompt),
    Ui2Feature("F21", "masonry", R.string.ui2_f21_name, "layout", "★★☆", R.string.ui2_f21_summary, R.string.ui2_f21_params, R.string.ui2_f21_prompt),
    Ui2Feature("F22", "responsive-grid", R.string.ui2_f22_name, "layout", "★★☆", R.string.ui2_f22_summary, R.string.ui2_f22_params, R.string.ui2_f22_prompt),
    Ui2Feature("F23", "sticky-stack", R.string.ui2_f23_name, "layout", "★★☆", R.string.ui2_f23_summary, R.string.ui2_f23_params, R.string.ui2_f23_prompt),
    Ui2Feature("F24", "safe-area", R.string.ui2_f24_name, "layout", "★★☆", R.string.ui2_f24_summary, R.string.ui2_f24_params, R.string.ui2_f24_prompt),
    Ui2Feature("F25", "toast", R.string.ui2_f25_name, "feedback", "★☆☆", R.string.ui2_f25_summary, R.string.ui2_f25_params, R.string.ui2_f25_prompt),
    Ui2Feature("F26", "dialog", R.string.ui2_f26_name, "feedback", "★★☆", R.string.ui2_f26_summary, R.string.ui2_f26_params, R.string.ui2_f26_prompt),
    Ui2Feature("F27", "action-sheet", R.string.ui2_f27_name, "feedback", "★★☆", R.string.ui2_f27_summary, R.string.ui2_f27_params, R.string.ui2_f27_prompt),
    Ui2Feature("F28", "skeleton", R.string.ui2_f28_name, "feedback", "★☆☆", R.string.ui2_f28_summary, R.string.ui2_f28_params, R.string.ui2_f28_prompt),
    Ui2Feature("F29", "empty-state", R.string.ui2_f29_name, "feedback", "★☆☆", R.string.ui2_f29_summary, R.string.ui2_f29_params, R.string.ui2_f29_prompt),
    Ui2Feature("F30", "progress", R.string.ui2_f30_name, "feedback", "★★☆", R.string.ui2_f30_summary, R.string.ui2_f30_params, R.string.ui2_f30_prompt),
    Ui2Feature("F31", "loading-button", R.string.ui2_f31_name, "feedback", "★☆☆", R.string.ui2_f31_summary, R.string.ui2_f31_params, R.string.ui2_f31_prompt),
    Ui2Feature("F32", "line-chart", R.string.ui2_f32_name, "data_display", "★★★", R.string.ui2_f32_summary, R.string.ui2_f32_params, R.string.ui2_f32_prompt),
    Ui2Feature("F33", "bar-chart", R.string.ui2_f33_name, "data_display", "★★☆", R.string.ui2_f33_summary, R.string.ui2_f33_params, R.string.ui2_f33_prompt),
    Ui2Feature("F34", "donut-chart", R.string.ui2_f34_name, "data_display", "★★★", R.string.ui2_f34_summary, R.string.ui2_f34_params, R.string.ui2_f34_prompt),
    Ui2Feature("F35", "stat-card", R.string.ui2_f35_name, "data_display", "★★☆", R.string.ui2_f35_summary, R.string.ui2_f35_params, R.string.ui2_f35_prompt),
    Ui2Feature("F36", "table", R.string.ui2_f36_name, "data_display", "★★☆", R.string.ui2_f36_summary, R.string.ui2_f36_params, R.string.ui2_f36_prompt),
    Ui2Feature("F37", "timeline", R.string.ui2_f37_name, "data_display", "★★☆", R.string.ui2_f37_summary, R.string.ui2_f37_params, R.string.ui2_f37_prompt),
    Ui2Feature("F38", "spring-drag", R.string.ui2_f38_name, "motion", "★★★", R.string.ui2_f38_summary, R.string.ui2_f38_params, R.string.ui2_f38_prompt),
    Ui2Feature("F39", "staggered-reveal", R.string.ui2_f39_name, "motion", "★★☆", R.string.ui2_f39_summary, R.string.ui2_f39_params, R.string.ui2_f39_prompt),
    Ui2Feature("F40", "swipe-card", R.string.ui2_f40_name, "motion", "★★★", R.string.ui2_f40_summary, R.string.ui2_f40_params, R.string.ui2_f40_prompt),
    Ui2Feature("F41", "page-transition", R.string.ui2_f41_name, "motion", "★★☆", R.string.ui2_f41_summary, R.string.ui2_f41_params, R.string.ui2_f41_prompt),
    Ui2Feature("F42", "scroll-parallax", R.string.ui2_f42_name, "motion", "★★☆", R.string.ui2_f42_summary, R.string.ui2_f42_params, R.string.ui2_f42_prompt),
    Ui2Feature("F43", "onboarding", R.string.ui2_f43_name, "app_templates", "★★☆", R.string.ui2_f43_summary, R.string.ui2_f43_params, R.string.ui2_f43_prompt),
    Ui2Feature("F44", "login", R.string.ui2_f44_name, "app_templates", "★★☆", R.string.ui2_f44_summary, R.string.ui2_f44_params, R.string.ui2_f44_prompt),
    Ui2Feature("F45", "settings", R.string.ui2_f45_name, "app_templates", "★★☆", R.string.ui2_f45_summary, R.string.ui2_f45_params, R.string.ui2_f45_prompt),
    Ui2Feature("F46", "profile", R.string.ui2_f46_name, "app_templates", "★★☆", R.string.ui2_f46_summary, R.string.ui2_f46_params, R.string.ui2_f46_prompt),
)
