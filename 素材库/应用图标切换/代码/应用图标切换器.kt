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
 * 应用内切换桌面应用图标（功能编号 F01，唯一权威实现与示例工程
 * `android/.../ui/features/AppIconSwitcher.kt` 保持逻辑一致；本文件是其镜像，
 * 对象名使用中文名 [应用图标切换器]，文件内所有引用均指向本名，切勿与英文名混用）。
 *
 * 原理：Manifest 为每套图标声明一个 <activity-alias>（同一个 targetActivity、
 * 不同 android:icon），运行时用 PackageManager 互斥启用/禁用。
 * 详见同目录 `../实现方案.md` 与仓库根 `docs/功能逐项手册.md` 的 F01 节。
 *
 * 红线（历史上全踩过）：
 * 1. ComponentName 必须用完整类名：packageName + ".icon.Xxx"，禁止只写相对名拼凑猜测；
 * 2. 切换顺序必须先启用目标、再禁用其余，禁止先禁后启；
 * 3. 预览图用 foreground drawable，禁止拿 mipmap 自适应 XML 当 painterResource（会崩）；
 * 4. 图标载荷用 @mipmap/ic_launcher_* 自适应图标，不要改成裸 PNG。
 */
object 应用图标切换器 {

    data class IconOption(
        val key: String,
        val label: String,
        /** Manifest 中 activity-alias 的相对名，如 ".icon.Default"。 */
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

    /** 启用 [target]，其余全部禁用。成功返回 true，系统拒绝返回 false（调用方应提示用户）。 */
    fun apply(context: Context, target: IconOption): Boolean = runCatching {
        val pm = context.packageManager
        // 先启用新入口，保证任何瞬间都至少有一个桌面图标，避免图标消失或被系统杀进程。
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

    /** 读取当前生效的图标（直接读组件真实状态，不依赖另存的偏好）。 */
    fun current(context: Context): IconOption =
        all().firstOrNull {
            context.packageManager.getComponentEnabledSetting(it.component(context)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } ?: all().first()
}

/** 设置页的图标选择区：一排图标预览，点击切换，选中项描边高亮。 */
@Composable
fun 应用图标选择器(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val options = remember { 应用图标切换器.all() }
    var selectedKey by remember {
        mutableStateOf(应用图标切换器.current(context).key)
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
                            if (应用图标切换器.apply(context, option)) {
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
