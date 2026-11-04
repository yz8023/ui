# 批 G 清单：开源取材的第四批素材（S19–S22）＋ 两个结构性批次（批 A 测试／批 D-架构）

写清单的时间：2026-11-04。这一份与前三批不同——它把**再加素材**和**两个一直未开工的结构性缺口**放在一起做，
因为核对下来它们是同一件事：**素材的核心逻辑该有一个能被测试的落点，而那个落点就是分层**。

---

## 一、为什么是这 4 项（防撞车核对）

批 F 清单 §五 留下的队列里，逐项过了一遍与既有 95 条的关系：

| 候选 | 判定 | 理由 |
|---|---|---|
| `woltapp/blurhash`（MIT ★17084）＋`saket/telephoto` 的 BlurHash 部分 | **做（S19）** | 批 F 的 S16 只做了「多趟偏移叠加」的近似占位，并在页面上如实降级说明。BlurHash 是有公开规范的**编码算法**（base83＋DCT 分量，4×3 时 hash 长度 = 6+2×(12−1) = 28），可以真实现、真往返验证。与既有「模糊管线」不撞：那条讲的是渲染管线（半径／采样／性能），这条讲的是**把一张图压成 30 字符字符串**的编解码 |
| `airbnb/lottie-android`（Apache-2.0 ★35765） | **做（S20），但只取时间轴** | 批 F 剔它是因为缓动曲线部分与 W02 令牌实验室（时长 7 档／曲线 9 条／弹簧 6 组／错峰 5 种）正面撞车。这次只取**多轨关键帧时间轴**：轨道、关键帧、时间游标、区间插值读数——W02 管「一条动画怎么缓动」，S20 管「多条动画怎么在时间上排布」，不重叠 |
| `adrielcafe/voyager`（MIT ★3091） | **做（S21）** | 既有导航类素材是「抽屉导航」（控件形态），没有一条讲**返回栈的操作语义**：push/pop/replace/replaceAll/popToRoot/popUntil、singleTop 去重、返回键什么时候该退出。这是栈状态机，不是控件 |
| `android/nowinandroid`（Apache-2.0 ★22028） | **做（S22），并且它就是批 D 的参考** | 与「渲染分层剥开」不撞：那条是渲染层（画布／图层／玻璃），这条是**架构分层**（UI→状态持有者→仓库→存储）与单向数据流。S22 的数据不是编的，是**本仓库批 D 重构之后的真实分层**，逐节点给文件与职责 |
| `InsertKoinIO/koin`（Apache-2.0 ★10025） | 不做 | 依赖注入图在没有多模块、没有真实注入需求的本仓库里只能画成示意图，违反「不许用静态占位卡冒充」。留给真有 DI 需求时 |
| `chrisbanes/tivi`（Apache-2.0 ★6719） | 不做 | 与 nowinandroid 重叠，且价值在大型多模块工程，本仓库单模块用不上 |
| `detekt/detekt`＋`cashapp/paparazzi`＋`mockk/mockk` | **并入批 A** | 这三个是测试与静态检查工具，本来就该跟「0 测试文件」这个缺口一起做，不是素材 |
| `JetBrains/compose-multiplatform`、`google/horologist`、`skydoves/Orbital`、`skydoves/Cloudy` | 不做 | 分别因为：本仓库不可预览多平台产物；Wear 赛道无关；动效与玻璃已经够厚（批 F 已判定） |

## 二、硬口径（沿用批 D/E/F，一条不松）

1. 每项都是**可预览的真实实现**，不是 HTML、不是截图、不是静态占位卡。
2. 每项配**可复制提示词**（点按展开、长按复制＋触感）。
3. **零新依赖**：`build.gradle.kts` 的 `dependencies` 块零改动（`testImplementation junit` 是批 A 之前就有的，且不打进 APK）。
4. 来源在 App 内「关于 → 开源致谢」声明，并同步 `素材库/UI2体验馆/参考来源-总表.md`。
5. 新代码中文零硬编码（`kt_scan` 基线 **193 不许上升**）；数据侧存稳定英文键。
6. 先写清单（本文件）再动工。

## 三、本批 4 项

**S19 BlurHash 编解码与真实占位** — 纯 Kotlin 实现 base83 编解码、DC/AC 分量、sRGB↔线性互转、4×3 默认组件数；
页面给：可拖的组件数（x 1–9 × y 1–9）、原图与解码图的并排对照、hash 字符串逐字符着色（第 1 字符=尺寸标志、第 2=最大 AC 量化值、3–6=DC、其后每 2 字符一个 AC），
以及**往返误差读数**（解码图与原图的平均绝对误差）。占位策略按 telephoto：先解 hash 顶上，再换清图。
核心在 `domain/BlurHash.kt`，**批 A 直接测它**（base83 往返、纯色图 AC 全零、尺寸标志反解、非法串返回 null、采样器与像素数组两条入口结果一致）。

**S20 多轨关键帧时间轴** — 3 条轨道（位移／透明度／缩放）各带若干关键帧，时间游标可拖；
上方画轨道条与关键帧菱形，下方给游标处的**逐轨读数**与当前落在第几个区间、区间内进度、插值方式。
插值只给 LINEAR／STEP／SMOOTH（smoothstep）三种——**刻意不做曲线库**，缓动曲线是 W02 的地盘，这条只管时间排布。
核心在 `domain/KeyframeTrack.kt`（纯 Kotlin，批 A 测边界：游标在首帧前／末帧后／区间端点／零长区间）。

**S21 路由与返回栈状态机** — 可视化一个真实栈：点 push/pop/replace/replaceAll/popToRoot/popUntil 看栈变化，
每步记录一条操作日志（操作→结果→是否改变栈），并显示 `canGoBack`、深度、当前路由、singleTop 去重是否命中。
右侧给「返回键该怎么处理」的判定：栈深 1 时返回键该退出（或交系统），否则弹一层。
核心在 `domain/RouteStack.kt`（纯 Kotlin，批 A 测语义：单元素栈 pop 返回 null 且不改栈、replace 换顶不动底、popToRoot 计数、popUntil 找不到返回 -1）。

**S22 架构分层与单向数据流** — 数据是**本仓库批 D 重构后的真实分层**：UI（composable）→ 状态持有者（`AppSettingsState`，`@Stable`）→ 仓库（`SettingsRepository`，唯一持 `SharedPreferences`）→ 存储；
外加 `domain/`（零 Android 依赖的规则层）。点节点看真实文件路径、职责、允许的依赖方向；点边看「谁调用谁」与**为什么不能反向**（domain 不许 import androidx、UI 不许直接碰 SharedPreferences）。
页面还给一条真实的状态流演示：改一个值 → 规则层夹取 → 仓库落盘 → 状态持有者通知 → UI 重组，逐环节可点。
核心数据是常量表（文件路径与职责），**不引 nowinandroid 代码**，只借鉴其分层口径与依赖方向约束。

## 四、批 A：测试（仓库当前 0 个测试文件）

**做法**：不为了凑数写「测试 UI 文案」这种没有断言价值的东西。测试对象是**纯 Kotlin 领域层**——
这正是把核心逻辑从 composable 里抽出来的直接收益：它能在 JVM 上跑，不需要 Robolectric、不需要模拟器、不需要 Android SDK。

| 测试文件 | 覆盖 | 关键断言 |
|---|---|---|
| `BlurHashTest` | `domain/BlurHash.kt` | base83 编解码往返；纯色图 AC 全零且长度 = 4+2×(xc×yc−1)；尺寸标志反解出组件数；非法字符／长度不符返回 null；采样器入口与像素数组入口结果一致；解码 DC 与原图平均色一致（容差内） |
| `KeyframeTrackTest` | `domain/KeyframeTrack.kt` | 首帧前取首值、末帧后取末值；线性中点；STEP 保持左值；SMOOTH 在 0.5 处等于中点（smoothstep 性质）；零长区间不崩；进度归一化 |
| `RouteStackTest` | `domain/RouteStack.kt` | 单元素栈 pop 返回 null 且深度不变；push/singleTop 去重；replace 换顶不动底；replaceAll 清空重建；popToRoot 返回弹出数；popUntil 找不到返回 −1 |
| `SettingsRulesTest` | `domain/SettingsRules.kt` | 三个夹取的边界与幂等；旧版视觉模式映射四组合真值表；种子色十六进制格式（含负数 ARGB 的无符号处理） |

**本地自证**：沙箱里没有 Android SDK 也没有 Gradle 分发缓存，但领域层零 Android 依赖，
所以用 kotlinc ＋ Maven Central 的 junit 4.13.2／hamcrest-core 1.3 **在本地把测试真跑一遍**（命令记在 §七），绿了再交给 CI。

**CI 强制执行**：`.github/workflows/build-apk.yml` 增加 `./gradlew :app:testDebugUnitTest` 步骤——
测试不进 CI 就等于没写。这一步会让每次 push 多约 1 分钟。

**如实降级**：数据层（`SettingsRepository` 持 `SharedPreferences`）与 UI 层（composable）在 JVM 单元测试里覆盖不到，
那需要 Robolectric 或仪器化测试（要引入新依赖，与零依赖口径冲突）。所以本批的测试边界就画在「纯 Kotlin 领域层」，页面上和文档里都写明，不假装全覆盖。

## 五、批 D-架构：分层（状态写在 composable → 领域层／仓库层）

主参考 `android/nowinandroid`（Apache-2.0）的分层口径。**范围刻意收窄到一条竖切**，不做大重构：

1. **修掉 S18 揪出的那个真疑点**：`AppSettingsState(private val prefs: Context)` —— 形参名叫 `prefs`、类型是 `Context`，比实际需要的宽。
   改为 `AppSettingsState(private val repo: SettingsRepository)`，新增 `data/SettingsRepository.kt` 作为唯一持 `SharedPreferences` 的地方。
   `rememberAppSettingsState()` 里 `Context` 只用于就地取 prefs、用完即弃，状态对象不再持任何 `Context`。
2. **规则上提到领域层**：`importThemeJson` 里那些 `coerceIn` 抽成 `domain/SettingsRules.kt`，
   并让 `updateXxx` 也走同一套夹取——**这是一处真实的行为一致性修复**：原先只有「导入」会夹取、手动改值不会，两条路径规则不一致。
3. **旧键兼容逻辑上提**：`legacyVisualMode()` 的 dyn/glass→VisualMode 判定抽成 `SettingsRules.legacyVisualModeKey()`（返回稳定英文键），
   UI 层负责键→枚举映射（沿用批 C 定的「数据侧存稳定英文键」惯例）。
4. **不动的东西**：prefs 文件名 `liquidui.settings`、全部 `KEY_*` 常量值、`exportThemeJson` 的 JSON schema（`schema=1` 与字段名），
   以保证老用户升级后设置不丢、导出的主题 JSON 仍能互相导入。
5. **不引入 ViewModel**：本仓库的设置状态是应用级、由 `rememberAppSettingsState()` ＋ `applicationContext` 提供，
   进程重建后从 prefs 恢复，加 ViewModel 只会多一层而不解决任何现存问题——这条判断写进 S22 与架构文档，不装作漏了。

**顺带修正 S18 的一处事实错误**（批 F 已发布，必须改）：S18 的引用链把第 3 环写成 `MainActivity`，
但 `AppSettingsState.kt:388` 传的是 `LocalContext.current.applicationContext`，所以**今天并不持有 Activity、不构成现行泄漏**；
真实情况是「类型比需要的宽 → 潜在风险」，一旦有人从非 application 的 `LocalContext` 传 Activity 进来才会成链。
批 D 把类型收窄后，这条风险消失。S18 页面改为：引用链保留为**案例**并标注「已于批 D 修掉」，
第 3 环措辞改为潜在路径，表格里「长生命周期对象持 Context」一行由 `1/1 唯一真疑点` 改为 `1/1 潜在（批 D 已收窄为 SharedPreferences）`。

## 六、集成点（要动的文件）

| 文件 | 动作 |
|---|---|
| `domain/BlurHash.kt`、`domain/KeyframeTrack.kt`、`domain/RouteStack.kt`、`domain/SettingsRules.kt` | 新增，纯 Kotlin，零 Android/Compose import |
| `data/SettingsRepository.kt` | 新增，唯一持 `SharedPreferences` |
| `ui/screens/AppSettingsState.kt` | 构造参数 `Context`→`SettingsRepository`；读写改走仓库；夹取改走 `SettingsRules` |
| `ui/features/ui2/previews/PreviewOssHash.kt`（S19/S20）、`PreviewOssArch.kt`（S21/S22） | 新增，只做渲染与交互 |
| `ui/features/ui2/previews/PreviewOssAudit.kt` | 修 S18 的事实错误（引用链第 3 环、表格那一行、结论） |
| `Ui2ExtraCatalog.kt`／`Ui2PreviewHost.kt`／`InfoScreens.kt` | +4 条目录／+4 路由＋4 import／致谢 31→35 |
| `android/app/src/test/java/.../domain/*Test.kt` | 新增 4 个测试文件 |
| `.github/workflows/build-apk.yml` | 增加 `:app:testDebugUnitTest` 步骤 |
| `strings.xml` | S19–S22 文案＋目录 16 条＋致谢 16 条 |
| 文档四件套＋`参考来源-总表.md`＋`docs/架构分层与测试批次.md` | 95→99、strings 计数、来源 32→36 |

## 七、验收口径（每条都要过）

五道静态闸（`probe-i18n`／`probe-composable-scope`／`check-template`／XML 体检／kotlinc 语法类）＋
`kt_scan` 硬编码中文不上升＋**批 A 的测试在本地 JVM 真跑通过**＋CI `:app:assembleDebug` 与 `:app:testDebugUnitTest` 全绿＋
About 页 4 条新来源可见可点＋文档四件套与镜像不漂移＋发版同时出 APK 与源码 zip＋下载后校验 SHA256 与 zip 条目。
**真机未验的部分必须在汇报里点名**，不许说成已验收。

本地跑测试的命令（沙箱无 Android SDK，靠领域层零依赖才可行）：

```
kotlinc android/app/src/main/java/com/monkeycode/liquidui/domain/*.kt \
        android/app/src/test/java/com/monkeycode/liquidui/domain/*.kt \
        -cp junit-4.13.2.jar:hamcrest-core-1.3.jar -d /tmp/tout
java -cp /tmp/tout:junit-4.13.2.jar:hamcrest-core-1.3.jar:kotlin-stdlib.jar \
     org.junit.runner.JUnitCore <各测试类全名>
```

## 八、批 H 队列（下一批的候补，许可证已核实）

`InsertKoinIO/koin`（Apache-2.0 ★10025，等有真实注入需求再做）、`chrisbanes/tivi`（Apache-2.0 ★6719）、
`JetBrains/compose-multiplatform`（Apache-2.0 ★19469，先解决可预览性）、`google/horologist`（Apache-2.0 ★702，Wear 赛道）、
`detekt/detekt`（Apache-2.0 ★7106，静态检查规则集可做成素材：把规则映射到本仓库真实违规数）、
`cashapp/paparazzi`（Apache-2.0 ★2638，截图测试，要引依赖）、`mockk/mockk`（Apache-2.0 ★5766，同上）。
另有视频来源未取材：同合集的表单 7、控件 7 连、导航 9、动画+鼠标 12、异常处理 14、调试 14、日志 12。

## 九、落地结果（2026-11-05 实测回填）

### 提交与发版

| 项 | 值 |
| --- | --- |
| PR | [#29](https://github.com/yz8023/ui/pull/29)（26 文件 +3759/−155，已合并） |
| 合并提交 | `d6634ef` ← `f1eb5fb`（CI 修复）← `de928e5`（版本号）← `e244b6a`（素材）← `c47c7e3`（批 D）← `ad68f24`（批 A+domain） |
| tag | `v2.14.0`（指向 `d6634ef`，annotated） |
| versionCode / versionName | 22 → **23** / 2.13.0 → **2.14.0** |
| Release | id 434346732，`draft=False`，**2 个资产一次成功**（批 F 那次需要 rerun-failed-jobs，本批没有复发） |
| `liquidui-v2.14.0-release.apk` | **3282140 B**，sha256 `94528685a93e9a9d972a4fb3c1bb87fabbcee1179e37c23a8776171b303cac52` |
| `liquidui-v2.14.0-source.zip` | **4865129 B**，sha256 `d0b1700d215269dc49d4103fb1d466e83602f50e14c12b83d0d73ef55f72d60f` |

### CI 实测（「CI 绿才合并」在这一批真的挡下了一次事故）

| run | 提交 | 结论 | 说明 |
| --- | --- | --- | --- |
| #187 | `ad68f24` | success | 首次跑新增的 `Run unit tests` 步骤 |
| #188 / #189 / #190 | `c47c7e3` / `e244b6a` / `de928e5` | **failure** | `:app:compileDebugKotlin FAILED` |
| #191 | `f1eb5fb` | success | `> Task :app:testDebugUnitTest` 执行、BUILD SUCCESSFUL in 2m 54s |
| #193 | main | — | 合并后的 debug 构建 |
| Release #21 | `v2.14.0` | success | R8 release APK + `git archive` 源码 zip |

真错只有一条，两处引用：

```
e: AppSettingsState.kt:297:13 Unresolved reference 'KEY_SEED_HEX'.
e: AppSettingsState.kt:336:61 Unresolved reference 'KEY_SEED_HEX'.
```

成因：批 D 把导出/导入 JSON 的种子色十六进制改走规则层时用了 `KEY_SEED_HEX`，但伴生对象的键常量是从原代码逐个委托过来的，而原代码这一处写的是裸字面量 `"customSeedHex"`、**根本没有对应常量**，委托循环覆盖不到它。本地无法自证：`AppSettingsState` 依赖 Compose 与 `SharedPreferences`，沙箱里没有 Android SDK。修法＝补 `const val KEY_SEED_HEX = SettingsRules.KEY_SEED_HEX`，并加了一道自检（文件里用到的 `KEY_*` 与声明的 `KEY_*` 做集合差，现在 26/26 差集为空）。

顺带把 11 处属性初值的浮点字面量改引 `SettingsRules.DEFAULT_*`：上一提交的说明里写了「初值改引 DEFAULT_*」，实际只改了 `customSeed` 一处，这次补齐，让说法与代码一致。

### 素材落地实测

四条全部是真实现，源码 zip 内已核对文件在位（12 个新文件 + 8 个新目录条目 = zip 512 → 532）。

- **S19**（`PreviewOssHash.kt`）：MAE 表在设备上 `remember` 现场编解码算。纯色 8×8 `0xFF3E8FE0`、4×3 → hash `LU7EDUpMfQpMpMkDfQkDfQfQfQfQ`（28 字符）。实测 MAE：纯色 1×1=**0.00**／2×2=11.64／4×3=**13.53**／6×5=15.10／9×9=7.93；渐变 16×12 1×1=63.52／2×2=15.80／4×3=**14.60**／6×5=17.22／9×9=17.74。测量日期常量 `HASH_MEASURE_DATE="2026-11-04"`。
- **S20**（同文件）：三轨共用游标，插值只有 LINEAR/STEP/SMOOTH，缓动归 W02。批 F 主动剔除的 lottie 在本批以「只取多轨关键帧时间轴」的方式闭环。
- **S21**（`PreviewOssArch.kt`）：跑 `domain/RouteStack.kt` 真状态机。页面用 `remember` 存 `revision` 计数器（`RouteStack` 不是 snapshot 状态，不自增就不触发重组），且与 `stack` 同生命周期，旋转后读数才与栈一致。
- **S22**（同文件）：展示批 D 真实分层并活体接真实 prefs。滑杆范围故意给 −0.6..2.4（规则是 0..1）以看见 `clampByKey` 夹取；「写入真实设置」真的调 `updateMotionDamping`；恢复出厂回到 0.50。测量日期常量 `LAYER_MEASURE_DATE="2026-11-05"`。

批 D 重构后的分层实测：`domain/` 4 文件、非 `kotlin.*` 的 import 只有 1 处（`java.util.Locale`）、`android`/`androidx`/`R`/`ui` 的 import **0 处**；`AppSettingsState` 407 行、`store.` 0 处、字面 `coerceIn` 0 处、`repo.` 49 处、`SettingsRules.` 66 处、`Context` 只在 `rememberAppSettingsState()` 出现一次。

### S18 修正（已发布内容的事实错误）

1. 原文「`AppSettingsState` 持 Context → MainActivity → 泄漏」：`rememberAppSettingsState()` 传的是 **applicationContext**，当时就不构成 Activity 泄漏，真实问题只是类型比需要的宽（批 D 已修）。
2. 复测时四条命令的粗命中数各多 1 处，逐条眼看后全部来自**页面自己**——检测命令以字符串写在被检测的源码树里，于是命令命中自己。修法＝所有命令加 `--exclude=PreviewOssAudit.kt`。
3. Handler／Timer 松口径 9→10：第 10 处是 `RouteStack.kt` 的 KDoc 提到 `BackHandler`，仍被 `grep -v BackHandler` 滤掉，真实数 0。
4. 「长生命周期对象持 Context」由 1/1 变 **1/0（已修）**；现在唯一的粗命中是 `SettingsRepository.kt` 的 KDoc 引用旧签名——一个由文档制造的误报样本。

### 闸门与文档

`probe-i18n` 退 0（警告回基线 9；纠正了 23 处 `formatted="false"` 误用——带实参的串不该加，加了 `%%` 不还原）｜`probe-composable-scope` 82 个 .kt 越界 0｜`check-template` 退 0｜`kt_scan` 硬编码中文 **193 未上升**｜领域层+测试编译 0 error 0 warning｜本地 JVM **OK (71 tests)**｜`build.gradle.kts` 依赖块 **18 行逐字未改**（`testImplementation` 未新增，junit 早已在）。

四份文档同步：`功能清单.md`（95→99、46+53）、`功能逐项手册.md`（F15 段 39→53 条增补、验收 85→99、筛选计数 77→99）、`参考来源-总表.md`（+4 来源、telephoto 记 S16 与 S19、合计 18→22 条素材／32→36 上游）、`套壳上手指南.md`（2147→2312，重写 §7「这套壳还缺什么」）。另修一个同名陷阱：素材库 F04 镜像里的 `SettingsRepository(context)` 与新建的 `data.SettingsRepository(prefs)` 同名不同物，照抄会撞「构造签名不符」而非「找不到符号」，镜像横幅与 `check-template.py` 的 `KNOWN_UNDECLARED` 注释都已写明。

### 未验收的部分（不要当成已验收）

**全部改动未在真机上验证过。** 沙箱没有 Android SDK、没有模拟器，本地只能编译领域层与跑 JVM 测试；四条素材的界面表现只经过 CI 的 `assembleDebug`／`assembleRelease` 编译验证与代码审查，没有人在真机上点过。批 D 改的是设置项读写主链，虽然对外契约（prefs 文件名、25 个键的字面值、导出字段集合、14 处使用点签名）由三条测试锁住，仍建议真机过一遍：改主题/玻璃参数 → 杀进程重开看是否保留；导出 JSON → 导入 → 重置；S22 页面点「写入真实设置」后看设置页滑杆是否同步变化；S20 拖动 scrub 与播放是否跟手；S19 换图案后 MAE 表是否重算。
