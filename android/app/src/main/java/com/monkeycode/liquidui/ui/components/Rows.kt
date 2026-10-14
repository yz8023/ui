package com.monkeycode.liquidui.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monkeycode.liquidui.ui.theme.AppShape
import com.monkeycode.liquidui.ui.theme.LocalAppChrome
import com.monkeycode.liquidui.ui.theme.VisualMode

/** Section label used above a group of settings. */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    // O神模式：Miuix 分组小标题（灰色小字，与卡片内文对齐），不再用主色。
    if (LocalAppChrome.current.visualMode == VisualMode.OShin) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(start = 32.dp, bottom = 8.dp, top = 6.dp)
        )
        return
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(start = 20.dp, bottom = 8.dp)
    )
}

/**
 * A quiet preference row inside its own glass card.
 * Mirrors the reference settings: 22dp radius, generous row height, icon +
 * title + optional subtitle, optional trailing control.
 */
@Composable
fun PreferenceRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val clickable = if (enabled && onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickable)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                // O神模式去掉彩色圆形底，图标直接以主色呈现（HyperOS 设置行做法）。
                .background(
                    if (LocalAppChrome.current.visualMode == VisualMode.OShin) androidx.compose.ui.graphics.Color.Transparent
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.13f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.55f)
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.55f)
                )
            }
        }
        trailing?.invoke()
    }
}

/** Wraps a [PreferenceRow] in its own glass card with standard screen insets. */
@Composable
fun PreferenceCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            // O神模式：16dp 分组卡圆角（而非玻璃的 22dp）。
            .liquidGlass(
                shape = if (LocalAppChrome.current.visualMode == VisualMode.OShin)
                    androidx.compose.foundation.shape.RoundedCornerShape(16.dp) else AppShape.card,
                elevation = 1.dp
            ),
        content = { content() }
    )
}

/** Large, bold display headline. The type-scale jump is the design. */
@Composable
fun DisplayTitle(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayLarge
) {
    Text(
        text = text,
        style = style,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

/** Muted supporting copy under a title. */
@Composable
fun SupportingText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}
