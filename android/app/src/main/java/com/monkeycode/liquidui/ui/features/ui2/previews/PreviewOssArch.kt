package com.monkeycode.liquidui.ui.features.ui2.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.domain.RouteStack
import com.monkeycode.liquidui.domain.SettingsRules
import com.monkeycode.liquidui.ui.screens.rememberAppSettingsState

/**
 * 批 G 开源取材 S21–S22：路由与返回栈状态机 / 架构分层与单向数据流。
 *
 * S21 依据 adrielcafe/voyager（MIT）的返回栈语义，S22 依据 android/nowinandroid（Apache-2.0）的
 * screen / state-holder / repository 分层。**都不引依赖**：
 * S21 跑的是本仓库 `domain/RouteStack.kt` 这个真状态机（不是画几个方块），
 * S22 展示的是本仓库批 D 改造之后的真实分层，页面上那个值是从真实的
 * ui → state → data → storage 链路上读上来的，写回也走同一条链路。
 */

// ==================== S21 路由与返回栈状态机 ====================

private val ROUTE_KEYS = listOf("home", "list", "detail", "comment", "editor")

private val ROUTE_LABEL = mapOf(
    "home" to R.string.pv_oss_route_r_home,
    "list" to R.string.pv_oss_route_r_list,
    "detail" to R.string.pv_oss_route_r_detail,
    "comment" to R.string.pv_oss_route_r_comment,
    "editor" to R.string.pv_oss_route_r_editor,
)

private val ROUTE_OP_LABEL = mapOf(
    RouteStack.OP_PUSH to R.string.pv_oss_route_op_push,
    RouteStack.OP_POP to R.string.pv_oss_route_op_pop,
    RouteStack.OP_REPLACE to R.string.pv_oss_route_op_replace,
    RouteStack.OP_REPLACE_ALL to R.string.pv_oss_route_op_replaceall,
    RouteStack.OP_POP_TO_ROOT to R.string.pv_oss_route_op_poptoroot,
    RouteStack.OP_POP_UNTIL to R.string.pv_oss_route_op_popuntil,
)

/** replaceAll 用的那串路由：模拟「登录成功后重建成 首页→列表→详情 这种既定栈」。 */
private val ROUTE_REBUILD = listOf("home", "list", "detail")

private const val ROUTE_LOG_VISIBLE = 6

@Composable
fun PreviewS21() {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current

    // RouteStack 是普通类、不是 snapshot 状态，所以每次操作后手动 bump 一次 revision 触发重组。
    // 这本身就是 S22 要讲的事：状态持有者必须让 UI 知道「我变了」。
    val stack = remember { RouteStack("home") }
    var revision by remember { mutableIntStateOf(0) }
    var target by rememberSaveable { mutableStateOf("detail") }
    var singleTop by rememberSaveable { mutableStateOf(true) }

    val entries = stack.entries.reversed()
    val logs = stack.log.takeLast(ROUTE_LOG_VISIBLE).reversed()
    val depth = stack.depth
    val current = stack.current
    val root = stack.root
    val canGoBack = stack.canGoBack
    val shouldExit = stack.shouldExitOnBack()
    val opCount = stack.log.size

    val card = RoundedCornerShape(10.dp)
    val pill = RoundedCornerShape(percent = 50)
    val canGoBackText = if (canGoBack) {
        stringResource(R.string.pv_oss_route_yes)
    } else {
        stringResource(R.string.pv_oss_route_no)
    }
    val shouldExitText = if (shouldExit) {
        stringResource(R.string.pv_oss_route_yes)
    } else {
        stringResource(R.string.pv_oss_route_no)
    }
    val singleTopLabel = if (singleTop) {
        stringResource(R.string.pv_oss_route_singletop_on)
    } else {
        stringResource(R.string.pv_oss_route_singletop_off)
    }

    Column(
        Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            stringResource(R.string.pv_oss_route_intro),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )

        // ---- 目标路由 ----
        Text(
            stringResource(R.string.pv_oss_route_target_title),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurface,
        )
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ROUTE_KEYS.forEach { key ->
                val selected = key == target
                Text(
                    context.getString(ROUTE_LABEL.getValue(key)),
                    Modifier.clip(pill)
                        .background(if (selected) scheme.primaryContainer else scheme.surfaceVariant)
                        .clickable {
                            target = key
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                )
            }
        }

        // ---- 栈可视化：上＝栈顶＝当前屏 ----
        Text(
            stringResource(R.string.pv_oss_route_stack_title, revision),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurface,
        )
        entries.forEachIndexed { i, route ->
            val isTop = i == 0
            Row(
                Modifier.fillMaxWidth().clip(card)
                    .background(if (isTop) scheme.primaryContainer else scheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "${depth - i}",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    color = if (isTop) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                )
                Text(
                    context.getString(ROUTE_LABEL.getValue(route)),
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isTop) scheme.onPrimaryContainer else scheme.onSurface,
                )
                Text(
                    route,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = if (isTop) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                )
                if (isTop) {
                    Text(
                        stringResource(R.string.pv_oss_route_current_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onPrimaryContainer,
                    )
                }
            }
        }

        // ---- 操作 ----
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RouteStack.OPERATIONS.forEach { op ->
                Text(
                    context.getString(ROUTE_OP_LABEL.getValue(op)),
                    Modifier.clip(pill).background(scheme.secondaryContainer)
                        .clickable {
                            when (op) {
                                RouteStack.OP_PUSH -> stack.push(target, singleTop)
                                RouteStack.OP_POP -> stack.pop()
                                RouteStack.OP_REPLACE -> stack.replace(target)
                                RouteStack.OP_REPLACE_ALL -> stack.replaceAll(ROUTE_REBUILD)
                                RouteStack.OP_POP_TO_ROOT -> stack.popToRoot()
                                else -> stack.popUntil(target)
                            }
                            revision++
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSecondaryContainer,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.pv_oss_route_op_back),
                Modifier.clip(pill).background(scheme.errorContainer)
                    .clickable {
                        stack.onBackPressed()
                        revision++
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onErrorContainer,
            )
            Text(
                singleTopLabel,
                Modifier.clip(pill).background(scheme.surfaceVariant)
                    .clickable {
                        singleTop = !singleTop
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.pv_oss_route_op_clearlog),
                Modifier.clip(pill).background(scheme.surfaceVariant)
                    .clickable {
                        stack.clearLog()
                        revision++
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
        }

        // ---- 读数 ----
        Text(
            stringResource(
                R.string.pv_oss_route_readout,
                depth,
                context.getString(ROUTE_LABEL.getValue(current)),
                context.getString(ROUTE_LABEL.getValue(root)),
                canGoBackText,
                shouldExitText,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurface,
        )

        // ---- 操作日志：区分「操作被调用」与「状态被改变」 ----
        Text(
            stringResource(R.string.pv_oss_route_log_title, opCount),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurface,
        )
        if (logs.isEmpty()) {
            Text(
                stringResource(R.string.pv_oss_route_log_empty),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
        }
        logs.forEach { entry ->
            val changedLabel = if (entry.changed) {
                context.getString(R.string.pv_oss_route_changed)
            } else {
                context.getString(R.string.pv_oss_route_unchanged)
            }
            Row(
                Modifier.fillMaxWidth().clip(card)
                    .background(if (entry.changed) scheme.surfaceVariant else scheme.errorContainer)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(
                        R.string.pv_oss_route_log_row,
                        context.getString(ROUTE_OP_LABEL.getValue(entry.operation)),
                        context.getString(ROUTE_LABEL.getValue(entry.route)),
                        entry.depthAfter,
                        changedLabel,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (entry.changed) scheme.onSurfaceVariant else scheme.onErrorContainer,
                )
            }
        }

        Text(
            stringResource(R.string.pv_oss_route_hint),
            Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.pv_oss_route_back_note),
            Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.pv_oss_route_api),
            Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(0.dp))
    }
}

// ==================== S22 架构分层与单向数据流 ====================

private val LAYER_KEYS = listOf("ui", "state", "data", "domain", "storage")

private val LAYER_NAME = mapOf(
    "ui" to R.string.pv_oss_layer_l_ui,
    "state" to R.string.pv_oss_layer_l_state,
    "data" to R.string.pv_oss_layer_l_data,
    "domain" to R.string.pv_oss_layer_l_domain,
    "storage" to R.string.pv_oss_layer_l_storage,
)

private val LAYER_FILE = mapOf(
    "ui" to R.string.pv_oss_layer_file_ui,
    "state" to R.string.pv_oss_layer_file_state,
    "data" to R.string.pv_oss_layer_file_data,
    "domain" to R.string.pv_oss_layer_file_domain,
    "storage" to R.string.pv_oss_layer_file_storage,
)

private val LAYER_DUTY = mapOf(
    "ui" to R.string.pv_oss_layer_duty_ui,
    "state" to R.string.pv_oss_layer_duty_state,
    "data" to R.string.pv_oss_layer_duty_data,
    "domain" to R.string.pv_oss_layer_duty_domain,
    "storage" to R.string.pv_oss_layer_duty_storage,
)

private val LAYER_BAN = mapOf(
    "ui" to R.string.pv_oss_layer_ban_ui,
    "state" to R.string.pv_oss_layer_ban_state,
    "data" to R.string.pv_oss_layer_ban_data,
    "domain" to R.string.pv_oss_layer_ban_domain,
    "storage" to R.string.pv_oss_layer_ban_storage,
)

/** 依赖边：`from → to`。方向是这一页的重点，所以每条边都配一句「为什么只能这个方向」。 */
private val EDGE_KEYS = listOf("ui_state", "state_data", "state_domain", "data_domain", "data_storage")

private val EDGE_LABEL = mapOf(
    "ui_state" to R.string.pv_oss_layer_e_ui_state,
    "state_data" to R.string.pv_oss_layer_e_state_data,
    "state_domain" to R.string.pv_oss_layer_e_state_domain,
    "data_domain" to R.string.pv_oss_layer_e_data_domain,
    "data_storage" to R.string.pv_oss_layer_e_data_storage,
)

private val EDGE_WHY = mapOf(
    "ui_state" to R.string.pv_oss_layer_w_ui_state,
    "state_data" to R.string.pv_oss_layer_w_state_data,
    "state_domain" to R.string.pv_oss_layer_w_state_domain,
    "data_domain" to R.string.pv_oss_layer_w_data_domain,
    "data_storage" to R.string.pv_oss_layer_w_data_storage,
)

/** 一次改值的完整旅程：每一步都对应本仓库里一个真实存在的函数。 */
private val FLOW_KEYS = listOf("gesture", "clamp", "write", "notify", "recompose")

private val FLOW_LABEL = mapOf(
    "gesture" to R.string.pv_oss_layer_f_gesture,
    "clamp" to R.string.pv_oss_layer_f_clamp,
    "write" to R.string.pv_oss_layer_f_write,
    "notify" to R.string.pv_oss_layer_f_notify,
    "recompose" to R.string.pv_oss_layer_f_recompose,
)

private val FLOW_CODE = mapOf(
    "gesture" to R.string.pv_oss_layer_c_gesture,
    "clamp" to R.string.pv_oss_layer_c_clamp,
    "write" to R.string.pv_oss_layer_c_write,
    "notify" to R.string.pv_oss_layer_c_notify,
    "recompose" to R.string.pv_oss_layer_c_recompose,
)

/** 领域层纯净度实测日期（数字写在资源里，改代码后要重测）。 */
private const val LAYER_MEASURE_DATE = "2026-11-05"

/** 探测滑杆的范围故意超出 0..1，这样才看得见规则层把越界值夹回来。 */
private const val PROBE_MIN = -0.6f
private const val PROBE_MAX = 2.4f

private fun fmt2f(v: Float): String = String.format(java.util.Locale.US, "%.2f", v)

@Composable
fun PreviewS22() {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current

    // 真的把批 D 改造后的那条链路接上：读的是真实 prefs，写的也是真实 prefs
    val settings = rememberAppSettingsState()
    var probe by rememberSaveable { mutableFloatStateOf(1.7f) }
    var selectedLayer by rememberSaveable { mutableStateOf("state") }
    var selectedEdge by rememberSaveable { mutableStateOf("state_data") }
    var selectedStep by rememberSaveable { mutableStateOf("clamp") }

    val liveDamping = settings.motionDamping
    val clamped = SettingsRules.clampByKey(SettingsRules.KEY_DAMPING, probe)
    val ruleRange = SettingsRules.rangeOf(SettingsRules.KEY_DAMPING)
    val ruleName = SettingsRules.KEY_DAMPING
    val seedHex = SettingsRules.customSeedHex(settings.customSeed)

    val card = RoundedCornerShape(10.dp)
    val pill = RoundedCornerShape(percent = 50)

    Column(
        Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            stringResource(R.string.pv_oss_layer_intro, LAYER_MEASURE_DATE),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )

        // ---- 五层：点一层看职责与禁区 ----
        Text(
            stringResource(R.string.pv_oss_layer_title),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurface,
        )
        LAYER_KEYS.forEachIndexed { i, key ->
            val selected = key == selectedLayer
            Column(
                Modifier.fillMaxWidth().clip(card)
                    .background(if (selected) scheme.primaryContainer else scheme.surfaceVariant)
                    .clickable {
                        selectedLayer = key
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${i + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        color = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                    )
                    Text(
                        context.getString(LAYER_NAME.getValue(key)),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
                    )
                }
                Text(
                    context.getString(LAYER_FILE.getValue(key)),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                )
                if (selected) {
                    Text(
                        context.getString(LAYER_DUTY.getValue(key)),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onPrimaryContainer,
                    )
                    Text(
                        context.getString(LAYER_BAN.getValue(key)),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onPrimaryContainer,
                    )
                }
            }
            if (i < LAYER_KEYS.lastIndex) {
                Text(
                    stringResource(R.string.pv_oss_layer_arrow),
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }

        // ---- 依赖边 ----
        Text(
            stringResource(R.string.pv_oss_layer_edge_title),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurface,
        )
        EDGE_KEYS.forEach { key ->
            val selected = key == selectedEdge
            Column(
                Modifier.fillMaxWidth().clip(card)
                    .background(if (selected) scheme.secondaryContainer else scheme.surfaceVariant)
                    .clickable {
                        selectedEdge = key
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    context.getString(EDGE_LABEL.getValue(key)),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    color = if (selected) scheme.onSecondaryContainer else scheme.onSurface,
                )
                if (selected) {
                    Text(
                        context.getString(EDGE_WHY.getValue(key)),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSecondaryContainer,
                    )
                }
            }
        }

        // ---- 一次改值的完整旅程 ----
        Text(
            stringResource(R.string.pv_oss_layer_flow_title),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurface,
        )
        FLOW_KEYS.forEachIndexed { i, key ->
            val selected = key == selectedStep
            Column(
                Modifier.fillMaxWidth().clip(card)
                    .background(if (selected) scheme.tertiaryContainer else scheme.surfaceVariant)
                    .clickable {
                        selectedStep = key
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    stringResource(R.string.pv_oss_layer_flow_step, i + 1, context.getString(FLOW_LABEL.getValue(key))),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) scheme.onTertiaryContainer else scheme.onSurface,
                )
                if (selected) {
                    Text(
                        context.getString(FLOW_CODE.getValue(key)),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = scheme.onTertiaryContainer,
                    )
                }
            }
        }

        // ---- 活体演示：滑杆给一个越界值，看规则层怎么夹，再真的写一次 ----
        Text(
            stringResource(R.string.pv_oss_layer_probe_title, ruleName),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurface,
        )
        Slider(
            value = probe,
            onValueChange = { probe = it },
            valueRange = PROBE_MIN..PROBE_MAX,
        )
        Text(
            stringResource(
                R.string.pv_oss_layer_probe,
                fmt2f(probe),
                fmt2f(clamped),
                fmt2f(ruleRange[0]),
                fmt2f(ruleRange[1]),
            ),
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = scheme.onSurface,
        )
        Text(
            stringResource(R.string.pv_oss_layer_live, fmt2f(liveDamping), seedHex),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.pv_oss_layer_write),
                Modifier.clip(pill).background(scheme.primaryContainer)
                    .clickable {
                        // 真的写：走 state → rules → data → storage 全链路，上面那个「当前真实值」会跟着变
                        settings.updateMotionDamping(probe)
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onPrimaryContainer,
            )
            Text(
                stringResource(R.string.pv_oss_layer_restore),
                Modifier.clip(pill).background(scheme.surfaceVariant)
                    .clickable {
                        settings.updateMotionDamping(SettingsRules.DEFAULT_DAMPING)
                        probe = SettingsRules.DEFAULT_DAMPING
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
        }

        Text(
            stringResource(R.string.pv_oss_layer_purity, LAYER_MEASURE_DATE),
            Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.pv_oss_layer_purity_cmd),
            Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = scheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.pv_oss_layer_contract),
            Modifier.fillMaxWidth().clip(card).background(scheme.surfaceVariant).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.pv_oss_layer_scope),
            Modifier.fillMaxWidth().clip(card).background(scheme.errorContainer).padding(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onErrorContainer,
        )
        Spacer(Modifier.height(0.dp))
    }
}
