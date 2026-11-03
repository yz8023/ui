package com.monkeycode.liquidui.ui.features.adminquery

import android.content.Context
import androidx.annotation.StringRes
import com.monkeycode.liquidui.R
import java.time.LocalDate

/**
 * 后台数据查询规范（手册 F16）目录数据与模拟数据集。
 *
 * 规范条文提炼自 B 站 UP「KD一对A」《查数据查到崩溃？这套烂后台连改了四刀》
 * （BV1Vuxr6BEDp，2026-10-31，标签：B端设计 / 后台设计 / 交互设计 / 设计规范），
 * 本仓库按其公开表述独立实现为真实可操作的 Compose 演示，未复制其任何视频素材；
 * 出处与口径见 CREDITS.md 与 素材库/后台数据查询规范/参考来源.md。
 *
 * 四刀：筛选分层 + 慢查询批量提交 / 时间区间写明起止并超限置灰 /
 * 已选条件标签聚合 / 看板点柱下钻并导出明细转负责人。
 */
data class AdminQueryNorm(
    val id: String,
    @StringRes val nameRes: Int,
    // 稳定英文键（筛选与 rememberSaveable 用它），文案查 AdminQueryCategoryLabels
    val category: String,
    val difficulty: String,
    @StringRes val summaryRes: Int,
    @StringRes val ruleRes: Int,
    @StringRes val badCaseRes: Int,
    @StringRes val paramsRes: Int,
    @StringRes val promptRes: Int,
)

/** 目录页的分类筛选项（稳定英文键，顺序 = 芯片展示顺序；文案见 AdminQueryCategoryLabels）。 */
val AdminQueryCategories = listOf("filter", "time_range", "tags", "dashboard", "combined")

/**
 * 分类键 → 文案资源。渲染：`stringResource(AdminQueryCategoryLabels.getValue(key))`。
 * 用 `getValue` 不用 `[]`：键写错要当场崩，不要静默显示空白。
 */
val AdminQueryCategoryLabels: Map<String, Int> = mapOf(
    "filter" to R.string.aq_category_filter,
    "time_range" to R.string.aq_category_time_range,
    "tags" to R.string.aq_category_tags,
    "dashboard" to R.string.aq_category_dashboard,
    "combined" to R.string.aq_category_combined,
)

/** 规范条目（Q01–Q05）。Q 编号独立于手册 F 编号与 UI2 的 F 编号，引用时写「Q02」。 */
val AdminQueryNorms: List<AdminQueryNorm> = listOf(
    AdminQueryNorm(
        id = "Q01",
        nameRes = R.string.aq_q01_name,
        category = "filter",
        difficulty = "★★☆",
        summaryRes = R.string.aq_q01_summary,
        ruleRes = R.string.aq_q01_rule,
        badCaseRes = R.string.aq_q01_bad_case,
        paramsRes = R.string.aq_q01_params,
        promptRes = R.string.aq_q01_prompt,
    ),

    AdminQueryNorm(
        id = "Q02",
        nameRes = R.string.aq_q02_name,
        category = "time_range",
        difficulty = "★☆☆",
        summaryRes = R.string.aq_q02_summary,
        ruleRes = R.string.aq_q02_rule,
        badCaseRes = R.string.aq_q02_bad_case,
        paramsRes = R.string.aq_q02_params,
        promptRes = R.string.aq_q02_prompt,
    ),

    AdminQueryNorm(
        id = "Q03",
        nameRes = R.string.aq_q03_name,
        category = "tags",
        difficulty = "★★☆",
        summaryRes = R.string.aq_q03_summary,
        ruleRes = R.string.aq_q03_rule,
        badCaseRes = R.string.aq_q03_bad_case,
        paramsRes = R.string.aq_q03_params,
        promptRes = R.string.aq_q03_prompt,
    ),

    AdminQueryNorm(
        id = "Q04",
        nameRes = R.string.aq_q04_name,
        category = "dashboard",
        difficulty = "★★☆",
        summaryRes = R.string.aq_q04_summary,
        ruleRes = R.string.aq_q04_rule,
        badCaseRes = R.string.aq_q04_bad_case,
        paramsRes = R.string.aq_q04_params,
        promptRes = R.string.aq_q04_prompt,
    ),

    AdminQueryNorm(
        id = "Q05",
        nameRes = R.string.aq_q05_name,
        category = "combined",
        difficulty = "★★★",
        summaryRes = R.string.aq_q05_summary,
        ruleRes = R.string.aq_q05_rule,
        badCaseRes = R.string.aq_q05_bad_case,
        paramsRes = R.string.aq_q05_params,
        promptRes = R.string.aq_q05_prompt,
    ),

)

// ─── 模拟数据集（本地生成，不请求网络） ─────────────────────────────────────

/**
 * 明细行。字段名即 CSV 表头，导出时直接映射。
 *
 * `dept`/`channel`/`status`/`owner` 存的是**稳定英文键**而不是文案：这些值既要参与
 * 过滤与聚合的比较、又要显示给人看、还要写进 CSV。只外置显示侧会让纯函数拿到键却
 * 输出键，所以统一「数据侧只认键，边界上解析文案」——文案见 [AdminValueLabels]，
 * 组合体内用 `stringResource(AdminValueLabels.getValue(key))`，非组合上下文用
 * [adminValueLabel]（它还能拼复合负责人键）。
 */
data class AdminRow(
    val date: LocalDate,
    val dept: String,
    val channel: String,
    val status: String,
    val amount: Int,
    val owner: String,
)

/** 20 个部门小组的键：Q03 标签聚合规则的样本量来源（顺序 = 生成器取样顺序，别改）。 */
val AdminDepts: List<String> = (1..20).map { "dept_%02d".format(it) }

val AdminChannels: List<String> = listOf("ch_app", "ch_mini_program", "ch_live", "ch_offline", "ch_agent")

val AdminStatuses: List<String> = listOf("st_done", "st_processing", "st_refunded", "st_abnormal")

val AdminAmountBuckets: List<String> = listOf("amt_all", "amt_lt_1k", "amt_1k_5k", "amt_gt_5k")

/**
 * 键 → 文案资源。部门/渠道/状态/金额档/负责人合在一张表里查：键都带维度前缀
 * （`dept_`/`ch_`/`st_`/`amt_`/`own_`）所以不会撞。查表用 getValue 不用 []，
 * 键写错当场崩，好过界面上静默空白。
 */
val AdminValueLabels: Map<String, Int> = mapOf(
    "dept_01" to R.string.aq_dept_01,
    "dept_02" to R.string.aq_dept_02,
    "dept_03" to R.string.aq_dept_03,
    "dept_04" to R.string.aq_dept_04,
    "dept_05" to R.string.aq_dept_05,
    "dept_06" to R.string.aq_dept_06,
    "dept_07" to R.string.aq_dept_07,
    "dept_08" to R.string.aq_dept_08,
    "dept_09" to R.string.aq_dept_09,
    "dept_10" to R.string.aq_dept_10,
    "dept_11" to R.string.aq_dept_11,
    "dept_12" to R.string.aq_dept_12,
    "dept_13" to R.string.aq_dept_13,
    "dept_14" to R.string.aq_dept_14,
    "dept_15" to R.string.aq_dept_15,
    "dept_16" to R.string.aq_dept_16,
    "dept_17" to R.string.aq_dept_17,
    "dept_18" to R.string.aq_dept_18,
    "dept_19" to R.string.aq_dept_19,
    "dept_20" to R.string.aq_dept_20,
    "ch_app" to R.string.aq_channel_app,
    "ch_mini_program" to R.string.aq_channel_mini_program,
    "ch_live" to R.string.aq_channel_live,
    "ch_offline" to R.string.aq_channel_offline,
    "ch_agent" to R.string.aq_channel_agent,
    "st_done" to R.string.aq_status_done,
    "st_processing" to R.string.aq_status_processing,
    "st_refunded" to R.string.aq_status_refunded,
    "st_abnormal" to R.string.aq_status_abnormal,
    "amt_all" to R.string.common_all,
    "amt_lt_1k" to R.string.aq_amount_lt_1k,
    "amt_1k_5k" to R.string.aq_amount_1k_5k,
    "amt_gt_5k" to R.string.aq_amount_gt_5k,
    "own_duty" to R.string.aq_demo_text_11,
    "own_ch_xi" to R.string.aq_owner_xi,
    "own_ch_lin" to R.string.aq_owner_lin,
    "own_ch_zhao" to R.string.aq_owner_zhao,
    "own_ch_sun" to R.string.aq_owner_sun,
    "own_ch_zhou" to R.string.aq_owner_zhou,
)

/** 看板下钻的三个维度：键参与 `when (dim)` 分派与 `dim` 状态（顺序 = 芯片顺序）。 */
val AdminDims = listOf("channel", "dept", "status")

/** 维度键 → 文案（这三条文案 Demo 里本来就有资源，直接复用）。 */
val AdminDimLabels: Map<String, Int> = mapOf(
    "channel" to R.string.aq_demo_text_13,
    "dept" to R.string.aq_demo_text_14,
    "status" to R.string.aq_demo_text_15,
)

/** 数据保留期（天）：Q02 超限置灰的判定线。 */
const val ADMIN_RETENTION_DAYS = 90

/** 渠道键 → 负责人键（模拟数据的对应关系，本身不是文案）。 */
val AdminChannelOwners: Map<String, String> = mapOf(
    "ch_app" to "own_ch_xi",
    "ch_mini_program" to "own_ch_lin",
    "ch_live" to "own_ch_zhao",
    "ch_offline" to "own_ch_sun",
    "ch_agent" to "own_ch_zhou",
)

/**
 * 600 行模拟明细，按固定种子的线性同余生成：同一台机器每次进入结果一致，
 * 便于验收「命中条数」「导出行数」是否自洽。数据为本地生成，不涉及任何真实业务。
 */
val AdminRows: List<AdminRow> = run {
    val today = LocalDate.now()
    var seed = 20261031L
    fun next(): Int {
        seed = seed * 6364136223846793005L + 1442695040888963407L
        return ((seed ushr 33) % 100_000).toInt()
    }
    (0 until 600).map { i ->
        val r = next()
        val channel = AdminChannels[(r / 7) % AdminChannels.size]
        val status = AdminStatuses[(r / 13) % AdminStatuses.size]
        AdminRow(
            date = today.minusDays(((i * 7 + r) % 400).toLong()),
            dept = AdminDepts[r % AdminDepts.size],
            channel = channel,
            status = status,
            amount = 120 + (r * 37) % 9_800,
            // 复合负责人不预生成 20 条文案：存 `own_duty_of_<部门键>`，解析时拼格式资源
            owner = if (status == "st_abnormal") AdminChannelOwners[channel] ?: "own_duty"
            else "$DUTY_OF_PREFIX" + AdminDepts[(r / 3) % AdminDepts.size],
        )
    }
}

/**
 * 时间快捷档位。days=区间天数；本季度按季度首日实算。
 *
 * `key` 参与 [range] 的分派，`labelRes` 只用于显示——原先这里只有一个 `label: String`，
 * 而 `range()` 里写的是 `label == "昨日"`：**拿显示文案当分派键**，文案一外置或一换语言
 * 就会静默走到 else 分支（昨日变成「今天往前 0 天」）。判定依据必须是稳定键。
 */
data class AdminRangePreset(
    val key: String,
    @StringRes val labelRes: Int,
    val days: Int,
    val quarterBased: Boolean = false,
)

val AdminRangePresets: List<AdminRangePreset> = listOf(
    AdminRangePreset("today", R.string.aq_range_today, 1),
    AdminRangePreset("yesterday", R.string.aq_range_yesterday, 1), // 区间由 range() 特例给出：昨天单日
    AdminRangePreset("last7", R.string.aq_range_last7, 7),
    AdminRangePreset("last30", R.string.aq_range_last30, 30),
    AdminRangePreset("quarter", R.string.aq_range_quarter, 0, quarterBased = true),
    AdminRangePreset("last365", R.string.aq_range_last365, 365),
)

/** 档位实际起止（含首尾）。本季度 = 季度首日 ~ 今天；昨日 = 昨天单日起两天窗口取昨天。 */
fun AdminRangePreset.range(today: LocalDate): Pair<LocalDate, LocalDate> = when {
    quarterBased -> {
        val firstMonthOfQuarter = ((today.monthValue - 1) / 3) * 3 + 1
        LocalDate.of(today.year, firstMonthOfQuarter, 1) to today
    }
    key == "yesterday" -> today.minusDays(1) to today.minusDays(1)
    else -> today.minusDays((days - 1).toLong()) to today
}

/** 生效条件（已提交查询的那一份）。 */
data class AdminFilter(
    val start: LocalDate,
    val end: LocalDate,
    val statuses: Set<String> = emptySet(),
    val depts: Set<String> = emptySet(),
    val channels: Set<String> = emptySet(),
    val amountBucket: String = "amt_all",
    val onlyAbnormal: Boolean = false,
) {
    /** 条件项数（用于「n 项待生效」计数）。 */
    val conditionCount: Int
        get() = listOf(
            statuses.isNotEmpty(), depts.isNotEmpty(), channels.isNotEmpty(),
            amountBucket != "amt_all", onlyAbnormal,
        ).count { it } + 1 // 时间始终算 1 项
}

/** 纯函数：按生效条件过滤明细。 */
fun filterAdminRows(rows: List<AdminRow>, f: AdminFilter): List<AdminRow> = rows.filter { r ->
    !r.date.isBefore(f.start) && !r.date.isAfter(f.end) &&
        (f.statuses.isEmpty() || r.status in f.statuses) &&
        (f.depts.isEmpty() || r.dept in f.depts) &&
        (f.channels.isEmpty() || r.channel in f.channels) &&
        (f.amountBucket == "amt_all" || when (f.amountBucket) {
            "amt_lt_1k" -> r.amount < 1_000
            "amt_1k_5k" -> r.amount in 1_000..5_000
            else -> r.amount > 5_000
        }) &&
        (!f.onlyAbnormal || r.status == "st_abnormal")
}

/** 纯函数：按维度聚合金额与笔数，降序。 */
fun aggregateAdminRows(rows: List<AdminRow>, dim: String): List<Triple<String, Int, Int>> {
    // dim 是稳定键（见 AdminDims）：调用方从芯片拿到的必须是键而不是文案，
    // 否则加了第二种语言就会静默落到 else（永远按部门聚合）。
    val key: (AdminRow) -> String = when (dim) {
        "channel" -> { r -> r.channel }
        "status" -> { r -> r.status }
        else -> { r -> r.dept }
    }
    return rows.groupBy(key)
        .map { (k, list) -> Triple(k, list.sumOf { it.amount }, list.size) }
        .sortedByDescending { it.second }
}

/**
 * 纯函数：切片 → CSV（表头与 AdminRow 字段一一对应）。
 *
 * [label] 把维度键解析成文案：CSV 是给人核对的，导出的应该是「第07组」而不是 `dept_07`。
 * 故意不给默认值——默认 `{ it }` 会让漏传的调用点静默导出键。组合层传
 * `{ context.adminValueLabel(it) }` 即可（导出发生在 onClick 里，不是组合上下文）。
 */
fun adminRowsToCsv(rows: List<AdminRow>, label: (String) -> String): String = buildString {
    appendLine("date,dept,channel,status,amount,owner")
    rows.forEach { r ->
        appendLine("${r.date},${label(r.dept)},${label(r.channel)},${label(r.status)},${r.amount},${label(r.owner)}")
    }
}

/** 复合负责人键的前缀：`own_duty_of_dept_07` = 「值班组长 · 第07组」。 */
private const val DUTY_OF_PREFIX = "own_duty_of_"

/**
 * 维度键 → 文案。给非组合上下文用（`buildList`、`remember`、onClick 里的 CSV 导出）；
 * 组合体内也可以直接 `stringResource(AdminValueLabels.getValue(key))`，但复合负责人键
 * 要拼格式资源，统一走这个函数最省事。
 */
fun Context.adminValueLabel(key: String): String =
    if (key.startsWith(DUTY_OF_PREFIX)) {
        getString(R.string.aq_owner_duty_of, adminValueLabel(key.removePrefix(DUTY_OF_PREFIX)))
    } else {
        getString(AdminValueLabels.getValue(key))
    }

/** 纯函数：转交话术六要素（维度、时间、金额、笔数、占比、负责人）。 */
fun adminHandoffText(
    dim: String,
    slice: String,
    filter: AdminFilter,
    amount: Int,
    count: Int,
    sharePercent: Int,
    owner: String,
    exportedRows: Int,
): String = buildString {
    appendLine("【后台数据转交】$dim = $slice")
    appendLine("时间范围：${filter.start} ~ ${filter.end}（含首尾）")
    appendLine("金额合计：¥$amount｜笔数：$count｜占全量：$sharePercent%")
    appendLine("负责人：$owner")
    appendLine("明细：已导出 $exportedRows 行 CSV，可直接核对")
    append("请确认口径后回复处理结论；如需扩大范围请在同一时间口径下追加。")
}
