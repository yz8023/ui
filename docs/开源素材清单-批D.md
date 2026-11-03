# 批 D 清单：开源取材的 8 类新素材（S01–S08）

> 立项依据：用户要求「增加更多的模板与素材类型，从 GitHub 开源仓库里找关于开发／UI／设计／交互／布局方面的，在关于里申明来源；每一个都要可预览 + 可复制提示词，要求与前面一致」。
>
> 本文是**施工清单**，先定范围再动工。所有许可证与仓库状态均已用 GitHub API 逐条核实（核实日期 2026-11-03），不凭记忆填写。

---

## 一、这批补的是什么缺口

先做过查重：UI2 体验馆现有 **77 条**（F01–F46 + G/M/V/X/W/T 增补 31 条），已覆盖折线／柱状／环形图、骨架屏、空状态、瀑布流、响应式栅格、粘性堆叠、弹簧拖拽、手势卡片、表单校验、页面转场、动效令牌等；模板侧 F01–F21 已覆盖液态玻璃、图标切换、权限、壁纸、主题配置、欢迎引导、底部导航、状态反馈、弹窗术语 P01–P12、数据查询 Q01–Q05、工具基建 U01–U04。

**真正还缺的是四类**，正好对应开源社区最成熟、而本仓库一条都没有的部分：

| 缺口 | 为什么重要（套壳视角） | 本批对应 |
|---|---|---|
| 设计令牌的可验证化 | 现有主题页只能「看」颜色，不能「算」对比度是否达标；换品牌色后没人知道文字还可不可读 | S01 |
| 图标的网格与规格 | 换图标是套壳第一步（`prompts/01-换图标`），但库里没有一张能对照的 24dp 网格与四轴说明 | S02 |
| 日期区间的真实控件 | Q02 已经把「时间写明起止」写成规范，却没有可复用的月历区间选择器 | S03 |
| 宽屏/折叠屏的双栏 | F22 是栅格、F24 是安全区，都没有「窄屏单栏↔宽屏双栏」的列表-详情切换 | S04 |
| 列表重排、下拉刷新、首次引导 | 三种最高频的交互，库里全是静态展示，没有可拖可拉可步进的实体 | S05/S06/S07 |
| 质量自检工具 | 上架前的无障碍项全靠人肉，库里没有一个能当场跑出结果的检查器 | S08 |

---

## 二、五条硬口径（与前几批一致）

1. **零新增依赖**。延续批 B「2092 行零依赖」的做法：只用已有的 Compose foundation／material3／animation／ui + JDK（`java.time`，minSdk 26 起原生可用）。不引 coil、不引 kizitonwose/Calendar、不引 Reorderable。理由很实在——本仓库无法本地构建，唯一验证是 CI，多一个第三方坐标就多一个不可控的版本/BOM 兼容风险。
2. **只借鉴交互模式与公开参数，代码独立实现**。与 F14 处理 OShin（AGPL-3.0，只取公开视觉参数）同一口径。上游一行代码都不复制，所以 About 里写的是「参考来源」，并如实标注：本仓库 MIT，未复制上游源码，因此不触发 Apache-2.0/MIT 的源码再分发条款。
3. **真实可操作，不许 HTML／截图／占位卡冒充**。每一条都要能点、能拖、能改参数并立刻看到结果；凡是「算」出来的（对比度、亮度、日期跨度、触摸目标尺寸）必须是真的算，不是写死的数字。
4. **文案一律进 `strings.xml`**。批 C 刚把硬编码中文从 2152 压到 193 条，这批新增的 8 条素材**不许把数字加回去**：写代码时直接用 `stringResource`，键名走 `ui2_s0N_*` 与 `oss_*` 口径；`probe-i18n.py` 与 `check-template.py` 必须退 0。
5. **提示词体例照抄现有条目**，只改最后一句：现有 77 条结尾是「并报告与 Web 版机制的差异」（因为 UI2 原件是网页）；本批来源是 GitHub 仓库，改为「**并报告与上游开源实现的机制差异**」，其余结构（`在 Android Compose 中实现「名称」（UI2 增补条目 S0N，分类：开源素材）。要点：…。关键参数：…。要求：真实可操作实现（非静态图）；状态与视觉严格同步；边界（到端/为空/快速连点）有明确表现；完成后自检：…`）逐字保持一致。

---

## 三、本批 8 项

分类键统一用新增的 `oss_kit`（分类文案「开源素材」），并加进 `Ui2ExtraCategories`，这样目录页会多一枚筛选芯片，8 条能被一次筛出来。

| ID | 名称 | 来源仓库 | 许可证 | ★ | 借鉴什么 |
|---|---|---|---|---|---|
| S01 | 色彩令牌与对比度检查 | `material-foundation/material-color-utilities` | Apache-2.0 | 2302 | 色调板分档（T0–T100）、暗色映射规则、对比度门槛 |
| S02 | 图标网格与四轴规格 | `google/material-design-icons` | Apache-2.0 | 54220 | 24dp 视口／2dp 内边距／关键形状（keyline shapes）／fill-weight-grade-光学尺寸四轴 |
| S03 | 月历与日期区间选择 | `kizitonwose/Calendar` | MIT | 5625 | 月视图网格、连续区间选择、日期标记（in-date/out-date）、月份翻页 |
| S04 | 列表-详情双栏自适应 | `android/compose-samples`（Reply／JetNews） | Apache-2.0 | 23551 | 断点切换单栏↔双栏、窄屏返回栈、宽屏保持选中项 |
| S05 | 长按拖拽排序 | `Calvin-LL/Reorderable` | Apache-2.0 | 1376 | 长按激活、拖动换位、落位动画、拖动中的抬升视觉 |
| S06 | 下拉刷新 | `google/accompanist`（swiperefresh） | Apache-2.0 | 7847 | 阈值触发、指示器位移与旋转、回弹、刷新中锁定 |
| S07 | 教练引导气泡 | `microsoft/fluentui-android`（TeachingCallout） | MIT | 685 | 箭头指向目标控件、多步序列、跳过／不再显示、遮罩挖孔 |
| S08 | 无障碍自检器 | `google/Accessibility-Test-Framework-for-Android` | Apache-2.0 | 524 | 触摸目标 ≥48dp、文本对比度 ≥4.5:1、动效开关尊重、语义标签缺失清单 |

### 逐项实现要点

**S01 色彩令牌与对比度检查** — 三个真算的东西：① WCAG 2.x 相对亮度 `L = 0.2126R + 0.7152G + 0.0722B`（通道先做 sRGB 反伽马），对比度 `(L1+0.05)/(L2+0.05)`，这块是**精确实现**；② 由种子色生成 13 档色调板（T0/10/20/…/100），**明度档用 HSL 插值近似**——上游的 HCT/CAM16 色空间约 500 行且涉及色适应矩阵，本批不复刻，页面与文档都写明「近似，非上游算法」，不冒充；③ 浅→暗映射规则（primary T40→T80 等）随主题实时切换。交互：色相/饱和度滑杆改种子色 → 全档重算 → 每档显示对白底与黑底的对比度并标 AA/AAA → 点色块复制 HEX。边界：纯黑纯白种子色、对比度恰好 4.5 的临界值。

**S02 图标网格与四轴规格** — Canvas 实绘 24dp 视口网格：外框 24、安全区 2dp 内边距（即 20dp 活区）、四条关键形状（圆 Ø20、方 18×18、竖矩 16×20、横矩 20×16）可开关叠加。四轴模拟器：`fill`（0/1 描边↔填充）、`weight`（100–700 映射到线宽 1.2–3.0dp）、`grade`（-50–200 微调笔画）、`optical size`（20/24/40dp 视口，同一图形按视口重算线宽与圆角）。示例图标用 Canvas path 画一个真实可辨的图形（铃铛），四轴改动立刻可见。附尺寸对照条（18/20/24/36/48dp 实绘）。边界：weight 拉到极值不许溢出视口。

**S03 月历与日期区间选择** — `java.time.YearMonth`／`LocalDate` 真算：首日星期偏移（`DayOfWeek` + 周一/周日起始可切）、当月天数、上月补位灰显。区间选择状态机：无选择→点第一次设起点→点第二次若晚于起点设终点、否则改设起点→已选区间内再点重置。视觉：起止实心、中间淡色连续、单日选中为圆。日期标记：今天描边、过去日期降透明、超出 90 天上限的置灰不可点（与 Q02 规范一致，`ADMIN_RETENTION_DAYS = 90` 同源思路）。月份翻页用 `AnimatedContent` 左右滑。交互：月份前后翻、点日期、清除、显示「共 n 天（含首尾）」实时计算。边界：跨年翻页、起止同一天、终点早于起点。

**S04 列表-详情双栏自适应** — `BoxWithConstraints` 读实际宽度：`< 600dp` 单栏（列表→点进详情→返回回列表，且**保留滚动位置与选中项**，与批 C 之前修的「素材库返回不回顶部」同一口径）；`≥ 600dp` 双栏（左列表右详情，选中项高亮，右侧空态提示「选一条」）。列表数据用本地固定假数据（明示模拟，不联网）。附断点标尺实时显示当前宽度与所处档位（Compact/Medium/Expanded）。边界：拖动窗口跨越断点、双栏下删除选中项、单栏下按系统返回。

**S05 长按拖拽排序** — `LazyColumn` + `detectDragGesturesAfterLongPress`：长按激活（触感反馈）、拖动时该项抬升（阴影 + 缩放 1.03）、按当前指针位置与 `layoutInfo.visibleItemsInfo` 的 bounds 求目标索引并实时交换数据、松手落位（`Modifier.animateItem()`，Compose 1.7 起 `animateItemPlacement` 已废弃，用新名）、拖到列表上下边缘自动滚动。附「重置顺序」与「显示当前序号」。边界：拖到首位/末位、快速连续拖动、列表短于一屏（不触发滚动）。

**S06 下拉刷新** — `Modifier.nestedScroll` + `NestedScrollConnection`：`onPreScroll` 在列表未到顶时不拦截，到顶后吃掉向下的位移累积成 `overscroll`；超过阈值（72dp）标记待触发；`onPostScroll` 处理惯性余量；松手 `onPreFling` 决定进入刷新态还是回弹。指示器：位移 0→72dp 映射旋转 0→360°，达阈值变实色，刷新中保持 48dp 高度并持续旋转，完成后回弹。刷新耗时用 900ms 模拟（明示假数据，不联网）。边界：刷新中再次下拉（锁定不重复触发）、阈值临界反复拉、刷新中列表滚动。

**S07 教练引导气泡** — 页面里放 4 个真实控件作为目标；用 `onGloballyPositioned` + `positionInParent()` 抓目标在容器内的 bounds，气泡按 bounds 定位并画三角箭头指向目标（上/下/左/右四向自动避让）。遮罩用 `Canvas` + `BlendMode.Clear` 在目标处挖孔。序列状态机：步骤 1→4，「下一步」「跳过」「不再显示」（写入 `rememberSaveable`，本次进程内有效，并说明真实项目应落 DataStore）。边界：目标贴近屏幕边缘时箭头翻转、跳过后再进入、快速连点下一步。

**S08 无障碍自检器** — 五项**当场真跑**的检查，不是清单文字：① 触摸目标：对页面内 6 个真实控件用 `onGloballyPositioned` 量实际尺寸，< 48dp 判不合格并给出差值；② 文本对比度：取当前主题的 `onSurface`/`surface`、`primary`/`onPrimary` 等 5 组配对，用 S01 的同一套 WCAG 公式算比值，标 AA(4.5)/AAA(7)；③ 系统动效开关：读 `Settings.Global.ANIMATOR_DURATION_SCALE`（无需权限），为 0 时提示「应关闭装饰性动画」；④ 字体缩放：读 `LocalDensity.fontScale`，> 1.3 时提示检查截断；⑤ 语义标签：列出本页图标按钮的 `contentDescription` 有无，缺失的点名。每项给通过/警告/不合格三态与一句可执行的修法。边界：主题切到暗色后重算、把系统字体调到最大后重跑。

---

## 四、已核实但本轮不做（候补队列，留给后续批次）

许可证都查过了，写在这里是为了下一批不用重查：

| 仓库 | 许可证 | ★ | 不做的理由 |
|---|---|---|---|
| `coil-kt/coil` | Apache-2.0 | 11954 | 图片加载三态与缓存：要真图真网络才有意义；离线缓存部分「网络壁纸与离线缓存」已覆盖 |
| `skydoves/Balloon` | Apache-2.0 | 4017 | 气泡提示：与 P06 Tooltip、P09 Popover、本批 S07 三重重叠 |
| `skydoves/landscapist` | Apache-2.0 | 2587 | 同 coil，图片加载 |
| `tehras/charts` | Apache-2.0 | 777 | 图表：UI2 F32/F33/F34 已有折线/柱状/环形 |
| `SmartToolFactory/Compose-Extended-Gestures` | Apache-2.0 | 104 | 进阶手势：F40 手势卡片 + X04 弹窗手势实验室已覆盖大半 |
| `fornewid/material-motion-compose` | Apache-2.0 | 660 | 转场：W01/W02/W03 动效馆 + F41 页面转场已覆盖 |
| `android/nowinandroid` | Apache-2.0 | 22002 | 架构分层与分页：**这是批 D-架构（screen/viewmodel/repository 分层）的主参考**，不该混进素材批次 |
| `MikeOrtiz/TouchImageView` | MIT | 2748 | 双指缩放看图：View 体系，转 Compose 的成本高于收益，候补 |
| `material-components/material-components-android` | Apache-2.0 | 17444 | 上游处于 MAINTENANCE MODE，且本仓库是纯 Compose 体系 |
| `google/flexbox-layout` | Apache-2.0 | 18285 | **仓库已归档**（archived=1）；Compose foundation 自带 `FlowRow`/`FlowColumn` |
| `google/Accessibility-Test-Framework-for-Android` | Apache-2.0 | 524 | 已用于 S08，但它是 JUnit 体系的检查库，本批只取检查项口径，不引依赖 |

**核实中发现的两个坑（说明为什么不能凭记忆写来源）**：`halilozercan/compose-colorpicker` 与 `android/paging-samples` 两个仓库名 **API 返回 404，根本不存在**；Paging 的真身在 `androidx/androidx` 单体内。若凭记忆写进 About，就是给用户埋了一条假来源。

---

## 五、集成点清单（要动的文件）

| 文件 | 改什么 |
|---|---|
| `ui/features/ui2/previews/PreviewOssDesign.kt`（新） | S01、S02 的真实实现 |
| `ui/features/ui2/previews/PreviewOssLayout.kt`（新） | S03、S04 |
| `ui/features/ui2/previews/PreviewOssInteraction.kt`（新） | S05、S06、S07 |
| `ui/features/ui2/previews/PreviewOssQuality.kt`（新） | S08 |
| `ui/features/ui2/Ui2ExtraCatalog.kt` | 追加 8 条 `Ui2Feature("S0N", "s0N", …, "oss_kit", 难度, …)` |
| `ui/features/ui2/Ui2Catalog.kt` | `Ui2CategoryLabels` 加 `"oss_kit"`；`Ui2ExtraCategories` 在 `Ui2ExtraCatalog.kt` 里加 `"oss_kit"` |
| `ui/features/ui2/Ui2PreviewHost.kt` | `when` 加 8 条路由 |
| `ui/screens/InfoScreens.kt` | `allCredits` 追加 8 条（编号 14–21），url 留在代码里 |
| `res/values/strings.xml` | 8×4 条素材文案 + 1 条分类文案 + 8×4 条来源申明 = **65 条** |
| `素材库/UI2体验馆/S0N-<slug>/` | 每条 3 件套（实现方案／环境要求／参考来源）= 24 个文件；并更新 `参考来源-总表.md` |
| `docs/功能清单.md`、`docs/功能逐项手册.md` | 追加 S 系列 |
| `docs/把本仓库当壳用-套壳上手指南.md` | 条数与 UI2 总数（77 → 85）同步 |

**不需要动**：`MIRRORS`（`Ui2Catalog`/`Ui2ExtraCatalog`/`Ui2PreviewHost`/`Ui2Gallery`/`InfoScreens` 全部不在表内，已核实）；`AndroidManifest.xml`（不新增 Activity）；`build.gradle.kts` 的依赖块（零新增依赖）。

---

## 六、验收口径（每条都要过）

- CI `:app:assembleDebug` 两个 run 全绿；
- `scripts/probe-i18n.py` 退 0（新增的 `stringResource` 上下文合法、参数数对得上、格式串合法）；
- `scripts/check-template.py` 退 0（未声明引用扫描 = 所有 `R.string.*` 都真实存在）；
- `strings.xml` 无重名／无空值／键名全 ASCII／往返解析逐字节一致；
- kotlinc 语法类错误 0，且相对 HEAD 基线**无新增错误签名**（唯一能抓「注释里写了会闭合注释的字符」与解构问题的一闸）；
- 分类合计不许因为这批而上升（新代码不带硬编码中文）；
- About 页 8 条来源可见、可点、跳转 URL 正确；
- **真机未验证**：以上全是 CI／静态／资源层校验。8 条素材的手感（拖拽跟手度、下拉阈值、气泡定位）没在真机上跑过，按既有口径**不算已验收**。

---

## 七、落地结果（施工完成后回填）

| 项 | 结果 |
|---|---|
| 实现文件 | `PreviewOssDesign.kt`（S01/S02）、`PreviewOssLayout.kt`（S03/S04）、`PreviewOssInteraction.kt`（S05/S06/S07）、`PreviewOssQuality.kt`（S08） |
| 目录注册 | `Ui2ExtraCatalog.kt` +8 条；新分类键 `oss_kit`（进筛选芯片，`Ui2ExtraCategories` 2 → 3 类）；`Ui2Catalog.kt` 的 `Ui2CategoryLabels` +1；`Ui2PreviewHost.kt` +8 条路由 |
| 来源申明 | `InfoScreens.kt` 的 `allCredits` 13 → **21 条**（编号 14–21），App 内「关于 → 开源致谢」可见、可点、可跳转 |
| 资源 | `strings.xml` 1657 → **1890 条**（+233：预览页文案 168、目录条目 32、分类文案 1、致谢 32） |
| 硬编码中文 | 分类合计**仍是 193 条**——这批新代码一条都没加回去（写完即用 `stringResource`，4 个新文件的中文字面量实测 0） |
| 依赖 | `build.gradle.kts` 依赖块**零改动** |
| 静态闸 | 4 个新文件 kotlinc 语法类错误 **0**；`probe-i18n.py` 退 0（实参个数与格式串全对）；`check-template.py` 退 0 |
| UI2 总数 | 77 → **85 条** |

**实现过程中改掉的两处自己的错**（记下来防止重犯）：

1. 一开始拿资源 id 做加减（`R.string.pv_oss_cal_weekday_0 + (d.value % 7)`）——资源 id **不保证连号**，而且在 `android.nonFinalResIds=true` 下根本不是编译期常量。改成把 7 个 id 逐条列进 `OssWeekdayLabels`。
2. 遮罩挖孔一开始想用 `BlendMode.Clear`——那个必须配 `graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }` 才正确，否则会把整层擦掉。改成用四块矩形围出孔洞，视觉一致且零 API 风险。

**S08 的一处如实降级**：语义标签检查做不成运行时遍历（遍历 `SemanticsNode` 需要 compose-ui-test 依赖，本仓库 0 测试依赖），所以做成「声明式自检」——把本页图标控件有没有传 `contentDescription` 逐条列出并点名缺失项。页面上与文档里都写明了，不冒充真遍历。其余四项（触摸目标实测、WCAG 对比度精算、系统动画开关、字体缩放）都是当场真跑。

**真机未验证**：以上全部是 CI／静态／资源层校验。8 条素材的手感（拖拽跟手度、下拉阈值、气泡定位与箭头避让、日历跨月翻页）都还没在真机上跑过，按既有口径**不算已验收**。
