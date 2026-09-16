# 构建与发布 (Build & Release)

## 1. 环境要求

| 依赖 | 版本 |
|---|---|
| JDK | 17+ |
| Android SDK | platform-36、build-tools 35.0.0、platform-tools |
| Gradle | 8.11.1（工程内已含 wrapper，推荐使用） |

工程通过 `gradle/libs.versions.toml` 锁定 AGP 8.7.3 / Kotlin 2.3.10 /
Compose BOM 2024.10.01。backdrop-android 玻璃库要求 compileSdk≥36，工程已配
`compileSdk = 36` 并在 `gradle.properties` 写入 `android.suppressUnsupportedCompileSdk=36`。

## 2. 构建 Debug 包（日常 / 分发体验）

```bash
cd /workspace/android

# local.properties 必须指向你的 SDK 路径
# 内容：sdk.dir=/path/to/android-sdk

./gradlew assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

Debug 包使用 Android 默认 debug 签名，可直接安装到任意开启了"允许安装未知来源"
的设备上体验。产物副本已放于仓库 `dist/liquid-ui-template-debug.apk`。

## 3. 数学验证产物完整性

```bash
/opt/android-sdk/build-tools/35.0.0/apksigner verify --print-certs app/build/outputs/apk/debug/app-debug.apk
# Signer #1 certificate ... CN=Android Debug
```

## 4. 发布 Release 包

`app/build.gradle.kts` 里 release 变体示例（占位）片段：

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("...")        // keystore 路径
            storePassword = "..."          // 建议从环境变量注入，勿入库
            keyAlias = "..."
            keyPassword = "..."
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

发布步骤：

```bash
# 1. 生成 keystore（首次）
keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000

# 2. 填入 app/build.gradle.kts signingConfigs 后
./gradlew assembleRelease

# 3. 验证并对齐（AGP 已自动执行 zipalign；apksigner 校验签名）
/opt/android-sdk/build-tools/35.0.0/apksigner verify --print-certs \
  app/build/outputs/apk/release/app-release.apk
```

## 5. 签名信息注意事项

- keystore 与口令属于机密：演示用 debug 签名可直接分发；正式发布前必须配置
  自有 release 签名，并妥善备份 keystore（丢失即无法更新同签名应用）。
- 模板内 「MD5/路径/token」 一类敏感占位应随业务替换，不要沿用示例值。

## 6. 常见问题

| 现象 | 处理 |
|---|---|
| `Could not find com.android.tools.build:gradle:8.7.3` | 检查 `gradle/libs.versions.toml` 校验版本未写错；首次需要联网 |
| `SDK location not found` | `local.properties` 写 `sdk.dir=` 指向你的 Android SDK |
| `FAILURE: Build failed ... :wrapper` | services.gradle.org 可达性；用工程自带版本或 `./gradlew --offline`（依赖已缓存时） |
| R8 混淆后组件异常（编译期仅 warning） | 添加 `-keep class com.monkeycode.liquidui.** { *; }` 到 `proguard-rules.pro` |

## 7. 一键体验（本机在线预览）

浏览器可先行体验交互：

```bash
cd /workspace/web-preview
python3 -m http.server 8080
# 打开 http://localhost:8080
```

这是纯静态脚手架交互预览，用于快速审阅"动效手感"，与 APK 内实现同一套 token。