package com.monkeycode.liquidui.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monkeycode.liquidui.ui.theme.OShinTokens

/**
 * O神（Miuix 风格）组件组 —— 功能 F14 的可复用布局件。
 *
 * 与 OShin 公开界面的对应关系：
 * - [OShinCard]      ≈ 其 Card（16dp 圆角、实色卡面、无描边阴影，组内多行合并）
 * - [OShinSmallTitle]≈ Miuix SmallTitle（卡片组上方的灰色分组标题）
 * - [OShinArrowRow]  ≈ 其 FunArrow/SuperArrow（标题+摘要+右侧文案+箭头）
 * - [OShinSwitchRow] ≈ 其 FunSwitch 行（标题+摘要+蓝色开关）
 * 均为按视觉规范的独立实现，未复制 OShin 的 AGPL 源码。
 */

/** 分组卡片：实色、16dp 圆角。外部边距由调用方控制（页面统一左右 16dp、组间 6dp）。 */
@Composable
fun OShinCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 4.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(OShinTokens.CardRadius))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(contentPadding),
        content = content
    )
}

/** 分组小标题：卡片组上方、灰色、与卡片内文左对齐（页面边距 16 + 行内距 16）。 */
@Composable
fun OShinSmallTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 32.dp, end = 16.dp, top = 14.dp, bottom = 8.dp)
    )
}

/** 箭头行：标题（+摘要）+ 右侧文案 + 「›」。行高、内距与 OShin 列表项一致（内距 16dp）。 */
@Composable
fun OShinArrowRow(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    rightText: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.45f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!summary.isNullOrBlank()) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (!rightText.isNullOrBlank()) {
            Text(
                text = rightText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        Text(
            text = "›",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha * 0.8f),
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

/** 开关行：复用模板的 CompactSwitch（其选中色读主题 primary，切到 O神即为系统蓝）。 */
@Composable
fun OShinSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.45f)
            )
            if (!summary.isNullOrBlank()) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.45f)
                )
            }
        }
        CompactSwitch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

/** 组内分隔：只在行与行之间缩进显示（左缩 16dp），对齐 OShin 分组列表的细线。 */
@Composable
fun OShinDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .height(0.6.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    )
}

/** 页头：大标题 + 副标题（OShin 功能页的左对齐大标题式）。 */
@Composable
fun OShinPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(Modifier.height(8.dp))
}

/** O神悬浮底栏里单个标签的选中色取值（供 AppRoot 壳层用）。 */
@Composable
fun oshinTabColors(selected: Boolean): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    val dark = MaterialTheme.colorScheme.background == OShinTokens.BackgroundDark
    val accent = if (dark) OShinTokens.BottomAccentDark else OShinTokens.BottomAccentLight
    return if (selected) {
        accent to accent
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/** 占位说明：本文件只依赖 MaterialTheme token，任何主题下可用，但在 VisualMode.OShin 下与其余壳层配套。 */
@Composable
fun OShinIconBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}
