package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.SectionTitle
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * 批 D 开源取材·布局两条：S03 月历与日期区间选择、S04 列表-详情双栏自适应。
 *
 * 来源申明：
 *  · S03 参考 kizitonwose/Calendar（MIT）的月视图网格、连续区间选择与 in-month/out-of-month
 *    标记口径。日期全部用 JDK `java.time` 真算（minSdk 26 起原生可用，不需要 desugaring），
 *    不引该库依赖；可选窗口 [今天-90, 今天] 与本仓库 Q02 时间筛选规范同源。
 *  · S04 参考 android/compose-samples（Apache-2.0）里 Reply／JetNews 的窗口尺寸类断点与
 *    列表-详情双栏切换。断点用 `BoxWithConstraints` 实测宽度得出，不引 window-size-class 依赖。
 * 两者都只借鉴交互模式与公开参数，未复制任何上游源码；本仓库为 MIT。
 */

/** 与 Q02 时间筛选规范同源：可回溯 90 天，结束不超过今天。 */
private const val OSS_CALENDAR_WINDOW_DAYS = 90L

/** 周日..周六。资源 id 必须逐条列，**不能拿 id 做加减**（资源 id 不保证连号）。 */
private val OssWeekdayLabels = listOf(
    R.string.pv_oss_cal_weekday_sun,
    R.string.pv_oss_cal_weekday_mon,
    R.string.pv_oss_cal_weekday_tue,
    R.string.pv_oss_cal_weekday_wed,
    R.string.pv_oss_cal_weekday_thu,
    R.string.pv_oss_cal_weekday_fri,
    R.string.pv_oss_cal_weekday_sat
)

private class OssCalendarCell(val date: LocalDate, val inMonth: Boolean)

// ─── S03 月历与日期区间选择 ─────────────────────────

@Composable
fun PreviewS03() {
    val today = remember { LocalDate.now() }
    val earliest = remember(today) { today.minusDays(OSS_CALENDAR_WINDOW_DAYS) }
    var month by remember { mutableStateOf(YearMonth.from(today)) }
    var start by remember { mutableStateOf<LocalDate?>(null) }
    var end by remember { mutableStateOf<LocalDate?>(null) }
    var mondayFirst by remember { mutableStateOf(true) }

    // 网格 = 上月尾巴（灰显不可点）+ 当月每一天 + 下月开头补齐整周
    val cells = remember(month, mondayFirst) {
        val first = month.atDay(1)
        val leadCount = if (mondayFirst) {
            (first.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
        } else {
            first.dayOfWeek.value % 7
        }
        val lead = (0 until leadCount).map { OssCalendarCell(first.minusDays((leadCount - it).toLong()), false) }
        val days = (1..month.lengthOfMonth()).map { OssCalendarCell(month.atDay(it), true) }
        val tailCount = (7 - (lead.size + days.size) % 7) % 7
        val last = month.atEndOfMonth()
        val tail = (1..tailCount).map { OssCalendarCell(last.plusDays(it.toLong()), false) }
        lead + days + tail
    }
    val headerLabels = if (mondayFirst) {
        OssWeekdayLabels.subList(1, 7) + OssWeekdayLabels.subList(0, 1)
    } else {
        OssWeekdayLabels
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_cal_title))
            Text(
                stringResource(R.string.pv_oss_cal_intro),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        GlassCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OssCalStepButton(
                    labelRes = R.string.pv_oss_cal_prev,
                    enabled = month.isAfter(YearMonth.from(earliest)),
                    onClick = { month = month.minusMonths(1) }
                )
                Text(
                    stringResource(R.string.pv_oss_cal_month, month.year, month.monthValue),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OssCalStepButton(
                    labelRes = R.string.pv_oss_cal_next,
                    enabled = month.isBefore(YearMonth.from(today)),
                    onClick = { month = month.plusMonths(1) }
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth()) {
                headerLabels.forEach { res ->
                    Text(
                        stringResource(res),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { cell ->
                        OssCalDay(
                            cell = cell,
                            today = today,
                            earliest = earliest,
                            start = start,
                            end = end,
                            onPick = { d ->
                                val s = start
                                when {
                                    // 无选择、或已有完整区间 → 重新设起点
                                    s == null || end != null -> {
                                        start = d; end = null
                                    }
                                    // 已有起点：晚于起点则设终点，否则改设起点
                                    d.isAfter(s) -> end = d
                                    else -> start = d
                                }
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Switch(checked = mondayFirst, onCheckedChange = { mondayFirst = it })
                Text(stringResource(R.string.pv_oss_cal_monday_first), fontSize = 12.sp)
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle(stringResource(R.string.pv_oss_cal_result_title))
            val s = start
            val e = end
            Text(
                when {
                    s == null -> stringResource(R.string.pv_oss_cal_result_none)
                    e == null -> stringResource(R.string.pv_oss_cal_result_start_only, s.toString())
                    else -> stringResource(
                        R.string.pv_oss_cal_result_range,
                        s.toString(), e.toString(), ChronoUnit.DAYS.between(s, e) + 1
                    )
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(
                    R.string.pv_oss_cal_window,
                    earliest.toString(), today.toString(), OSS_CALENDAR_WINDOW_DAYS.toInt()
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            OssCalStepButton(
                labelRes = R.string.pv_oss_cal_clear,
                enabled = s != null,
                onClick = { start = null; end = null }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.pv_oss_cal_rule),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OssCalDay(
    cell: OssCalendarCell,
    today: LocalDate,
    earliest: LocalDate,
    start: LocalDate?,
    end: LocalDate?,
    onPick: (LocalDate) -> Unit
) {
    val d = cell.date
    val selectable = cell.inMonth && !d.isBefore(earliest) && !d.isAfter(today)
    val isEdge = d == start || d == end
    val inRange = start != null && end != null && d.isAfter(start) && d.isBefore(end)
    val bg = when {
        isEdge -> MaterialTheme.colorScheme.primary
        inRange -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        else -> Color.Transparent
    }
    val fg = when {
        isEdge -> MaterialTheme.colorScheme.onPrimary
        !selectable -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
        !cell.inMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.34f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        Modifier
            .weight(1f)
            .padding(vertical = 2.dp)
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .then(
                if (d == today) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                else Modifier
            )
            .then(if (selectable) Modifier.clickable { onPick(d) } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            d.dayOfMonth.toString(),
            fontSize = 13.sp,
            color = fg,
            fontWeight = if (isEdge) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun OssCalStepButton(labelRes: Int, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (enabled) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            stringResource(labelRes),
            fontSize = 12.sp,
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    }
}

// ─── S04 列表-详情双栏自适应 ────────────────────────

private class OssPaneItem(val titleRes: Int, val bodyRes: Int)

private val OssPaneItems = listOf(
    OssPaneItem(R.string.pv_oss_pane_item_01_title, R.string.pv_oss_pane_item_01_body),
    OssPaneItem(R.string.pv_oss_pane_item_02_title, R.string.pv_oss_pane_item_02_body),
    OssPaneItem(R.string.pv_oss_pane_item_03_title, R.string.pv_oss_pane_item_03_body),
    OssPaneItem(R.string.pv_oss_pane_item_04_title, R.string.pv_oss_pane_item_04_body),
    OssPaneItem(R.string.pv_oss_pane_item_05_title, R.string.pv_oss_pane_item_05_body),
    OssPaneItem(R.string.pv_oss_pane_item_06_title, R.string.pv_oss_pane_item_06_body)
)

/** 断点口径与 compose-samples 的 WindowSizeClass 一致：600dp / 840dp。 */
private val OssPaneBreakpoint = 600.dp

@Composable
fun PreviewS04() {
    var selected by rememberSaveable { mutableStateOf(-1) }
    val listState = rememberLazyListState()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val widthDp = maxWidth.value.roundToInt()
        val twoPane = maxWidth >= OssPaneBreakpoint
        val classRes = when {
            maxWidth < OssPaneBreakpoint -> R.string.pv_oss_pane_class_compact
            maxWidth < 840.dp -> R.string.pv_oss_pane_class_medium
            else -> R.string.pv_oss_pane_class_expanded
        }
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            GlassCard(Modifier.fillMaxWidth()) {
                SectionTitle(stringResource(R.string.pv_oss_pane_title))
                Text(
                    stringResource(
                        R.string.pv_oss_pane_metric,
                        widthDp,
                        stringResource(classRes),
                        stringResource(
                            if (twoPane) R.string.pv_oss_pane_mode_two else R.string.pv_oss_pane_mode_one
                        )
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.pv_oss_pane_intro),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
            if (twoPane) {
                Row(Modifier.fillMaxWidth().weight(1f)) {
                    OssPaneList(
                        state = listState,
                        selected = selected,
                        onSelect = { selected = it },
                        modifier = Modifier.weight(0.4f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.weight(0.6f)) {
                        if (selected in OssPaneItems.indices) {
                            OssPaneDetail(selected, onBack = null)
                        } else {
                            OssPaneEmpty()
                        }
                    }
                }
            } else if (selected in OssPaneItems.indices) {
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    OssPaneDetail(selected, onBack = { selected = -1 })
                }
            } else {
                OssPaneList(
                    state = listState,
                    selected = selected,
                    onSelect = { selected = it },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OssPaneList(
    state: LazyListState,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier) {
        SectionTitle(stringResource(R.string.pv_oss_pane_list_title))
        // 用同一个 LazyListState：单栏进详情再返回时，滚动位置原样保留（与素材库「返回不回顶部」同一口径）
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(OssPaneItems) { i, item ->
                val on = i == selected
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (on) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                        .clickable { onSelect(i) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        stringResource(item.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Text(
                        stringResource(item.bodyRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
private fun OssPaneDetail(index: Int, onBack: (() -> Unit)?) {
    val item = OssPaneItems[index]
    GlassCard(Modifier.fillMaxSize()) {
        if (onBack != null) {
            OssCalStepButton(labelRes = R.string.pv_oss_pane_back, enabled = true, onClick = onBack)
            Spacer(Modifier.height(10.dp))
        }
        SectionTitle(stringResource(item.titleRes))
        Text(stringResource(item.bodyRes), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.pv_oss_pane_detail_index, index + 1),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.pv_oss_pane_detail_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OssPaneEmpty() {
    GlassCard(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.pv_oss_pane_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.pv_oss_pane_empty_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
