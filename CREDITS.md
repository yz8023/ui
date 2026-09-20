# 致谢 · 源码项目来源

本素材库的代码与视觉语言建立在多个优秀的开源项目之上。衷心感谢所有原作者们
的无私分享。下面按功能点列出**直接参考**的项目来源。

## 液态玻璃视觉语言（根基）

| 项目 | 链接 | 说明 |
|---|---|---|
| **kd64i/dyparse** | https://github.com/kd64i/dyparse | 液态玻璃 + Miuix 风格的视觉语言来源：配色、卡片圆角、设置页结构、玻璃面板质感均参考于此 |
| **Miuix · chibatching** | https://github.com/chibatching/Miuix | MIUI 风格的 Compose 组件库，控件语义与布局结构参考 |

## 玻璃渲染引擎

| 项目 | 链接 | 说明 |
|---|---|---|
| **backdrop-android · kyant0** | https://github.com/kyant0/backdrop-android | 真 backdrop 玻璃库（`io.github.kyant0:backdrop-android`）：采样、模糊、折射、高光的底层实现 |
| **shapes · kyant0** | https://github.com/kyant0/shapes | 自定义 Shape（`Capsule`）支持库 |

## 动效设计系统

| 项目 | 链接 | 说明 |
|---|---|---|
| **feitangyuan/motion-web** | https://github.com/feitangyuan/motion-web | 动效设计系统：时长 / 缓动 / 弹簧 token 的来源（`motion-tokens.md`） |
| **Framer Motion** | https://www.framer.com/motion/ | 弹簧参数 `(stiffness, damping)` 记法与预设的本源 |

## 应用图标切换

| 项目 / 文档 | 链接 | 说明 |
|---|---|---|
| Android 官方 · activity-alias | https://developer.android.com/guide/topics/manifest/activity-alias-element | alias 组件声明机制 |
| Android 官方 · PackageManager | https://developer.android.com/reference/android/content/pm/PackageManager | `setComponentEnabledSetting` 组件启用/禁用 |
| 社区 · Sitepoint 教程 | https://www.sitepoint.com/changing-your-android-apps-icon-dynamically/ | 运行时换图标的经典实现思路 |

## 应用欢迎引导界面

| 项目 | 链接 | 说明 |
|---|---|---|
| **CYQawa/YunX（云析）** | https://github.com/CYQawa/YunX | 首次启动引导页结构（品牌区 / 功能列表 / 免费卡 / 免责卡 / 开源入口 / 底部主操作），AGPL-3.0 |

## 主题色配置与切换

| 项目 | 链接 | 说明 |
|---|---|---|
| **CYQawa/YunX（云析）** | https://github.com/CYQawa/YunX | 三态外观模式（跟随系统 / 浅色 / 深色）、动态色彩开关（Android 12+）、10 色预选板 + HSV 自定义调色盘、HCT/TonalSpot 种子色生成完整颜色方案，AGPL-3.0 |
| **material-color-utilities** | https://github.com/material-foundation/material-color-utilities | `SchemeTonalSpot` + `Hct` 算法，从单一种子色生成全套 Material 3 颜色令牌（含于 `com.google.android.material:material`） |

## 桌面图标动态切换

| 项目 | 链接 | 说明 |
|---|---|---|
| **CYQawa/YunX（云析）** | https://github.com/CYQawa/YunX | activity-alias 两套图标（经典 / 新图标）切换、变体值 SharedPreferences 持久化、Compose 设置页卡片集成，AGPL-3.0 |

## 其他资源

- **Material 3 / Jetpack Compose**（Google）：https://developer.android.com/jetpack/compose
  —— 主题、组件、动画的官方实现与文档。
- **Material Icons**（Apache-2.0）：内联图标矢量路径来自 androidx material icons 源码。

## 开源许可证说明

| 项目 | 许可证 |
|---|---|
| backdrop-android / shapes | Apache-2.0 |
| Miuix | Apache-2.0 |
| Android (AOSP) | Apache-2.0 |
| Material Icons | Apache-2.0 |

本素材库自己的代码（`素材库/*/代码/` 与示例应用）以 **MIT** 许可证开源，
可自由用于个人与商业项目。引用上游代码时请保留对应项目的许可证声明并署名。

再次感谢所有开源贡献者。
