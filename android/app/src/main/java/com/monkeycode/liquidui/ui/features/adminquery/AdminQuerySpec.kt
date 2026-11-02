package com.monkeycode.liquidui.ui.features.adminquery

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

/** 明细行。字段名即 CSV 表头，导出时直接映射。 */
data class AdminRow(
    val date: LocalDate,
    val dept: String,
    val channel: String,
    val status: String,
    val amount: Int,
    val owner: String,
)

/** 20 个部门小组：Q03 标签聚合规则的样本量来源。 */
val AdminDepts: List<String> = (1..20).map { "第%02d组".format(it) }

val AdminChannels: List<String> = listOf("App", "小程序", "直播", "线下", "代理商")

val AdminStatuses: List<String> = listOf("已完成", "处理中", "已退款", "异常")

val AdminAmountBuckets: List<String> = listOf("全部", "< 1k", "1k–5k", "> 5k")

/** 数据保留期（天）：Q02 超限置灰的判定线。 */
const val ADMIN_RETENTION_DAYS = 90

/** 渠道 → 负责人（模拟）。 */
val AdminChannelOwners: Map<String, String> = mapOf(
    "App" to "陈曦 · 增长组",
    "小程序" to "林涛 · 平台组",
    "直播" to "赵敏 · 内容电商组",
    "线下" to "孙浩 · 渠道组",
    "代理商" to "周颖 · 商务组",
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
            owner = if (status == "异常") AdminChannelOwners[channel] ?: "值班组长" else "值班组长 · ${AdminDepts[(r / 3) % AdminDepts.size]}",
        )
    }
}

/** 时间快捷档位。days=区间天数；本季度按季度首日实算。 */
data class AdminRangePreset(val label: String, val days: Int, val quarterBased: Boolean = false)

val AdminRangePresets: List<AdminRangePreset> = listOf(
    AdminRangePreset("今日", 1),
    AdminRangePreset("昨日", 1), // 区间由 range() 特例给出：昨天单日
    AdminRangePreset("近7日", 7),
    AdminRangePreset("近30日", 30),
    AdminRangePreset("本季度", 0, quarterBased = true),
    AdminRangePreset("近一年", 365),
)

/** 档位实际起止（含首尾）。本季度 = 季度首日 ~ 今天；昨日 = 昨天单日起两天窗口取昨天。 */
fun AdminRangePreset.range(today: LocalDate): Pair<LocalDate, LocalDate> = when {
    quarterBased -> {
        val firstMonthOfQuarter = ((today.monthValue - 1) / 3) * 3 + 1
        LocalDate.of(today.year, firstMonthOfQuarter, 1) to today
    }
    label == "昨日" -> today.minusDays(1) to today.minusDays(1)
    else -> today.minusDays((days - 1).toLong()) to today
}

/** 生效条件（已提交查询的那一份）。 */
data class AdminFilter(
    val start: LocalDate,
    val end: LocalDate,
    val statuses: Set<String> = emptySet(),
    val depts: Set<String> = emptySet(),
    val channels: Set<String> = emptySet(),
    val amountBucket: String = "全部",
    val onlyAbnormal: Boolean = false,
) {
    /** 条件项数（用于「n 项待生效」计数）。 */
    val conditionCount: Int
        get() = listOf(
            statuses.isNotEmpty(), depts.isNotEmpty(), channels.isNotEmpty(),
            amountBucket != "全部", onlyAbnormal,
        ).count { it } + 1 // 时间始终算 1 项
}

/** 纯函数：按生效条件过滤明细。 */
fun filterAdminRows(rows: List<AdminRow>, f: AdminFilter): List<AdminRow> = rows.filter { r ->
    !r.date.isBefore(f.start) && !r.date.isAfter(f.end) &&
        (f.statuses.isEmpty() || r.status in f.statuses) &&
        (f.depts.isEmpty() || r.dept in f.depts) &&
        (f.channels.isEmpty() || r.channel in f.channels) &&
        (f.amountBucket == "全部" || when (f.amountBucket) {
            "< 1k" -> r.amount < 1_000
            "1k–5k" -> r.amount in 1_000..5_000
            else -> r.amount > 5_000
        }) &&
        (!f.onlyAbnormal || r.status == "异常")
}

/** 纯函数：按维度聚合金额与笔数，降序。 */
fun aggregateAdminRows(rows: List<AdminRow>, dim: String): List<Triple<String, Int, Int>> {
    val key: (AdminRow) -> String = when (dim) {
        "渠道" -> { r -> r.channel }
        "状态" -> { r -> r.status }
        else -> { r -> r.dept }
    }
    return rows.groupBy(key)
        .map { (k, list) -> Triple(k, list.sumOf { it.amount }, list.size) }
        .sortedByDescending { it.second }
}

/** 纯函数：切片 → CSV（表头与 AdminRow 字段一一对应）。 */
fun adminRowsToCsv(rows: List<AdminRow>): String = buildString {
    appendLine("date,dept,channel,status,amount,owner")
    rows.forEach { r ->
        appendLine("${r.date},${r.dept},${r.channel},${r.status},${r.amount},${r.owner}")
    }
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
