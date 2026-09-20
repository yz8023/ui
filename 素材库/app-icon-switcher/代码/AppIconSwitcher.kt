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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.monkeycode.liquidui.R

/**
 * 应用内切换桌面应用图标。
 *
 * 原理：Manifest 里为每个图标声明一个 <activity-alias>（同一个 targetActivity，
 * 不同的 android:icon），运行时用 PackageManager 互斥启用/禁用这些 alias。
 * 详见 素材库/应用图标切换/实现方案.md。
 */
object AppIconSwitcher {

    data class IconOption(
        val key: String,
        val label: String,
        val component: ComponentName,
        val previewRes: Int,
    )

    /** 全部可选图标。新增图标时：加一套 mipmap + 一个 alias + 这里加一行。 */
    fun all(): List<IconOption> = listOf(
        IconOption(
            key = "default",
            label = "冰蓝",
            component = ComponentName(PACKAGE, "$PACKAGE.icon.Default"),
            previewRes = R.mipmap.ic_launcher,
        ),
        IconOption(
            key = "galaxy",
            label = "星夜",
            component = ComponentName(PACKAGE, "$PACKAGE.icon.Galaxy"),
            previewRes = R.mipmap.ic_launcher_galaxy,
        ),
        IconOption(
            key = "coral",
            label = "珊瑚",
            component = ComponentName(PACKAGE, "$PACKAGE.icon.Coral"),
            previewRes = R.mipmap.ic_launcher_coral,
        ),
    )

    private const val PACKAGE = "com.monkeycode.liquidui"

    /** 启用 [target]，其余全部禁用。异步生效，桌面 Launcher 会刷新图标。 */
    fun apply(context: Context, target: IconOption) {
        val pm = context.packageManager
        all().forEach { option ->
            val state = if (option.key == target.key) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            pm.setComponentEnabledSetting(
                option.component,
                state,
                PackageManager.DONT_KILL_APP,
            )
        }
    }

    /** 读取当前生效的图标。 */
    fun current(context: Context): IconOption =
        all().firstOrNull {
            context.packageManager.getComponentEnabledSetting(it.component) ==
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
                            selectedKey = option.key
                            AppIconSwitcher.apply(context, option)
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
                    IconPreview(
                        painter = painterResource(option.previewRes),
                        modifier = Modifier.size(56.dp),
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

@Composable
private fun IconPreview(painter: Painter, modifier: Modifier = Modifier) {
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier.clip(CircleShape),
    )
}
