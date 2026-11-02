package com.monkeycode.liquidui.ui.features.toolkit

/**
 * 工具与基建（手册 F18–F21 / 条目 U01–U04）的数据与提示词。
 *
 * U 编号独立于手册 F 编号、UI2 的 F/G/M/V/X/W/T 编号、数据查询 Q 编号、弹窗术语 P 编号；
 * 引用时写「U01」或「手册 F18」，不要混写。
 *
 * 这四条不是「界面样式」，而是**做完一个 App 迟早要写的基建**：更新检查、日志分享、
 * 文档渲染、大屏适配。实现全部在本包内，都是真实可运行的代码，不是示意图：
 * - U01 [AppUpdateChecker]：真发 HTTPS 请求打 GitHub Releases API、真下载、真调安装器
 * - U02 [AppLog]：真环形缓冲 + 真 `ACTION_SEND` 分享
 * - U03 [MarkdownParser] / [MarkdownText]：真解析真渲染（零第三方依赖）
 * - U04 [AdaptiveAppShell]：真按窗口宽度切 `NavigationBar` / `NavigationRail`
 */
data class ToolkitEntry(
    val id: String,
    val name: String,
    val en: String,
    val category: String,
    val difficulty: String,
    val summary: String,
    val why: String,
    val keyImpl: String,
    val pitfalls: String,
    val params: String,
    val prompt: String
)

/** 目录页的分类筛选项。 */
val ToolkitCategories = listOf("联网与发布", "可观测性", "内容渲染", "布局适配")

val ToolkitEntries = listOf(
    ToolkitEntry(
        id = "U01",
        name = "应用内更新检查",
        en = "In-app Update Check",
        category = "联网与发布",
        difficulty = "C ★★★",
        summary = "拉 GitHub Releases 最新版，比版本号，给「有新版/已最新/失败」三态，能真下载真安装。",
        why = "侧载分发（不上应用商店）的 App 必须有自更新，否则用户永远停在旧版。" +
            "常见错误是只做「打开网页让用户自己下」，或者只比 versionCode 不比语义版本，" +
            "再或者请求失败时什么都不显示——用户分不清「已是最新」和「网断了」。",
        keyImpl = "① `HttpURLConnection` GET `api.github.com/repos/{owner}/{repo}/releases/latest`，" +
            "`org.json` 解析（都是框架自带，不加依赖）；② 版本号比较按 `.` 拆段逐位比，忽略前导 v、缺位补 0，" +
            "这样 2.10.0 > 2.9.0 才不会判错；③ 当前版本从 `PackageManager.getPackageInfo().versionName` 读，" +
            "不依赖 BuildConfig，宿主不用开 `buildConfig = true`；④ 从 assets 里挑 `.apk` 附件拿 `browser_download_url` 与 `size`；" +
            "⑤ 下载写应用私有 `cacheDir/updates/`，先写 `.part` 再 `renameTo`，边下边报进度，" +
            "下完比对 `Content-Length` 防半包；⑥ 安装走 `FileProvider.getUriForFile` + `ACTION_VIEW` + " +
            "`application/vnd.android.package-archive` + `FLAG_GRANT_READ_URI_PERMISSION`；" +
            "⑦ Android 8+ 先查 `packageManager.canRequestPackageInstalls()`，未授权就跳" +
            "`ACTION_MANAGE_UNKNOWN_APP_SOURCES` 授权页，并提供「用浏览器下载」兜底。",
        pitfalls = "× 用 versionCode 或字符串直接比大小（\"2.9.0\" > \"2.10.0\" 会判错）；" +
            "× 把网络请求写在主线程（NetworkOnMainThreadException）；" +
            "× 失败态与最新态共用一句文案，用户无法判断该重试还是该放心；" +
            "× APK 下到公共下载目录（Android 10+ 分区存储要权限，且卸载不清理）；" +
            "× 直接用 `Uri.fromFile` 传 APK（Android 7+ 抛 FileUriExposedException）；" +
            "× 未授权「安装未知应用」时静默失败，用户点了没反应；" +
            "× 把 GitHub token 硬编码进客户端（等于公开，谁都能拿来打你的 API 额度）。",
        params = "连接超时 8s / 读取超时 12s；下载读取超时 60s、缓冲 64KB；" +
            "APK 匹配规则：优先 `.apk` 后缀，其次名字含 release；" +
            "默认仓库 `yz8023/ui`（示例 App 检查的就是本模板自己的 Release）；" +
            "未带 token 时 GitHub API 限流 60 次/小时/IP，失败文案里已写明这一点。",
        prompt = """给 Android App 加「应用内更新检查 + 下载安装」，分发渠道是 GitHub Releases（不是应用商店）。

需求要点：
1. 请求 GET https://api.github.com/repos/{owner}/{repo}/releases/latest，用 HttpURLConnection + org.json，不要引第三方网络库；请求必须在 Dispatchers.IO 上。
2. 当前版本用 PackageManager.getPackageInfo(packageName, 0).versionName 读取，不要依赖 BuildConfig。
3. 版本比较按语义化版本：去掉前导 v，按 "." 拆成整数段逐位比较，缺位补 0（保证 2.10.0 比 2.9.0 新）。
4. 结果必须是显式三态枚举：UP_TO_DATE / UPDATE_AVAILABLE / FAILED，每种态给不同文案；FAILED 要区分无网（UnknownHostException）、超时（SocketTimeoutException）、TLS 失败、HTTP 403（API 限流）、HTTP 404（没有 Release），不要笼统写「检查失败」。
5. 从 assets 数组里挑 APK：优先 name 以 .apk 结尾，其次 name 含 release；取 browser_download_url 与 size，用 size 做进度分母。
6. 下载写到 context.cacheDir/updates/ 下，先写 xxx.apk.part，完成后 renameTo 正式名；边下边回调 (written, total)；total 为 -1（服务器没给 Content-Length）时进度条改为不确定态；下载完校验 written == total，不等就删文件并报错。
7. 安装用 FileProvider.getUriForFile(context, packageName + ".fileprovider", file) + Intent.ACTION_VIEW + setDataAndType(uri, "application/vnd.android.package-archive") + FLAG_ACTIVITY_NEW_TASK or FLAG_GRANT_READ_URI_PERMISSION。禁止 Uri.fromFile（Android 7+ 会抛 FileUriExposedException）。
8. Android 8+ 安装前先 packageManager.canRequestPackageInstalls()；未授权时不要静默失败：跳 Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES（data 为 package:包名）引导授权，并同时给一个「用浏览器下载」的兜底按钮。
9. 需要改的配置一并给出：Manifest 加 REQUEST_INSTALL_PACKAGES 权限、FileProvider 声明（authorities 为 包名.fileprovider，exported=false，grantUriPermissions=true）、res/xml/file_paths.xml 里加 <cache-path name="updates" path="updates/" />。
10. 界面：一张卡片显示「当前版本 / 最新版本 / 发布时间 / APK 大小 / 更新日志（Markdown）」，一个检查按钮（检查中转圈且禁用）、一个下载按钮（下载中显示百分比与已下 MB，可取消），下载完成自动调起安装。检查失败时卡片内显示错误原因与「重试」。

约束：GitHub token 不许硬编码进客户端；不要把 APK 下到公共目录；所有失败都要有用户可读的原因。"""
    ),
    ToolkitEntry(
        id = "U02",
        name = "运行日志与分享",
        en = "Log Collection & Share",
        category = "可观测性",
        difficulty = "C ★★☆",
        summary = "内存环形缓冲存应用自己的日志，可按级别/关键词过滤，一键复制或系统分享。",
        why = "用户报「点了没反应」时，logcat 你拿不到——Android 4.1 起普通应用读不了完整 logcat，" +
            "所以想让远程用户帮你复现问题，必须**应用自己存一份**再让他分享出来。" +
            "这也是 Kit 类模板把「日志分享」列为核心功能的原因。",
        keyImpl = "① 单例 `object AppLog` 持一个 `mutableStateListOf<AppLogEntry>`（SnapshotStateList，" +
            "Compose 界面能直接观察，写入即刷新）；② 上限 400 条，超出丢最旧的（环形缓冲语义）；" +
            "③ 四个级别 D/I/W/E，写入时**同时**打一份到 `android.util.Log`，开发期两边都能看到；" +
            "④ 每条带单调递增 seq，导出文本首行写明「共 n 条（缓冲 m/400）· 时间范围」，" +
            "对方一眼能看出日志有没有被截断；⑤ `dump(level, keyword)` 生成纯文本，" +
            "`buildShareIntent` 返回 `ACTION_SEND` + `text/plain` 的 Intent，" +
            "调用方用 `Intent.createChooser` 包一层并 `runCatching`（用户可能没装能接收的应用）；" +
            "⑥ 异常入日志时带 `javaClass.simpleName: message`，比只记一句「失败了」有用得多；" +
            "⑦ 需要跨进程/重启留存时，把 `AppLog.persist` 换成写 " +
            "`getExternalFilesDir(null)` 的实现，自己处理轮转与体积上限。",
        pitfalls = "× 只打 logcat 就指望用户能给你日志（拿不到）；" +
            "× 无上限地往列表里塞（长时间运行内存涨）；" +
            "× 把 token、密码、手机号、完整 URL 带 query 一起记进日志再分享出去（隐私事故）；" +
            "× 日志写公共目录（要存储权限，卸载还不清理）；" +
            "× 分享时不包 `runCatching`，用户没装邮件/聊天应用就直接崩；" +
            "× 用 `String` 拼接后 `mutableStateOf` 整块替换（大日志时每次写入都重建全文，界面卡顿）。",
        params = "缓冲上限 400 条；时间格式 `MM-dd HH:mm:ss.SSS`（Locale.US，避免设备上非公历导致解析歧义）；" +
            "导出为 `text/plain`，分享主题「应用日志 n 条」；默认不落盘（`persist` 为 null）。",
        prompt = """给 Android App 加「运行日志收集 + 一键分享」，目的是让用户报 bug 时能把日志发给我。

需求要点：
1. 单例 object AppLog，内部用 androidx.compose.runtime.mutableStateListOf 存日志条目，让 Compose 界面能直接观察并自动刷新；不要用 String 拼接后整体替换 state。
2. 条目字段：seq（单调递增 Long）、timeMillis、level（枚举 D/I/W/E）、tag、message。提供 d/i/w/e 四个便捷方法，e 支持传 Throwable，记录为 "message\n异常类名: message"。
3. 环形缓冲：上限 400 条，超出时移除最旧的一条；写入用 @Synchronized 保证多线程安全。
4. 每条日志同时写一份到 android.util.Log（对应 d/i/w/e），开发期 logcat 也能看到。
5. 只存内存不写文件，因此不需要任何存储权限；但要留一个 var persist: ((AppLogEntry) -> Unit)? 钩子，宿主需要落盘时可以换成写 getExternalFilesDir 的实现。
6. dump(level: AppLogLevel?, keyword: String): String 导出纯文本：首行是摘要「应用日志导出 · 共 n 条（缓冲 m/400） · 起始时间 → 结束时间」，可选带「仅 X 级」「关键词「xx」」；之后每行格式为 "MM-dd HH:mm:ss.SSS seq L/tag: message"，时间格式用 SimpleDateFormat + Locale.US。
7. buildShareIntent(context, level, keyword) 返回 Intent.ACTION_SEND、type = "text/plain"、EXTRA_SUBJECT 为「应用日志 n 条」、EXTRA_TEXT 为 dump 结果，并加 FLAG_ACTIVITY_NEW_TASK。调用方用 Intent.createChooser 包一层，外面套 runCatching（用户可能没装能接收的应用），失败时提示改用「复制」。
8. 界面：一个日志面板（底部弹窗或独立页），顶部显示各级别条数统计（如 共 128 · E 2 · W 5），可按级别筛选（全部/D/I/W/E）与关键词搜索，列表倒序（最新在上），底部三个动作：复制到剪贴板、系统分享、清空。清空要有二次确认或提供撤销。
9. 隐私：日志里不许出现 token、密码、完整带 query 的 URL、手机号；在代码注释里写明这条红线，并在导出文本首行提示「日志可能包含设备信息，请自行确认后再发送」。
10. 在真实调用点打日志：网络请求起止与耗时、权限授予/拒绝结果、异常捕获、关键用户动作（如切换主题、发送通知）。不要只在日志页自己打自己。

约束：不要引第三方日志库；不要写公共存储目录；分享失败必须有兜底（复制）。"""
    ),
    ToolkitEntry(
        id = "U03",
        name = "Markdown 渲染",
        en = "Markdown Rendering",
        category = "内容渲染",
        difficulty = "C ★★☆",
        summary = "自写解析器 + Compose 渲染：标题/粗斜体/行内码/链接/围栏代码/列表/引用/分隔线，零第三方依赖。",
        why = "更新日志、说明页、隐私政策、README 展示都要渲染 Markdown。" +
            "常见做法是塞一个 WebView 显示 HTML——那样主题不跟随、字体不统一、" +
            "深色模式要另写一套 CSS，还得处理 WebView 的初始化开销与安全设置。" +
            "用 Compose 直接渲染，颜色全部走 `MaterialTheme.colorScheme`，主题切换零成本。",
        keyImpl = "① 解析分两层：块级（按行扫描）+ 行内（按字符扫描）；" +
            "块级产出密封接口 `MdBlock`（Heading/Paragraph/BulletList/OrderedList/CodeBlock/Quote/Rule），" +
            "行内产出 `MdInline`（Text/Bold/Italic/Code/Link）；" +
            "② 围栏代码块（``` 或 ~~~）内部**原样保留**，不进块级解析，否则代码里的 `#` 会被当标题；" +
            "③ 列表/引用是连续行聚合，聚合完要把行号回退一格（`i--`）交回外层自增，避免吃掉下一行；" +
            "④ 行内优先级：行内码 → 链接 → 粗体 → 斜体，用 `indexOf` 找闭合符，找不到就按普通字符处理" +
            "（这样未闭合的 `*` 不会把后文全吞成斜体）；" +
            "⑤ 渲染用 `buildAnnotatedString` + `withStyle(SpanStyle(...))`，" +
            "链接用 `withLink(LinkAnnotation.Url(url))`（Compose 1.7+ 稳定 API），" +
            "点击自动走 `LocalUriHandler`，不用自己写 Intent；" +
            "⑥ 代码块用 `SelectionContainer` 包住，长按可选中复制；横向超宽用 `horizontalScroll` 而不是折行；" +
            "⑦ 解析结果 `remember(source)` 缓存，同一篇文档不会每帧重解析。",
        pitfalls = "× 用 WebView 渲 Markdown（主题不跟随、深色模式另写一套、初始化开销大）；" +
            "× 围栏代码块内容也走块级解析（代码里的 `#`/`-` 被当成标题和列表）；" +
            "× 用正则一次性替换全部行内语法（嵌套与未闭合情况会互相踩，越改越乱）；" +
            "× 链接用 `ClickableText` + 手算 offset（已废弃，且 offset 计算容易错行）；" +
            "× 代码块不设横向滚动（长行把布局撑破或强行折行，代码没法读）；" +
            "× 每帧重新 parse（列表里滚动时明显掉帧）。",
        params = "支持：`#`~`######` 标题、`**粗**`、`*斜*`/`_斜_`、`` `行内码` ``、`[文字](链接)`、" +
            "``` / ~~~ 围栏代码块（带语言名）、`- * +` 无序列表、`1.`/`1)` 有序列表（支持起始编号）、" +
            "`>` 引用（连续行合并）、`---`/`***`/`___` 分隔线。" +
            "**不支持**：表格、图片、嵌套列表、任务列表、HTML 内联、脚注——" +
            "需要时要么扩解析器，要么引 `org.commonmark:commonmark`（约 200KB）。" +
            "行内代码 13sp 等宽、代码块 12sp/行高 18sp、引用左侧竖条 3dp。",
        prompt = """用 Jetpack Compose 实现一个 Markdown 渲染组件，不要引第三方库，也不要用 WebView。

需求要点：
1. 解析分两层。块级：按行扫描，产出密封接口 MdBlock 的实现——MdHeading(level, inlines)、MdParagraph(inlines)、MdBulletList(items)、MdOrderedList(items, start)、MdCodeBlock(language, code)、MdQuote(inlines)、MdRule。行内：产出密封接口 MdInline 的实现——MdText、MdBold、MdItalic、MdCode、MdLink(text, url)。
2. 支持的语法：# 到 ###### 标题；**粗体**；*斜体* 与 _斜体_；`行内代码`；[文字](链接)；``` 与 ~~~ 围栏代码块（可带语言名）；- * + 无序列表；1. 与 1) 有序列表（保留起始编号）；> 引用（连续多行合并成一段）；--- *** ___ 分隔线。
3. 围栏代码块内部原样保留，不要再做块级或行内解析（代码里的 # 和 - 不是标题和列表）。
4. 列表与引用要把连续行聚合成一个块，聚合结束后把行索引回退一格，交回外层循环自增，避免吃掉下一行。
5. 行内解析按优先级扫描：行内码 → 链接 → 粗体 → 斜体；用 indexOf 找闭合符，找不到闭合符就把当前字符当普通文本追加（未闭合的 * 不能把后文全吞成斜体）。斜体的 _ 只在后面不是字母数字时才闭合（避免把 snake_case 拆开）。
6. 渲染：块级用 Column + Arrangement.spacedBy(10.dp)；标题按级别映射 MaterialTheme.typography（1→headlineSmall、2→titleLarge、3→titleMedium、其余→titleSmall）并加粗；行内用 buildAnnotatedString + withStyle(SpanStyle(...))；链接用 withLink(LinkAnnotation.Url(url))，点击交给 LocalUriHandler，不要自己写 Intent；引用渲染为左侧 3dp 主色竖条 + 次要色文字（Row 用 height(IntrinsicSize.Min) + 竖条 fillMaxHeight）。
7. 代码块：等宽字体 12sp、行高 18sp，外层用 SelectionContainer 让长按可选中复制，横向用 horizontalScroll(rememberScrollState()) 而不是折行；有语言名时在块顶部显示语言标签；背景用 surfaceVariant 半透明 + 10dp 圆角。
8. 解析结果用 remember(source) 缓存，source 不变不重复解析。
9. 对外只暴露一个 @Composable fun MarkdownText(source: String, modifier: Modifier = Modifier)。
10. 明确写清不支持的语法（表格、图片、嵌套列表、任务列表、HTML 内联），并在 KDoc 里给出「需要时改引 org.commonmark:commonmark」的提示。

约束：颜色全部走 MaterialTheme.colorScheme（深色模式自动适配）；不要每帧重解析；不要用已废弃的 ClickableText。"""
    ),
    ToolkitEntry(
        id = "U04",
        name = "自适应导航（NavigationRail）",
        en = "Adaptive Navigation",
        category = "布局适配",
        difficulty = "C ★★☆",
        summary = "同一套路由：窗口 <600dp 用底部 NavigationBar，≥600dp 用侧边 NavigationRail，展开态带文字标签。",
        why = "折叠屏展开、平板、ChromeOS 窗口、桌面模式分屏下，底部导航条会占掉一大块纵向空间，" +
            "而且五个图标摊在 1200dp 宽上点起来很怪。Material 3 的答案是宽屏改用 NavigationRail。" +
            "不做适配的 App 在大屏上「能用但很业余」。",
        keyImpl = "① 分级枚举 `AppWindowClass`：COMPACT(<600dp) / MEDIUM(600–839) / EXPANDED(≥840)，" +
            "断点沿用 Material 3 官方数值；② 宽度取 `LocalConfiguration.current.screenWidthDp`——" +
            "这是**窗口**宽度，多窗口/分屏/折叠屏展开时 Activity 拿到的就是当前窗口尺寸，" +
            "旋转与形变会自动触发重组，不用自己监听；③ 不想加依赖就不用 " +
            "`material3-window-size-class`，想用官方版就把 `rememberAppWindowClass()` 换成 " +
            "`calculateWindowSizeClass()`，其余代码不动；④ 外壳 `AdaptiveAppShell`：COMPACT 走 " +
            "`Column(内容 weight(1f) + AdaptiveBottomBar)`，其余走 " +
            "`Row(AdaptiveSideRail + 内容 weight(1f))`，内容区永远拿整块剩余空间，" +
            "不需要自己算内边距；⑤ 侧栏文字标签只在 EXPANDED 显示（MEDIUM 宽度宝贵，只留图标），" +
            "label 参数**始终传 lambda**、内部按开关决定渲不渲染，避免条件式返回 @Composable lambda 造成类型推断问题；" +
            "⑥ 底部栏 `alwaysShowLabel = items.size <= 4`：五个及以上目的地时只给选中项显示文字，" +
            "否则标签会挤成两行；⑦ 选中下标由宿主持有并存 `rememberSaveable`，旋转/形变不丢当前页。",
        pitfalls = "× 用 `LocalConfiguration.screenWidthDp` 之外的「屏幕物理宽度」判断（分屏时判错）；" +
            "× 只在 `onCreate` 判一次宽度，旋转/折叠屏展开后不更新；" +
            "× 宽屏继续用底部导航（大屏上图标间距过大、纵向空间浪费）；" +
            "× 侧栏与内容用固定 dp 宽度拼（不同窗口尺寸下溢出或留白）；" +
            "× NavigationRailItem 的 label 传 `null` 与传空 lambda 混着写，导致类型推断报错；" +
            "× 忘了给 `contentDescription`，无障碍服务读不出目的地名称；" +
            "× 切换布局时丢掉选中页（selectedIndex 没存 saveable）。",
        params = "断点 600dp / 840dp；侧栏内边距上下 8dp、条目间距 4dp；" +
            "侧栏容器色 `surface`；底部栏标签上限 1 行；" +
            "演示页提供 360–1000dp 的宽度滑杆，手机上也能实时看到两种布局切换。",
        prompt = """用 Jetpack Compose 实现自适应导航外壳：窄屏底部导航、宽屏侧边 NavigationRail，同一套路由数据，不要写两份页面。

需求要点：
1. 定义分级枚举 AppWindowClass：COMPACT（<600dp）、MEDIUM（600–839dp）、EXPANDED（≥840dp），每个带 label 与 minWidthDp；提供纯函数 windowClassOf(widthDp: Int) 做分级，方便写单元测试。
2. 宽度来源用 LocalConfiguration.current.screenWidthDp（这是窗口宽度，分屏/折叠屏/桌面窗口都对），封装成 @Composable fun rememberAppWindowClass()。不要引 material3-window-size-class 依赖，但要在注释里写明：宿主想用官方版就把它换成 calculateWindowSizeClass()，其余代码不动。
3. 导航项数据类 AdaptiveNavItem(id: String, label: String, icon: ImageVector)；id 用于状态保存，label 用于无障碍与宽屏文字。
4. 外壳 @Composable fun AdaptiveAppShell(items, selectedIndex, onSelect, modifier, windowClass = rememberAppWindowClass(), showLabelsOnRail = windowClass == EXPANDED, content: @Composable () -> Unit)：COMPACT 时用 Column（内容 Box 占 weight(1f)，底部放 AdaptiveBottomBar）；其余用 Row（左侧 AdaptiveSideRail，内容 Box 占 weight(1f) + fillMaxHeight）。内容区必须拿到整块剩余空间，不要让调用方自己算内边距。
5. AdaptiveBottomBar 用 NavigationBar + NavigationBarItem（icon + label，label 限 1 行），alwaysShowLabel = items.size <= 4（目的地多于 4 个时只给选中项显示文字）。
6. AdaptiveSideRail 用 NavigationRail + NavigationRailItem，containerColor = MaterialTheme.colorScheme.surface，contentPadding 上下 8dp，条目间距 4dp；showLabels 为 true 时显示文字标签。注意：label 参数要始终传一个 lambda、在 lambda 内部按 showLabels 决定是否渲染 Text，不要写「条件式返回 @Composable lambda 或 null」，那样类型推断容易报错。
7. 两个子组件都要单独 public 暴露，方便宿主套自己的 Scaffold 或加顶部栏。
8. selectedIndex 由调用方持有，文档里提醒用 rememberSaveable 存，旋转与形变不丢当前页；形变时布局自动切换，不需要监听器。
9. 每个图标都要有 contentDescription = label，保证无障碍可用。
10. 另外给一个演示：用 BoxWithConstraints + 宽度滑杆（360–1000dp）把外壳放进受限宽度的容器里，让手机上也能实时看到「底部栏 ↔ 侧边栏」的切换，并显示当前分级与宽度数值。

约束：不要重复写两份页面内容；不要用固定 dp 宽度拼侧栏与内容；颜色走 MaterialTheme.colorScheme。"""
    )
)
