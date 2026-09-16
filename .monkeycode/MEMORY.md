# User Instruction Memory

This file records user instructions, preferences, and teachings for reference in future interactions.

## Format

### User Instruction Entry
User instruction entries should follow this format:

[User Instruction Summary]
- Date: [YYYY-MM-DD]
- Context: [Mentioned scenario or time]
- Instructions:
  - [Content of user teaching or instruction, described line by line]

### Project Knowledge Entry
Entries discovered by the Agent during task execution should follow this format:

[Project Knowledge Summary]
- Date: [YYYY-MM-DD]
- Context: Discovered by Agent while performing [specific task description]
- Category: [Operations & Deployment|Build Methods|Testing Methods|Troubleshooting & Debugging|Workflow & Collaboration|Environment Configuration]
- Instructions:
  - [Specific knowledge points, described line by line]

## Deduplication Strategy
- Before adding a new entry, check for similar or identical instructions.
- If a duplicate is found, skip the new entry or merge it with the existing one.
- When merging, update the context or date information.
- This helps avoid redundant entries and keeps the memory file tidy.

## Entries

[Project Knowledge Summary]
- Date: 2026-09-16
- Context: Discovered by Agent while building the liquid-ui-template Android Compose project and investigating debug APK size (16.5MB -> 9.7MB)
- Category: Build Methods
- Instructions:
  - Android 项目在 /workspace/android，构建命令 `cd /workspace/android && ./gradlew :app:assembleDebug --no-daemon`（首次生成 gradlew 需 `--network-timeout=120000`）
  - JDK 为 /usr/lib/jvm/java-17-openjdk-amd64，编译类命令必须通过 background terminal 执行并设 memory/cpu 上限，禁止直接跑前台 bash
  - 本项目 debug APK 体积根因是 `androidx.compose.material:material-icons-extended`（约 2000 图标）；已改为内联 9 个图标到 `ui/theme/AppIcons.kt`（ImageVector.Builder DSL），不要重新引入 extended 依赖
  - 图标新增方式：从 androidx material icons 源码（Apache-2.0）复制 ImageVector 声明加入 AppIcons，路径命令 `path {}` DSL 与 androidx 源码 1:1 对应
  - APK 产物：`/workspace/android/app/build/outputs/apk/debug/app-debug.apk`，发布拷贝到 `/workspace/dist/liquid-ui-template-debug.apk`
  - 签名校验：`/opt/android-sdk/build-tools/35.0.0/apksigner verify --print-certs <apk>`
  - AppSettingsState 中改变量的 setter 命名会与属性 setter 冲突（JVM 签名 clash），命名须用 `updateXxx` 而非 `setXxx`
