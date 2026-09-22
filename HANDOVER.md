# 项目交接文档 (HANDOVER)

> 交接日期：2026-09-20
> 交接人：MonkeyCode AI Agent
> 项目定位：**Android 功能素材库 · 选择式开发**
> 远程仓库：`https://github.com/yz8023/ui`

---

## 1. 项目是什么

把常用的 Android 开发能力拆成一个个**中文命名的功能文件夹**（`素材库/`），
每个功能点包含 `实现方案.md / 代码/ / 参考来源.md / 环境要求.md`，
让普通人可以按需挑选功能、照抄代码，拼出自己的 App。

- 功能索引：`README.md`
- 源码致谢：`CREDITS.md`
- 示例应用：`android/`（所有素材的组合演示，可编译）

## 2. 功能点清单

| 功能点 | 目录 | 说明 |
|---|---|---|
| 应用图标切换 | `素材库/应用图标切换/` | activity-alias + `setComponentEnabledSetting` 运行时换图标（独立功能） |
| 液态玻璃主题 | `素材库/液态玻璃主题/` | backdrop-android 真玻璃 + 7 套配色换肤（根基） |
| 底部导航栏 | `素材库/底部导航栏/` | 可拖拽弹簧玻璃底栏，自动降级 Material3 |
| 动效系统 | `素材库/动效系统/` | token 化弹簧 / 按压冲击 / 错峰入场 |
| 玻璃常用组件 | `素材库/玻璃常用组件/` | 按钮 / 开关 / 取色盘 / 弹窗 / 输入框 |
| 状态反馈组件 | `素材库/状态反馈组件/` | 加载 / 空态 / 错误 / 成功 / 离线提示 / 骨架屏 |
| 权限请求组件 | `素材库/权限请求组件/` | 权限说明卡 + 请求 + 系统设置跳转 |
| 主题配置导入导出 | `素材库/主题配置导入导出/` | 主题 JSON 导入 / 导出 / 跨设备恢复 |
| 协议与开源许可页面 | `素材库/协议与开源许可页面/` | 隐私说明 / 开源许可 / 项目主页入口 |

依赖关系：`液态玻璃主题`（根基）→ `底部导航栏` / `玻璃常用组件`，三者共用
`动效系统`；`应用图标切换` 独立。

## 3. 示例应用（android/）

完整可编译工程，演示素材组合：

- 设置页：三态视觉风格（玻璃 / 普通 / MD3）、7 套配色、自定义 HSV 取色、
  桌面壁纸背景、通知预览
- **应用图标切换**：设置页「应用图标」区，冰蓝 / 星夜 / 珊瑚三套图标即时切换
  （Manifest 的 `<activity-alias>` + `ui/features/AppIconSwitcher.kt`）
- 液态玻璃底栏 + 四页面导航 + 全应用弹簧动效
- 首次启动欢迎引导页已接入，可在设置页重新查看
- 设置页新增主题 JSON 导入 / 导出、合规说明与项目主页入口
- 组件页新增加载 / 成功 / 离线 / 错误状态反馈演示

版本：`versionCode=6 / versionName=2.1.0`。

## 4. 环境要求

| 依赖 | 版本 |
|---|---|
| JDK | 17 |
| Android SDK | platform 36 + build-tools 35.0.0（backdrop 要求 compileSdk 36） |
| Gradle | 8.11.1（wrapper） |
| Kotlin / AGP | 2.3.10 / 8.7.3 |
| Compose BOM | 2024.10.01 |

本机 SDK 路径：`/opt/android-sdk`（写入 `android/local.properties`，不入库）。

## 5. 构建

```bash
cd android
./gradlew :app:assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

- 只产出 debug 包，**不做 release 发布**（已清理签名/发布流程）。
- CI（`.github/workflows/build-apk.yml`）推送后构建 debug APK 并上传 artifact。
- R8 阶段内存峰值高，本机构建需给后台终端分配 ≥45% 内存预算。

## 6. 目录结构

```
/workspace/
├── README.md                  # 功能索引 + 使用指南
├── CREDITS.md                 # 致谢：源码项目来源
├── 素材库/                    # 功能素材库（5 个中文功能文件夹）
├── android/                   # 示例应用（组合演示，可编译）
├── docs/                      # 环境搭建 / 构建指南 / 组合指南
├── preview/                   # 预览截图与 GIF
├── web-preview/               # 纯静态 Web 交互预览
└── .github/workflows/         # CI（debug APK）
```

## 7. 已知问题与坑

| 问题 | 说明 / 规避 |
|---|---|
| Gradle wrapper 首次失败 | `--network-timeout=120000` 重试 |
| backdrop 需 compileSdk 36 | `gradle.properties` 已配 `android.suppressUnsupportedCompileSdk=36` |
| backdrop 为 Kotlin 2.3 元数据 | Kotlin 已锁定 2.3.10，勿降 2.0.x |
| 弹窗玻璃退化成 tint | 必须用 `GlassDialogHost` 同窗浮层，不能用系统 `Dialog` |
| 图标切换后桌面没变 | Launcher 缓存，稍等/重启桌面 |
| `setXxx` 命名冲突 | 状态更新统一 `updateXxx`（JVM 签名冲突） |
| 依赖下载慢 | `docs/构建指南.md` 有阿里云镜像 |

## 8. Git 与版本

- 分支：`main`，远程 `origin/main`。
- 推送状态：见提交时确认；推送后可手动创建 MR。

## 9. 交接推送状态

- [ ] 已推送交接代码到远程（需用户确认并授权）
- [ ] 未推送（默认，保持只读安全边界）

> 当前状态：等待用户确认是否推送。
