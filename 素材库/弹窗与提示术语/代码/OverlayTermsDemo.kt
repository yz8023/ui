package com.monkeycode.liquidui.ui.features.overlayterms

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.theme.AppIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 弹窗与提示术语（F17）的真实 Compose 演示：P01–P12 每条都能实际操作。
 * 全部用 Compose/Material3 与系统 API 真实实现——真 Toast、真 AlertDialog、真 Dialog、
 * 真 DropdownMenu 锚定、真 SnackbarHost（可撤销）、真 NotificationManager 发送、
 * 真 ModalBottomSheet、真进度三档。术语定义与提示词见 [OverlayTerms]。
 */

/** 预览分发：详情页按条目 id 调用。 */
@Composable
fun OverlayTermsPreview(term: OverlayTerm) {
    when (term.id) {
        "P01" -> PreviewP01()
        "P02" -> PreviewP02()
        "P03" -> PreviewP03()
        "P04" -> PreviewP04()
        "P05" -> PreviewP05()
        "P06" -> PreviewP06()
        "P07" -> PreviewP07()
        "P08" -> PreviewP08()
        "P09" -> PreviewP09()
        "P10" -> PreviewP10()
        "P11" -> PreviewP11()
        else -> PreviewP12()
    }
}

// ─── 共用原子控件 ───────────────────────────────────────────────────────────

@Composable
private fun OTChip(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    val bg = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }
    val fg = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        selected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}

@Composable
private fun OTButton(text: String, enabled: Boolean = true, primary: Boolean = true, loading: Boolean = false, onClick: () -> Unit) {
    val wallpaperText10 = stringResource(R.string.wallpaper_text_10)
    val bg = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        primary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }
    val fg = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        primary -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = fg)
            Spacer(Modifier.width(8.dp))
        }
        Text(if (loading) wallpaperText10 else text, color = fg, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun OTNote(text: String, accent: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// ─── P01 Alert 警告框 ───────────────────────────────────────────────────────

@Composable
private fun PreviewP01() {
    var open by remember { mutableStateOf(false) }
    var blocked by remember { mutableStateOf(true) }
    var seen by remember { mutableIntStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote(stringResource(R.string.ot_demo_text_1))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OTChip(stringResource(R.string.ot_demo_text_2), selected = blocked, onClick = { blocked = true })
            OTChip(stringResource(R.string.ot_demo_text_3), selected = !blocked, onClick = { blocked = false })
        }
        OTButton(stringResource(R.string.ot_demo_text_4)) { open = true }
        OTNote("已确认 $seen 次。")
        OTNote(stringResource(R.string.ot_demo_text_5))
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { if (!blocked) { open = false; seen += 1 } },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = !blocked,
                dismissOnClickOutside = !blocked,
            ),
            title = { Text(stringResource(R.string.ot_demo_text_6)) },
            text = { Text(stringResource(R.string.ot_demo_text_7)) },
            confirmButton = {
                TextButton(onClick = { open = false; seen += 1 }) { Text(stringResource(R.string.ot_demo_text_8)) }
            },
        )
    }
}

// ─── P02 Toast 轻提示 ───────────────────────────────────────────────────────

@Composable
private fun PreviewP02() {
    val context = LocalContext.current
    var light by remember { mutableStateOf<String?>(null) }
    var fired by remember { mutableIntStateOf(0) }
    LaunchedEffect(light) {
        val msg = light
        if (msg != null) {
            delay(2200)
            light = null
        }
    }
    Box {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OTNote(stringResource(R.string.ot_demo_text_9))
            OTButton(stringResource(R.string.ot_demo_text_10)) {
                Toast.makeText(context, context.getString(R.string.ot_demo_text_85), Toast.LENGTH_SHORT).show()
                fired += 1
            }
            OTButton(stringResource(R.string.ot_demo_text_11), primary = false) {
                light = "已复制到剪贴板"
                fired += 1
            }
            OTNote("已触发 $fired 次。系统 Toast 的限制：Android 12+ 应用在后台时不显示、样式不可定制、不能带按钮。")
            OTNote(stringResource(R.string.ot_demo_text_12))
            OTNote(stringResource(R.string.ot_demo_text_13))
        }
        if (light != null) {
            Surface(
                Modifier.align(Alignment.BottomCenter).padding(top = 12.dp),
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            ) {
                Text(light ?: "", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp))
            }
        }
    }
}

// ─── P03 Modal 模态框 ───────────────────────────────────────────────────────

@Composable
private fun PreviewP03() {
    var open by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf<String?>(null) }
    var outsideClosable by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote(stringResource(R.string.ot_demo_text_14))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OTChip(stringResource(R.string.ot_demo_text_15), selected = !outsideClosable, onClick = { outsideClosable = false })
            OTChip(stringResource(R.string.ot_demo_text_16), selected = outsideClosable, onClick = { outsideClosable = true })
        }
        OTButton(stringResource(R.string.ot_demo_text_17)) { name = saved ?: "未命名项目"; open = true }
        if (saved != null) OTNote("当前名称：$saved", accent = true)
        OTNote(stringResource(R.string.ot_demo_text_18))
        OTNote(stringResource(R.string.ot_demo_text_19))
    }
    if (open) {
        Dialog(
            onDismissRequest = { if (outsideClosable) open = false },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = outsideClosable),
        ) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.ot_demo_text_20), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text(stringResource(R.string.ot_demo_text_21)) }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OTNote(stringResource(R.string.ot_demo_text_22))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { open = false }) { Text(stringResource(R.string.common_cancel)) }
                        TextButton(onClick = { saved = name; open = false }, enabled = name.isNotBlank()) { Text(stringResource(R.string.ot_demo_text_23)) }
                    }
                }
            }
        }
    }
}

// ─── P04 Drawer 抽屉 ────────────────────────────────────────────────────────

@Composable
private fun PreviewP04() {
    var open by remember { mutableStateOf(false) }
    var picked by remember { mutableStateOf("概览") }
    val progress by animateFloatAsState(if (open) 1f else 0f, tween(260), label = "drawer")
    BackHandler(enabled = open) { open = false }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote(stringResource(R.string.ot_demo_text_24))
        Box(
            Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.ot_demo_text_25), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.ot_demo_text_94, picked), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OTButton(stringResource(R.string.ot_demo_text_26)) { open = true }
                OTNote(stringResource(R.string.ot_demo_text_27))
            }
            if (progress > 0.001f) {
                Box(
                    Modifier.matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f * progress))
                        .clickable { open = false }
                )
                Surface(
                    Modifier.align(Alignment.CenterStart).fillMaxHeight().width(210.dp)
                        .offset { IntOffset(((progress - 1f) * 210.dp.toPx()).roundToInt(), 0) },
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.ot_demo_text_28), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        listOf(stringResource(R.string.ot_demo_text_29), stringResource(R.string.ot_demo_text_30), stringResource(R.string.ot_demo_text_31), stringResource(R.string.ot_demo_text_32), stringResource(R.string.ot_demo_text_33)).forEach { item ->
                            Text(
                                item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (item == picked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (item == picked) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                    .clickable { picked = item; open = false }.padding(horizontal = 10.dp, vertical = 11.dp),
                            )
                        }
                    }
                }
            }
        }
        OTNote(stringResource(R.string.ot_demo_text_34))
    }
}

// ─── P05 Popconfirm 气泡确认框 ──────────────────────────────────────────────

@Composable
private fun PreviewP05() {
    val otDemoText86 = stringResource(R.string.ot_demo_text_86)
    val otDemoText87 = stringResource(R.string.ot_demo_text_87)
    val otDemoText88 = stringResource(R.string.ot_demo_text_88)
    var items by remember { mutableStateOf(listOf(otDemoText86, otDemoText87, otDemoText88)) }
    var pending by remember { mutableStateOf<String?>(null) }
    var undo by remember { mutableStateOf<Pair<Int, String>?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote(stringResource(R.string.ot_demo_text_35))
        if (items.isEmpty()) OTNote(stringResource(R.string.ot_demo_text_36))
        items.forEachIndexed { index, name ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Box {
                    TextButton(onClick = { pending = name }) { Text("删除") }
                    DropdownMenu(expanded = pending == name, onDismissRequest = { pending = null }) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("确定删除「$name」？", style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = { pending = null }) { Text("取消") }
                                TextButton(onClick = {
                                    undo = index to name
                                    items = items.filterNot { it == name }
                                    pending = null
                                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OTChip(stringResource(R.string.ot_demo_text_37), selected = false, onClick = { items = listOf("报表 A", "报表 B", "报表 C"); undo = null })
            undo?.let { (i, n) ->
                OTChip("撤销上一次（$n 回到第 ${i + 1} 位）", selected = false, onClick = {
                    val list = items.toMutableList(); list.add(i.coerceAtMost(list.size), n); items = list; undo = null
                })
            }
        }
        OTNote(stringResource(R.string.ot_demo_text_38))
        OTNote(stringResource(R.string.ot_demo_text_39))
    }
}

// ─── P06 Tooltip 文字提示 ───────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PreviewP06() {
    var tipKey by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(tipKey) {
        if (tipKey != null) {
            delay(1800)
            tipKey = null
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote(stringResource(R.string.ot_demo_text_40))
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(
                "notif" to ("通知" to stringResource(R.string.ot_demo_text_41)),
                "sync" to ("同步" to stringResource(R.string.ot_demo_text_42)),
                "lock" to ("锁定" to stringResource(R.string.ot_demo_text_43)),
            ).forEach { (key, pair) ->
                val (label, text) = pair
                Box {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (key == "notif") AppIcons.Notifications else if (key == "sync") AppIcons.RotateRight else AppIcons.Shield,
                            contentDescription = text,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp).combinedClickable(onClick = { }, onLongClick = { tipKey = key }),
                        )
                        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = tipKey == key, onDismissRequest = { tipKey = null }) {
                        Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                }
            }
        }
        OTNote(stringResource(R.string.ot_demo_text_44))
        OTNote(stringResource(R.string.ot_demo_text_45))
        OTNote(stringResource(R.string.ot_demo_text_46))
    }
}

// ─── P07 Snackbar 条幅提示 ──────────────────────────────────────────────────

@Composable
private fun PreviewP07() {
    val otDemoText89 = stringResource(R.string.ot_demo_text_89)
    val otDemoText90 = stringResource(R.string.ot_demo_text_90)
    val otDemoText91 = stringResource(R.string.ot_demo_text_91)
    val scope = rememberCoroutineScope()
    val host = remember { SnackbarHostState() }
    var items by remember { mutableStateOf(listOf(otDemoText89, otDemoText90, otDemoText91)) }
    Box(Modifier.fillMaxWidth().height(230.dp)) {
        Column(Modifier.padding(bottom = 56.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OTNote(stringResource(R.string.ot_demo_text_47))
            if (items.isEmpty()) {
                OTNote(stringResource(R.string.ot_demo_text_48))
            }
            items.forEachIndexed { index, name ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = {
                        scope.launch {
                            val list = items.toMutableList()
                            list.removeAt(index)
                            items = list
                            val result = host.showSnackbar("已删除「$name」", actionLabel = "撤销", duration = SnackbarDuration.Short)
                            if (result == SnackbarResult.ActionPerformed) {
                                val back = items.toMutableList()
                                back.add(index.coerceAtMost(back.size), name)
                                items = back
                            }
                        }
                    }) { Text("删除") }
                }
            }
            OTChip(stringResource(R.string.ot_demo_text_49), selected = false, onClick = { items = listOf("草稿 1", "草稿 2", "草稿 3") })
            OTNote(stringResource(R.string.ot_demo_text_50))
        }
        SnackbarHost(hostState = host, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// ─── P08 Notification 系统通知 ──────────────────────────────────────────────

@Composable
private fun PreviewP08() {
    val context = LocalContext.current
    var status by remember { mutableStateOf<String?>(null) }
    var sent by remember { mutableIntStateOf(0) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        status = if (granted) "已授权，现在点「发送真实通知」能收到" else "被拒绝：Android 13+ 通知发不出去，需到系统设置手动开启"
    }
    fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun send() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "overlay_terms_demo"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "术语演示", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "弹窗与提示术语 P08 的演示通知渠道"
            manager.createNotificationChannel(channel)
        }
        val notification = Notification.Builder(context, channelId)
            .setContentTitle("导出完成")
            .setContentText("报表 A.csv 已生成，点击查看")
            .setSmallIcon(R.drawable.ic_stat_launcher)
            .setAutoCancel(true)
            .build()
        manager.notify(2001, notification)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote(stringResource(R.string.ot_demo_text_97))
        OTNote("当前系统版本 SDK ${Build.VERSION.SDK_INT}；授权状态：${if (hasPermission()) "已授权" else "未授权（Android 13+ 必须先申请 POST_NOTIFICATIONS）"}", accent = true)
        if (!hasPermission()) {
            OTButton(stringResource(R.string.ot_demo_text_98), primary = false) { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        }
        OTButton(stringResource(R.string.ot_demo_text_99), enabled = hasPermission()) {
            send()
            sent += 1
            status = "已调用 notify(2001)，去通知栏看"
        }
        if (status != null) OTNote(status!!, accent = true)
        OTNote("本机已发送 $sent 次。未授权时点按钮是禁用态，不会静默失败——这是规范要求。")

        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(AppIcons.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.ot_demo_text_100), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.ot_demo_text_101), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(stringResource(R.string.ot_demo_text_102), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        OTNote(stringResource(R.string.ot_demo_text_103))
        OTNote(stringResource(R.string.ot_demo_text_104))
    }
}

// ─── P09 Popover 气泡卡片 ───────────────────────────────────────────────────

@Composable
private fun PreviewP09() {
    var open by remember { mutableStateOf(false) }
    var notifyOn by remember { mutableStateOf(true) }
    var picked by remember { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote(stringResource(R.string.ot_demo_text_51))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                OTChip(stringResource(R.string.ot_demo_text_52), selected = open, onClick = { open = !open })
                DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        listOf("分享" to stringResource(R.string.ot_demo_text_53), "导出" to stringResource(R.string.ot_demo_text_54), "归档" to stringResource(R.string.ot_demo_text_55)).forEach { (title, sub) ->
                            Row(
                                Modifier.fillMaxWidth().clickable { picked = title; open = false }.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(AppIcons.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(title, style = MaterialTheme.typography.bodyMedium)
                                    Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.ot_demo_text_56), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Box(
                                Modifier.size(width = 42.dp, height = 26.dp).clip(RoundedCornerShape(percent = 50))
                                    .background(if (notifyOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { notifyOn = !notifyOn }.padding(3.dp),
                                contentAlignment = if (notifyOn) Alignment.CenterEnd else Alignment.CenterStart,
                            ) {
                                Box(Modifier.size(20.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary))
                            }
                        }
                    }
                }
            }
        }
        // picked 是 `var … by remember { mutableStateOf<String?>(null) }`：委托属性不做智能转换，
        // 直接当 vararg Any 传编译不过（模板 "$picked" 不报错，换成 stringResource 才暴露）。
        val pickedNow = picked
        if (pickedNow != null) OTNote(stringResource(R.string.ot_demo_text_95, pickedNow), accent = true)
        OTNote(stringResource(R.string.ot_demo_text_57))
        OTNote(stringResource(R.string.ot_demo_text_58))
    }
}

// ─── P10 BottomSheet / ActionSheet ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreviewP10() {
    val otDemoText92 = stringResource(R.string.ot_demo_text_92)
    val otDemoText93 = stringResource(R.string.ot_demo_text_93)
    var open by remember { mutableStateOf(false) }
    var chosen by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote(otDemoText92)
        OTButton(otDemoText93) { open = true }
        if (chosen != null) OTNote("已选择：$chosen", accent = true)
        OTNote("面板可半展开（露一半）与全展开两档，拖把手可切换；下拉关闭与内部滚动不打架。")
        OTNote("反例对照：ActionSheet 不给「取消」项，或把整页内容塞进面板造成滚动嵌套。")
    }
    if (open) {
        ModalBottomSheet(onDismissRequest = { open = false }, sheetState = sheetState) {
            Column(Modifier.padding(bottom = 20.dp)) {
                listOf(stringResource(R.string.ot_demo_text_59), stringResource(R.string.ot_demo_text_60), stringResource(R.string.ot_demo_text_61), stringResource(R.string.ot_demo_text_62)).forEach { action ->
                    Text(
                        action,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().clickable {
                            chosen = action
                            scope.launch { sheetState.hide() }
                            open = false
                        }.padding(horizontal = 24.dp, vertical = 15.dp),
                    )
                }
                // 文案上提到 lambda 外：clickable {} 不是组合上下文，里面不能调 stringResource()
                val deleteLabel = stringResource(R.string.ot_demo_text_63)
                Text(
                    deleteLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().clickable {
                        chosen = deleteLabel
                        scope.launch { sheetState.hide() }
                        open = false
                    }.padding(horizontal = 24.dp, vertical = 15.dp),
                )
                HorizontalDivider()
                Text(
                    stringResource(R.string.common_cancel),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().clickable {
                        scope.launch { sheetState.hide() }
                        open = false
                    }.padding(horizontal = 24.dp, vertical = 15.dp),
                )
            }
        }
    }
}

// ─── P11 Loading / Progress 三档 ────────────────────────────────────────────

@Composable
private fun PreviewP11() {
    var mode by remember { mutableStateOf("按钮内") }
    var running by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var done by remember { mutableIntStateOf(0) }
    LaunchedEffect(running) {
        while (running) {
            delay(90)
            progress = (progress + 0.04f).coerceAtMost(1f)
            if (progress >= 1f) {
                running = false
                done += 1
            }
        }
    }
    Box {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OTNote(stringResource(R.string.ot_demo_text_64))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(stringResource(R.string.ot_demo_text_65), stringResource(R.string.ot_demo_text_66), stringResource(R.string.ot_demo_text_67)).forEach { m ->
                    OTChip(m, selected = mode == m, onClick = { mode = m })
                }
            }
            if (mode == "非阻塞") {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                OTNote(stringResource(R.string.ot_demo_text_68), accent = true)
            }
            OTButton(
                text = when (mode) {
                    "按钮内" -> stringResource(R.string.ot_demo_text_69)
                    "全局遮罩" -> stringResource(R.string.ot_demo_text_70)
                    else -> stringResource(R.string.ot_demo_text_71)
                },
                loading = mode == "按钮内" && running,
                enabled = !running || mode != "按钮内",
            ) {
                progress = 0f
                running = true
            }
            OTNote(if (running) "进行中：${(progress * 100).toInt()}%" else "已完成 $done 次任务。", accent = running)
            if (mode == "按钮内") OTNote(stringResource(R.string.ot_demo_text_72))
            if (mode == "全局遮罩") OTNote(stringResource(R.string.ot_demo_text_73))
            OTNote(stringResource(R.string.ot_demo_text_74))
        }
        if (mode == "全局遮罩" && running) {
            Box(
                Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator()
                        Text(stringResource(R.string.ot_demo_text_96, (progress * 100).toInt()), style = MaterialTheme.typography.labelMedium)
                        TextButton(onClick = { running = false; progress = 0f }) { Text(stringResource(R.string.common_cancel)) }
                    }
                }
            }
        }
    }
}

// ─── P12 Badge / Dot ────────────────────────────────────────────────────────

@Composable
private fun PreviewP12() {
    var count by remember { mutableIntStateOf(5) }
    var dot by remember { mutableStateOf(true) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote(stringResource(R.string.ot_demo_text_75))
        Row(horizontalArrangement = Arrangement.spacedBy(26.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                Icon(AppIcons.Notifications, contentDescription = stringResource(R.string.ot_demo_content_desc_1, count), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(30.dp))
                if (count > 0) {
                    Surface(
                        Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-6).dp),
                        shape = RoundedCornerShape(percent = 50),
                        color = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ) {
                        Text(
                            if (count > 99) "99+" else "$count",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                        )
                    }
                }
            }
            Box {
                Icon(AppIcons.Settings, contentDescription = if (dot) stringResource(R.string.ot_demo_text_76) else stringResource(R.string.ot_demo_text_77), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(30.dp))
                if (dot) {
                    Box(Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp).size(9.dp).background(MaterialTheme.colorScheme.error, CircleShape))
                }
            }
            Column {
                Text(stringResource(R.string.ot_demo_text_78), style = MaterialTheme.typography.labelMedium)
                Text(stringResource(R.string.ot_demo_text_79), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OTChip("+1", selected = false, onClick = { count += 1 })
            OTChip("−1", selected = false, enabled = count > 0, onClick = { count -= 1 })
            OTChip(stringResource(R.string.ot_demo_text_80), selected = false, onClick = { count = 100 })
            OTChip(stringResource(R.string.ot_demo_text_81), selected = false, onClick = { count = 0; dot = false })
            OTChip(stringResource(R.string.ot_demo_text_82), selected = dot, onClick = { dot = true })
        }
        OTNote("当前 count = $count → 显示 ${if (count == 0) "（不渲染角标）" else if (count > 99) "99+" else "$count"}", accent = true)
        OTNote(stringResource(R.string.ot_demo_text_83))
        OTNote(stringResource(R.string.ot_demo_text_84))
    }
}
