package com.monkeycode.liquidui.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.components.EmptyState
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.InfoBanner
import com.monkeycode.liquidui.ui.components.PrimaryButton
import com.monkeycode.liquidui.ui.components.SecondaryButton
import com.monkeycode.liquidui.ui.components.StatTile
import com.monkeycode.liquidui.ui.components.Tag
import com.monkeycode.liquidui.ui.theme.AuroraAmber
import com.monkeycode.liquidui.ui.theme.AuroraMint
import com.monkeycode.liquidui.ui.theme.AuroraRose
import com.monkeycode.liquidui.ui.theme.AuroraViolet
import com.monkeycode.liquidui.ui.motion.Motion
import com.monkeycode.liquidui.ui.motion.StaggeredReveal
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.res.stringResource

private data class Principle(
    val index: String,
    val title: String,
    val body: String,
    val accent: androidx.compose.ui.graphics.Color
)


private data class FeatureMaterial(
    val name: String,
    val description: String,
    val status: String,
    val accent: androidx.compose.ui.graphics.Color
)

private val featureMaterials = listOf(
    FeatureMaterial("液态玻璃主题", "真 backdrop 玻璃、7 套配色、普通 / MD3 降级。", "已接入", AuroraMint),
    FeatureMaterial("应用欢迎引导", "首次启动说明、免费声明、开源入口，可从设置重新查看。", "新增", AuroraAmber),
    FeatureMaterial("状态反馈组件", "加载、空态、错误、成功、离线提示与重试骨架。", "新增", AuroraRose),
    FeatureMaterial("主题导入导出", "外观和动效偏好一键复制为 JSON，可跨设备恢复。", "新增", AuroraViolet),
    FeatureMaterial("权限请求组件", "通知、媒体、相机、位置权限的解释卡与设置跳转素材。", "素材", AuroraMint)
)

private val principles = listOf(
    Principle(
        index = "01",
        title = "动效即材质",
        body = "动效是界面材料本身，不是事后装饰。每个位移都有阻尼、动量和过冲回弹。",
        accent = AuroraMint
    ),
    Principle(
        index = "02",
        title = "反 AI 塑料感",
        body = "禁用无意义的 0→1 淡入与线性匀速。统一使用二阶弹簧阻尼与各向异性形变。",
        accent = AuroraViolet
    ),
    Principle(
        index = "03",
        title = "可验证的手感",
        body = "时序、缓动、错峰全部以 token 固化，可被量化核对，避免同一产品出现两套节奏。",
        accent = AuroraAmber
    ),
    Principle(
        index = "04",
        title = "降级即设计",
        body = "减少动效模式是一套完整设计，而不是坏掉的页面：默认终态始终可用。",
        accent = AuroraRose
    )
)

@Composable
fun HomeScreen(
    settings: AppSettingsState,
    onOpenComponents: () -> Unit,
    onOpenMotion: () -> Unit
) {
    val staggerEnabled = !settings.reducedMotion

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            StaggeredReveal(index = 0, enabled = staggerEnabled) {
                Column(
                    modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Tag(text = "Android · Jetpack Compose")
                    Text(
                        text = stringResource(R.string.home_text_1),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.home_text_2) +
                            stringResource(R.string.home_text_3),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            StaggeredReveal(index = 1, enabled = staggerEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatTile(
                        label = stringResource(R.string.home_label_1),
                        value = "42",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = stringResource(R.string.home_label_2),
                        value = "18",
                        accent = AuroraMint,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = stringResource(R.string.home_label_3),
                        value = "7",
                        accent = AuroraViolet,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            StaggeredReveal(index = 2, enabled = staggerEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PrimaryButton(
                        onClick = onOpenComponents,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.home_text_4)) }
                    SecondaryButton(
                        onClick = onOpenMotion,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.home_text_5)) }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.home_text_6),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 22.dp, top = 12.dp)
            )
        }

        itemsIndexed(principles) { index, principle ->
            StaggeredReveal(
                index = 3 + index,
                enabled = staggerEnabled,
                staggerMs = Motion.Stagger.StandardList
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = principle.index,
                            style = MaterialTheme.typography.displaySmall,
                            color = principle.accent.copy(alpha = 0.85f)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = principle.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = principle.body,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.home_text_7),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 22.dp, top = 14.dp)
            )
        }

        itemsIndexed(featureMaterials) { index, material ->
            StaggeredReveal(index = 8 + index, enabled = staggerEnabled) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Tag(text = material.status, accent = material.accent)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = material.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = material.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            StaggeredReveal(index = 9, enabled = staggerEnabled) {
                InfoBanner(
                    text = stringResource(R.string.home_text_8),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
