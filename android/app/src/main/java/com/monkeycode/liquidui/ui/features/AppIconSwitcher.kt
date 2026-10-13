package com.monkeycode.liquidui.ui.features

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.monkeycode.liquidui.R

/**
 * 应用内切换桌面应用图标（素材库功能点 01）。
 *
 * 原理：Manifest 为每套图标声明一个 <activity-alias>（同一个 targetActivity、
 * 不同 android:icon），运行时用 PackageManager 互斥启用/禁用。
 */
object AppIconSwitcher {

    data class IconOption(
        val key: String,
        val label: String,
        val className: String,
        val previewRes: Int,
    )

    fun all(): List<IconOption> = listOf(
        IconOption(
            key = "default",
            label = "冰蓝",
            className = ".icon.Default",
            previewRes = R.drawable.ic_launcher_foreground,
        ),
        IconOption(
            key = "galaxy",
            label = "星夜",
            className = ".icon.Galaxy",
            previewRes = R.drawable.ic_launcher_foreground_galaxy,
        ),
        IconOption(
            key = "coral",
            label = "珊瑚",
            className = ".icon.Coral",
            previewRes = R.drawable.ic_launcher_foreground_coral,
        ),
    )

    private fun IconOption.component(context: Context) =
        ComponentName(context.packageName, context.packageName + className)

    fun apply(context: Context, target: IconOption): Boolean = runCatching {
        val pm = context.packageManager
        // Enable the new launcher entry first so there is never a moment with no launcher icon.
        pm.setComponentEnabledSetting(
            target.component(context),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP,
        )
        all().filterNot { it.key == target.key }.forEach { option ->
            pm.setComponentEnabledSetting(
                option.component(context),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP,
            )
        }
        true
    }.getOrDefault(false)

    fun current(context: Context): IconOption =
        all().firstOrNull {
            context.packageManager.getComponentEnabledSetting(it.component(context)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } ?: all().first()
}

/** 设置页的图标选择区：一排图标预览，点击切换，选中项描边高亮。 */
@Composable
fun AppIconPicker(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val options = remember { AppIconSwitcher.all() }
    var selectedKey by remember {
        mutableStateOf(AppIconSwitcher.current(context).key)
    }

    Column(modifier = modifier) {
        Text(
            text = "桌面图标",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "选择后桌面图标立即更换（个别桌面需稍等刷新）",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            options.forEach { option ->
                val selected = selectedKey == option.key
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            if (AppIconSwitcher.apply(context, option)) {
                                selectedKey = option.key
                            }
                        }
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = CircleShape,
                        )
                        .padding(8.dp),
                ) {
                    Image(
                        painter = painterResource(option.previewRes),
                        contentDescription = option.label,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                    )
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}
