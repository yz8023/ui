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
import androidx.compose.ui.res.stringResource
import com.monkeycode.liquidui.R
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
                    text = stringResource(R.string.info_text_1),
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
        title = stringResource(R.string.info_title_1),
        subtitle = stringResource(R.string.info_subtitle_1),
        onBack = onBack,
    ) {
        item {
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.common_library_subtitle), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.info_text_2), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("v${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.info_text_3), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { SectionTitle(stringResource(R.string.info_section_title_1)) }
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
            ) { Text(stringResource(R.string.info_text_4)) }
        }
    }
}

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    InfoPage(
        title = stringResource(R.string.common_privacy),
        subtitle = stringResource(R.string.info_subtitle_2),
        onBack = onBack,
    ) {
        item { SectionTitle(stringResource(R.string.info_section_title_2)) }
        item { InfoCard(stringResource(R.string.info_text_5), stringResource(R.string.info_text_6)) }
        item { InfoCard(stringResource(R.string.info_text_7), stringResource(R.string.info_text_8)) }
        item { InfoCard(stringResource(R.string.common_notification), stringResource(R.string.info_text_9)) }
        item { InfoCard(stringResource(R.string.info_text_10), stringResource(R.string.info_text_11)) }
        item { InfoCard(stringResource(R.string.info_text_12), stringResource(R.string.info_text_13)) }
    }
}

@Composable
fun PermissionCenterScreen(onBack: () -> Unit) {
    InfoPage(
        title = stringResource(R.string.common_permissions),
        subtitle = stringResource(R.string.info_subtitle_3),
        onBack = onBack,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            item {
                PermissionRequestCard(
                    PermissionSpec(
                        permission = Manifest.permission.POST_NOTIFICATIONS,
                        title = stringResource(R.string.info_title_2),
                        description = stringResource(R.string.info_desc_1),
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                InfoCard(
                    stringResource(R.string.info_text_14),
                    stringResource(R.string.info_text_15),
                )
            }
        } else {
            item {
                PermissionRequestCard(
                    PermissionSpec(
                        permission = Manifest.permission.READ_EXTERNAL_STORAGE,
                        title = stringResource(R.string.info_title_3),
                        description = stringResource(R.string.info_desc_2),
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item { InfoCard(stringResource(R.string.info_title_2), stringResource(R.string.info_text_16)) }
        }
        item { InfoCard(stringResource(R.string.info_text_17), stringResource(R.string.info_text_18)) }
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
