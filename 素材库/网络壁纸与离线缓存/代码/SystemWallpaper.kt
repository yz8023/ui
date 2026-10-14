package com.monkeycode.liquidui.ui.features

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
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
 * System (home-screen) wallpaper access.
 *
 * Why this file looks the way it does — lessons taken from the YunX / pan-x
 * codebase, whose system-wallpaper mode actually works on current devices:
 *
 *  1. The read itself is one line: `WallpaperManager.getInstance(context)
 *     .drawable`, synchronously (the drawable is lazily decoded, so fetching
 *     it during composition is cheap) and wrapped in try/catch. pan-x never
 *     gates it behind a runtime permission prompt. (An earlier version of
 *     this template gated it behind `READ_MEDIA_IMAGES`; that permission has
 *     no effect on `WallpaperManager` at all, and a single denial blocked
 *     the feature forever.)
 *
 *  2. The hidden precondition pan-x relies on: its manifest declares
 *     `MANAGE_EXTERNAL_STORAGE`, and its download flow actively sends the
 *     user to the system "all files access" settings page
 *     (`Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`) to turn it
 *     on. That grant is exactly what unlocks the real wallpaper on modern
 *     Android:
 *       - Up to Android 12, `READ_EXTERNAL_STORAGE` unlocks the real
 *         wallpaper; without it the system returns the built-in default.
 *       - Android 13 returns the default wallpaper (some builds throw).
 *       - Android 14+ `getDrawable()` always throws [SecurityException] —
 *         *unless* the app holds all-files access, in which case the real
 *         wallpaper is returned on every version (documented on
 *         `WallpaperManager.getDrawable`).
 *
 *     A wallpaper feature on Android 14+ therefore only works if the app
 *     declares `MANAGE_EXTERNAL_STORAGE` and guides the user through the
 *     system settings grant, exactly like pan-x does. There is no runtime
 *     permission dialog for it, and no other API route around it;
 *     `getWallpaperFile()` is gated by the same permission.
 *
 *  3. Callers treat `null` as "not granted / platform refused" and must say
 *     so in the UI instead of silently pretending success.
 */
fun loadSystemWallpaper(context: Context): Drawable? {
    val manager = try {
        WallpaperManager.getInstance(context)
    } catch (_: RuntimeException) {
        return null
    }
    // Primary route: the live wallpaper drawable (what pan-x uses).
    try {
        manager.drawable?.let { return it }
    } catch (_: SecurityException) {
        // Android 14+ without all-files access lands here; try the file route
        // as well before giving up (it is gated by the same grant, but some
        // OEM builds answer one route and not the other).
    } catch (_: RuntimeException) {
    }
    // Fallback route: decode the system wallpaper file directly.
    return try {
        val descriptor = manager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM) ?: return null
        descriptor.use {
            BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
                ?.let { bitmap -> BitmapDrawable(context.resources, bitmap) }
        }
    } catch (_: SecurityException) {
        null
    } catch (_: RuntimeException) {
        null
    }
}

/** True when this device/build currently hands the app a wallpaper drawable. */
fun isSystemWallpaperAvailable(context: Context): Boolean =
    loadSystemWallpaper(context) != null

/**
 * True when the app holds "all files access" (Android 11+). This is the
 * grant that makes the real system wallpaper readable on Android 14+.
 * Always false below Android 11, where the legacy storage permission rules.
 */
fun hasAllFilesAccess(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()

/**
 * Intent for the system settings page where the user can grant this app
 * "all files access" — the same page pan-x sends its users to. There is no
 * runtime-permission-dialog equivalent for this grant.
 */
fun allFilesAccessSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
        data = Uri.parse("package:${context.packageName}")
    }

/**
 * Remembers the current system wallpaper, re-reading it whenever the app
 * returns to the foreground so changing the home-screen wallpaper (or coming
 * back from the all-files-access settings page) is picked up without
 * restarting the app.
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
