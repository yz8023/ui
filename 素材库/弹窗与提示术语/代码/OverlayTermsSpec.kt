package com.monkeycode.liquidui.ui.features.overlayterms

import com.monkeycode.liquidui.R

import androidx.annotation.StringRes

/**
 * 弹窗与提示术语（手册 F17）目录数据：12 个「突然冒出来的东西」到底各叫什么、
 * 什么时候用哪个、与邻居的区别、以及在 Android / Jetpack Compose 里的对应实现。
 *
 * P01–P06 六个术语来自 B 站 ZTough《Vibe Coding 术语课：别再管所有弹窗都叫「弹窗」了》
 * （BV14Utf6QEnB，2026-09-03，时长 04:45）视频页面公开简介点名（Alert、Toast、Modal、
 * Drawer、Popconfirm、Tooltip）。P07–P12 是本仓库按前端通用体系补足的同族术语
 * （视频页面文字未点名，标注见 素材库/弹窗与提示术语/参考来源.md）。
 * 定义、区别对照、反例与提示词全部为本仓库独立撰写，未复制视频画面/音频/字幕。
 */
data class OverlayTerm(
    val id: String,
    val en: String,
    @StringRes val nameRes: Int,
    // 稳定英文键（筛选与 rememberSaveable 用它），文案查 OverlayCategoryLabels
    val category: String,
    val difficulty: String,
    @StringRes val summaryRes: Int,
    @StringRes val definitionRes: Int,
    @StringRes val whenToUseRes: Int,
    @StringRes val vsNeighborRes: Int,
    @StringRes val androidImplRes: Int,
    @StringRes val badCaseRes: Int,
    @StringRes val paramsRes: Int,
    @StringRes val promptRes: Int,
)

/** 目录页的分类筛选项（稳定英文键，顺序 = 芯片展示顺序；文案见 OverlayCategoryLabels）。 */
val OverlayCategories = listOf("blocking", "transient", "anchored", "panel", "status")

/**
 * 分类键 → 文案资源。渲染：`stringResource(OverlayCategoryLabels.getValue(key))`。
 * 用 `getValue` 不用 `[]`：键写错要当场崩，不要静默显示空白。
 */
val OverlayCategoryLabels: Map<String, Int> = mapOf(
    "blocking" to R.string.ot_category_blocking,
    "transient" to R.string.ot_category_transient,
    "anchored" to R.string.ot_category_anchored,
    "panel" to R.string.ot_category_panel,
    "status" to R.string.ot_category_status,
)

/** 12 条术语（P 编号独立于手册 F 编号与 UI2 的 F 编号，引用时写「P05」）。 */
val OverlayTerms: List<OverlayTerm> = listOf(
    OverlayTerm(
        id = "P01",
        en = "Alert",
        nameRes = R.string.ot_p01_name,
        category = "blocking",
        difficulty = "★☆☆",
        summaryRes = R.string.ot_p01_summary,
        definitionRes = R.string.ot_p01_definition,
        whenToUseRes = R.string.ot_p01_when_to_use,
        vsNeighborRes = R.string.ot_p01_vs_neighbor,
        androidImplRes = R.string.ot_p01_android_impl,
        badCaseRes = R.string.ot_p01_bad_case,
        paramsRes = R.string.ot_p01_params,
        promptRes = R.string.ot_p01_prompt,
    ),

    OverlayTerm(
        id = "P02",
        en = "Toast",
        nameRes = R.string.ot_p02_name,
        category = "transient",
        difficulty = "★☆☆",
        summaryRes = R.string.ot_p02_summary,
        definitionRes = R.string.ot_p02_definition,
        whenToUseRes = R.string.ot_p02_when_to_use,
        vsNeighborRes = R.string.ot_p02_vs_neighbor,
        androidImplRes = R.string.ot_p02_android_impl,
        badCaseRes = R.string.ot_p02_bad_case,
        paramsRes = R.string.ot_p02_params,
        promptRes = R.string.ot_p02_prompt,
    ),

    OverlayTerm(
        id = "P03",
        en = "Modal",
        nameRes = R.string.ot_p03_name,
        category = "blocking",
        difficulty = "★★☆",
        summaryRes = R.string.ot_p03_summary,
        definitionRes = R.string.ot_p03_definition,
        whenToUseRes = R.string.ot_p03_when_to_use,
        vsNeighborRes = R.string.ot_p03_vs_neighbor,
        androidImplRes = R.string.ot_p03_android_impl,
        badCaseRes = R.string.ot_p03_bad_case,
        paramsRes = R.string.ot_p03_params,
        promptRes = R.string.ot_p03_prompt,
    ),

    OverlayTerm(
        id = "P04",
        en = "Drawer",
        nameRes = R.string.ot_p04_name,
        category = "panel",
        difficulty = "★★☆",
        summaryRes = R.string.ot_p04_summary,
        definitionRes = R.string.ot_p04_definition,
        whenToUseRes = R.string.ot_p04_when_to_use,
        vsNeighborRes = R.string.ot_p04_vs_neighbor,
        androidImplRes = R.string.ot_p04_android_impl,
        badCaseRes = R.string.ot_p04_bad_case,
        paramsRes = R.string.ot_p04_params,
        promptRes = R.string.ot_p04_prompt,
    ),

    OverlayTerm(
        id = "P05",
        en = "Popconfirm",
        nameRes = R.string.ot_p05_name,
        category = "anchored",
        difficulty = "★★☆",
        summaryRes = R.string.ot_p05_summary,
        definitionRes = R.string.ot_p05_definition,
        whenToUseRes = R.string.ot_p05_when_to_use,
        vsNeighborRes = R.string.ot_p05_vs_neighbor,
        androidImplRes = R.string.ot_p05_android_impl,
        badCaseRes = R.string.ot_p05_bad_case,
        paramsRes = R.string.ot_p05_params,
        promptRes = R.string.ot_p05_prompt,
    ),

    OverlayTerm(
        id = "P06",
        en = "Tooltip",
        nameRes = R.string.ot_p06_name,
        category = "anchored",
        difficulty = "★☆☆",
        summaryRes = R.string.ot_p06_summary,
        definitionRes = R.string.ot_p06_definition,
        whenToUseRes = R.string.ot_p06_when_to_use,
        vsNeighborRes = R.string.ot_p06_vs_neighbor,
        androidImplRes = R.string.ot_p06_android_impl,
        badCaseRes = R.string.ot_p06_bad_case,
        paramsRes = R.string.ot_p06_params,
        promptRes = R.string.ot_p06_prompt,
    ),

    OverlayTerm(
        id = "P07",
        en = "Snackbar",
        nameRes = R.string.ot_p07_name,
        category = "transient",
        difficulty = "★★☆",
        summaryRes = R.string.ot_p07_summary,
        definitionRes = R.string.ot_p07_definition,
        whenToUseRes = R.string.ot_p07_when_to_use,
        vsNeighborRes = R.string.ot_p07_vs_neighbor,
        androidImplRes = R.string.ot_p07_android_impl,
        badCaseRes = R.string.ot_p07_bad_case,
        paramsRes = R.string.ot_p07_params,
        promptRes = R.string.ot_p07_prompt,
    ),

    OverlayTerm(
        id = "P08",
        en = "Notification",
        nameRes = R.string.ot_p08_name,
        category = "transient",
        difficulty = "★★★",
        summaryRes = R.string.ot_p08_summary,
        definitionRes = R.string.ot_p08_definition,
        whenToUseRes = R.string.ot_p08_when_to_use,
        vsNeighborRes = R.string.ot_p08_vs_neighbor,
        androidImplRes = R.string.ot_p08_android_impl,
        badCaseRes = R.string.ot_p08_bad_case,
        paramsRes = R.string.ot_p08_params,
        promptRes = R.string.ot_p08_prompt,
    ),

    OverlayTerm(
        id = "P09",
        en = "Popover",
        nameRes = R.string.ot_p09_name,
        category = "anchored",
        difficulty = "★★☆",
        summaryRes = R.string.ot_p09_summary,
        definitionRes = R.string.ot_p09_definition,
        whenToUseRes = R.string.ot_p09_when_to_use,
        vsNeighborRes = R.string.ot_p09_vs_neighbor,
        androidImplRes = R.string.ot_p09_android_impl,
        badCaseRes = R.string.ot_p09_bad_case,
        paramsRes = R.string.ot_p09_params,
        promptRes = R.string.ot_p09_prompt,
    ),

    OverlayTerm(
        id = "P10",
        en = "BottomSheet / ActionSheet",
        nameRes = R.string.ot_p10_name,
        category = "panel",
        difficulty = "★★☆",
        summaryRes = R.string.ot_p10_summary,
        definitionRes = R.string.ot_p10_definition,
        whenToUseRes = R.string.ot_p10_when_to_use,
        vsNeighborRes = R.string.ot_p10_vs_neighbor,
        androidImplRes = R.string.ot_p10_android_impl,
        badCaseRes = R.string.ot_p10_bad_case,
        paramsRes = R.string.ot_p10_params,
        promptRes = R.string.ot_p10_prompt,
    ),

    OverlayTerm(
        id = "P11",
        en = "Loading / Progress",
        nameRes = R.string.ot_p11_name,
        category = "status",
        difficulty = "★★☆",
        summaryRes = R.string.ot_p11_summary,
        definitionRes = R.string.ot_p11_definition,
        whenToUseRes = R.string.ot_p11_when_to_use,
        vsNeighborRes = R.string.ot_p11_vs_neighbor,
        androidImplRes = R.string.ot_p11_android_impl,
        badCaseRes = R.string.ot_p11_bad_case,
        paramsRes = R.string.ot_p11_params,
        promptRes = R.string.ot_p11_prompt,
    ),

    OverlayTerm(
        id = "P12",
        en = "Badge / Dot",
        nameRes = R.string.ot_p12_name,
        category = "status",
        difficulty = "★☆☆",
        summaryRes = R.string.ot_p12_summary,
        definitionRes = R.string.ot_p12_definition,
        whenToUseRes = R.string.ot_p12_when_to_use,
        vsNeighborRes = R.string.ot_p12_vs_neighbor,
        androidImplRes = R.string.ot_p12_android_impl,
        badCaseRes = R.string.ot_p12_bad_case,
        paramsRes = R.string.ot_p12_params,
        promptRes = R.string.ot_p12_prompt,
    ),

)
