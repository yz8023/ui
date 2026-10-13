package com.monkeycode.liquidui.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monkeycode.liquidui.BuildConfig
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.PrimaryButton
import com.monkeycode.liquidui.ui.components.SectionTitle
import com.monkeycode.liquidui.ui.features.PermissionRequestCard
import com.monkeycode.liquidui.ui.features.PermissionSpec

private data class OpenSourceCredit(
    val name: String,
    val author: String,
    val purpose: String,
    val license: String,
    val url: String,
)

private val allCredits = listOf(
    OpenSourceCredit("StudyTrack（学迹）", "L1angK1NG 与项目贡献者", "液态玻璃参数体系、浅色视觉、卡片/按钮/指标组件及自定义壁纸体验参考；底部导航仍使用本项目原实现", "上游仓库未提供 LICENSE；本项目采用独立实现，不直接复制其源码", "https://github.com/L1angK1NG/StudyTrack"),
    OpenSourceCredit("dyparse", "kd64i", "液态玻璃与 Miuix 风格的视觉语言、配色、圆角及设置页结构参考", "以原仓库许可证为准", "https://github.com/kd64i/dyparse"),
    OpenSourceCredit("Miuix", "chibatching 及贡献者", "MIUI 风格 Compose 组件语义与布局参考", "Apache-2.0", "https://github.com/chibatching/Miuix"),
    OpenSourceCredit("backdrop-android", "kyant0 及贡献者", "玻璃背景采样、模糊、折射和高光渲染", "Apache-2.0", "https://github.com/kyant0/backdrop-android"),
    OpenSourceCredit("shapes", "kyant0 及贡献者", "Capsule 等自定义 Shape 支持", "Apache-2.0", "https://github.com/kyant0/shapes"),
    OpenSourceCredit("motion-web", "feitangyuan", "动效时长、缓动与弹簧 token 的设计参考", "以原仓库许可证为准", "https://github.com/feitangyuan/motion-web"),
    OpenSourceCredit("Framer Motion", "Motion 团队及贡献者", "弹簧 stiffness / damping 参数体系参考", "MIT", "https://www.framer.com/motion/"),
    OpenSourceCredit("YunX（云析）", "CYQawa 及贡献者", "欢迎引导、主题配置、动态桌面图标等交互结构参考", "AGPL-3.0", "https://github.com/CYQawa/YunX"),
    OpenSourceCredit("Material Color Utilities", "Google Material Foundation", "HCT / TonalSpot 配色算法参考", "Apache-2.0", "https://github.com/material-foundation/material-color-utilities"),
    OpenSourceCredit("Jetpack Compose / Material 3", "Android Open Source Project / Google", "界面、主题、动画及 Android 平台能力", "Apache-2.0", "https://developer.android.com/compose"),
    OpenSourceCredit("Material Icons", "Android Open Source Project", "应用内矢量图标路径", "Apache-2.0", "https://fonts.google.com/icons"),
    OpenSourceCredit("Android activity-alias / PackageManager", "Android Open Source Project / Google", "运行时切换桌面应用图标", "官方文档", "https://developer.android.com/guide/topics/manifest/activity-alias-element"),
    OpenSourceCredit("Changing an Android App Icon", "SitePoint 社区作者", "activity-alias 图标切换实现思路参考", "文章", "https://www.sitepoint.com/changing-your-android-apps-icon-dynamically/"),
)

@Composable
private fun InfoPage(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "‹ 返回",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp),
                )
                Text(title, style = MaterialTheme.typography.displayMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        content()
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    InfoPage(
        title = "关于",
        subtitle = "完整列出直接参考的项目、作者、用途与许可证，尊重每一位开源作者。",
        onBack = onBack,
    ) {
        item {
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Android 功能素材库", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text("Liquid Motion UI 示例应用", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("v${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(10.dp))
                Text("本项目自身代码采用 MIT 许可证。若复制或分发上游代码，请同时遵守对应上游许可证并保留署名。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { SectionTitle("参考项目与作者") }
        allCredits.forEach { credit ->
            item(key = credit.url) {
                GlassCard(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(credit.url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        runCatching { context.startActivity(intent) }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(credit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("作者：${credit.author}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Text(credit.purpose, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("许可证：${credit.license}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("打开 ↗", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        item {
            PrimaryButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yz8023/ui")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching { context.startActivity(intent) }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) { Text("查看本项目源码与 CREDITS.md") }
        }
    }
}

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    InfoPage(
        title = "隐私说明",
        subtitle = "逐项说明数据保存位置、权限用途和网络行为。",
        onBack = onBack,
    ) {
        item { SectionTitle("数据处理") }
        item { InfoCard("本地偏好", "主题、动效、桌面图标与引导完成状态仅保存在设备本地 SharedPreferences，不会上传。") }
        item { InfoCard("壁纸读取", "仅在你主动开启桌面壁纸背景后申请媒体读取权限；读取内容只用于本机背景绘制，不保存、不上传。") }
        item { InfoCard("通知", "仅在你点击“发送测试通知”时申请通知权限并创建本地通知，不向服务器发送数据。") }
        item { InfoCard("网络与账号", "应用没有账号、分析、广告、遥测或后台上传功能。点击项目链接时会交给系统浏览器打开。") }
        item { InfoCard("权限撤回", "可随时在 Android 系统设置的“应用信息 → 权限”中撤回授权；撤回不影响其他离线功能。") }
    }
}

@Composable
fun PermissionCenterScreen(onBack: () -> Unit) {
    InfoPage(
        title = "权限管理",
        subtitle = "权限不是介绍弹窗：可在这里真实请求、检查授权状态，并在拒绝后进入系统设置恢复。",
        onBack = onBack,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            item {
                PermissionRequestCard(
                    PermissionSpec(
                        permission = Manifest.permission.POST_NOTIFICATIONS,
                        title = "通知权限",
                        description = "用于你主动触发的主题预览通知。",
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                PermissionRequestCard(
                    PermissionSpec(
                        permission = Manifest.permission.READ_MEDIA_IMAGES,
                        title = "媒体图片权限",
                        description = "仅在启用桌面壁纸背景时读取系统返回的壁纸图像。",
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        } else {
            item {
                PermissionRequestCard(
                    PermissionSpec(
                        permission = Manifest.permission.READ_EXTERNAL_STORAGE,
                        title = "存储读取权限",
                        description = "仅在启用桌面壁纸背景时读取系统返回的壁纸图像。",
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item { InfoCard("通知权限", "Android 12 及以下无需单独申请通知运行时权限。") }
        }
        item { InfoCard("最小权限原则", "应用不申请位置、相机、麦克风、通讯录或电话权限。所有权限均由明确的用户操作触发。") }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
