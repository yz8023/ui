# 批 F 清单：开源取材的第三批素材（S15–S18）

> 承接批 D（S01–S08，v2.11.0）与批 E（S09–S14，v2.12.0）。用户要求「继续」，
> 直接吃 `docs/开源素材清单-批E.md` §四 里**已核实过许可证**的候补队列。
>
> 硬口径与前两批完全一致：零新依赖（按上游公开规范独立实现）、真实行为（不许 HTML/截图/静态占位卡冒充）、
> 新代码中文零硬编码（`kt_scan` 合计不许上升，当前 193）、数据侧存稳定英文键、关于页申明来源、
> 提示词点按展开 + 长按复制 + 触感、真机未验不吹。
>
> 许可证/星标/归档状态/最近推送已于 2026-11-04 用 GitHub API 核实（批 E 那轮查的，本轮直接复用，未过期）。

## 一、为什么是这 4 项（防撞车核对）

先查现有 91 条素材再定范围。本轮**主动剔除了批 E 排在首位的 lottie**：

| 候选 | 处置 | 理由 |
|---|---|---|
| `airbnb/lottie-android`（关键帧时间轴与缓动曲线） | **剔除** | 缓动曲线部分与 **W02 令牌实验室**（时长 7 档／曲线 9 条／弹簧 6 组／错峰 5 种）正面撞车；只有「多轨关键帧时间轴」是新的，单独成项体量不够，留到与动效馆一起重构时再做 |
| `skydoves/Orbital`（轨道动画）／`skydoves/Cloudy`（实时模糊） | 剔除 | 动效与玻璃拟态本仓库已厚（W01–W03、F37–F42、G01–G06、F14 页间模糊） |
| `JetBrains/compose-multiplatform`（一套 UI 多端） | 暂缓 | 工程价值高但**不可预览**（要真多平台构建面才讲得清），不符合「每项都能预览」的要求 |
| `wasabeef/glide-transformations` | **入选 S15** | 91 条里没有任何图片变换素材 |
| `saket/telephoto` | **入选 S16** | 有「手势卡片 F40」「弹簧拖拽 F37」「X04 弹窗手势实验室」，但**没有可缩放看图**（双指缩放 + 双击 + 边界夹取 + 占位） |
| `material-components/material-web` | **入选 S17** | W02 令牌实验室管**动效**令牌，S01 管**色彩**令牌；**字阶／形状阶／间距**没人管，而这正是套壳改品牌时第一个要动的地方 |
| `square/leakcanary` | **入选 S18** | 91 条里没有任何内存／质量自检素材；S08 是无障碍自检，方向不同 |

## 二、本批 4 项

| ID | 名称 | 来源仓库 | 许可证 | ★ | 最近推送 | 借鉴什么 |
|---|---|---|---|---|---|---|
| S15 | 图片变换九宫格 | [`wasabeef/glide-transformations`](https://github.com/wasabeef/glide-transformations) | Apache-2.0 | 9873 | 2022-03-08 | 变换清单与参数口径：圆形／圆角／灰度／反色／双色调／亮度对比度／形状遮罩／旋转翻转／模糊 |
| S16 | 可缩放看图与占位 | [`saket/telephoto`](https://github.com/saket/telephoto) | Apache-2.0 | 1572 | 2026-07-22 | 双指缩放绕质心、双击切换、平移边界夹取、缩放上限下限、低清占位先顶上再换清晰图 |
| S17 | 字阶·形状阶·间距对照表 | [`material-components/material-web`](https://github.com/material-components/material-web) | Apache-2.0 | 11407 | 2026-11-03 | Type scale 15 档、Shape scale 7 档、4dp 间距栅格；**并当场比对出本仓库的偏离** |
| S18 | 内存泄漏与引用链自检 | [`square/leakcanary`](https://github.com/square/leakcanary) | Apache-2.0 | 30005 | 2026-10-21 | 引用链（GC Root → … → 泄漏对象）读法、八类常见泄漏模式、检测命令与修法 |

> `wasabeef/glide-transformations` 上游 **2022-03 后未更新**，且是 Glide 体系。本项取的是**变换清单与参数口径**（哪些变换值得做、每个变换的关键参数是什么），实现全部走 Compose 的 `ColorMatrix`／`ColorFilter`／`DrawScope.clipPath`／多趟叠加，不引 Glide。

### 逐项实现要点与关键参数

**S15 图片变换九宫格** — 底图仍是 `Canvas` 现场画（渐变 + 斜纹 + 同心圆，不引图片资源、不联网，与 S11 同口径）。九宫格每格一个变换，点某格下方出参数与实现要点：
① 圆形裁切 `clip(CircleShape)`；② 圆角裁切 12dp；③ 灰度 `ColorMatrix().setToSaturation(0f)`；④ 反色 5×4 矩阵（对角 −1、平移 +255）；⑤ 双色调 = 先灰度再暗部 `BlendMode.Modulate` 染色 + 亮部 `BlendMode.Lighten` 提色；⑥ 亮度／对比度 = 矩阵对角缩放 + 平移；⑦ 形状遮罩 `DrawScope.clipPath(星形 Path)`；⑧ 旋转翻转 `rotate(12f)`／`scale(-1f, 1f)`；⑨ 模糊 = **3×3 共 9 趟低透明度偏移叠加**近似（如实标注：真模糊要 `RenderEffect`，API 31+，本仓库 minSdk 26 不能只靠它）。
踩坑：`ColorFilter` 只能挂在 `DrawScope` 的绘制调用上，`graphicsLayer` 没有 `colorFilter`；`BlendMode` 叠加要配 `compositingStrategy = Offscreen` 才不会渗透到背景（批 D 已在遮罩挖孔上踩过同源坑）。

**S16 可缩放看图与占位** — ① `detectTransformGestures` 拿质心／平移／缩放，缩放绕质心：`offset = (offset - centroid) * (newScale/scale) + centroid + pan`；② 缩放夹在 1x–4x；③ 平移边界夹取 `|offset| ≤ (scale-1) × 容器边长 / 2`（中心为变换原点时），夹完要报告「已触边」；④ `detectTapGestures(onDoubleTap)` 在 1x 与 2.5x 之间切换，放大时以点击点为中心；⑤ 四态可切：占位／加载中／成功／失败，占位用 S15 的多趟叠加模糊画一张低清版顶上（telephoto 的 BlurHash 占位思路，零依赖近似），失败态给重试；⑥ 实时读出 scale／offset／是否触边，并给「回到 1x」。
踩坑：`detectTransformGestures` 与 `detectTapGestures` 要挂在**两个独立的 `pointerInput`** 上，写在同一个里会互相吃掉事件。

**S17 字阶·形状阶·间距对照表** — 这条的价值全在**真实比对**，不是抄一遍 M3 规格表：
① 字阶：15 档**当场从 `MaterialTheme.typography` 读现值**（不硬编码，改主题即跟着变），与 M3 建议值逐档比 size／lineHeight／tracking／weight，差值不为 0 的行标黄；
② 形状：M3 七档（none 0／xs 4／s 8／m 12／l 16／xl 28／full 50%）与本仓库 `OShinShapes`（`ui/theme/OShinTheme.kt:129`：xs 8／s 12／m 16／l 16／xl 24，无 none 与 full）并排画出来；
③ 间距：本仓库实测 dp 用量 TOP 12（`8.dp` 195 次、`10.dp` 174、`12.dp` 156、`16.dp` 136、`6.dp` 115、`14.dp` 96、`4.dp` 84、`22.dp` 50、`24.dp` 42、`20.dp` 33、`2.dp` 33、`1.dp` 21），标出**脱离 4dp 栅格的 10／14／22 共 320 次**；另标出绕过字阶的硬编码字号（`12.sp` 30 次、`11.sp` 14、`9.sp` 6、`10.sp` 6、`18.sp` 5、`16.sp` 5）；
④ 一个「只看待修正」筛选；⑤ 形状与间距的数值是**构建期人工实测的快照**，页面上写明测量命令与日期（2026-11-04），改主题后需要重测——这一点如实标注，不假装是运行时扫描。
已实测到的三个真偏离：字阶只定义了 12/15 档（缺 `headlineLarge`、`titleSmall`，走 Compose 默认值）；`large` 与 `medium` 都是 16dp（形状阶失去区分度）；`displayLarge` 52/56/−1.6 对 M3 的 57/64/−0.25（字距差 1.35sp，大屏标题会明显偏紧）。

**S18 内存泄漏与引用链自检** — ① 引用链可视化：按 LeakCanary 的读法画一条**本仓库真实形状**的链——`GC Root（线程／组合根）` → `AppSettingsState`（`ui/screens/AppSettingsState.kt:36`，构造参数写作 `private val prefs: Context`：**名叫 prefs、类型是 Context**，于是这个状态对象把 Context 一路带着走）→ `MainActivity` → 泄漏；每个节点标「谁持有谁」与「为什么断不掉」，并给出修法（改成持 `SharedPreferences`，或只在组合内取 `LocalContext.current`）。

② 八类常见模式清单，每条给**可复算的检测命令 + 粗 grep 命中数 + 逐条眼看后的真实数 + 一句修法**。本仓库 2026-11-04 实测：

| 模式 | 粗 grep 命中 | 眼看后真实 | 结论 |
|---|---|---|---|
| `GlobalScope` | 0 | 0 | 干净 |
| `object`/`companion object` 里持 Context/View/Activity/Bitmap | 0 | 0 | 干净 |
| 长生命周期对象持 Context | 1 | **1** | `AppSettingsState.kt:36`，唯一真疑点 |
| 注册/注销配对（`addListener`/`registerReceiver`/`observe`） | 0 / 0 | 0 / 0 | 干净 |
| `while (true)` 无限循环 | 7 | **0 风险** | 7 处全在 `LaunchedEffect` 或挂起手势检测器（`DragGestureDetector.kt`）里，随组合离开自动取消 |
| `remember` 里缓存 Bitmap | 0 | 0 | 干净 |
| WebView | 0 | 0 | 干净 |
| 自建 `CoroutineScope(…)`（需自己 cancel） | **17** | **0** | 17 处全是 `rememberCoroutineScope()`，随组合自动取消，不是自建作用域 |
| `android.os.Handler` / `java.util.Timer` | **9** | **0** | 9 处全是 Compose 的 `BackHandler(`，跟 `android.os.Handler` 无关；两个 import 全仓 0 次 |
| `getSystemService` | 3 | 3 | 正常用法，未静态缓存 |

③ **这条素材真正的结论不是「本仓库有 N 个泄漏」，而是「文本扫描的误报率有多高」**：上表两条最像风险的计数（17 与 9）眼看后**误报率 100%**（17/17、9/9），因为它们分别撞上了 `rememberCoroutineScope()` 与 `BackHandler(` 这两个同名但语义完全不同的 Compose API。所以「扫出 N 处」在逐条眼看之前**没有意义**——这也正是 LeakCanary 存在的理由：它靠运行时对象图与 GC 触发，不靠文本匹配。检测命令要用边界（`\bandroid\.os\.Handler`、排除 `BackHandler`、区分 `rememberCoroutineScope`）才有一点可信度。

④ **如实降级**：App 运行时扫不了自己的源码（那需要 LeakCanary 依赖，违反零依赖口径），所以这条是「构建期人工核实快照 + 检测方法教学」，页面上明说数据来自 2026-11-04 的构建期核实、改代码后要重测，不假装在运行时检测。

## 三、集成点（要动的文件）

| 文件 | 改什么 |
|---|---|
| `ui/features/ui2/previews/PreviewOssImage.kt`（新） | S15、S16 |
| `ui/features/ui2/previews/PreviewOssAudit.kt`（新） | S17、S18 |
| `ui/features/ui2/Ui2ExtraCatalog.kt` | 追加 4 条（分类键沿用 `oss_kit`） |
| `ui/features/ui2/Ui2PreviewHost.kt` | `when` 追加 4 条路由 **+ 4 个逐个 import**（批 E 就是漏了 import 才红的） |
| `ui/screens/InfoScreens.kt` | `allCredits` 27 → **31 条**（编号 28–31） |
| `res/values/strings.xml` | 4×4 条目录文案 + 预览页文案 + 4×4 条致谢 |
| `素材库/UI2体验馆/参考来源-总表.md` | +4 行来源、合计 28 → 32 |
| `docs/功能清单.md`、`docs/功能逐项手册.md` | 91 → **95 条**、46+45 → 46+49 |
| `docs/把本仓库当壳用-套壳上手指南.md` | `strings.xml` 条数同步 |
| `docs/开源素材清单-批F.md`（本文） | §四 回填落地结果 |

**不需要动**：`MIRRORS`（`Ui2*`/`InfoScreens`/`previews/PreviewOss*` 均不在表内）；`build.gradle.kts` 依赖块；`AndroidManifest.xml`。

## 四、落地结果（施工完成，2026-11-04）

| 项 | 结果 |
|---|---|
| 实现文件 | `PreviewOssImage.kt`（S15/S16，534 行）、`PreviewOssAudit.kt`（S17/S18，521 行），均按 §三 计划落地，未改名 |
| 目录注册 | `Ui2ExtraCatalog.kt` +4 条（分类键沿用 `oss_kit`，未新增分类）；`Ui2PreviewHost.kt` +4 条路由 **+4 条 import**（host 是逐个 import，批 E 曾因此 CI 红过） |
| 来源申明 | `InfoScreens.kt` 的 `allCredits` 27 → **31 条**（编号 28–31：glide-transformations／telephoto／material-web／leakcanary，URL 留在代码里）；`素材库/UI2体验馆/参考来源-总表.md` +4 行、+批 F 说明，合计改为「18 条开源取材素材／32 个上游来源」 |
| 资源 | `strings.xml` 2029 → **2147 条**（+118：预览页文案 86、目录条目 16、致谢 16） |
| 硬编码中文 | `kt_scan` 分类合计**仍是 193 条**（A108/B29/C3 10/D46），两个新文件贡献 0 条 |
| 依赖 | `build.gradle.kts` 依赖块**零改动**（未引 Glide／telephoto／LeakCanary） |
| 静态闸 | `probe-i18n.py` 退 0（启发式警告回到基线 9 条）、`probe-composable-scope.py` 退 0（越界 0）、`check-template.py` 退 0、XML 体检 118 条新资源占位符全合规、kotlinc 语法类 **0** |
| UI2 总数 | 91 → **95 条**（46 + 49） |
| 文档同步 | `功能清单.md`（91→95、46+45→46+49）、`功能逐项手册.md`（45→49 条、S01–S14→S01–S18、补两个新预览文件名，并顺手补上批 E 漏改的尾部说明）、`把本仓库当壳用-套壳上手指南.md`（2029→2147 两处） |

**实现过程中改掉的四处自己的错**（记下来防止重犯）：

1. **S18 的两条实测计数原本是错的**——粗 grep 得出「`Handler(|Timer(` 9 处」「自建 `CoroutineScope(` 17 处」，逐条眼看后发现 9/9 全是 Compose 的 `BackHandler(`、17/17 全是 `rememberCoroutineScope()`，真实值都是 **0**，误报率 100%。清单文档在提交前已改准，并把这条升格为 S18 的**主结论**（见 §二 S18 ③）：计数在眼看之前没有意义，检测命令必须带边界（`\bandroid\.os\.Handler`、`grep -v BackHandler`、`grep -v rememberCoroutineScope`）。
2. **目录文案的 `formatted` 判据被举例写的占位符骗过**——`ui2_s17_prompt` 里既有教学用的 `%1$s`，又有裸 `%.1f`／`50%`／`43%`，原判据「含 `%` 且无 `%N$` 才标 `formatted="false"`」因此漏标，`probe-i18n.py` 报 4 条非法 `%` 序列。目录文案永不带实参，判据改成「只要含 `%` 就一律标 `formatted="false"`」。
3. **形状阶的 sentinel 值撞车**——`-1` 同时表示「M3 的 full 档」与「本仓库未定义」，导致 `full` 档被判成两边一致（其实是 M3 有、本仓库没有）。改成 refDp 用 `-1` 表 full、repoDp 用 `-2` 表未定义，两个语义分开。
4. **内联 `if/else` 当 `stringResource` 的实参会被 i18n 探针误判成假属性**——S16 与 S17 共新增 4 条警告（基线 9 条 → 13 条）。一律上提成局部 val（`edgeText`／`definedText`／`refText`／`repoText`）后回到基线 9 条。

**两处如实降级**（页面上都明说了，不假装能力）：

- **S15 的模糊**：真模糊要 `RenderEffect.createBlurEffect`，API 31 起才有，本仓库 minSdk 26 → 用 3×3 共 9 趟、每趟 1/9 透明度、偏移 3.5px 的叠加近似，并写明大图会看出重影。
- **S18 的自检**：App 运行时扫不了自己的源码（那要引 LeakCanary，违反零依赖口径）→ 是「构建期人工核实快照 + 检测方法教学」，每个数字都标了快照日期 2026-11-04 与可复算命令，改完代码要重测。

**真机未验证**：以上全部是 CI／静态／资源层校验。九宫格各变换的实际观感（尤其双色调的 Modulate+Lighten 配比、9 趟近似模糊在真机分辨率下的重影程度）、S16 缩放的跟手度与双击锚点是否「指哪打哪」、S17 条形图与 15 档表格在小屏上的排版，都要真机才算验收。

## 五、批 G 队列（已核实许可证，下一批不用重查）

`airbnb/lottie-android`（Apache-2.0，★35766，只取多轨关键帧时间轴，缓动曲线部分已被 W02 覆盖）、
`JetBrains/compose-multiplatform`（Apache-2.0，★19469，需要先解决「不可预览」的问题）、
`detekt/detekt` + `cashapp/paparazzi` + `mockk/mockk`（Apache-2.0，★7106／2638／5766，**建议与批 A 合并做**：本仓库当前 0 测试文件）、
`adrielcafe/voyager`（MIT，★3091）+ `InsertKoinIO/koin`（Apache-2.0，★10025）+ `android/nowinandroid`（Apache-2.0，★22005，**留给分层批次**：screen/viewmodel/repository，本仓库状态仍写在 composable 里）、
`saket/telephoto` 的 BlurHash 部分（本批只做了低清占位近似）、`chrisbanes/tivi`（Apache-2.0，★6719，与 nowinandroid 重叠）、
`google/horologist`（Apache-2.0，★702，Wear 赛道）。

**仍未开工的两个结构性缺口**（比再加素材更要紧）：批 A 测试（0 测试文件）、批 D-架构分层（状态写在 composable，换页即丢）。
