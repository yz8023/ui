package com.monkeycode.liquidui.ui.features.toolkit

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.theme.AppIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 工具与基建（F18–F21）的四条可操作演示。全部是真实行为：
 * U01 真的发 HTTPS 请求打 GitHub Releases API、真的下载文件、真的调起安装器；
 * U02 真的往 [AppLog] 写日志、真的走系统分享；
 * U03 真的解析并渲染 Markdown（可在输入框里改文字实时看结果）；
 * U04 真的按宽度切换底部栏与侧边栏。
 *
 * 控件全部自带（`TKChip`/`TKButton`/`TKNote`/`TKKeyValue`），不依赖素材集合外的组件，
 * 方便整包复制进宿主。
 */
@Composable
fun ToolkitPreview(entry: ToolkitEntry) {
    when (entry.id) {
        "U01" -> UpdateCheckDemo()
        "U02" -> LogShareDemo()
        "U03" -> MarkdownDemo()
        "U04" -> AdaptiveNavDemo()
    }
}

// ---------------------------------------------------------------- U01 更新检查

private data class UpdateUi(
    val checking: Boolean = false,
    val result: UpdateCheckResult? = null,
    val simulated: Boolean = false,
    val downloading: Boolean = false,
    val written: Long = 0,
    val total: Long = -1,
    val apkPath: String? = null,
    val needInstallPermission: Boolean = false,
    val error: String? = null
)

@Composable
private fun UpdateCheckDemo() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var ui by remember { mutableStateOf(UpdateUi()) }
    var job by remember { mutableStateOf<Job?>(null) }
    val state = ui

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TKKeyValue(stringResource(R.string.tk_demo_text_1), AppUpdateChecker.currentVersionName(context))
        TKKeyValue(stringResource(R.string.tk_demo_text_2), AppUpdateChecker.DEFAULT_REPO)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TKButton(
                label = if (state.checking) stringResource(R.string.common_checking) else stringResource(R.string.tk_demo_text_3),
                enabled = !state.checking && !state.downloading,
                onClick = {
                    ui = state.copy(checking = true, error = null, simulated = false)
                    AppLog.i("UpdateDemo", "手动触发检查")
                    job = scope.launch {
                        val r = withContext(Dispatchers.IO) { AppUpdateChecker.check(context) }
                        ui = state.copy(checking = false, result = r)
                    }
                }
            )
            if (state.downloading) {
                TKButton(
                    label = stringResource(R.string.common_cancel_download),
                    onClick = {
                        job?.cancel()
                        AppLog.w("UpdateDemo", context.getString(R.string.tk_demo_text_44))
                        ui = state.copy(downloading = false, written = 0, error = "已取消下载")
                    }
                )
            }
        }

        TKNote(stringResource(R.string.tk_demo_text_4))

        // 三态预览：明示为模拟数据，方便没网时也能看清界面分支。
        Text(stringResource(R.string.tk_demo_text_5), style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TKChip(stringResource(R.string.tk_demo_text_6), false) {
                ui = UpdateUi(
                    simulated = true,
                    result = UpdateCheckResult(
                        state = UpdateState.UPDATE_AVAILABLE,
                        currentVersion = "2.7.0",
                        latest = ReleaseInfo(
                            tag = "v2.8.0",
                            name = "Liquid UI v2.8.0",
                            body = context.getString(R.string.tk_demo_body_1),
                            publishedAt = "2026-11-02T08:00:00Z",
                            apkUrl = "https://example.invalid/liquidui-v2.8.0-release.apk",
                            apkName = "liquidui-v2.8.0-release.apk",
                            apkBytes = 1_979_392L
                        ),
                        message = context.getString(R.string.tk_demo_message_1),
                        elapsedMillis = 412
                    )
                )
            }
            TKChip(stringResource(R.string.tk_demo_text_7), false) {
                ui = UpdateUi(
                    simulated = true,
                    result = UpdateCheckResult(
                        state = UpdateState.UP_TO_DATE,
                        currentVersion = "2.8.0",
                        latest = ReleaseInfo("v2.8.0", "Liquid UI v2.8.0", "", "2026-11-02T08:00:00Z", null, null, -1),
                        message = context.getString(R.string.tk_demo_message_2),
                        elapsedMillis = 388
                    )
                )
            }
            TKChip(stringResource(R.string.tk_demo_text_8), false) {
                ui = UpdateUi(
                    simulated = true,
                    result = UpdateCheckResult(
                        state = UpdateState.FAILED,
                        currentVersion = AppUpdateChecker.currentVersionName(context),
                        latest = null,
                        message = context.getString(R.string.tk_demo_message_3),
                        elapsedMillis = 205
                    )
                )
            }
        }

        val result = state.result
        if (result != null) {
            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = when (result.state) {
                        UpdateState.UPDATE_AVAILABLE -> MaterialTheme.colorScheme.primary
                        UpdateState.UP_TO_DATE -> MaterialTheme.colorScheme.tertiary
                        UpdateState.FAILED -> MaterialTheme.colorScheme.error
                    }
                ) {
                    Text(
                        when (result.state) {
                            UpdateState.UPDATE_AVAILABLE -> stringResource(R.string.tk_demo_text_6)
                            UpdateState.UP_TO_DATE -> stringResource(R.string.tk_demo_text_7)
                            UpdateState.FAILED -> stringResource(R.string.tk_demo_text_8)
                        },
                        color = when (result.state) {
                            UpdateState.UPDATE_AVAILABLE -> MaterialTheme.colorScheme.onPrimary
                            UpdateState.UP_TO_DATE -> MaterialTheme.colorScheme.onTertiary
                            UpdateState.FAILED -> MaterialTheme.colorScheme.onError
                        },
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    if (state.simulated) stringResource(R.string.tk_demo_text_53, result.message) else result.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TKKeyValue(stringResource(R.string.tk_demo_text_9), "${result.elapsedMillis} ms")
            val latest = result.latest
            if (latest != null) {
                TKKeyValue(stringResource(R.string.tk_demo_text_10), latest.tag)
                TKKeyValue(stringResource(R.string.tk_demo_text_11), latest.publishedAt)
                TKKeyValue(stringResource(R.string.tk_demo_text_12), (latest.apkName ?: stringResource(R.string.tk_demo_text_13)) + " · " + AppUpdateChecker.humanSize(latest.apkBytes))
                if (latest.body.isNotBlank()) {
                    Text(stringResource(R.string.tk_demo_text_14), style = MaterialTheme.typography.labelMedium)
                    MarkdownText(
                        latest.body,
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    )
                }
            }

            // 下载 + 安装：只有真请求拿到真 URL 时才允许下载。
            val url = latest?.apkUrl
            if (result.state == UpdateState.UPDATE_AVAILABLE && url != null) {
                if (state.downloading) {
                    val total = state.total
                    val written = state.written
                    if (total > 0) {
                        val ratio = (written.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
                        Text(
                            "${(ratio * 100).toInt()}% · ${AppUpdateChecker.humanSize(written)} / ${AppUpdateChecker.humanSize(total)}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(stringResource(R.string.tk_demo_text_54, AppUpdateChecker.humanSize(written)), style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    TKButton(
                        label = if (state.apkPath != null) stringResource(R.string.common_redownload_install) else stringResource(R.string.common_download_install),
                        enabled = !state.simulated,
                        onClick = {
                            if (state.simulated) return@TKButton
                            ui = state.copy(downloading = true, written = 0, total = latest.apkBytes, error = null, needInstallPermission = false)
                            AppLog.i("UpdateDemo", "开始下载 ${latest.apkUrl}")
                            job = scope.launch {
                                val outcome = runCatching {
                                    withContext(Dispatchers.IO) {
                                        AppUpdateChecker.download(context, url, latest.apkName ?: "update.apk") { w, t ->
                                            ui = ui.copy(written = w, total = if (t > 0) t else -1)
                                        }
                                    }
                                }
                                outcome.onSuccess { file ->
                                    val installed = AppUpdateChecker.installApk(context, file)
                                    ui = ui.copy(
                                        downloading = false,
                                        apkPath = file.absolutePath,
                                        needInstallPermission = !installed,
                                        error = if (installed) null else context.getString(R.string.tk_demo_text_45)
                                    )
                                }.onFailure { t ->
                                    AppLog.e("UpdateDemo", context.getString(R.string.tk_demo_text_46), t)
                                    ui = ui.copy(downloading = false, error = "下载失败：${t.javaClass.simpleName} ${t.message}")
                                }
                            }
                        }
                    )
                    if (state.simulated) TKNote(stringResource(R.string.tk_demo_text_15))
                }
            }

            if (state.needInstallPermission) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TKButton(stringResource(R.string.tk_demo_text_16), true) { AppUpdateChecker.openUnknownSourcesSettings(context) }
                    TKButton(stringResource(R.string.tk_demo_text_17), true) { url?.let { AppUpdateChecker.openInBrowser(context, it) } }
                }
            }
            state.apkPath?.let { TKKeyValue(context.getString(R.string.tk_demo_text_47), it) }
            if (result.state == UpdateState.FAILED) {
                TKButton(stringResource(R.string.common_retry), !state.checking) {
                    ui = state.copy(checking = true, error = null, simulated = false)
                    job = scope.launch {
                        val r = withContext(Dispatchers.IO) { AppUpdateChecker.check(context) }
                        ui = state.copy(checking = false, result = r)
                    }
                }
            }
        }
        state.error?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

// ---------------------------------------------------------------- U02 日志分享

@Composable
private fun LogShareDemo() {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    var level by remember { mutableStateOf<AppLogLevel?>(null) }
    var keyword by remember { mutableStateOf("") }
    var confirmingClear by remember { mutableStateOf(false) }
    var shareNote by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(confirmingClear) {
        if (confirmingClear) {
            delay(3000)
            confirmingClear = false
        }
    }
    // 直接读 AppLog.entries（SnapshotStateList），写入即刷新。
    val all = AppLog.entries
    val picked = all.filter { e ->
        (level == null || e.level == level) &&
            (keyword.isBlank() || e.tag.contains(keyword, true) || e.message.contains(keyword, true))
    }
    val counts = AppLog.counts()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TKKeyValue(
            stringResource(R.string.tk_demo_text_18),
            stringResource(R.string.tk_demo_text_55, all.size, AppLog.MAX_ENTRIES, counts[AppLogLevel.DEBUG] ?: 0) +
                "I ${counts[AppLogLevel.INFO] ?: 0} · W ${counts[AppLogLevel.WARN] ?: 0} · E ${counts[AppLogLevel.ERROR] ?: 0}"
        )

        Text(stringResource(R.string.tk_demo_text_19), style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TKButton(stringResource(R.string.tk_demo_text_20), true) { AppLog.d("Demo", context.getString(R.string.tk_demo_text_48)) }
            TKButton(stringResource(R.string.tk_demo_text_21), true) { AppLog.i("Demo", context.getString(R.string.tk_demo_text_49)) }
            TKButton(stringResource(R.string.tk_demo_text_22), true) { AppLog.w("Demo", context.getString(R.string.tk_demo_text_50)) }
            TKButton(stringResource(R.string.tk_demo_text_23), true) {
                AppLog.e("Demo", context.getString(R.string.tk_demo_text_51), java.io.IOException("connect timed out"))
            }
        }
        TKNote(stringResource(R.string.tk_demo_text_24))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TKChip(stringResource(R.string.common_all), level == null) { level = null }
            AppLogLevel.entries.forEach { l -> TKChip(l.label, level == l) { level = if (level == l) null else l } }
        }
        OutlinedTextField(
            value = keyword,
            onValueChange = { keyword = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.tk_demo_text_25)) },
            singleLine = true
        )
        Text(stringResource(R.string.tk_demo_text_56, picked.size), style = MaterialTheme.typography.labelMedium)

        // 刻意不套可滚动容器：本页外层已是纵向列表，嵌套同向滚动会互相抢手势。
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 220.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (picked.isEmpty()) {
                Text(stringResource(R.string.tk_demo_text_26), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                picked.takeLast(8).reversed().forEach { e ->
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            e.level.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = when (e.level) {
                                AppLogLevel.ERROR -> MaterialTheme.colorScheme.error
                                AppLogLevel.WARN -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            "#${e.seq} ${e.tag}: ${e.message.replace("\n", " ⏎ ")}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TKButton(stringResource(R.string.common_copy_all), true) {
                clipboard.setText(AnnotatedString(AppLog.dump(level, keyword)))
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                shareNote = "已复制 ${picked.size} 条到剪贴板"
            }
            TKButton(stringResource(R.string.common_share), true) {
                val intent = AppLog.buildShareIntent(context, level, keyword)
                val ok = runCatching { context.startActivity(Intent.createChooser(intent, context.getString(R.string.common_shared_10))) }.isSuccess
                AppLog.i("Demo", if (ok) context.getString(R.string.common_shared_11) else context.getString(R.string.tk_demo_text_52))
                shareNote = if (ok) "已调起系统分享面板" else "分享失败：没有可接收的应用，请改用「复制全文」"
            }
            TKButton(if (confirmingClear) stringResource(R.string.tk_demo_text_27) else stringResource(R.string.common_clear), true) {
                if (confirmingClear) {
                    AppLog.clear()
                    confirmingClear = false
                    shareNote = "已清空缓冲"
                } else {
                    confirmingClear = true
                    shareNote = "再点一次确认清空（3 秒后自动取消）"
                }
            }
        }
        shareNote?.let { TKNote(it) }
        TKNote(stringResource(R.string.tk_demo_text_28))
    }
}

// ---------------------------------------------------------------- U03 Markdown

private const val MD_CHANGELOG = """# v2.8.0 更新日志

新增 **F17 弹窗与提示术语**（P01–P12），把「加个弹窗」这种模糊需求拆成 12 个术语。

## 新增

- 素材库第四分区：*弹窗提示术语 · 12*
- 每条含定义、何时用、`与邻居的区别`、反例与提示词
- 详见 [实现方案](https://github.com/yz8023/ui)

## 修复

1. 列表返回不再回到顶部
2. 提示词长按复制补上触感反馈

> 术语 P01–P06 出自视频简介点名，P07–P12 为本仓库补足。

---

```kotlin
SnackbarHostState().showSnackbar(
    message = "已删除",
    actionLabel = "撤销"
)
```
"""

private const val MD_SYNTAX = """### 支持清单

- 标题 `#`~`######`
- **粗体** 与 *斜体*、_斜体_
- `行内代码` 与围栏代码块
- [链接](https://github.com/yz8023/ui)
- 有序列表：

1. 第一项
2. 第二项

> 引用：连续多行会合并成一段。

***

不支持：表格、图片、嵌套列表、任务列表、HTML 内联。
需要时改引 `org.commonmark:commonmark`（约 200KB 依赖）。
"""

private const val MD_PRIVACY = """## 隐私说明（示例）

本模板**不联网上传任何用户数据**。唯一的网络行为有两处：

1. 更新检查：请求 `api.github.com`（U01）
2. 壁纸缓存：请求你配置的壁纸地址

日志只存内存，*不写文件*，分享由用户手动触发。

- 不申请通讯录、位置、麦克风
- 存储权限仅用于「所有文件访问」读取系统壁纸
"""

@Composable
private fun MarkdownDemo() {
    var source by remember { mutableStateOf(MD_CHANGELOG) }
    var editing by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    val blocks = remember(source) { MarkdownParser.parse(source) }
    // inlines / links / codes 原先声明在 buildString 内部（非组合 lambda），
    // 带格式参数的文案取不到它们；搬到外面来，语义不变（每次组合同样只算一遍）。
    val inlines = blocks.flatMap { b ->
        when (b) {
            is MdHeading -> b.inlines
            is MdParagraph -> b.inlines
            is MdQuote -> b.inlines
            is MdBulletList -> b.items.flatten()
            is MdOrderedList -> b.items.flatten()
            else -> emptyList()
        }
    }
    val links = inlines.count { it is MdLink }
    val codes = inlines.count { it is MdCode }
    val tkDemoText57 = stringResource(R.string.tk_demo_text_57, blocks.size)
    val tkDemoText58 = stringResource(R.string.tk_demo_text_58, inlines.size, links, codes)

    val summary = buildString {
        append(tkDemoText57)
        append(blocks.groupBy { it::class.simpleName ?: "?" }
            .entries.sortedByDescending { it.value.size }
            .joinToString("、") { "${it.key} × ${it.value.size}" })
        append(tkDemoText58)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TKChip(stringResource(R.string.tk_demo_text_29), source == MD_CHANGELOG) { source = MD_CHANGELOG }
            TKChip(stringResource(R.string.tk_demo_text_30), source == MD_SYNTAX) { source = MD_SYNTAX }
            TKChip(stringResource(R.string.tk_demo_text_31), source == MD_PRIVACY) { source = MD_PRIVACY }
        }
        TKKeyValue(stringResource(R.string.tk_demo_text_32), summary)

        TKButton(if (editing) stringResource(R.string.tk_demo_text_33) else stringResource(R.string.tk_demo_text_34), true) {
            editing = !editing
            if (editing) draft = source
        }
        if (editing) {
            OutlinedTextField(
                value = draft,
                onValueChange = {
                    draft = it
                    source = it
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                label = { Text(stringResource(R.string.tk_demo_text_35)) }
            )
        }

        HorizontalDivider()
        MarkdownText(
            source,
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        )
        TKNote(stringResource(R.string.tk_demo_text_36))
    }
}

// ---------------------------------------------------------------- U04 自适应导航

@Composable
private fun AdaptiveNavDemo() {
    val navHome = stringResource(R.string.nav_home)
    val navGallery = stringResource(R.string.nav_gallery)
    val navMotion = stringResource(R.string.nav_motion)
    val navSettings = stringResource(R.string.nav_settings)
    val realClass = rememberAppWindowClass()
    var widthDp by remember { mutableFloatStateOf(420f) }
    var selected by remember { mutableIntStateOf(0) }
    val items = remember {
        listOf(
            AdaptiveNavItem("home", navHome, AppIcons.BlurOn),
            AdaptiveNavItem("gallery", navGallery, AppIcons.Widgets),
            AdaptiveNavItem("motion", navMotion, AppIcons.Animation),
            AdaptiveNavItem("settings", navSettings, AppIcons.Settings)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TKKeyValue(stringResource(R.string.tk_demo_text_37), "${realClass.label}（${realClass.name}）")
        TKKeyValue(stringResource(R.string.tk_demo_text_38), "${widthDp.toInt()} dp → ${windowClassOf(widthDp.toInt()).label}")

        Text(stringResource(R.string.tk_demo_text_39), style = MaterialTheme.typography.labelMedium)
        Slider(
            value = widthDp,
            onValueChange = { widthDp = it },
            valueRange = 360f..1000f,
            steps = 13
        )

        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
        ) {
            val targetWidth = widthDp.toInt().dp
            Box(
                Modifier
                    .width(if (maxWidth < targetWidth) maxWidth else targetWidth)
                    .fillMaxSize()
                    .align(Alignment.Center)
            ) {
                AdaptiveAppShell(
                    items = items,
                    selectedIndex = selected,
                    onSelect = { selected = it },
                    windowClass = windowClassOf(widthDp.toInt())
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            items[selected].label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.tk_demo_text_59, items[selected].id, windowClassOf(widthDp.toInt()).label) +
                                if (windowClassOf(widthDp.toInt()) == AppWindowClass.COMPACT) stringResource(R.string.tk_demo_text_40) else stringResource(R.string.tk_demo_text_41),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TKNote(stringResource(R.string.tk_demo_text_42))
                        Icon(
                            items[selected].icon,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        TKNote(stringResource(R.string.tk_demo_text_43))
    }
}

// ---------------------------------------------------------------- 自带原子控件

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TKChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.combinedClickable(onClick = onClick),
        shape = RoundedCornerShape(percent = 50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TKButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.combinedClickable(onClick = { if (enabled) onClick() }),
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
private fun TKKeyValue(key: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            key,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp)
        )
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun TKNote(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    )
}

