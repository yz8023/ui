package com.monkeycode.liquidui.ui.features

import android.app.WallpaperManager
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * System (home-screen) wallpaper access, following the approach proven in the
 * YunX / pan-x codebase (`rememberWallpaperSource` in its Wallpaper.kt):
 *
 *  - No runtime permission gate. The wallpaper is requested directly and any
 *    refusal degrades to `null` (callers fall back to the aurora scene).
 *    Requesting `READ_MEDIA_IMAGES` first — the previous behaviour — could
 *    never unlock the wallpaper on Android 13+, so a single denied dialog
 *    permanently blocked the feature even on devices whose OEM build would
 *    happily return the drawable.
 *  - The read is synchronous: [WallpaperManager]'s drawable is lazily
 *    decoded, so fetching it during composition is cheap.
 *
 * Platform reality (documented on `WallpaperManager.getDrawable`):
 *  - Up to Android 12 the real wallpaper requires `READ_EXTERNAL_STORAGE`
 *    (declared in the manifest with maxSdkVersion 32; without the grant the
 *    system returns the built-in default wallpaper instead of throwing).
 *  - Android 13 returns the default wallpaper (some builds throw).
 *  - Android 14+ always throws [SecurityException]; only apps holding
 *    `MANAGE_EXTERNAL_STORAGE` can still read the real wallpaper, which a
 *    template/sample app must not request. Callers must therefore treat
 *    `null` as "platform refused" and tell the user, not fail silently.
 */
fun loadSystemWallpaper(context: Context): Drawable? =
    try {
        WallpaperManager.getInstance(context).drawable
    } catch (_: SecurityException) {
        null
    } catch (_: RuntimeException) {
        null
    }

/** True when this device/build currently hands the app a wallpaper drawable. */
fun isSystemWallpaperAvailable(context: Context): Boolean =
    loadSystemWallpaper(context) != null

/**
 * Remembers the current system wallpaper, re-reading it whenever the app
 * returns to the foreground so changing the home-screen wallpaper while the
 * app is backgrounded is picked up without restarting.
 */
@Composable
fun rememberSystemWallpaper(): Drawable? {
    val context = LocalContext.current
    var resumeTick by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) resumeTick++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return remember(context, resumeTick) { loadSystemWallpaper(context) }
}
