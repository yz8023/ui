package com.monkeycode.liquidui.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.monkeycode.liquidui.ui.theme.PaletteId
import com.monkeycode.liquidui.ui.theme.ThemeMode
import com.monkeycode.liquidui.ui.theme.VisualMode
import androidx.compose.runtime.snapshotFlow
import org.json.JSONObject

/**
 * Every user-facing knob the template exposes, persisted so a fresh launch
 * restores the exact look the user configured. This is the only place the app
 * writes to disk — business features added on top should follow the same pattern
 * (one state holder, one preferences file).
 */
@Stable
class AppSettingsState(private val prefs: Context) {

    private val store = prefs.getSharedPreferences("liquidui.settings", Context.MODE_PRIVATE)

    var themeMode by mutableStateOf(
        runCatching { ThemeMode.valueOf(store.getString(KEY_THEME, ThemeMode.System.name)!!) }
            .getOrDefault(ThemeMode.System)
    )
        private set

    var palette by mutableStateOf(
        runCatching { PaletteId.valueOf(store.getString(KEY_PALETTE, PaletteId.Aurora.name)!!) }
            .getOrDefault(PaletteId.Aurora)
    )
        private set

    var visualMode by mutableStateOf(
        runCatching { VisualMode.valueOf(store.getString(KEY_VISUAL_MODE, legacyVisualMode().name)!!) }
            .getOrDefault(legacyVisualMode())
    )
        private set

    /** True when the old independent knob would have shown the glass look. */
    private fun legacyVisualMode(): VisualMode {
        val glass = store.getBoolean(KEY_GLASS_ENABLED, true)
        val dyn = store.getBoolean(KEY_DYNAMIC, false)
        return when {
            dyn -> VisualMode.Md3
            glass -> VisualMode.Glass
            else -> VisualMode.Normal
        }
    }

    var reducedMotion by mutableStateOf(store.getBoolean(KEY_REDUCED, false))
        private set

    /** True once the user has completed the first-launch onboarding flow. */
    var onboardingCompleted by mutableStateOf(store.getBoolean(KEY_ONBOARDING, false))
        private set

    var motionDamping by mutableFloatStateOf(store.getFloat(KEY_DAMPING, 0.5f))
        private set

    var wallpaperBackground by mutableStateOf(store.getBoolean(KEY_WALLPAPER, false))
        private set

    var glassOpacity by mutableFloatStateOf(
        if (store.contains(KEY_OPACITY)) store.getFloat(KEY_OPACITY, 0.55f)
        else store.getFloat(KEY_FROST, 0.55f)
    )
        private set

    var glassRefraction by mutableFloatStateOf(store.getFloat(KEY_REFRACTION, 1f))
        private set

    var cornerScale by mutableFloatStateOf(store.getFloat(KEY_CORNER, 1f))
        private set

    /** Free-form accent seed colour used when [PaletteId.Custom] is active. */
    var customSeed by mutableIntStateOf(store.getInt(KEY_CUSTOM_SEED, Color(0xFF3E8FE0).toArgb()))
        private set

    /** 0f..1f — how deep/light the custom background reads. */
    var customShade by mutableFloatStateOf(store.getFloat(KEY_CUSTOM_SHADE, 0.5f))
        private set

    fun updateOnboardingCompleted(value: Boolean) {
        onboardingCompleted = value
        store.edit().putBoolean(KEY_ONBOARDING, value).apply()
    }

    fun updateThemeMode(value: ThemeMode) {
        themeMode = value
        store.edit().putString(KEY_THEME, value.name).apply()
    }

    fun updatePalette(value: PaletteId) {
        palette = value
        store.edit().putString(KEY_PALETTE, value.name).apply()
    }

    fun updateVisualMode(value: VisualMode) {
        visualMode = value
        store.edit().putString(KEY_VISUAL_MODE, value.name).apply()
    }

    fun updateReducedMotion(value: Boolean) {
        reducedMotion = value
        store.edit().putBoolean(KEY_REDUCED, value).apply()
    }

    /** Flow that emits the latest onboarding-completed flag whenever it changes. */
    val onboardingCompletedFlow = snapshotFlow { onboardingCompleted }

    fun completeOnboarding() {
        onboardingCompleted = true
        store.edit().putBoolean(KEY_ONBOARDING, true).apply()
    }

    /** Reset onboarding so the next launch shows the welcome screen again. */
    fun resetOnboarding() {
        onboardingCompleted = false
        store.edit().putBoolean(KEY_ONBOARDING, false).apply()
    }

    fun updateMotionDamping(value: Float) {
        motionDamping = value
        store.edit().putFloat(KEY_DAMPING, value).apply()
    }

    fun updateWallpaperBackground(value: Boolean) {
        wallpaperBackground = value
        store.edit().putBoolean(KEY_WALLPAPER, value).apply()
    }

    fun updateGlassOpacity(value: Float) {
        glassOpacity = value
        store.edit().putFloat(KEY_OPACITY, value).apply()
    }

    fun updateGlassRefraction(value: Float) {
        glassRefraction = value
        store.edit().putFloat(KEY_REFRACTION, value).apply()
    }

    fun updateCornerScale(value: Float) {
        cornerScale = value
        store.edit().putFloat(KEY_CORNER, value).apply()
    }

    fun updateCustomSeed(value: Color) {
        customSeed = value.toArgb()
        store.edit().putInt(KEY_CUSTOM_SEED, value.toArgb()).apply()
    }

    fun updateCustomShade(value: Float) {
        customShade = value
        store.edit().putFloat(KEY_CUSTOM_SHADE, value).apply()
    }


    fun exportThemeJson(): String = JSONObject().apply {
        put("schema", 1)
        put(KEY_THEME, themeMode.name)
        put(KEY_PALETTE, palette.name)
        put(KEY_VISUAL_MODE, visualMode.name)
        put(KEY_REDUCED, reducedMotion)
        put(KEY_DAMPING, motionDamping)
        put(KEY_WALLPAPER, wallpaperBackground)
        put(KEY_OPACITY, glassOpacity)
        put(KEY_REFRACTION, glassRefraction)
        put(KEY_CORNER, cornerScale)
        put(KEY_CUSTOM_SEED, customSeed)
        put("customSeedHex", "#%08X".format(customSeed.toLong() and 0xFFFFFFFFL))
        put(KEY_CUSTOM_SHADE, customShade)
    }.toString(2)

    fun importThemeJson(json: String): Boolean = runCatching {
        val data = JSONObject(json)
        updateThemeMode(
            runCatching { ThemeMode.valueOf(data.optString(KEY_THEME, themeMode.name)) }
                .getOrDefault(themeMode)
        )
        updatePalette(
            runCatching { PaletteId.valueOf(data.optString(KEY_PALETTE, palette.name)) }
                .getOrDefault(palette)
        )
        updateVisualMode(
            runCatching { VisualMode.valueOf(data.optString(KEY_VISUAL_MODE, visualMode.name)) }
                .getOrDefault(visualMode)
        )
        updateReducedMotion(data.optBoolean(KEY_REDUCED, reducedMotion))
        updateMotionDamping(data.optDouble(KEY_DAMPING, motionDamping.toDouble()).toFloat().coerceIn(0f, 1f))
        updateWallpaperBackground(data.optBoolean(KEY_WALLPAPER, wallpaperBackground))
        updateGlassOpacity(data.optDouble(KEY_OPACITY, glassOpacity.toDouble()).toFloat().coerceIn(0f, 1f))
        updateGlassRefraction(data.optDouble(KEY_REFRACTION, glassRefraction.toDouble()).toFloat().coerceIn(0f, 1f))
        updateCornerScale(data.optDouble(KEY_CORNER, cornerScale.toDouble()).toFloat().coerceIn(0.8f, 1.5f))
        if (data.has(KEY_CUSTOM_SEED)) updateCustomSeed(Color(data.optInt(KEY_CUSTOM_SEED, customSeed)))
        updateCustomShade(data.optDouble(KEY_CUSTOM_SHADE, customShade.toDouble()).toFloat().coerceIn(0f, 1f))
        true
    }.getOrDefault(false)

    fun reset() {
        updateThemeMode(ThemeMode.System)
        updatePalette(PaletteId.Aurora)
        updateVisualMode(VisualMode.Glass)
        updateReducedMotion(false)
        updateMotionDamping(0.5f)
        updateWallpaperBackground(false)
        updateGlassOpacity(0.55f)
        updateGlassRefraction(1f)
        updateCornerScale(1f)
        updateCustomSeed(Color(0xFF3E8FE0))
        updateCustomShade(0.5f)
        onboardingCompleted = false
        store.edit().putBoolean(KEY_ONBOARDING, false).apply()
    }

    private companion object {
        const val KEY_THEME = "themeMode"
        const val KEY_PALETTE = "palette"
        const val KEY_VISUAL_MODE = "visualMode"
        const val KEY_GLASS_ENABLED = "glassEnabled"
        const val KEY_DYNAMIC = "dynamicColor"
        const val KEY_REDUCED = "reducedMotion"
        const val KEY_DAMPING = "motionDamping"
        const val KEY_WALLPAPER = "wallpaperBackground"
        const val KEY_FROST = "glassFrost"
        const val KEY_OPACITY = "glassOpacity"
        const val KEY_REFRACTION = "glassRefraction"
        const val KEY_CORNER = "cornerScale"
        const val KEY_CUSTOM_SEED = "customSeed"
        const val KEY_CUSTOM_SHADE = "customShade"
        const val KEY_ONBOARDING = "onboardingCompleted"
    }
}

@Composable
fun rememberAppSettingsState(): AppSettingsState {
    val context = LocalContext.current.applicationContext
    return remember(context) { AppSettingsState(context) }
}
