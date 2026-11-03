# 批 E 清单：开源取材的第二批素材（S09–S14）

> 承接批 D（S01–S08，已随 v2.11.0 发布）。用户要求「功能还不够，增加更多的模板与素材类型」，
> 方向仍是**开发／UI／设计／交互／布局**，从 GitHub 开源仓库取材，**在「关于」页申明来源**，
> 每项都要**可预览的真实实现** + **可复制的提示词**（点按展开、长按复制 + 触感）。
>
> 本文是施工清单，先定范围再动工。许可证／星标／归档状态／最近推送**全部用 GitHub API `GET /repos/{owner}/{repo}` 逐条核实**（核实时间 2026-11-04），不是凭记忆写的——本轮核实就剔掉了 4 个归档或无许可证的仓库、6 个根本不存在的仓库名。

## 一、这批补的是什么缺口

先把「已经有什么」查清楚，避免撞车（现有 UI2 目录 **85 条**，其中 S01–S08 是批 D 的开源取材）：

| 缺口 | 现状 | 本批补 |
|---|---|---|
| 多级下拉菜单 | 有「面包屑」「抽屉导航」「侧边栏」，但**没有级联下拉**（点一级进下一级、可返回） | S09 |
| 列表行原地展开成详情 | 有「列表-详情双栏自适应」（S04，靠断点切栏），**没有原地放大/缩回**这种转场 | S10 |
| 图片裁剪 | 85 条里**完全没有**裁剪框（拖角、锁比例、旋转、三分线） | S11 |
| 可编辑富文本 | 有「输入框」「表单校验」，**没有带工具栏的富文本编辑**（粗斜体/列表/链接） | S12 |
| 交互状态层与海拔叠加 | S01 是色彩令牌与对比度，「令牌实验室」偏颜色/间距；**状态层不透明度与暗色海拔提亮没人讲** | S13 |
| 开源清单与许可证合规 | 「关于」页已有 21 条致谢（手写）；**没有「怎么从依赖自动生成这张表」的方法素材** | S14 |

S14 与用户的诉求直接对应：这批新增来源本身也要进「关于」页，顺手把「怎么维护这张表」做成素材。

## 二、硬口径（沿用批 D，一条不松）

1. **零新依赖**：只按开源项目的公开规范与交互定义**独立实现**，`build.gradle.kts` 依赖块不动。避免许可证污染，也避免构建面变化。
2. **真实行为**：级联菜单要真能进/退级；裁剪框要真能拖角并算出裁剪矩形；富文本要真能改 `TextFieldValue` 的样式区间；展开动画要真能缩回并还原滚动位置。不许 HTML、截图、静态占位卡冒充。
3. **新代码中文零硬编码**：写完即用 `stringResource`，`kt_scan` 分类合计**不许因为这批上升**（当前 193）。
4. **数据侧存稳定英文键**：分类/状态/模式一律英文键（`oss_kit`、`crop_free`…），文案查表——批 C 的规矩，`when` 与比较操作数不许用 `stringResource`。
5. **来源可追溯**：`InfoScreens.kt` 的 `allCredits` 追加（编号 **22–27**），每条含项目名/作者/用途/许可证/URL；`素材库/UI2体验馆/参考来源-总表.md` 同步登记。
6. **提示词统一范式**：`Ui2Feature.promptRes` 一条提示词，详情页沿用「点按展开 + 长按复制 + `HapticFeedbackType.LongPress`」。
7. **真机未验不吹**：动画时长、手势阈值、拖拽跟手度只能真机判定，文档标「待真机」。

## 三、本批 6 项

分类键沿用批 D 的 `oss_kit`（不新增分类，避免筛选芯片过多）。

| ID | 名称 | 来源仓库 | 许可证 | ★ | 最近推送 | 借鉴什么 |
|---|---|---|---|---|---|---|
| S09 | 级联菜单 | [`saket/cascade`](https://github.com/saket/cascade) | Apache-2.0 | 2104 | 2024-06-15 | 多级下拉：进下一级横向滑入、返回上一级、面包屑跳级、返回键先退级后关闭、宽度自适应 |
| S10 | 展开式列表→详情 | [`saket/InboxRecyclerView`](https://github.com/saket/InboxRecyclerView) | Apache-2.0 | 1958 | 2021-08-26 | 点行原地放大成详情（非页面转场）、其余行按距离位移淡出、返回缩回并还原 `LazyListState` |
| S11 | 图片裁剪框 | [`CanHub/Android-Image-Cropper`](https://github.com/CanHub/Android-Image-Cropper) | Apache-2.0 | 1505 | 2026-04-28 | 四角/四边拖拽、比例锁定（自由/1:1/4:3/16:9）、旋转 90°、三分网格线、裁剪外遮罩、实时输出尺寸 |
| S12 | 富文本编辑器 | [`MohamedRejeb/compose-rich-editor`](https://github.com/MohamedRejeb/compose-rich-editor) | Apache-2.0 | 1867 | 2026-11-02 | 工具栏（粗/斜/下划线/删除线/列表/链接）、选区样式 toggle、`AnnotatedString` 区间叠加、纯文本导出 |
| S13 | 状态层与海拔叠加 | [`androidx/androidx`](https://github.com/androidx/androidx)（Compose Material3） | Apache-2.0 | 6129 | 2026-11-03 | 状态层不透明度 hover 8%／focus 12%／pressed 12%／dragged 16%、层色取 `onSurface`、暗色按海拔提亮表面 |
| S14 | 关于页开源清单与许可证合规 | [`mikepenz/AboutLibraries`](https://github.com/mikepenz/AboutLibraries) | Apache-2.0 | 4491 | 2026-11-02 | 从依赖元数据生成库清单、许可证归类与冲突提示（GPL 与 MIT 不兼容）、关于页排版、缺失许可证要显式标出 |

### 逐项实现要点与关键参数

**S09 级联菜单** — 三级数据（分类 → 子类 → 具体项），`AnimatedContent` + `slideInHorizontally` 做层级切换，方向随进入/返回反转；顶部面包屑显示当前路径，点任意一级跳回；选中末级项回填到输入框并显示完整路径；`BackHandler(enabled = depth > 0)` 让返回键先退级、顶级才关闭。
参数：菜单宽 220dp、项高 48dp、层级动画 200ms、圆角 12dp、最多 3 级（超出显示「层级过深应改设计」的提示——这是规范要点，不是偷懒）。
踩坑：`Popup`/`Dialog` 里的返回键别直接杀页面；用 `onDismissRequest` 收口。

**S10 展开式列表→详情** — 5 行列表，点行用 `graphicsLayer` 的 scale/translation + 圆角插值放大到全屏详情（纯 Compose，不用共享元素 API，兼容 minSdk 26）；其余行按距离做位移与淡出；返回时反向播放并把 `LazyListState` 还原到点击前的 index/offset；详情内层 `LazyColumn` 真可滚；一个「显示动画参数」浮层实时打印 progress/scale/translation。
参数：动画 320ms、`FastOutSlowInEasing`、终态圆角 0dp、其余行位移系数 0.35 × 行高、淡出到 0.2。
踩坑：还原滚动位置必须在动画**结束后**再 `scrollToItem`，否则被动画的 layout pass 打断；点击瞬间先记 `firstVisibleItemIndex/Offset`。

**S11 图片裁剪框** — 底图用 `Canvas` 现场画（渐变 + 网格 + 文字，**不引图片资源、不联网**），裁剪框四角/四边可拖（`detectDragGestures` + 命中判定 24dp），比例锁定四档（自由/1:1/4:3/16:9，切换时以中心为基准重算矩形并夹在图内），旋转 90° 交换宽高，三分网格线与裁剪外半透明遮罩（遮罩挖孔**不用** `BlendMode.Clear`，用四条矩形拼——批 D 已踩过这个坑），底部实时显示裁剪矩形的 dp 值与按比例换算后的输出像素。
参数：最小裁剪框 48dp、手柄命中区 24dp、遮罩 45% 黑、网格线 1dp/30% 白、比例档 `free/1_1/4_3/16_9`。
踩坑：拖角时要同时夹住「不小于最小尺寸」和「不出界」两个约束，顺序反了会抖。

**S12 富文本编辑器** — `BasicTextField` + `TextFieldValue`，样式以 `SpanStyle` 区间列表存（`List<Pair<IntRange, SpanStyle>>`），工具栏六个动作按当前选区 toggle；渲染时把区间合并成 `AnnotatedString`；支持有序/无序列表（按行首标记实现，不引依赖）与链接（选区加 `LinkAnnotation`/下划线+主色）；「导出纯文本」按钮把内容复制到剪贴板（长按复制范式一致）。
参数：工具栏按钮 40dp、样式区间冲突时后写覆盖、选区为空时动作作用于当前词。
踩坑：`TextFieldValue` 的 selection 在文本变化后要夹回合法区间，否则越界崩溃。

**S13 状态层与海拔叠加** — 一块可交互表面，四种状态各叠一层并显示合成不透明度（多层同时命中时按 `1-(1-a)(1-b)` 合成，不是相加）；海拔 0/1/2/3/4/5 六档并排，暗色下按近似式提亮表面、亮色下靠阴影；「叠层可视化」开关把状态层染成半透明红看清边界；计算式与取值明文展示。
参数：hover 8%／focus 12%／pressed 12%／dragged 16%；层色 `onSurface`（容器内用 `onSurfaceVariant`）；暗色海拔叠加近似 `alpha ≈ 4.5 × ln(elevation + 1)%`（**标注为近似式，待真机与官方实现比对**）；状态层要盖在内容之上、圆角之内（`clip(shape)` 后再 `background`）。
踩坑：多状态叠加别用加法（12%+12% ≠ 24%，要按不透明度合成）。

**S14 关于页开源清单与许可证合规** — 一个可交互的「依赖 → 致谢表」生成器：① 内置本仓库真实依赖清单（composeBom 2024.10.01、kotlin 2.3.10、AGP 8.7.3、Gradle 8.11.1 等）与批 D/E 取材的 14 个仓库，每条带许可证；② 按许可证归类统计（Apache-2.0 / MIT / GPL-3.0 / 无许可证），GPL 与 MIT 同存时**显式标红提示兼容性风险**（本仓库 MIT，批 B 参考过 GPL-3.0 的 KernelSU-Style-UI-Kit，只学规范不抄代码——这条风险要写出来）；③ 无许可证的仓库显示「默认保留所有权利，不可用」（本轮真的剔除了 `saket/press` 与 `hackware1993/MagicIndicator`）；④ 一键生成致谢文本并复制到剪贴板（格式与「关于」页一致）；⑤ 展示本仓库「关于」页 21→27 条致谢的维护流程。
参数：许可证键 `apache_2_0/mit/gpl_3_0/none/other`（英文键，不存中文）；兼容性矩阵 MIT↔Apache-2.0 相容、GPL-3.0→MIT 不相容。
踩坑：这条素材的内容会随依赖变化，**不要写死条数**，用列表长度算。

## 四、已核实但本轮不做（候补队列，许可证已查，下一批不用重查）

| 仓库 | 许可证 | ★ | 最近推送 | 不做的理由 |
|---|---|---|---|---|
| `airbnb/lottie-android` | Apache-2.0 | 35766 | 2026-02-15 | 关键帧时间轴与缓动曲线实验室：价值高，但本批额度已满，**批 F 首选** |
| `square/leakcanary` | Apache-2.0 | 30005 | 2026-10-21 | 泄漏引用链自检：开发向素材，批 F |
| `JetBrains/compose-multiplatform` | Apache-2.0 | 19469 | 2026-11-03 | 一套 UI 多端：工程向，需要多平台构建面，批 F 评估 |
| `wasabeef/glide-transformations` | Apache-2.0 | 9873 | 2022-03-08 | 图片变换九宫格（圆角/圆形/灰度/duotone/遮罩）：上游 2022 年后未更新，取的是变换清单不是代码，批 F |
| `detekt/detekt` + `cashapp/paparazzi` | Apache-2.0 | 7106 / 2638 | 2026-11-03 | 静态检查规则集 + 视觉回归：**与批 A（测试）合并做更合适**，本仓库当前 0 测试文件 |
| `adrielcafe/voyager` + `InsertKoinIO/koin` | MIT / Apache-2.0 | 3091 / 10025 | 2026-06-06 / 2026-11-03 | 导航与屏幕模型分层：**批 D-架构（screen/viewmodel/repository）的主参考**，与 `android/nowinandroid` 一起留给分层批次 |
| `saket/telephoto` | Apache-2.0 | 1572 | 2026-07-22 | 可缩放看图 + BlurHash 占位：与「手势卡片」「X04 弹窗手势实验室」部分重叠，批 F 再判 |
| `skydoves/Orbital` | Apache-2.0 | 1203 | 2025-10-15 | 轨道动画：本仓库动效素材已厚（W01–W03、F37–F42、动效配方 20 条） |
| `skydoves/Cloudy` | Apache-2.0 | 1292 | 2026-09-13 | 实时模糊：已有「模糊管线」与 F14 页间模糊 |
| `material-components/material-web` | Apache-2.0 | 11407 | 2026-11-03 | 字阶/形状阶/动效令牌：与「令牌实验室」「动效规范索引 25」重叠度高，需先比对再定 |
| `mockk/mockk` | Apache-2.0 | 5766 | 2026-11-02 | 单测替身：与批 A（测试）合并做 |
| `google/horologist` | Apache-2.0 | 702 | 2026-11-03 | Wear OS 方向，与本仓库（手机 UI 模板库）不同赛道 |
| `chrisbanes/tivi` | Apache-2.0 | 6719 | 2024-11-02 | 整 App 架构，与 nowinandroid 重叠，不单独成项 |
| `coil-kt/coil` / `skydoves/landscapist` | Apache-2.0 | 11955 / 2587 | 2026-10-31 | 图片加载：要真图真网络才有意义，批 D 已判定不做 |

### 本轮剔除（不能用，附实测理由）

| 仓库 | API 实测结果 | 处置 |
|---|---|---|
| `ernestoyaquello/VerticalStepperForm` | Apache-2.0 但 **archived = true**（2025-06-12） | 剔除；且与现有「步进器」「表单校验」重复 |
| `hackware1993/MagicIndicator` | **license = 无**（★9790） | 剔除；无许可证即默认保留所有权利，不能声明为可用来源；且与「标签页」重复 |
| `androidx/constraintlayout` | Apache-2.0 但 **archived = true**（2023-10-24） | 剔除；Compose foundation 的 `Layout`/`FlowRow` 已能表达同类约束 |
| `robolectric/robolectric` | **license = NOASSERTION** | 剔除；许可证不明确的不写进来源声明 |
| `saket/press` | **license = 无**（★1854） | 剔除（同上） |
| `Kunzisoft/Android-SwitchDateTimePicker` | Apache-2.0，★384，2021-09-07 | 剔除；星少且 4 年未更新，与现有「选择器」重复 |
| `huanghaibin-dev/CalendarView` | Apache-2.0，★9417 | 剔除；与 S03「月历与日期区间选择」重复 |
| `saket/reveal-swipe`、`SmartToolfactory/Compose-MotionLayout`、`SmartToolfactory/Compose-Charts`、`valentinilk/ComposeShimmer`、`fornewid/material-color-utilities`、`halilozercan/compose-better-date-time-picker` | **API 404，仓库不存在** | 剔除；凭记忆写的仓库名有 6 个是错的，全部以 API 实测为准 |

> **教训（与批 D 一致，再记一遍）**：来源声明是要发布给用户看的东西，仓库名、许可证、归档状态**必须逐个用 API 核实**。本轮 18 个候选里 6 个不存在、3 个无许可证或许可证不明、2 个已归档。

## 五、集成点清单（要动的文件）

| 文件 | 改什么 |
|---|---|
| `ui/features/ui2/previews/PreviewOssWidgets.kt`（新） | S09、S10、S11、S12 的真实实现 |
| `ui/features/ui2/previews/PreviewOssSystem.kt`（新） | S13、S14 |
| `ui/features/ui2/Ui2ExtraCatalog.kt` | 追加 6 条 `Ui2Feature("S09"…"S14", "s09"…"s14", …, "oss_kit", 难度, …)` |
| `ui/features/ui2/Ui2PreviewHost.kt` | `when` 追加 6 条路由 |
| `ui/screens/InfoScreens.kt` | `allCredits` 追加 6 条（编号 **22–27**），url 留在代码里 |
| `res/values/strings.xml` | 6×4 条素材文案（name/summary/params/prompt）+ 预览页文案 + 6×4 条来源申明 |
| `素材库/UI2体验馆/参考来源-总表.md` | 登记 6 个新来源（含许可证与核实日期） |
| `docs/功能清单.md`、`docs/功能逐项手册.md` | 追加 S09–S14 |
| `docs/把本仓库当壳用-套壳上手指南.md` | UI2 总数 85 → **91**、`strings.xml` 条数同步 |
| `docs/开源素材清单-批E.md`（本文） | §七 回填落地结果 |

**不需要动**：`MIRRORS`（`Ui2ExtraCatalog`/`Ui2PreviewHost`/`InfoScreens`/`previews/PreviewOss*` 均不在表内，批 D 已核实）；`build.gradle.kts` 依赖块（零新依赖）；`AndroidManifest.xml`。

## 六、验收口径（每条都要过）

- CI `:app:assembleDebug` 两个 run 全绿；
- `scripts/probe-i18n.py` 退 0（新增 `stringResource` 上下文合法、实参个数对、格式串合法）；
- `scripts/probe-composable-scope.py` 退 0（批 D 新增的常驻探针）；
- `scripts/check-template.py` 退 0（未声明引用扫描 = 所有 `R.string.*` 真实存在；镜像逐行相同）；
- `strings.xml` 无重名／无空值／键名全 ASCII／往返解析逐字节一致；
- kotlinc 语法类错误 0，且相对 HEAD 基线**无新增错误签名**；
- `kt_scan` 分类合计**不上升**（当前 193）；
- About 页 6 条新来源可见、可点、URL 正确（21 → 27 条）；
- 提示词长按复制 + 触感可在代码里核对（`combinedClickable` + `HapticFeedbackType.LongPress`）；
- **真机未验证**：以上全是 CI／静态／资源层校验。裁剪拖拽跟手度、展开动画时长、状态层观感都要真机才算验收。

## 七、落地结果（施工完成后回填）

| 项 | 结果 |
|---|---|
| 实现文件 | `PreviewOssWidgets.kt`（S09/S10/S11/S12）、`PreviewOssSystem.kt`（S13/S14），均按 §五 计划落地，未改名 |
| 目录注册 | `Ui2ExtraCatalog.kt` +6 条（分类键沿用 `oss_kit`，未新增分类）；`Ui2PreviewHost.kt` +6 条路由 |
| 来源申明 | `InfoScreens.kt` 的 `allCredits` 21 → **27 条**（编号 22–27，URL 留在代码里）；`素材库/UI2体验馆/参考来源-总表.md` +6 行、+批 E 说明，并把批 D 漏改的「合计 14 个上游来源」修正为 28 |
| 资源 | `strings.xml` 1890 → **2029 条**（+139：预览页文案 91、目录条目 24、致谢 24） |
| 硬编码中文 | `kt_scan` 分类合计**仍是 193 条**——两个新文件贡献 0 条（中途一度升到 198，是 `LicEntry` 的仓库标签混进了中文，全部改 ASCII 后回落） |
| 依赖 | `build.gradle.kts` 依赖块**零改动** |
| 静态闸 | `probe-i18n.py` 退 0、`probe-composable-scope.py` 退 0（越界 0 处）、`check-template.py` 退 0、kotlinc 语法类 **0** |
| UI2 总数 | 85 → **91 条**（46 + 45） |
| 文档同步 | `功能清单.md`（85→91、46+39→46+45）、`功能逐项手册.md`（39→45 条并补 S01–S14 枚举与 6 个预览文件名）、`把本仓库当壳用-套壳上手指南.md`（1890→2029） |

**实现过程中改掉的四处自己的错**（记下来防止重犯）：

1. **`graphicsLayer` 的 `scaleY` 会把详情内容纵向拉长变形**——S10 一开始把 InboxRecyclerView 的「放大」直译成缩放，文字会跟着变形。改成动画**布局高度**（72dp → 容器高），内容过半程淡入，圆角同步插值；位移交给布局自然完成，不手动 `translationY`。
2. **`detectDragGestures` 的 `dragAmount` 是相邻两次的增量，不是从按下起的总位移**——S11 一开始累加增量，拖动会漂。改用 `change.position - dragOrigin`。
3. **格式串不支持精度**：`scripts/probe-i18n.py` 的 token 正则只认 `%N$x`，`%1$.1f`／`%1$.2f` 会被判「含非法 % 序列」。精度改在 Kotlin 侧用 `String.format(Locale.US, …)` 预格式化，资源里只留 `%1$s`。
4. **施工脚本的 `replace(old, new, 1)` 与 `assert count == n` 不配套**：断言放行了 2 处命中、替换只改了第 1 处，漏掉一处中文标签（`kt_scan` 193 → 194 才发现）。凡是 `n > 1` 的补丁，替换也必须带上 `n`。

**S12 的一处如实降级**：零依赖下 `BasicTextField` 不能在编辑态渲染 `SpanStyle`，所以是「纯文本编辑 + 下方预览区实时渲染样式」，页面上已明说这是降级不是偷懒（与批 D 的 S08 同一处理方式）。

**真机未验证**：以上全部是 CI／静态／资源层校验。裁剪拖拽的跟手度、展开动画 320ms 的观感、状态层 8/12/16% 的实际视觉、暗色海拔叠加近似式（`4.5 × ln(elev+1)%`）与官方实现的偏差，都要真机才算验收。
