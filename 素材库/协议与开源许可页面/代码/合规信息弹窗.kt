package com.monkeycode.liquidui.ui.features

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.monkeycode.liquidui.ui.components.GlassDialog

/** Small legal/about dialog that can be opened from Settings. */
@Composable
fun LegalInfoDialog(
    type: LegalInfoType,
    onDismiss: () -> Unit
) {
    GlassDialog(
        onDismissRequest = onDismiss,
        title = if (type == LegalInfoType.Licenses) "开源许可与致谢" else "隐私说明",
        onConfirm = onDismiss,
        confirmText = "知道了",
        dismissText = null
    ) {
        Text(
            text = if (type == LegalInfoType.Licenses) LICENSE_TEXT else PRIVACY_TEXT,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

enum class LegalInfoType { Licenses, Privacy }

const val PROJECT_HOME = "https://github.com/yz8023/ui"

fun android.content.Context.openProjectHome() {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PROJECT_HOME))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

private const val LICENSE_TEXT =
    "本示例应用基于 Jetpack Compose、Material 3、backdrop-android、shapes-android 与 Material Icons 等开源项目构建。" +
        "完整项目来源与许可证请见仓库 CREDITS.md。复制素材到你的项目时，请保留对应上游项目的署名与许可证说明。"

private const val PRIVACY_TEXT =
    "本示例只在设备本地保存主题、动效、图标与引导页状态等偏好设置。" +
        "通知预览仅由用户点击触发；壁纸背景仅在你主动开启并授权后读取。项目不包含账号系统，也不会上传个人数据。"
