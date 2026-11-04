package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.monkeycode.liquidui.R
import kotlin.math.abs

/**
 * 批 F 开源取材 S17–S18：字阶·形状阶·间距对照表 / 内存泄漏与引用链自检。
 *
 * S17 依据 material-components/material-web（Apache-2.0）公布的 Material 3 字号、行高、
 * 字距与形状阶数值，逐档与本仓库 ui/theme/Type.kt、ui/theme/OShinTheme.kt 的**实际生效值**对照，
 * 并附全仓 dp/sp 用量实测（2026-11-04）。
 * S18 依据 square/leakcanary（Apache-2.0）的引用链读法，把本仓库构建期人工核实的泄漏模式清单
 * 做成可视化：**重点是粗 grep 的误报率**，运行时自检本身如实降级为「检测方法教学」。
 *
 * 不引任何依赖：M3 数值按公开规范抄录，本仓库数值靠脚本实测，全部标了取数命令与日期。
 */

private val AUDIT_TABS = listOf("type", "shape", "spacing")

private val AUDIT_TAB_LABEL = mapOf(
    "type" to R.string.pv_oss_spec_tab_type,
    "shape" to R.string.pv_oss_spec_tab_shape,
    "spacing" to R.string.pv_oss_spec_tab_spacing,
)

private const val AUDIT_DATE = "2026-11-04"

/** 批 D 落地后对「长生命周期对象持 Context」这一行的复测日期。 */
private const val AUDIT_DATE_D = "2026-11-05"

// ==================== S17 字阶·形状阶·间距对照表 ====================

/** M3 参考值按 material-web 公开规范抄录；live 取 MaterialTheme.typography 的实际生效值。 */
private class TypeSlot(
    val name: String,
    val live: TextStyle,
    val refSize: Float,
    val refLine: Float,
    val refTracking: Float,
    val refWeight: Int,
)

private class ShapeSlot(val name: String, val refDp: Int, val repoDp: Int)

private class SpaceSlot(val dp: Int, val count: Int)

// 全仓 dp 用量 TOP12，取数命令见页面底部
private val SPACE_STATS = listOf(
    SpaceSlot(8, 195), SpaceSlot(10, 174), SpaceSlot(12, 156), SpaceSlot(16, 136),
    SpaceSlot(6, 115), SpaceSlot(14, 96), SpaceSlot(4, 84), SpaceSlot(22, 50),
    SpaceSlot(24, 42), SpaceSlot(20, 33), SpaceSlot(2, 33), SpaceSlot(1, 21),
)

// 绕过字阶的硬编码字号（sp），取数命令见页面底部
private val SP_STATS = listOf(
    SpaceSlot(12, 30), SpaceSlot(11, 14), SpaceSlot(9, 6),
    SpaceSlot(10, 6), SpaceSlot(18, 5), SpaceSlot(16, 5),
)

// refDp：-1 = M3 的 full 档（50%，即圆形）；repoDp：-2 = 本仓库未定义该档
private val SHAPE_SLOTS = listOf(
    ShapeSlot("none", 0, -2),
    ShapeSlot("extraSmall", 4, 8),
    ShapeSlot("small", 8, 12),
    ShapeSlot("medium", 12, 16),
    ShapeSlot("large", 16, 16),
    ShapeSlot("extraLarge", 28, 24),
    ShapeSlot("full", -1, -2),
)

private fun fmt1(v: Float): String = String.format(java.util.Locale.US, "%.1f", v)

private fun fmt2(v: Float): String = String.format(java.util.Locale.US, "%+.2f", v)

@Composable
fun PreviewS17() {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val typo = MaterialTheme.typography

    var tab by rememberSaveable { mutableStateOf("type") }
    var onlyTodo by rememberSaveable { mutableStateOf(false) }

    val slots = listOf(
        TypeSlot("displayLarge", typo.displayLarge, 57f, 64f, -0.25f, 400),
        TypeSlot("displayMedium", typo.displayMedium, 45f, 52f, 0f, 400),
        TypeSlot("displaySmall", typo.displaySmall, 36f, 44f, 0f, 400),
        TypeSlot("headlineLarge", typo.headlineLarge, 32f, 40f, 0f, 400),
        TypeSlot("headlineMedium", typo.headlineMedium, 28f, 36f, 0f, 400),
        TypeSlot("headlineSmall", typo.headlineSmall, 24f, 32f, 0f, 400),
        TypeSlot("titleLarge", typo.titleLarge, 22f, 28f, 0f, 400),
        TypeSlot("titleMedium", typo.titleMedium, 16f, 24f, 0.15f, 500),
        TypeSlot("titleSmall", typo.titleSmall, 14f, 20f, 0.1f, 500),
        TypeSlot("bodyLarge", typo.bodyLarge, 16f, 24f, 0.5f, 400),
        TypeSlot("bodyMedium", typo.bodyMedium, 14f, 20f, 0.25f, 400),
        TypeSlot("bodySmall", typo.bodySmall, 12f, 16f, 0.4f, 400),
        TypeSlot("labelLarge", typo.labelLarge, 14f, 20f, 0.1f, 500),
        TypeSlot("labelMedium", typo.labelMedium, 12f, 16f, 0.5f, 500),
        TypeSlot("labelSmall", typo.labelSmall, 11f, 16f, 0.5f, 500),
    )
    val diffCount = slots.count { slotOffSpec(it) }
    val undefinedCount = slots.size - diffCount

    val pill = RoundedCornerShape(percent = 50)
    val card = RoundedCornerShape(10.dp)
    val barColor = scheme.primary
    val offGridColor = scheme.error
    val okColor = scheme.secondary

    Column(
        Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AUDIT_TABS.forEach { key ->
                val on = key == tab
                Text(
                    context.getString(AUDIT_TAB_LABEL.getValue(key)),
                    Modifier.clip(pill).background(if (on) scheme.primary else scheme.surfaceVariant)
                        .clickable { tab = key }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (on) scheme.onPrimary else scheme.onSurfaceVariant,
                )
            }
            Text(
                if (onlyTodo) stringResource(R.string.pv_oss_spec_filter_todo_on)
                else stringResource(R.string.pv_oss_spec_filter_todo_off),
                Modifier.clip(pill)
                    .background(if (onlyTodo) scheme.tertiaryContainer else scheme.surfaceVariant)
                    .clickable { onlyTodo = !onlyTodo }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = if (onlyTodo) scheme.onTertiaryContainer else scheme.onSurfaceVariant,
            )
        }

        when (tab) {
            "type" -> {
                Text(
                    stringResource(R.string.pv_oss_spec_type_intro, AUDIT_DATE, diffCount, undefinedCount, slots.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
                slots.forEach { slot ->
                    val differs = slotOffSpec(slot)
                    if (onlyTodo && !differs) return@forEach
                    val liveSize = slot.live.fontSize.value
                    val liveLine = slot.live.lineHeight.value
                    val liveTrack = slot.live.letterSpacing.value
                    val liveWeight = slot.live.fontWeight?.weight ?: 400
                    // 分支上提成局部 val：内联 if/else 当实参会被 i18n 探针误判成假属性
                    val definedText = if (differs) {
                        stringResource(R.string.pv_oss_spec_defined)
                    } else {
                        stringResource(R.string.pv_oss_spec_undefined)
                    }
                    Column(
                        Modifier.fillMaxWidth().clip(card)
                            .background(if (differs) scheme.errorContainer else scheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(slot.name, style = MaterialTheme.typography.labelLarge, color = scheme.onSurface)
                            Text(
                                if (differs) stringResource(R.string.pv_oss_spec_diff)
                                else stringResource(R.string.pv_oss_spec_same),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (differs) scheme.onErrorContainer else scheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            stringResource(
                                R.string.pv_oss_spec_type_now,
                                fmt1(liveSize), fmt1(liveLine), fmt2(liveTrack), liveWeight, definedText,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (differs) scheme.onErrorContainer else scheme.onSurfaceVariant,
                        )
                        Text(
                            stringResource(
                                R.string.pv_oss_spec_type_ref,
                                fmt1(slot.refSize), fmt1(slot.refLine), fmt2(slot.refTracking), slot.refWeight,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (differs) scheme.onErrorContainer else scheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    stringResource(R.string.pv_oss_spec_type_cmd),
                    Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }

            "shape" -> {
                Text(
                    stringResource(R.string.pv_oss_spec_shape_intro),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
                SHAPE_SLOTS.forEach { s ->
                    val differs = s.repoDp != s.refDp
                    if (onlyTodo && !differs) return@forEach
                    // 同上：两处分支各上提一个 val
                    val refText = if (s.refDp >= 0) "${s.refDp}dp" else stringResource(R.string.pv_oss_spec_shape_full)
                    val repoText = if (s.repoDp >= 0) "${s.repoDp}dp" else stringResource(R.string.pv_oss_spec_shape_none)
                    Row(
                        Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // 本仓库档位的真实圆角；未定义（-1）时用虚线框表示
                        val radius = if (s.repoDp >= 0) s.repoDp.dp else 0.dp
                        Box(
                            Modifier.size(40.dp)
                                .clip(RoundedCornerShape(radius))
                                .background(if (s.repoDp >= 0) barColor else Color.Transparent)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(s.name, style = MaterialTheme.typography.labelLarge, color = scheme.onSurface)
                            Text(
                                stringResource(R.string.pv_oss_spec_shape_vals, refText, repoText),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (differs) offGridColor else scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Text(
                    stringResource(R.string.pv_oss_spec_shape_pitfall),
                    Modifier.fillMaxWidth().clip(card).background(scheme.errorContainer).padding(12.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onErrorContainer,
                )
            }

            else -> {
                Text(
                    stringResource(R.string.pv_oss_spec_space_intro, AUDIT_DATE),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
                val maxCount = SPACE_STATS.maxOf { it.count }
                SPACE_STATS.forEach { s ->
                    val onGrid = s.dp % 4 == 0
                    if (onlyTodo && onGrid) return@forEach
                    Column(
                        Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                stringResource(R.string.pv_oss_spec_space_row, s.dp, s.count),
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.onSurface,
                            )
                            Text(
                                if (onGrid) stringResource(R.string.pv_oss_spec_on_grid)
                                else stringResource(R.string.pv_oss_spec_off_grid),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (onGrid) okColor else offGridColor,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(Modifier.fillMaxWidth().height(6.dp).clip(pill).background(scheme.surface)) {
                            Box(
                                Modifier
                                    .fillMaxWidth(s.count.toFloat() / maxCount.toFloat())
                                    .height(6.dp)
                                    .clip(pill)
                                    .background(if (onGrid) barColor else offGridColor)
                            )
                        }
                    }
                }
                Text(
                    stringResource(R.string.pv_oss_spec_space_verdict),
                    Modifier.fillMaxWidth().clip(card).background(scheme.errorContainer).padding(12.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onErrorContainer,
                )
                Text(
                    stringResource(R.string.pv_oss_spec_sp_intro),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SP_STATS.forEach { s ->
                        Text(
                            stringResource(R.string.pv_oss_spec_space_row, s.dp, s.count),
                            Modifier.clip(pill).background(scheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    stringResource(R.string.pv_oss_spec_space_cmd),
                    Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(0.dp))
            }
        }
    }
}

/**
 * 与 M3 建议值不符，即说明本仓库改过这一档。未定义的档位 MaterialTheme 会原样给出 M3 默认值，
 * 恰好等于建议值，所以这一个判定同时回答「本仓库定义了吗」与「符合规范吗」——
 * 本仓库 Type.kt 定义了 15 档里的 13 档，缺 headlineLarge 与 titleSmall，实测就是 13 不符 + 2 未定义。
 */
private fun slotOffSpec(slot: TypeSlot): Boolean =
    abs(slot.live.fontSize.value - slot.refSize) > 0.01f ||
        abs(slot.live.lineHeight.value - slot.refLine) > 0.01f ||
        abs(slot.live.letterSpacing.value - slot.refTracking) > 0.01f ||
        (slot.live.fontWeight?.weight ?: 400) != slot.refWeight

// ==================== S18 内存泄漏与引用链自检 ====================

private class LeakRow(
    val labelRes: Int,
    val crude: Int,
    val real: Int,
    val verdictRes: Int,
    /** 0 = 无批注。只有被后续批次改动过、需要说明两次实测差异的行才填。 */
    val noteRes: Int = 0,
)

/**
 * 2026-11-04 构建期人工核实；数字取自 grep -rn + 逐条眼看，命令见页面底部。
 * 「长生命周期对象持 Context」这一行在 2026-11-05（批 D 收窄构造参数之后）复测过；
 * Handler／Timer 那一行的粗命中数也因本批新增文件从 9 变 10（第 10 处是 RouteStack.kt 的 KDoc
 * 提到 BackHandler，仍被 grep -v BackHandler 滤掉，真实数还是 0）。两次数字都保留、差异写在批注里——
 * 这一页的论点本来就是「数字随代码变动，别照抄」。
 */
private val LEAK_ROWS = listOf(
    LeakRow(R.string.pv_oss_leak_row_globalscope, 0, 0, R.string.pv_oss_leak_clean),
    LeakRow(R.string.pv_oss_leak_row_staticctx, 0, 0, R.string.pv_oss_leak_clean),
    LeakRow(
        R.string.pv_oss_leak_row_holdctx,
        1,
        0,
        R.string.pv_oss_leak_fixed_in_d,
        R.string.pv_oss_leak_row_holdctx_note,
    ),
    LeakRow(R.string.pv_oss_leak_row_register, 0, 0, R.string.pv_oss_leak_clean),
    LeakRow(R.string.pv_oss_leak_row_whiletrue, 7, 0, R.string.pv_oss_leak_cancellable),
    LeakRow(R.string.pv_oss_leak_row_bitmap, 0, 0, R.string.pv_oss_leak_clean),
    LeakRow(R.string.pv_oss_leak_row_webview, 0, 0, R.string.pv_oss_leak_clean),
    LeakRow(R.string.pv_oss_leak_row_scope, 17, 0, R.string.pv_oss_leak_false_alarm),
    LeakRow(R.string.pv_oss_leak_row_handler, 10, 0, R.string.pv_oss_leak_false_alarm),
    LeakRow(R.string.pv_oss_leak_row_service, 3, 3, R.string.pv_oss_leak_normal),
)

private val CHAIN_NODES = listOf(
    R.string.pv_oss_leak_chain_node1,
    R.string.pv_oss_leak_chain_node2,
    R.string.pv_oss_leak_chain_node3,
    R.string.pv_oss_leak_chain_node4,
)

@Composable
fun PreviewS18() {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current

    var showCmd by rememberSaveable { mutableStateOf(false) }

    val card = RoundedCornerShape(10.dp)
    val pill = RoundedCornerShape(percent = 50)
    val nodeColor = scheme.primaryContainer
    val riskColor = scheme.errorContainer
    // 第 2 环在批 D 已经改完，所以它不再用 errorContainer 报警，改用 tertiaryContainer 标「已修」
    val fixedColor = scheme.tertiaryContainer

    Column(
        Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            stringResource(R.string.pv_oss_leak_intro, AUDIT_DATE, AUDIT_DATE_D),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )

        // ---- 引用链：按 LeakCanary 的读法，节点从上往下就是「谁被谁拽着不放」 ----
        Text(stringResource(R.string.pv_oss_leak_chain_title), style = MaterialTheme.typography.labelLarge, color = scheme.onSurface)
        CHAIN_NODES.forEachIndexed { i, res ->
            Column(
                Modifier.fillMaxWidth().clip(card)
                    .background(if (i == 1) fixedColor else nodeColor)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
            ) {
                val ringFixed = i == 1
                Text(
                    if (ringFixed) stringResource(R.string.pv_oss_leak_chain_step_fixed, i + 1, AUDIT_DATE_D)
                    else stringResource(R.string.pv_oss_leak_chain_step, i + 1),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (ringFixed) scheme.onTertiaryContainer else scheme.onPrimaryContainer,
                )
                Text(
                    stringResource(res),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (ringFixed) scheme.onTertiaryContainer else scheme.onPrimaryContainer,
                )
            }
            if (i < CHAIN_NODES.lastIndex) {
                Text(
                    stringResource(R.string.pv_oss_leak_chain_arrow),
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
        Text(
            stringResource(R.string.pv_oss_leak_chain_fix),
            Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )

        // ---- 八类模式清单：粗 grep 命中 vs 眼看后真实 ----
        Text(stringResource(R.string.pv_oss_leak_table_title), style = MaterialTheme.typography.labelLarge, color = scheme.onSurface)
        LEAK_ROWS.forEach { row ->
            val alarm = row.crude > 0 && row.real == 0
            Column(
                Modifier.fillMaxWidth().clip(card)
                    .background(if (alarm) riskColor else scheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
            ) {
                Text(
                    context.getString(row.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (alarm) scheme.onErrorContainer else scheme.onSurface,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    stringResource(R.string.pv_oss_leak_table_counts, row.crude, row.real),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (alarm) scheme.onErrorContainer else scheme.onSurfaceVariant,
                )
                Text(
                    context.getString(row.verdictRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (alarm) scheme.onErrorContainer else scheme.onSurfaceVariant,
                )
                if (row.noteRes != 0) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        context.getString(row.noteRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }

        Text(
            stringResource(R.string.pv_oss_leak_selfref, AUDIT_DATE_D),
            Modifier.fillMaxWidth().clip(card).background(scheme.errorContainer).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onErrorContainer,
        )
        Text(
            stringResource(R.string.pv_oss_leak_verdict),
            Modifier.fillMaxWidth().clip(card).background(scheme.errorContainer).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onErrorContainer,
        )
        Text(
            stringResource(R.string.pv_oss_leak_downgrade),
            Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )

        Text(
            if (showCmd) stringResource(R.string.pv_oss_leak_cmd_hide)
            else stringResource(R.string.pv_oss_leak_cmd_show),
            Modifier.clip(pill).background(scheme.surfaceVariant)
                .clickable {
                    showCmd = !showCmd
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant,
        )
        if (showCmd) {
            // 命令全 ASCII，直接写在代码里；有边界的写法才是能复算的写法
            listOf(
                // 每条都带 --exclude：本页把检测命令以字符串形式写在被检测的源码树里，
                // 不排除自己就会命中自己（2026-11-05 复测时四条命令各多出 1 处，全是这种自指命中）
                "grep -rn --exclude=PreviewOssAudit.kt 'GlobalScope' android/app/src/main/java",
                "grep -rn '\\bandroid\\.os\\.Handler\\|\\bjava\\.util\\.Timer' android/app/src/main/java",
                "grep -rn --exclude=PreviewOssAudit.kt 'CoroutineScope(' android/app/src/main/java | grep -v rememberCoroutineScope",
                "grep -rn --exclude=PreviewOssAudit.kt 'while (true)' android/app/src/main/java",
                "grep -rn --exclude=PreviewOssAudit.kt 'remember.*Bitmap\\|WebView' android/app/src/main/java",
                "grep -rn 'private val [a-zA-Z]*: Context' android/app/src/main/java",
            ).forEach { cmd ->
                Text(
                    cmd,
                    Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(10.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}
