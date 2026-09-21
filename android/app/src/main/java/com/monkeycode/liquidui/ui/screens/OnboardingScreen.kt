package com.monkeycode.liquidui.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.components.AuroraBackground
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.InfoBanner
import com.monkeycode.liquidui.ui.components.PrimaryButton
import com.monkeycode.liquidui.ui.components.SecondaryButton
import com.monkeycode.liquidui.ui.components.Tag
import com.monkeycode.liquidui.ui.motion.StaggeredReveal
import com.monkeycode.liquidui.ui.theme.AppIcons
import com.monkeycode.liquidui.ui.theme.AuroraAmber
import com.monkeycode.liquidui.ui.theme.AuroraMint
import com.monkeycode.liquidui.ui.theme.AuroraRose
import com.monkeycode.liquidui.ui.theme.AuroraViolet

private data class OnboardingFeature(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accent: Color
)

private val onboardingFeatures = listOf(
    OnboardingFeature(
        icon = AppIcons.BlurOn,
        title = "液态玻璃主题",
        description = "玻璃面板、动态背景、折射强度、透明度与圆角都能在设置页实时调节。",
        accent = AuroraMint
    ),
    OnboardingFeature(
        icon = AppIcons.Widgets,
        title = "常用组件即拷即用",
        description = "按钮、输入框、开关、弹窗、状态反馈与偏好行保持统一的材质和动效。",
        accent = AuroraViolet
    ),
    OnboardingFeature(
        icon = AppIcons.Animation,
        title = "动效 token 系统",
        description = "弹簧、错峰入场、按压反馈和减少动效都收敛成可复用规范。",
        accent = AuroraAmber
    ),
    OnboardingFeature(
        icon = AppIcons.Settings,
        title = "选择式功能开发",
        description = "按需复制素材目录里的实现方案、代码、环境要求和参考来源。",
        accent = AuroraRose
    )
)

/**
 * First-run guide used by the sample app. It turns the standalone onboarding
 * material into a real, persisted entry point and can also be reopened from
 * Settings for documentation/demo purposes.
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AuroraBackground(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 32.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                StaggeredReveal(index = 0, enabled = true) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(R.mipmap.ic_launcher),
                            contentDescription = "应用图标",
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(24.dp))
                        )
                        Tag(text = "Android 功能素材库")
                        Text(
                            text = "选功能，抄代码，拼出你的 App",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "这里不是单一 UI Demo，而是一套可拆、可复制、可组合的 Compose 功能素材。",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                StaggeredReveal(index = 1, enabled = true) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            onboardingFeatures.forEach { feature ->
                                FeatureLine(feature)
                            }
                        }
                    }
                }
            }

            item {
                StaggeredReveal(index = 2, enabled = true) {
                    InfoBanner(
                        text = "所有偏好都保存在本地。你可以随时在「设置」中重新查看本引导、导入导出主题配置。",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                StaggeredReveal(index = 3, enabled = true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SecondaryButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/yz8023/ui")
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                runCatching { context.startActivity(intent) }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("开源仓库") }
                        PrimaryButton(
                            onClick = onFinish,
                            modifier = Modifier.weight(1f)
                        ) { Text("开始使用") }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureLine(feature: OnboardingFeature) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = feature.accent,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
