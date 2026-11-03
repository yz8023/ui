package com.monkeycode.liquidui.ui.features.ui2

import com.monkeycode.liquidui.R

/**
 * UI2 体验馆增补目录：46 条之外的内容（22 个交互拆解、XPopup 弹层馆、
 * motion-web 动效馆、结构生成器与并排对比），同样逐条真实 Compose 实现。
 * 系列前缀：G=玻璃拆解 M=动效案例 V=视频专题 X=弹层馆 W=动效馆 T=工具。
 * 字段口径与 `Ui2Feature`（见 Ui2Catalog.kt）一致：文案走 `strings.xml`，
 * `category` 是稳定英文键、文案查 `Ui2CategoryLabels`。
 */

/** 增补目录里进筛选芯片的分类键（玻璃拆解、动效案例，加批 D 的开源素材；其余只在详情页显示）。 */
val Ui2ExtraCategories = listOf("glass_breakdown", "motion_cases", "oss_kit")

val Ui2ExtraFeatures: List<Ui2Feature> = listOf(

    Ui2Feature("G01", "g01", R.string.ui2_g01_name, "glass_breakdown", "★★☆", R.string.ui2_g01_summary, R.string.ui2_g01_params, R.string.ui2_g01_prompt),
    Ui2Feature("G02", "g02", R.string.ui2_g02_name, "glass_breakdown", "★★☆", R.string.ui2_g02_summary, R.string.ui2_g02_params, R.string.ui2_g02_prompt),
    Ui2Feature("G03", "g03", R.string.ui2_g03_name, "glass_breakdown", "★★☆", R.string.ui2_g03_summary, R.string.ui2_g03_params, R.string.ui2_g03_prompt),
    Ui2Feature("G04", "g04", R.string.ui2_g04_name, "glass_breakdown", "★★☆", R.string.ui2_g04_summary, R.string.ui2_g04_params, R.string.ui2_g04_prompt),
    Ui2Feature("G05", "g05", R.string.ui2_g05_name, "glass_breakdown", "★★☆", R.string.ui2_g05_summary, R.string.ui2_g05_params, R.string.ui2_g05_prompt),
    Ui2Feature("G06", "g06", R.string.ui2_g06_name, "glass_breakdown", "★☆☆", R.string.ui2_g06_summary, R.string.ui2_g06_params, R.string.ui2_g06_prompt),
    Ui2Feature("M01", "m01", R.string.ui2_m01_name, "motion_cases", "★☆☆", R.string.ui2_m01_summary, R.string.ui2_m01_params, R.string.ui2_m01_prompt),
    Ui2Feature("M02", "m02", R.string.ui2_m02_name, "motion_cases", "★★☆", R.string.ui2_m02_summary, R.string.ui2_m02_params, R.string.ui2_m02_prompt),
    Ui2Feature("M03", "m03", R.string.ui2_m03_name, "motion_cases", "★★☆", R.string.ui2_m03_summary, R.string.ui2_m03_params, R.string.ui2_m03_prompt),
    Ui2Feature("M04", "m04", R.string.ui2_m04_name, "motion_cases", "★★☆", R.string.ui2_m04_summary, R.string.ui2_m04_params, R.string.ui2_m04_prompt),
    Ui2Feature("M05", "m05", R.string.ui2_m05_name, "motion_cases", "★☆☆", R.string.ui2_m05_summary, R.string.ui2_m05_params, R.string.ui2_m05_prompt),
    Ui2Feature("M06", "m06", R.string.ui2_m06_name, "motion_cases", "★★☆", R.string.ui2_m06_summary, R.string.ui2_m06_params, R.string.ui2_m06_prompt),
    Ui2Feature("M07", "m07", R.string.ui2_m07_name, "motion_cases", "★★☆", R.string.ui2_m07_summary, R.string.ui2_m07_params, R.string.ui2_m07_prompt),
    Ui2Feature("V01", "v01", R.string.ui2_v01_name, "video_topics", "★☆☆", R.string.ui2_v01_summary, R.string.ui2_v01_params, R.string.ui2_v01_prompt),
    Ui2Feature("V02", "v02", R.string.ui2_v02_name, "video_topics", "★★☆", R.string.ui2_v02_summary, R.string.ui2_v02_params, R.string.ui2_v02_prompt),
    Ui2Feature("V03", "v03", R.string.ui2_v03_name, "video_topics", "★★☆", R.string.ui2_v03_summary, R.string.ui2_v03_params, R.string.ui2_v03_prompt),
    Ui2Feature("V04", "v04", R.string.ui2_v04_name, "video_topics", "★★☆", R.string.ui2_v04_summary, R.string.ui2_v04_params, R.string.ui2_v04_prompt),
    Ui2Feature("V05", "v05", R.string.ui2_v05_name, "video_topics", "★☆☆", R.string.ui2_v05_summary, R.string.ui2_v05_params, R.string.ui2_v05_prompt),
    Ui2Feature("V06", "v06", R.string.ui2_v06_name, "video_topics", "★☆☆", R.string.ui2_v06_summary, R.string.ui2_v06_params, R.string.ui2_v06_prompt),
    Ui2Feature("V07", "v07", R.string.ui2_v07_name, "video_topics", "★★☆", R.string.ui2_v07_summary, R.string.ui2_v07_params, R.string.ui2_v07_prompt),
    Ui2Feature("V08", "v08", R.string.ui2_v08_name, "video_topics", "★★☆", R.string.ui2_v08_summary, R.string.ui2_v08_params, R.string.ui2_v08_prompt),
    Ui2Feature("V09", "v09", R.string.ui2_v09_name, "video_topics", "★★☆", R.string.ui2_v09_summary, R.string.ui2_v09_params, R.string.ui2_v09_prompt),
    Ui2Feature("X01", "x01", R.string.ui2_x01_name, "popup_gallery", "★★☆", R.string.ui2_x01_summary, R.string.ui2_x01_params, R.string.ui2_x01_prompt),
    Ui2Feature("X02", "x02", R.string.ui2_x02_name, "popup_gallery", "★☆☆", R.string.ui2_x02_summary, R.string.ui2_x02_params, R.string.ui2_x02_prompt),
    Ui2Feature("X03", "x03", R.string.ui2_x03_name, "popup_gallery", "★★☆", R.string.ui2_x03_summary, R.string.ui2_x03_params, R.string.ui2_x03_prompt),
    Ui2Feature("X04", "x04", R.string.ui2_x04_name, "popup_gallery", "★★☆", R.string.ui2_x04_summary, R.string.ui2_x04_params, R.string.ui2_x04_prompt),
    Ui2Feature("W01", "w01", R.string.ui2_w01_name, "motion_gallery", "★★☆", R.string.ui2_w01_summary, R.string.ui2_w01_params, R.string.ui2_w01_prompt),
    Ui2Feature("W02", "w02", R.string.ui2_w02_name, "motion_gallery", "★★☆", R.string.ui2_w02_summary, R.string.ui2_w02_params, R.string.ui2_w02_prompt),
    Ui2Feature("W03", "w03", R.string.ui2_w03_name, "motion_gallery", "★☆☆", R.string.ui2_w03_summary, R.string.ui2_w03_params, R.string.ui2_w03_prompt),
    Ui2Feature("T01", "t01", R.string.ui2_t01_name, "tools", "★★☆", R.string.ui2_t01_summary, R.string.ui2_t01_params, R.string.ui2_t01_prompt),
    Ui2Feature("T02", "t02", R.string.ui2_t02_name, "tools", "★☆☆", R.string.ui2_t02_summary, R.string.ui2_t02_params, R.string.ui2_t02_prompt),

    // ─── 批 D：开源取材八件套（S01–S08），来源见 InfoScreens 的开源致谢 14–21 ───
    Ui2Feature("S01", "s01", R.string.ui2_s01_name, "oss_kit", "★★☆", R.string.ui2_s01_summary, R.string.ui2_s01_params, R.string.ui2_s01_prompt),
    Ui2Feature("S02", "s02", R.string.ui2_s02_name, "oss_kit", "★★☆", R.string.ui2_s02_summary, R.string.ui2_s02_params, R.string.ui2_s02_prompt),
    Ui2Feature("S03", "s03", R.string.ui2_s03_name, "oss_kit", "★★★", R.string.ui2_s03_summary, R.string.ui2_s03_params, R.string.ui2_s03_prompt),
    Ui2Feature("S04", "s04", R.string.ui2_s04_name, "oss_kit", "★★☆", R.string.ui2_s04_summary, R.string.ui2_s04_params, R.string.ui2_s04_prompt),
    Ui2Feature("S05", "s05", R.string.ui2_s05_name, "oss_kit", "★★★", R.string.ui2_s05_summary, R.string.ui2_s05_params, R.string.ui2_s05_prompt),
    Ui2Feature("S06", "s06", R.string.ui2_s06_name, "oss_kit", "★★★", R.string.ui2_s06_summary, R.string.ui2_s06_params, R.string.ui2_s06_prompt),
    Ui2Feature("S07", "s07", R.string.ui2_s07_name, "oss_kit", "★★☆", R.string.ui2_s07_summary, R.string.ui2_s07_params, R.string.ui2_s07_prompt),
    Ui2Feature("S08", "s08", R.string.ui2_s08_name, "oss_kit", "★★★", R.string.ui2_s08_summary, R.string.ui2_s08_params, R.string.ui2_s08_prompt)
)
