# 动效系统 (Motion System)

动效 token 与物理交互遵循 `feitangyuan/motion-web` 的 motion 设计规范，并翻译为
Compose 原生实现。核心原则：**所有动效从 `ui/motion/Motion.kt` 读取 token，
调用点不出现魔法数**。

## 1. 动效 token

### v1.1 全局阻尼旋钮

`Motion.motionDamping`（默认 0.5）是全局弹簧阻尼，`Motion.DampedRatio(base)`
在弹簧构造时按 `lerp(0.72, 1.28, d)` 缩放基础阻尼比（clamp 0.25..1.25）——
一个滑块重调全应用手感，越低越弹。设置页滑块 → MainActivity `SideEffect` →
`Motion.motionDamping`，所有 spring token 读取点在下次组合时自动生效。

### 时长标尺

| Token | 值 | 用途 |
|---|---|---|
| `Instant` | 90ms | 游标反馈、tooltip |
| `Fast` | 180ms | 悬停、按钮激活、开关 |
| `Standard` | 320ms | 面板展开、抽屉 |
| `Medium` | 460ms | 区块揭示、图片打开 |
| `Slow` | 700ms | 页面进入、hero 揭示 |
| `Cinematic` | 1200ms | 冷开场、帷幕式进入 |

### 缓动字典

| Token | cubic-bezier | 性格 |
|---|---|---|
| `EaseOutExpo` | (0.16, 1, 0.3, 1) | 快进软着陆，揭示默认 |
| `EaseInExpo` | (0.7, 0, 0.84, 0) | 慢起硬出，仅用于退出 |
| `EaseInOutQuart` | (0.76, 0, 0.24, 1) | 对称顺滑，循环用 |
| `EaseOutBack` | (0.34, 1.56, 0.64, 1) | 轻微过冲着陆，俏皮悬停 |
| `EaseOutCirc` | (0, 0.55, 0.45, 1) | 干脆机械感 |
| `EaseSharp` | (0.65, 0, 0.45, 1) | 硬品牌砸入 |
| `EaseExpressive` | (0.68, -0.6, 0.32, 1.6) | 两端过冲，极少用 |

### 弹簧集合（核心）

参考源使用 Framer Motion 的 `(stiffness, damping)` 记法，Compose 使用
`(dampingRatio, stiffness)`。换算公式：

```
dampingRatio = damping / (2 * sqrt(stiffness * mass))
```

| Token | dampingRatio | stiffness | 性格 | 对应参考 |
|---|---|---|---|---|
| `gentle` | 0.90 | 240 | 软、慢、无回弹 | spring-gentle |
| `standard` | 0.82 | 520 | 自然，卡片/抽屉 | spring-default |
| `snappy` | 0.76 | 900 | 高级快感，按钮/标签 | spring-snappy |
| `bouncy` | 0.46 | 420 | 俏皮过冲 | spring-bouncy |
| `heavy` | 1.00 | 180 | 沉重迟钝，大面板 | spring-heavy |
| `stiff` | 0.90 | 2200 | 近即时，拖拽指针 | spring-stiff |

为直接在 Compose 圆资源路线演示过冲手感，`bouncy` 特意保留低阻比。

### 错峰阶跃

| Token | 每项延迟 | 用于 |
|---|---|---|
| `TightGrid` | 40ms | 密集网格 |
| `StandardList` | 70ms | 卡片列表、特性网格（默认） |
| `EditorialLine` | 90ms | 文本逐行揭示 |
| `DramaticCascade` | 150ms | 首页英雄区 |

### 指数跟随（`Motion.Follow`）

帧率无关的指数逼近，motion 规范 §7.1 的 Compose 版本。`tightness` 单位是
`1/s`（每秒逼近速率），不是每帧比例。分档：`CursorTightness` 18 / `CameraTightness`
4.5 / `RailTightness` 12 / `LevelTightness` 2.2。

## 2. 手势物理

### DampedDragAnimation（阻尼拖拽）

`ui/motion/DampedDragAnimation.kt`。拖拽过程中动态跟踪系统滚动速度，
为被拖拽元素注入带阻尼的惯性动量，松手后以弹簧回弹。这是底部标签栏与动效实验室
弹簧演示的基底。参考实现：`dyparse` 的 `DampedDragAnimation`。

### InteractiveHighlight（交互高亮）

`ui/motion/InteractiveHighlight.kt`。高亮/刮削动画与帧深度绑定（1 帧衰减），
帧率无关。配合 `DragGestureInspector.kt`（无副作用实现）读取拖拽速度。

### PlayfulTabs 与轴从独立

底部标签栏为签名组件：`LiquidBottomTabs` 同时组合了

- 阻尼线性位置插值（标签滑动）
- 高亮胶囊的弹性跟随（`InteractiveHighlight` + `Motion.RailTightness`）
- 按压瞬间体积压缩（体积守恒：宽增补高）
- X/Y 轴频率与幅度解耦，避免"抖成一团糊"

## 3. 现成动效 Modifier

`ui/motion/Modifiers.kt`：

| Modifier | 作用 | 来源 § |
|---|---|---|
| `pressScale` | 按压物理压缩（snappy 弹簧） | §4 |
| `impactShake` | 冲击抖动：双轴异频 + 线性衰减包络 | §8 |
| `idleBreathing` | 常态呼吸：双频不齐，画面永不静止 | §6 |
| `StaggeredReveal` | 错峰入场；`enabled=false` 时内容直接处于终态 | §5 |

冲击抖动两条规则（保证"有冲击力"而非"糊成一团"）：
2. X/Y 频率不同（47 与 31 rad），避免同频共振观感；
3. 振幅包络线性衰减至零，不是恒幅震荡。

## 4. 减少动效（Reduced Motion）

设置页开启后全局降级：

- 所有物理动画替换为临界阻尼（`Motion.CriticalDamping`，无过冲）；
- `StaggeredReveal` 的初始态默认为终态，内容永不不可见；
- 无限循环（aurora、idleBreathing）继续存在但参数收敛。

判断原则：**内容在 reduce 模式下必须完整可读**，动的只是"如何到达"，而不是"是否到达"。