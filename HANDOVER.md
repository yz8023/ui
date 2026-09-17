# Liquid Motion UI — 项目交接文档 (HANDOVER)

> 交接日期：2026-09-16
> 交接人：MonkeyCode AI Agent
> 源码 commit：`131effe`（Initial commit，本交接基于工作区未推送的完整代码）
> 远程仓库：`https://github.com/yz8023/ui`
> 预计上手时间：30 分钟内可构建出 debug APK

---

## 1. 项目概览

基于 **kd64i/dyparse**（液态玻璃 / Miuix 视觉语言）与 **feitangyuan/motion-web**
（物理动效 token）提炼的 **Android Jetpack Compose UI 模板库**。

- 一套可直接编译的 Android 工程（`android/`）
- 一个纯静态 Web 交互预览（`web-preview/`，无需构建）
- 五张截图 + 一张动效 GIF（`preview/`）
- 四份设计/构建文档（`docs/`）

产物：debug APK 约 **11.5 MB**（含 backdrop-android 玻璃库，不依赖
`material-icons-extended`）。

## 2. 交付物清单

| 交付物 | 路径 | 说明 |
|---|---|---|
| Android 工程 | `android/` | Gradle 8.11.1 + AGP 8.7.3，可离线镜像构建 |
| 静态交互预览 | `web-preview/index.html` | 单文件复刻玻璃/动效，可直接打开 |
| 预览截图 | `preview/01-home.png` ~ `05-settings.png` | 840x1800 @2x |
| 动效演示 | `preview/spring-bounce.gif` | 弹簧回弹物理演示 |
| 交接文档 | `docs/` + 本文件 | 设计/动效/模板/构建四份 + 本交接 |
| 构建产物 | `dist/liquid-ui-template-debug.apk` | 已签名 debug APK（Android Debug 证书） |
| CI 工作流 | `.github/workflows/build-apk.yml` | 推送后自动构建并上传 APK artifact |

## 3. 环境要求

| 依赖 | 版本 | 说明 |
|---|---|---|
| JDK | 17 | 编译必需 |
| Android SDK | platform 36 + build-tools 35.0.0 | backdrop-android 要求 compileSdk≥36 |
| Gradle | 8.11.1 | 已由 wrapper 固定 |
| Kotlin | 2.3.10 | backdrop/shapes 库为 Kotlin 2.3 元数据 |
| Compose BOM | 2024.10.01 | material3 / ui 等 |

本机 SDK 路径：`/opt/android-sdk`（已写入 `android/local.properties`，**不入库**）。

## 4. 快速构建（30 分钟内）

```bash
cd android

# 环境变量（本机已装 JDK17，可跳过）
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# 首次构建会下载依赖（已缓存时 2-4 分钟）
./gradlew :app:assembleDebug

# 产物
ls app/build/outputs/apk/debug/app-debug.apk
```

CI 验证：推送后 GitHub Actions 自动执行上述流程，artifacts 提供下载。

## 5. 目录结构

```
android/
  app/src/main/java/com/monkeycode/liquidui/
    MainActivity.kt           入口
    ui/theme/                 色彩/圆角/字体/Theme/AppChrome/AppIcons
    ui/components/            玻璃表面/控件/行组件/文本/反馈
    ui/motion/                弹簧阻尼/拖拽/高亮/修饰符
    ui/navigation/            AppRoot + Screen 枚举
    ui/screens/               首页/组件/动效/设置
  gradle/libs.versions.toml  版本目录
  app/build.gradle.kts        构建脚本（含签名骨架）
  app/proguard-rules.pro      R8 规则
web-preview/index.html        纯静态交互预览
docs/                         设计系统/动效系统/模板指南/构建发布
preview/                      截图与 GIF
.github/workflows/            CI
```

## 6. 技术栈

| 模块 | 选型 |
|---|---|
| UI | Jetpack Compose（Material 3），backdrop-android 真 backdrop 玻璃（`liquidGlass`） |
| 动效 | Compose `Animatable` + 阻尼谐振换算，无第三方动画库 |
| 导航 | 自研 `LiquidBottomTabs`（弹簧阻尼滚动 + 高亮跟随），无 Navigation 库 |
| 状态 | `SharedPreferences` 持久化 `AppSettingsState`，`LocalAppChrome` 全局旋钮 |
| 构建 | Gradle 8.11.1 / AGP 8.7.3 / Kotlin 2.3.10 / JDK 17 |
| 兼容 | minSdk 26 / target 35 / compileSdk 36 |
| 图标 | 11 个自托管 `ImageVector`（`AppIcons.kt`），不依赖 icons-extended |

## 6.1. v1.1 新增（2026-09-16 反馈迭代）

- `AppChrome.glassEnabled` 全局总开关：关掉后 `liquidGlass` 走纯色分支、AppRoot 用 Material3 `NavigationBar`、双层 backdrop 录制跳过。
- `PaletteId` 四套主题（冰蓝/薄荷/落日/素灰），`PaletteSeeds` + `schemeFor` 一处维护，设置页色卡即点即换。
- `GlassDialogHost` 同窗浮层弹窗：幕帘层录制成 backdrop 并与 aurora combined，卡片真折射；无 backdrop 时降级纯色幕帘。
- 壁纸背景：AppRoot 用 `WallpaperManager` drawable 作为 aurora 层封面绘制（`drawWallpaperCover`）。
- `Motion.motionDamping` 全局阻尼旋钮 + `Motion.DampedRatio(base)` token 级倍率，设置页滑块一调全 app 弹簧重算。
- 动效实验室：点选弹簧预设立即回弹演示；开关组件轨道放大加描边与阴影。
- 参考仓库署名进入设置页「关于」：dyparse / Miuix / motion。

## 7. 关键设计决策

1. **玻璃层用 dyparse 同款 backdrop-android**：页内面板采样 aurora 层、底栏采样
   content 层（combined），`vibrancy + blur + lens` 真折射，透明度/折射率全局可调
   （详见 `docs/DESIGN_SYSTEM.md` §4）。
2. **图标瘦身**：debug APK 曾达 16.5MB，根因是 `material-icons-extended`
   （~2000 图标进 dex）。已改为内联 9 个实际用到的图标，体积降至 ~9.3MB。
3. **动效换算**：Framer Motion 的 `dampingRatio`/`stiffness` → Compose
   `Spring(dampingRatio, stiffness)` 直接对应（详见 `docs/MOTION_SYSTEM.md`）。
4. **四个空功能页**：Home/Components/Motion/Settings 撑起模板壳，替换 screen body 即可。

## 8. 自定义模板

在 `TEMPLATE_GUIDE.md` 中有逐项说明，要点：

- 替换包名：`com.monkeycode.liquidui` → 你的包名（AndroidManifest / build.gradle.kts / Kotlin 目录）。
- 改应用名：`app/src/main/res/values/strings.xml` 的 `app_name`。
- 加页面：在 `Screen.kt` 枚举加一项（title/subtitle/icon），在 `AppRoot.kt` 挂 body。
- 加图标：复制任意 ImageVector 声明进 `AppIcons.kt`，用 `Icon(AppIcons.Xxx, ...)`。
- 换主题：改 `ui/theme/Color.kt` / `Shape.kt` / `Type.kt`。

## 9. 动效系统

统一走 `ui/motion/Motion.kt` 的预设弹簧，全部 token 化：

| token | 用途 |
|---|---|
| `SpringPresets.Springy` / `Gentle` / `Snappy` | 通用弹簧 |
| `DampedDragAnimation` | 拖拽回弹 |
| `InteractiveHighlight` | 按压高亮 |
| `pressFeedback` | 按压冲击动效 |

`AppSettingsState.reducedMotion` 为 true 时全局退化为无动效。

## 10. 构建与发布

`docs/BUILD_AND_RELEASE.md` 有完整说明，摘要：

- debug：`./gradlew :app:assembleDebug`
- release：`./gradlew :app:assembleRelease`（未配 keystore 时自动回退 debug 签名）
- 正式签名：在 `android/local.properties` 写 `RELEASE_STORE_FILE/PASSWORD/KEY_ALIAS/KEY_PASSWORD`，
  `app/build.gradle.kts` 的 `signingConfigs.template` 会自动拾取。
- 体积控制：release 已开 `minifyEnabled + shrinkResources`，R8 生效。

## 11. 已知问题与坑

| 问题 | 说明 / 规避 |
|---|---|
| Gradle wrapper 首次生成失败 | 下载 `services.gradle.org` 校验超时，加 `--network-timeout=120000` 重试 |
| backdrop 需 compileSdk 36 | `gradle.properties` 已设 `android.suppressUnsupportedCompileSdk=36`，本机需安装 platform 36 |
| backdrop 为 Kotlin 2.3 元数据 | 工程 Kotlin 已升至 2.3.10（版本目录 `kotlin`），勿降回 2.0.x |
| `Icons.Outlined.RotateRight` 弃用 | 编译仅 warning，已内联到 AppIcons，无需处理 |
| `statusBarColor` / `navigationBarColor` 弃用 | Theme.kt 中仅 warning，API 33+ 走系统 edge-to-edge |
| 依赖下载慢 | 见下方镜像表 |
| `setXxx` 命名冲突 | 属性 setter 与函数同名会 JVM 签名冲突，状态变更统一用 `updateXxx` |

## 12. 依赖镜像（中国大陆）

`android/gradle/libs.versions.toml` 用的仓库配置在 `android/settings.gradle.kts`，
默认走 Google Maven + Maven Central。需要加速时在
`android/settings.gradle.kts` 顶部插入阿里云镜像：

```kotlin
pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
        google()
        mavenCentral()
    }
}
```

Gradle 发行版加速：`android/gradle/wrapper/gradle-wrapper.properties` 的
distributionUrl 可换 `https://mirrors.cloud.tencent.com/gradle/gradle-8.11.1-bin.zip`。

## 13. Git 与版本

- 当前 HEAD：`131effe`（Initial commit，含 android/ 骨架早期版本）
- 工作区全部代码（含 Android 工程、docs、preview、web-preview、CI）**尚未提交**。
- 交接推送状态：见最后一节；未推送时 clone 仓库仅有早期骨架，需补本目录全部内容。

```bash
# 提交全部交接代码
git add -A
git commit -m "feat: Liquid Motion UI template (glass + motion)"

# 推送（需确认后执行，分支与合并请求由用户决定）
git push origin <branch>
```

版本信息：`versionCode = 2`，`versionName = "1.1.2"`（`app/build.gradle.kts`）。
产物：debug APK 约 **11.5 MB**；release APK（R8 压缩）约 **1.4 MB** —— 推荐交付
release 变体（含 backdrop-android，不依赖 `material-icons-extended`）。

## 13.1. v1.1.1 修复（真机反馈）

- **修复壁纸背景崩溃**：`WallpaperManager.getDrawable()` 在 API 31+ 需媒体权限，
  AppRoot 已 try/catch 降级到 aurora；Manifest 声明 `READ_MEDIA_IMAGES` +
  `READ_EXTERNAL_STORAGE(maxSdk 32)`；设置页首次开启前用 ActivityResult 请求权限，
  授权后才持久化开启。
- **弹窗真玻璃 + 动效**：新增 `LocalGlassContentBackdrop`（aurora+content 合成层），
  弹窗卡片改采样该层——折射背后真实页面，幕帘不打进卡片采样；卡片入场缩放+淡入
  （弹簧 + tween）。
- **修复开关变形**：`CompactSwitch` 拇指竖直居中（`contentAlignment = CenterStart`），
  不再顶到上边。
- **暗色主题美化**：深色中性色 Night 系列转冷调深蓝，`surfaceContainerHigh/Highest`
  与 aurora 底色同步细化。
- **新增三套风格**：珊瑚 / 樱花 / 星夜，`PaletteId` 现有 7 套；设置页配色改为
  自动换行的色卡 chips（FlowRow），不再挤在单行 SegmentedTabs。

## 13.2. v1.1.2 修复（真机反馈二轮）

- **修复壁纸背景不显示**：旧版壁纸只被 `auroraBackdrop` 采样层录制、没画到屏幕
  （页面玻璃面板能折射到壁纸，但底图始终是纯色 `surfaceColor`）。现在壁纸作为
  **可见内容**（`drawBehind`）绘制进根 Box，同时仍被采样层捕获 —— 直接可见 + 玻璃折射兼得。
- **修复底栏不响应玻璃设置**：`LiquidBottomTabs` 的容器胶囊曾硬编码
  `alpha = 0.4f` 与 `lens(24dp)`，运动胶囊硬编码 `lens(10dp/14dp)`，全部改读
  `AppChrome.glassOpacity` / `glassRefraction`（与 `GlassCard` 同一套口径，默认值视觉不变）。
- **修复底栏不响应阻尼旋钮**：`DampedDragAnimation` 与 `InteractiveHighlight`
  的弹簧阻尼虽已走 `Motion.DampedRatio(...)`，但规格在 `remember` 时冻结（key 不含
  `motionDamping`）。现在 `LiquidBottomTabs` 两个 `remember` 都加上 `damping` 作为 key，
  `InteractiveHighlight` 的 `press/position` 两弹簧改用 `Motion.DampedRatio(0.5f)`，
  阻尼滑块一拖全部弹簧重算。
- **APK 体积瘦身**：交付变体切到 release（`minifyEnabled + shrinkResources`），
  R8 将 11.5MB → **1.38MB**；CI 工作流同步改构建并上传 release APK。
- **构建资源**：release 构建 R8 阶段内存峰值高（本机 OOM 过），需给后台终端
  分配 ≥45% 内存预算；`-x lintVitalRelease` 可跳过 lint 以提速。

## 13.3. v1.1.3 壁纸再修复 + 三态视觉风格（真机反馈三轮 / 双开关合一）

- **壁纸仍不显示的候选根因修复**：`drawWallpaperCover` 曾对 `intrinsicWidth/Height <= 0`
  直接早退（部分设备壁纸 drawable 尺寸上报 -1），导致壁纸既不画到屏幕也不进采样层。
  现改为 intrinsic 尺寸非法时回退到「铺满整层」（bounds = 容器 size），两种绘制路径
  （可见内容 + `auroraBackdrop` 采样录制）都复用该兜底，不再静默空白。
- **三态视觉风格替代双开关**：原「液态玻璃风格」开关与「动态取色」开关合并为
  单一 `VisualMode` 三态（玻璃 / 普通 / MD3），设置页「风格」区改用 `SegmentedTabs`。
  迁移逻辑：读旧 `KEY_GLASS_ENABLED` / `KEY_DYNAMIC` 映射到 `KEY_VISUAL_MODE`
  （dynamicColor=true → Md3，否则 glassEnabled=true → Glass，再否则 Normal）。
- **MD3 = Material You 动态取色**：`LiquidUITheme` 的 `dynamicColor` 入参改为
  `visualMode`，`VisualMode.Md3` 且 SDK≥31 时用 `dynamicLightColorScheme/darkColorScheme`
  （沿用原 dynamic color 基础设施），样式本身仍是普通 Material（无玻璃）。
- **相关改动**：`AppChrome.glassEnabled` → `AppChrome.visualMode`（AppRoot/Glass.kt/
  MainActivity/设置页「生效中的 chrome」文案全部改判 `VisualMode`）；版本
  versionCode=3 / versionName=1.1.3。

## 13.4. v1.2.0 控件可拖拽 + 平滑换肤 + 自定义取色 + 通知预览 + 切屏动效

- **开关与分段控件可拖拽（需求①）**：`CompactSwitch` 拇指可水平拖动（`Animatable` +
  `detectHorizontalDragGestures`，拖动中实时跟随并屏蔽回弹，松手按 0.5 阈值弹簧吸附并反写
  状态）；`SegmentedTabs` 支持全轨拖动（`onSizeChanged` 取宽、dragAmount/step 换算
  fraction、松手吸附最近段）。两者容器统一走 `liquidGlass`，随 `visualMode` 三态自动
  降级为实心 Material（Glass.kt:169），「玻璃模式用玻璃材质」天然成立，无需逐控件加开关。
- **日夜模式平滑过渡（需求②）**：`Theme.kt` 新增 `rememberAnimatedColorScheme`——用
  `animateColorAsState` + `tween(Standard)` 对 ColorScheme 全部 37 个角色逐项交叉淡化，
  `LiquidUITheme` 的 MaterialTheme/状态栏/导航栏/侧栏背景全部改用动画后的 scheme，
  避免模式切换瞬间闪烁刺眼；reducedMotion 时直接取终态无色。

- **自定义背景配色（需求④）**：`PaletteId` 新增 `Custom("自定义")`；`AppSettingsState`
  增加 `customSeed`（KEY_CUSTOM_SEED，默认 #3E8FE0）与 `customShade`（KEY_CUSTOM_SHADE，
  默认 0.5）及 update/reset；`Theme.kt` 新增 `seedSchemeFor`——HSV 色相旋转生成
  secondary/tertiary（±40°）、按亮度判 onColor、背景与表面容器向种子色按 shade 偏移，
  自由取色即时生效；`LiquidUITheme` 新增 `customSeed/customShade` 参数并透传。
  设置页外观区选中「自定义」色卡后展开取色器：圆形 HSV 色盘（色相绕环、饱和沿半径，
  点击/拖动无极取色）+ 右侧明度滑条，另配背景深浅滑块；自绘实现、零新依赖。
- **通知预览（需求③）**：Manifest 加 `POST_NOTIFICATIONS`；设置页新增「通知」区
  「发送测试通知」按钮（API 33+ 先运行时授权），创建 `liquid_preview` 即时通道并发送，
  通知着色取当前主题主色。
- **切屏弹性动效（需求⑤）**：AppRoot 用 `AnimatedContent` 包住当前屏，入场
  `scaleIn(0.94)+fadeIn` 走 `Motion.bouncy` 弹簧（还原 motion-web 的弹性），退场轻缩淡出，
  reducedMotion 退化为时长 180ms 的 tween。
- **版本**：versionCode=4 / versionName=1.2.0。

## 14. 交接推送状态

- [ ] 已推送交接代码到远程（需用户确认并授权）
- [ ] 未推送（默认，保持只读安全边界）

**判定标准（用户约定）**：若代码来自 Git 仓库，先询问「是否推送交接代码到远程」，
同意后再要 token；失败说明原因不阻塞；拒绝则在本节标注「未推送」，不重复询问。

> 当前状态：等待用户确认是否推送。
