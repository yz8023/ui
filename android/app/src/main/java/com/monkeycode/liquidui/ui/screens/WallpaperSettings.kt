package com.monkeycode.liquidui.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.GlassTextField
import com.monkeycode.liquidui.ui.components.PrimaryButton
import com.monkeycode.liquidui.ui.components.SecondaryButton
import com.monkeycode.liquidui.ui.components.SegmentedTabs
import com.monkeycode.liquidui.ui.features.WallpaperStore
import com.monkeycode.liquidui.ui.features.allFilesAccessSettingsIntent
import com.monkeycode.liquidui.ui.features.hasAllFilesAccess
import com.monkeycode.liquidui.ui.features.isSystemWallpaperAvailable
import kotlinx.coroutines.launch

@Composable
fun WallpaperSettingsCard(settings: AppSettingsState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var url by remember(settings.networkWallpaperUrl) { mutableStateOf(settings.networkWallpaperUrl) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    fun applyCached(path: String, sourceUrl: String, success: String) {
        settings.updateCachedWallpaper(path, sourceUrl)
        message = success
        busy = false
    }

    val documentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            busy = true
            message = "正在复制并缓存图片…"
            scope.launch {
                WallpaperStore.cacheDocument(context, uri)
                    .onSuccess { applyCached(it.file.absolutePath, "", "本地图片已复制到应用内部并启用") }
                    .onFailure { busy = false; message = it.message ?: "图片缓存失败" }
            }
        }
    }

    // Android 12 and below only: the real wallpaper needs READ_EXTERNAL_STORAGE.
    // The mode is switched immediately either way (the pan-x/YunX approach —
    // never gate the feature behind a permission the platform does not use
    // for this call); the grant result only affects whether the real or the
    // built-in default wallpaper comes back, and the root re-reads on resume.
    val legacyStoragePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        message = if (granted) {
            "已切换为系统桌面壁纸"
        } else {
            "未授予存储权限，将显示系统默认壁纸；也可以使用本地图片或网络壁纸"
        }
    }
    // Android 11+: the real system wallpaper (and any wallpaper at all on
    // Android 14+) is only readable while the app holds "all files access".
    // Like pan-x, we send the user to the system settings page for this app —
    // the grant has no runtime dialog. Returning from that page bumps the
    // revision so the probe below (and the root wallpaper layer, via its
    // resume re-read) picks the wallpaper up immediately.
    val allFilesAccessLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        settings.bumpWallpaperRevision()
        message = if (hasAllFilesAccess()) {
            "已开启所有文件访问，系统壁纸已生效"
        } else {
            "仍未开启所有文件访问，系统壁纸暂时读不到；可改用本地图片或网络壁纸"
        }
    }
    val systemWallpaperAvailable = remember(settings.wallpaperMode, settings.wallpaperRevision) {
        settings.wallpaperMode == WallpaperMode.System && isSystemWallpaperAvailable(context)
    }
    val needsAllFilesAccess = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
        settings.wallpaperMode == WallpaperMode.System && !hasAllFilesAccess()

    GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("壁纸与背景", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "网络图片会下载、校验并复制到应用私有目录。即使原链接失效，已缓存壁纸仍可离线使用。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(14.dp))
        SegmentedTabs(
            labels = WallpaperMode.entries.map { it.label },
            selectedIndex = settings.wallpaperMode.ordinal,
            onSelected = { index ->
                when (val mode = WallpaperMode.entries[index]) {
                    WallpaperMode.Aurora -> settings.updateWallpaperMode(mode)
                    WallpaperMode.Cached -> {
                        if (!settings.cachedWallpaperPath.isNullOrBlank()) settings.updateWallpaperMode(mode)
                        else message = "请先选择本地图片或下载网络壁纸"
                    }
                    WallpaperMode.System -> {
                        // Switch first — never block the mode behind a prompt
                        // (the old READ_MEDIA_IMAGES gate did exactly that and
                        // could never unlock WallpaperManager anyway). Then
                        // guide the grant that actually unlocks the read:
                        // pre-13 the legacy storage runtime permission, on
                        // Android 11+ the all-files-access settings page
                        // (this is what makes pan-x's system wallpaper work
                        // on Android 14+, where the call otherwise always
                        // throws SecurityException).
                        settings.updateWallpaperMode(mode)
                        message = "已切换为系统桌面壁纸"
                        when {
                            Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                ) != PackageManager.PERMISSION_GRANTED ->
                                legacyStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !hasAllFilesAccess() ->
                                allFilesAccessLauncher.launch(allFilesAccessSettingsIntent(context))
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        if (settings.wallpaperMode == WallpaperMode.System) {
            Spacer(Modifier.height(10.dp))
            when {
                systemWallpaperAvailable -> Text(
                    "已读取当前系统桌面壁纸作为背景（更换桌面壁纸后回到应用即会刷新）。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )

                needsAllFilesAccess -> {
                    Text(
                        "系统壁纸需要先开启「所有文件访问」权限：Android 14 起系统只允许持有该权限的应用读取桌面壁纸（已自动回退极光背景）。开启后返回本页即可生效。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton(
                        onClick = {
                            allFilesAccessLauncher.launch(allFilesAccessSettingsIntent(context))
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("去开启所有文件访问权限") }
                }

                else -> Text(
                    "当前系统未返回桌面壁纸（可能是动态壁纸或厂商限制），已自动回退极光背景；可在下方选择同一张图片做本地缓存，显示效果一致。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        val preview = remember(settings.cachedWallpaperPath, settings.wallpaperRevision) {
            settings.cachedWallpaperPath?.let(BitmapFactory::decodeFile)
        }
        if (preview != null) {
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier.fillMaxWidth().height(176.dp).clip(RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.BottomStart,
            ) {
                Image(
                    bitmap = preview.asImageBitmap(),
                    contentDescription = "缓存壁纸预览",
                    modifier = Modifier.fillMaxWidth().height(176.dp),
                    contentScale = ContentScale.Crop,
                )
                Text(
                    text = if (settings.wallpaperMode == WallpaperMode.Cached) "缓存壁纸使用中" else "缓存壁纸已保留",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        SecondaryButton(
            onClick = { documentPicker.launch(arrayOf("image/*")) },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("选择本地图片并永久缓存") }

        Spacer(Modifier.height(14.dp))
        Text("网络壁纸", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        GlassTextField(
            value = url,
            onValueChange = { url = it; message = null },
            label = "壁纸图片链接",
            placeholder = "https://example.com/wallpaper.jpg",
            supportingText = "支持 HTTPS，最大 20MB；会缩放并缓存到本地",
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryButton(
                onClick = {
                    if (url.isBlank()) {
                        message = "请先填写图片链接"
                    } else {
                        busy = true
                        message = "正在下载并缓存网络壁纸…"
                        settings.updateNetworkWallpaperUrl(url.trim())
                        scope.launch {
                            WallpaperStore.cacheNetwork(context, url)
                                .onSuccess {
                                    applyCached(it.file.absolutePath, url.trim(), "网络壁纸已缓存并启用（${it.width}×${it.height}）")
                                }
                                .onFailure { busy = false; message = it.message ?: "网络壁纸下载失败" }
                        }
                    }
                },
                enabled = !busy,
                modifier = Modifier.weight(1f),
            ) { Text(if (busy) "处理中…" else "下载并应用") }
            SecondaryButton(
                onClick = {
                    WallpaperStore.clear(context)
                    settings.updateCachedWallpaper(null, "")
                    settings.updateWallpaperMode(WallpaperMode.Aurora)
                    url = ""
                    message = "缓存壁纸已清除"
                },
                enabled = !busy && !settings.cachedWallpaperPath.isNullOrBlank(),
                modifier = Modifier.weight(1f),
            ) { Text("清除缓存") }
        }

        message?.let {
            Spacer(Modifier.height(10.dp))
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = if (it.contains("失败") || it.contains("请先") || it.contains("未获得")) {
                    MaterialTheme.colorScheme.error
                } else MaterialTheme.colorScheme.primary,
            )
        }
    }
}
