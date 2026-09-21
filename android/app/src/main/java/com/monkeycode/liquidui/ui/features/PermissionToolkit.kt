package com.monkeycode.liquidui.ui.features

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.monkeycode.liquidui.ui.components.PreferenceCard
import com.monkeycode.liquidui.ui.components.PreferenceRow
import com.monkeycode.liquidui.ui.components.PrimaryButton
import com.monkeycode.liquidui.ui.components.SecondaryButton
import com.monkeycode.liquidui.ui.components.Tag
import com.monkeycode.liquidui.ui.theme.AppIcons

/** Description of one Android runtime permission and its user-facing rationale. */
data class PermissionSpec(
    val permission: String,
    val title: String,
    val description: String,
    val deniedHint: String = "拒绝后仍可在系统设置中重新开启。"
)

/** True when [permission] is currently granted. */
fun Context.isPermissionGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/** Opens the current app's system settings page for permanently denied permissions. */
fun Context.openAppPermissionSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

/**
 * A reusable permission explanation card.
 *
 * It covers the common flow: explain why the app needs a permission, request it,
 * show granted/denied state, and provide a settings shortcut for recovery.
 */
@Composable
fun PermissionRequestCard(
    spec: PermissionSpec,
    modifier: Modifier = Modifier,
    onResult: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var granted by remember(spec.permission) {
        mutableStateOf(context.isPermissionGranted(spec.permission))
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        granted = result
        onResult(result)
    }

    PreferenceCard(modifier = modifier) {
        PreferenceRow(
            icon = AppIcons.Shield,
            title = spec.title,
            subtitle = if (granted) spec.description else spec.description + "\n" + spec.deniedHint,
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (granted) {
                        Tag(text = "已授权", accent = MaterialTheme.colorScheme.primary)
                    } else {
                        SecondaryButton(onClick = { context.openAppPermissionSettings() }) {
                            Text("设置")
                        }
                        Spacer(Modifier.width(8.dp))
                        PrimaryButton(onClick = { launcher.launch(spec.permission) }) {
                            Text("授权")
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
