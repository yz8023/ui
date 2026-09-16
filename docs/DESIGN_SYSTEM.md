# 设计系统 (Design System)

视觉语言遵循参考项目 `kd64i/dyparse` 的液态玻璃 + Miuix 风格。系统设计围绕
"把静态样式的每一种取值收敛成一个 token" 展开，以便整体换肤。

## 1. 色彩 token

全部集中在 `ui/theme/Color.kt`。结构分四组：

| 分组 | 示例 | 说明 |
|---|---|---|
| 核心中性色（浅色模式） | `Ink` `Paper` `PaperContainer` | 正文 / 页面底 / 容器底 |
| 核心中性色（深色模式） | `Night` `NightContainer` | 深色底，避免纯黑 |
| 强调色 | `BlueDark` `BlueDarkContainer` | 主操作、激活态 |
| Aurora 语义色 | `AuroraMint` `AuroraAmber` `AuroraRose` `AuroraViolet` | 演示组件用点缀色 |

换肤 = 编辑本文件。深浅两套 `ColorScheme` 在 `Theme.kt` 中由这些 token 组装。

**v1.1 多主题**：`Theme.kt` 新增 `PaletteId`（冰蓝 / 薄荷 / 落日 / 素灰）与
`PaletteSeeds`（每套含 light/dark 的 primary / primaryContainer / secondary /
tertiary），`schemeFor(palette, dark)` 负责组装，设置页色卡即点即换；
`palettePrimaryLight(palette)` 供色卡预览取色。

## 2. 圆角 token

`ui/theme/Shape.kt` 暴露单一 `AppShape`：

```kotlin
object AppShape {
    val small = RoundedCornerShape(10.dp)
    val medium = RoundedCornerShape(14.dp)
    val large = RoundedCornerShape(18.dp)
    val pill = RoundedCornerShape(50)
}
```

设置页的「圆角缩放」滑块通过 `Theme.kt` 的 `scaledShapes(scale)` 在运行时把
`MaterialTheme.shapes.*` 整体放大/缩小，属于设计系统自带的"预览器"能力。

## 3. 文字 token

`ui/theme/Type.kt` 提供一套以 `sp` 定义的字号 + `FontWeight` 分级。模板内所有文字
标注样式均读取 `MaterialTheme.typography`，不在调用点手写字号。

## 4. 玻璃表面（liquidGlass）

`ui/components/Glass.kt` 中的 `Modifier.liquidGlass` 是所有面板的皮肤。它采样
`LocalGlassBackdrop` 提供的真 backdrop 层（与参考项目 dyparse 同款
`io.github.kyant0:backdrop-android:2.0.0-alpha03`），自下而上共四层：

1. 软投影 `Shadow`
2. 采样的 backdrop 经 `vibrancy()` + `blur(16dp)` + `lens()`（折射强度跟随 `glassRefraction`）
3. 半透明 tint 填充（密度跟随 `glassOpacity`）
4. 顶部 specular 高光 `Highlight`（替代旧版白色渐变描边）

`glassOpacity` / `glassRefraction` 是 `AppChrome` 的运行时可调 token（0..1），
由设置页「透明度」「折射率」滑块驱动，所有玻璃面板实时联动。

### Backdrop 图层结构（AppRoot.kt）

```
surfaceColor 底
 ├─ auroraBackdrop 层：AuroraBackground 单独录制进 GraphicsLayer
 │     └─ 所有页内玻璃面板采样它（卡片折射身后的色彩，无反馈回路）
 ├─ contentBackdrop 层：页面内容单独录制
 │     └─ 悬浮底栏采样 combined(aurora, content)，内容从玻璃下滚过时被折射
 └─ LiquidBottomTabs（最顶，采样 combined）
```

- 页内玻璃面板不采样 content 层：面板位于滚动流内，背后只有 aurora。
- 悬浮底栏必须采样 content：它是唯一"内容从其下方滚过"的玻璃。
- **v1.1 弹窗改为同窗浮层**：独立 Window 的 `Dialog` 无法跨窗口采样 backdrop，
  `Dialogs.kt` 的 `GlassDialogHost` 把浮层提升到 `AppRoot` 根 Box 末尾渲染，
  幕帘层录制为 scrim backdrop 并与 aurora `rememberCombinedBackdrop`，弹窗卡片
  真折射；无 backdrop（普通布局）时降级为纯色幕帘 + Material 卡片。

### 普通布局模式（glassEnabled=false）

`AppChrome.glassEnabled` 是液态玻璃总开关。关闭时 `liquidGlass` 走纯色分支
（shadow + clip + 实心 surfaceContainer，不采样 backdrop），`AppRoot` 跳过双层
backdrop 录制并改用 Material3 `NavigationBar`，整套玻璃机制彻底停用。

### 平台能力降级

`blur()` 需 SDK≥31，`lens()`（RuntimeShader）需 SDK≥33；不满足时库内部直接
跳过对应 effect，面板保持"采样 + tint + 高光"的优雅降级，minSdk=26 可用。

`AuroraBackground` 是玻璃层之下的装饰层：四个带独立轨道周期的径向渐变光斑，
作为"玻璃折射的对象"存在。图层顺序约定：

```
AuroraBackground（最底） → 玻璃面板 → 悬浮底部标签栏（最顶）
各页 LazyColumn 底部 contentPadding ≈ 108.dp，让内容从玻璃下滚过
```

## 5. 组件目录

`ui/components/`（组件代码自研，玻璃层依赖 backdrop-android）：

| 组件 | 文件 | 说明 |
|---|---|---|
| `liquidGlass` / `GlassCard` / `GlassPill` | `Glass.kt` | 玻璃表面三种形态 |
| `LiquidBottomTabs` | `LiquidBottomTabs.kt` | 签名组件：含弹簧阻尼滚动 + 高亮跟随 |
| `PrimaryButton` / `SecondaryButton` | `Controls.kt` | 物理按压反馈 + 冲击动效 |
| `SegmentedTabs` | `Controls.kt` | 分段选择器 |
| `CompactSwitch` / `SliderSetting` | `Controls.kt` | 开关 / 滑块设置行 |
| `InfoBanner` / `Toast` | `Feedback.kt` | 信息提示 |
| `TextField` 变体 | `TextFields.kt` | 输入框 |
| `StatTile` / `Tag` / `EmptyState` | `Rows.kt` / 组合页 | 信息展示 |

## 6. 运行时可调 AppChrome

`Theme.kt` 通过 `LocalAppChrome` 暴露全局旋钮，由 `AppSettingsState`
（`SharedPreferences` 持久化）驱动：
| 旋钮 | 作用 | 默认 |
|---|---|---|
| `glassEnabled` | 液态玻璃总开关（关=普通 Material 布局） | true |
| `glassOpacity` | 玻璃透明度（tint 密度） | 0.55 |
| `glassRefraction` | 折射率（lens 强度） | 1.0 |
| `cornerScale` | 全局圆角缩放 | 1.0 |
| `reducedMotion` | 减少动效开关 | false |
| `motionDamping` | 全局弹簧阻尼（写 `Motion.motionDamping`） | 0.5 |
| `wallpaperBackground` | 用桌面壁纸做背景层 | false |

> 旧版 `glassFrost` 持久化键保留读取（`KEY_FROST`），升级安装时旧值自动迁移为
> `glassOpacity`。

## 7. 图标

模板**不依赖 `material-icons-extended`**（约 2000 个图标，是 debug APK 体积
最大的单一来源）。仅把实际用到的 11 个图标内联进
`ui/theme/AppIcons.kt`，用稳定的 `ImageVector.Builder` + `path {}` DSL 声明：

`Animation` / `BlurOn` / `Image` / `Notifications` / `Palette` / `RotateRight` /
`Settings` / `Shield` / `Speed` / `WaterDrop` / `Widgets`

调用方式：`Icon(AppIcons.Settings, contentDescription = null)`。

新增图标时，从 `androidx material icons` 源码（Apache-2.0）复制 `ImageVector`
声明加入 `AppIcons`，不要重新引入 `material-icons-extended`。
