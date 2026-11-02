package com.monkeycode.liquidui.ui.features.toolkit

import com.monkeycode.liquidui.R

import androidx.annotation.StringRes

/**
 * 工具与基建（手册 F18–F21 / 条目 U01–U04）的数据与提示词。
 *
 * U 编号独立于手册 F 编号、UI2 的 F/G/M/V/X/W/T 编号、数据查询 Q 编号、弹窗术语 P 编号；
 * 引用时写「U01」或「手册 F18」，不要混写。
 *
 * 这四条不是「界面样式」，而是**做完一个 App 迟早要写的基建**：更新检查、日志分享、
 * 文档渲染、大屏适配。实现全部在本包内，都是真实可运行的代码，不是示意图：
 * - U01 [AppUpdateChecker]：真发 HTTPS 请求打 GitHub Releases API、真下载、真调安装器
 * - U02 [AppLog]：真环形缓冲 + 真 `ACTION_SEND` 分享
 * - U03 [MarkdownParser] / [MarkdownText]：真解析真渲染（零第三方依赖）
 * - U04 [AdaptiveAppShell]：真按窗口宽度切 `NavigationBar` / `NavigationRail`
 */
data class ToolkitEntry(
    val id: String,
    @StringRes val nameRes: Int,
    val en: String,
    // 稳定英文键（筛选与 rememberSaveable 用它），文案查 ToolkitCategoryLabels
    val category: String,
    val difficulty: String,
    @StringRes val summaryRes: Int,
    @StringRes val whyRes: Int,
    @StringRes val keyImplRes: Int,
    @StringRes val pitfallsRes: Int,
    @StringRes val paramsRes: Int,
    @StringRes val promptRes: Int,
)

/** 目录页的分类筛选项（稳定英文键，顺序 = 芯片展示顺序；文案见 ToolkitCategoryLabels）。 */
val ToolkitCategories = listOf("network_release", "observability", "content_render", "layout_adapt")

/**
 * 分类键 → 文案资源。渲染：`stringResource(ToolkitCategoryLabels.getValue(key))`。
 * 用 `getValue` 不用 `[]`：键写错要当场崩，不要静默显示空白。
 */
val ToolkitCategoryLabels: Map<String, Int> = mapOf(
    "network_release" to R.string.tk_category_network_release,
    "observability" to R.string.tk_category_observability,
    "content_render" to R.string.tk_category_content_render,
    "layout_adapt" to R.string.tk_category_layout_adapt,
)

val ToolkitEntries = listOf(
    ToolkitEntry(
        id = "U01",
        nameRes = R.string.tk_u01_name,
        en = "In-app Update Check",
        category = "network_release",
        difficulty = "C ★★★",
        summaryRes = R.string.tk_u01_summary,
        whyRes = R.string.tk_u01_why,
        keyImplRes = R.string.tk_u01_key_impl,
        pitfallsRes = R.string.tk_u01_pitfalls,
        paramsRes = R.string.tk_u01_params,
        promptRes = R.string.tk_u01_prompt,
    ),

    ToolkitEntry(
        id = "U02",
        nameRes = R.string.tk_u02_name,
        en = "Log Collection & Share",
        category = "observability",
        difficulty = "C ★★☆",
        summaryRes = R.string.tk_u02_summary,
        whyRes = R.string.tk_u02_why,
        keyImplRes = R.string.tk_u02_key_impl,
        pitfallsRes = R.string.tk_u02_pitfalls,
        paramsRes = R.string.tk_u02_params,
        promptRes = R.string.tk_u02_prompt,
    ),

    ToolkitEntry(
        id = "U03",
        nameRes = R.string.tk_u03_name,
        en = "Markdown Rendering",
        category = "content_render",
        difficulty = "C ★★☆",
        summaryRes = R.string.tk_u03_summary,
        whyRes = R.string.tk_u03_why,
        keyImplRes = R.string.tk_u03_key_impl,
        pitfallsRes = R.string.tk_u03_pitfalls,
        paramsRes = R.string.tk_u03_params,
        promptRes = R.string.tk_u03_prompt,
    ),

    ToolkitEntry(
        id = "U04",
        nameRes = R.string.tk_u04_name,
        en = "Adaptive Navigation",
        category = "layout_adapt",
        difficulty = "C ★★☆",
        summaryRes = R.string.tk_u04_summary,
        whyRes = R.string.tk_u04_why,
        keyImplRes = R.string.tk_u04_key_impl,
        pitfallsRes = R.string.tk_u04_pitfalls,
        paramsRes = R.string.tk_u04_params,
        promptRes = R.string.tk_u04_prompt,
    ),

)
