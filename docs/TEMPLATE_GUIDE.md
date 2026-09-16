# 模板套用指南 (Template Guide)

将本模板接入你的业务项目，并整体换肤、加入业务功能的分步指南。

## 1. 拷贝工程

```bash
cd /your/project/dir
cp -r /workspace/android ./
```

随后按你的包名/应用名批量替换：

| 位置 | 原值 | 改为 |
|---|---|---|
| `app/build.gradle.kts` | `applicationId` / `namespace` = `com.monkeycode.liquidui` | 你的包名 |
| `MainActivity.kt` | package 声明 | 你的包名 |
| 其余 `.kt` 文件 | package 声明 | 你的包名 |
| `res/values/strings.xml` | `app_name` | 你的应用名 |
| `res/drawable/ic_launcher_foreground.xml` | 参考图标 | 你的图标 |

重命名包前建议先用 IDE 的 Refactor → Rename Package 整体重命名，再核对
`AndroidManifest.xml` 与三个资源小图标的引用。

## 2. 本地构建

前置要求：JDK 17+、Android SDK（platform-35 + build-tools 35.0.0）、Gradle 8.11.1。

```bash
# 生成 local.properties（指向你的 SDK）
# 内容：sdk.dir=/path/to/your/android-sdk

cd android
./gradlew assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

若机器已有 Android Studio，直接用 `File → Open` 打开 `android/` 目录即可，
AS 会自动识别 wrapper 与 SDK。首次构建需联网下载依赖。

## 3. 整体换肤

全部视觉取值收敛在三个文件：

```text
ui/theme/Color.kt   # 色彩 token（见 docs/DESIGN_SYSTEM.md）
ui/theme/Shape.kt   # 圆角 token
ui/theme/Type.kt    # 文字 token
```

示例：改为暖橙品牌色。

```kotlin
// Color.kt
val BlueDark = Color(0xFFFFC966)      // 主强调
val BlueDarkContainer = Color(0xFF6B4E12)  // 容器底
val OnBlueDarkContainer = Color(0xFFFFEFC2)
val AuroraAmber = Color(0xFFFFC966)   // Aurora 语义色同步
```

不改任何组件代码，整个 app 的颜色体系即变化。Aurora 背景的浅色/深色光斑在
`Color.kt` 底部的 `AuroraLight` / `AuroraDark`。

## 4. 迁入业务页面

以新增一个"我的消息"页面为例：

1. `ui/navigation/Screen.kt` 增加 `object Messages : Screen("messages", "消息", icon)`；
2. `ui/screens/` 新增 `MessagesScreen.kt`，页面骨架复用现有样式：

```kotlin
@Composable
fun MessagesScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle("未读") }
        items(messages) { msg ->
            StaggeredReveal(index = /* 序号 */) { GlassCard { ... } }
        }
    }
}
```

3. `ui/navigation/AppRoot.kt` 的 `items` 列表加入新 tab：
   `LiquidBottomTabs` 会自动获得高亮与滑动行为，无需额外代码。

组件契约：页面内唯一需要遵守的约定是——业务卡片一律用 `GlassCard` 或
`liquidGlass` 表面，内容手把手（按压有反馈的）交互一律叠 `pressScale`。
把这两个约定延续下去，页面与既有皮肤会自然一致。

## 5. 设置与持久化

`ui/screens/AppSettingsState.kt` 是唯一的磁盘写入方（一个 `SharedPreferences`
文件）。新增业务设置项照抄既有 `var xxx by mutableStateOf(...) + updateXxx()`
模式即可。注意：**不要**把设置项定义在页面里，全部收敛到该 state holder，
保证模板"一处状态、持久化"的约定不被破坏。

## 6. 动效接入约定

- 新增动画一律从 `ui/motion/Motion.kt` 取 token，禁止调用点写魔法数；
- 周期性/入场动画用现成 Modifier：`pressScale`、`impactShake`、
  `idleBreathing`、`StaggeredReveal`；
- 自定义手势物理再考虑下沉到 `ui/motion/`，并写清来源与换算说明。

## 7. 减少动效保障

业务页面接入 `StaggeredReveal`（或任何入场动画）时，**必须**从全局
`LocalAppChrome.current.reducedMotion` 取值并传入 `enabled`，否则 reduce 模式下
可能出现不可见内容。模板内所有现有调用点均已遵循。

## 8. 发布（摘要）

发布流程完整见 `docs/BUILD_AND_RELEASE.md`：release 签名 → `assembleRelease`
→ 产物在 `app/build/outputs/apk/release/`。debug 包可直接分发给体验用户。