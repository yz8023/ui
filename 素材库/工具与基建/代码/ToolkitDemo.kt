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
        TKKeyValue("当前安装版本", AppUpdateChecker.currentVersionName(context))
        TKKeyValue("检查目标仓库", AppUpdateChecker.DEFAULT_REPO)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TKButton(
                label = if (state.checking) "检查中…" else "真实检查更新",
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
                    label = "取消下载",
                    onClick = {
                        job?.cancel()
                        AppLog.w("UpdateDemo", "用户取消下载")
                        ui = state.copy(downloading = false, written = 0, error = "已取消下载")
                    }
                )
            }
        }

        TKNote("检查会真的访问 api.github.com；无网时会返回 FAILED 并给出可读原因（不是笼统的「失败」）。")

        // 三态预览：明示为模拟数据，方便没网时也能看清界面分支。
        Text("三态预览（模拟数据，不发请求）", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TKChip("有新版", false) {
                ui = UpdateUi(
                    simulated = true,
                    result = UpdateCheckResult(
                        state = UpdateState.UPDATE_AVAILABLE,
                        currentVersion = "2.7.0",
                        latest = ReleaseInfo(
                            tag = "v2.8.0",
                            name = "Liquid UI v2.8.0",
                            body = "## 更新内容\n\n- 新增 **F17** 弹窗与提示术语（12 条）\n- 新增套壳上手指南\n\n> 详见仓库 `docs/`",
                            publishedAt = "2026-11-02T08:00:00Z",
                            apkUrl = "https://example.invalid/liquidui-v2.8.0-release.apk",
                            apkName = "liquidui-v2.8.0-release.apk",
                            apkBytes = 1_979_392L
                        ),
                        message = "发现新版本 v2.8.0（当前 2.7.0）",
                        elapsedMillis = 412
                    )
                )
            }
            TKChip("已最新", false) {
                ui = UpdateUi(
                    simulated = true,
                    result = UpdateCheckResult(
                        state = UpdateState.UP_TO_DATE,
                        currentVersion = "2.8.0",
                        latest = ReleaseInfo("v2.8.0", "Liquid UI v2.8.0", "", "2026-11-02T08:00:00Z", null, null, -1),
                        message = "已是最新版本 v2.8.0",
                        elapsedMillis = 388
                    )
                )
            }
            TKChip("失败", false) {
                ui = UpdateUi(
                    simulated = true,
                    result = UpdateCheckResult(
                        state = UpdateState.FAILED,
                        currentVersion = AppUpdateChecker.currentVersionName(context),
                        latest = null,
                        message = "HTTP 403：多半是 API 限流（未带 token 时 60 次/小时/IP），稍后再试",
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
                            UpdateState.UPDATE_AVAILABLE -> "有新版"
                            UpdateState.UP_TO_DATE -> "已最新"
                            UpdateState.FAILED -> "失败"
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
                    if (state.simulated) "模拟态 · ${result.message}" else result.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TKKeyValue("耗时", "${result.elapsedMillis} ms")
            val latest = result.latest
            if (latest != null) {
                TKKeyValue("最新 tag", latest.tag)
                TKKeyValue("发布时间", latest.publishedAt)
                TKKeyValue("APK 附件", (latest.apkName ?: "（该 Release 没有 APK）") + " · " + AppUpdateChecker.humanSize(latest.apkBytes))
                if (latest.body.isNotBlank()) {
                    Text("更新日志（Markdown 渲染，见 U03）", style = MaterialTheme.typography.labelMedium)
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
                        Text("下载中 · ${AppUpdateChecker.humanSize(written)}（服务器未给大小）", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    TKButton(
                        label = if (state.apkPath != null) "重新下载并安装" else "下载 APK 并安装",
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
                                        error = if (installed) null else "已下载，但系统未授权「安装未知应用」"
                                    )
                                }.onFailure { t ->
                                    AppLog.e("UpdateDemo", "下载失败", t)
                                    ui = ui.copy(downloading = false, error = "下载失败：${t.javaClass.simpleName} ${t.message}")
                                }
                            }
                        }
                    )
                    if (state.simulated) TKNote("模拟态用的是假下载地址，下载按钮已禁用；点上面的「真实检查更新」拿到真 URL 后才能下载。")
                }
            }

            if (state.needInstallPermission) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TKButton("去授权安装未知应用", true) { AppUpdateChecker.openUnknownSourcesSettings(context) }
                    TKButton("用浏览器下载", true) { url?.let { AppUpdateChecker.openInBrowser(context, it) } }
                }
            }
            state.apkPath?.let { TKKeyValue("已落盘", it) }
            if (result.state == UpdateState.FAILED) {
                TKButton("重试", !state.checking) {
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
            "缓冲",
            "${all.size}/${AppLog.MAX_ENTRIES} 条 · D ${counts[AppLogLevel.DEBUG] ?: 0} · " +
                "I ${counts[AppLogLevel.INFO] ?: 0} · W ${counts[AppLogLevel.WARN] ?: 0} · E ${counts[AppLogLevel.ERROR] ?: 0}"
        )

        Text("写日志（真实写入 AppLog，同时进 logcat）", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TKButton("D 调试", true) { AppLog.d("Demo", "用户展开日志面板") }
            TKButton("I 信息", true) { AppLog.i("Demo", "切换主题为深色") }
            TKButton("W 警告", true) { AppLog.w("Demo", "磁盘缓存超过 80%") }
            TKButton("E 异常", true) {
                AppLog.e("Demo", "拉取更新失败", java.io.IOException("connect timed out"))
            }
        }
        TKNote("U01 的检查/下载流程也会往这里写日志——这就是「在真实调用点打日志」，不是自己打自己。")

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TKChip("全部", level == null) { level = null }
            AppLogLevel.entries.forEach { l -> TKChip(l.label, level == l) { level = if (level == l) null else l } }
        }
        OutlinedTextField(
            value = keyword,
            onValueChange = { keyword = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("按 tag 或正文过滤…") },
            singleLine = true
        )
        Text("命中 ${picked.size} 条（只显示最新 8 条，完整内容用复制/分享取）", style = MaterialTheme.typography.labelMedium)

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
                Text("（没有符合条件的日志）", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            TKButton("复制全文", true) {
                clipboard.setText(AnnotatedString(AppLog.dump(level, keyword)))
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                shareNote = "已复制 ${picked.size} 条到剪贴板"
            }
            TKButton("系统分享", true) {
                val intent = AppLog.buildShareIntent(context, level, keyword)
                val ok = runCatching { context.startActivity(Intent.createChooser(intent, "分享日志")) }.isSuccess
                AppLog.i("Demo", if (ok) "调起分享面板" else "没有可接收分享的应用")
                shareNote = if (ok) "已调起系统分享面板" else "分享失败：没有可接收的应用，请改用「复制全文」"
            }
            TKButton(if (confirmingClear) "确认清空？" else "清空", true) {
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
        TKNote("隐私红线：日志里不要出现 token、密码、带 query 的完整 URL、手机号——分享出去就是泄露。")
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

    val summary = buildString {
        append("解析出 ${blocks.size} 个块：")
        append(blocks.groupBy { it::class.simpleName ?: "?" }
            .entries.sortedByDescending { it.value.size }
            .joinToString("、") { "${it.key} × ${it.value.size}" })
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
        append(" · 行内节点 ${inlines.size}（链接 $links、行内码 $codes）")
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TKChip("更新日志", source == MD_CHANGELOG) { source = MD_CHANGELOG }
            TKChip("语法清单", source == MD_SYNTAX) { source = MD_SYNTAX }
            TKChip("隐私说明", source == MD_PRIVACY) { source = MD_PRIVACY }
        }
        TKKeyValue("解析结果", summary)

        TKButton(if (editing) "停止编辑" else "自己改文字试试", true) {
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
                label = { Text("Markdown 源码（边打字边渲染）") }
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
        TKNote("颜色全部走 MaterialTheme.colorScheme，切深色模式自动适配；链接点击交给 LocalUriHandler，代码块可长按选中复制。")
    }
}

// ---------------------------------------------------------------- U04 自适应导航

@Composable
private fun AdaptiveNavDemo() {
    val realClass = rememberAppWindowClass()
    var widthDp by remember { mutableFloatStateOf(420f) }
    var selected by remember { mutableIntStateOf(0) }
    val items = remember {
        listOf(
            AdaptiveNavItem("home", "首页", AppIcons.BlurOn),
            AdaptiveNavItem("gallery", "素材", AppIcons.Widgets),
            AdaptiveNavItem("motion", "动效", AppIcons.Animation),
            AdaptiveNavItem("settings", "设置", AppIcons.Settings)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TKKeyValue("当前真机窗口分级", "${realClass.label}（${realClass.name}）")
        TKKeyValue("演示容器宽度", "${widthDp.toInt()} dp → ${windowClassOf(widthDp.toInt()).label}")

        Text("拖动滑杆改变容器宽度，观察底部栏与侧边栏的切换", style = MaterialTheme.typography.labelMedium)
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
                            "id=${items[selected].id} · 分级=${windowClassOf(widthDp.toInt()).label} · " +
                                if (windowClassOf(widthDp.toInt()) == AppWindowClass.COMPACT) "底部 NavigationBar" else "侧边 NavigationRail",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TKNote("内容区永远占满剩余空间：宿主不需要为两种布局各写一套内边距。")
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
        TKNote("真机分级由 LocalConfiguration.screenWidthDp 决定：旋转、分屏、折叠屏展开都会自动重算，不需要监听器。")
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

