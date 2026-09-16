package com.monkeycode.liquidui.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.monkeycode.liquidui.ui.components.CompactSwitch
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.GlassDialog
import com.monkeycode.liquidui.ui.components.GlassIconButton
import com.monkeycode.liquidui.ui.components.GlassTextField
import com.monkeycode.liquidui.ui.components.Meter
import com.monkeycode.liquidui.ui.components.PreferenceCard
import com.monkeycode.liquidui.ui.components.PreferenceRow
import com.monkeycode.liquidui.ui.components.PrimaryButton
import com.monkeycode.liquidui.ui.components.SecondaryButton
import com.monkeycode.liquidui.ui.components.SectionTitle
import com.monkeycode.liquidui.ui.components.SegmentedTabs
import com.monkeycode.liquidui.ui.components.Tag
import com.monkeycode.liquidui.ui.theme.AppIcons
import com.monkeycode.liquidui.ui.theme.AuroraAmber
import com.monkeycode.liquidui.ui.theme.AuroraMint
import com.monkeycode.liquidui.ui.theme.AuroraRose
import com.monkeycode.liquidui.ui.theme.AuroraViolet

@Composable
fun ComponentsScreen() {
    var text by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var notifications by rememberSaveable { mutableStateOf(true) }
    var analytics by rememberSaveable { mutableStateOf(false) }
    var segmented by rememberSaveable { mutableIntStateOf(0) }
    var meter by remember { mutableFloatStateOf(0.62f) }
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(top = 20.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "组件库",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "每个控件的默认态、聚焦态与按下权重都已固化。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { SectionTitle("按钮 · 按下权重", Modifier.padding(top = 8.dp)) }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrimaryButton(onClick = {}) { Text("主要操作") }
                    SecondaryButton(onClick = {}) { Text("次要操作") }
                    GlassIconButton(
                        icon = AppIcons.Notifications,
                        contentDescription = "通知",
                        onClick = {}
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrimaryButton(onClick = {}, enabled = false) { Text("禁用态") }
                    Tag(text = "ease-out-back · 180ms")
                }
            }
        }

        item { SectionTitle("输入 · 17dp 圆角") }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                GlassTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = "昵称",
                    placeholder = "输入内容",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                GlassTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "密码",
                    supportingText = "支持视觉转换与键盘类型",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item { SectionTitle("选择 · 弹簧滑块") }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                SegmentedTabs(
                    labels = listOf("系统", "浅色", "深色"),
                    selectedIndex = segmented,
                    onSelected = { segmented = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Tag(text = "薄荷", accent = AuroraMint)
                    Tag(text = "琥珀", accent = AuroraAmber)
                    Tag(text = "玫红", accent = AuroraRose)
                    Tag(text = "紫罗兰", accent = AuroraViolet)
                }
            }
        }

        item { SectionTitle("偏好行 · 设置语言") }
        item {
            PreferenceCard {
                PreferenceRow(
                    icon = AppIcons.Notifications,
                    title = "推送通知",
                    subtitle = "开关滑块使用弹簧回弹",
                    trailing = {
                        CompactSwitch(
                            checked = notifications,
                            onCheckedChange = { notifications = it }
                        )
                    }
                )
            }
        }
        item {
            PreferenceCard {
                PreferenceRow(
                    icon = AppIcons.Shield,
                    title = "匿名数据统计",
                    subtitle = "默认关闭，遵循隐私优先",
                    trailing = {
                        CompactSwitch(
                            checked = analytics,
                            onCheckedChange = { analytics = it }
                        )
                    }
                )
            }
        }

        item { SectionTitle("进度 · 弹簧填充") }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(meter * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.weight(1f))
                    SecondaryButton(onClick = {
                        meter = when {
                            meter < 0.3f -> 0.62f
                            meter < 0.7f -> 1f
                            else -> 0.18f
                        }
                    }) { Text("切换") }
                }
                Spacer(Modifier.height(12.dp))
                Meter(progress = meter)
            }
        }

        item { SectionTitle("浮层 · 玻璃对话框") }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(
                    text = "对话框同样是玻璃材质，圆角 28dp，进出场都有过冲。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                PrimaryButton(onClick = { showDialog = true }) { Text("打开对话框") }
            }
        }

        item { SectionTitle("配色 · 语义色板") }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val swatches = listOf(
                    "primary" to MaterialTheme.colorScheme.primary,
                    "secondary" to MaterialTheme.colorScheme.secondary,
                    "tertiary" to MaterialTheme.colorScheme.tertiary,
                    "mint" to AuroraMint,
                    "amber" to AuroraAmber,
                    "rose" to AuroraRose
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    swatches.forEach { (name, color) ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .liquidGlassSwatch(color)
                            )
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        GlassDialog(
            onDismissRequest = { showDialog = false },
            title = "确认操作",
            onConfirm = { showDialog = false },
            confirmText = "确认"
        ) {
            Text(
                text = "这是一个空白功能示例对话框。把 confirm 回调接到你的业务逻辑即可。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Modifier.liquidGlassSwatch(color: Color): Modifier =
    clip(RoundedCornerShape(14.dp)).background(color)
