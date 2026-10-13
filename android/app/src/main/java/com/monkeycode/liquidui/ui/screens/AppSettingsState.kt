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

enum class WallpaperMode(val label: String) {
    Aurora("极光"), System("系统壁纸"), Cached("本地缓存")
}

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

    var wallpaperMode by mutableStateOf(
        runCatching {
            WallpaperMode.valueOf(
                store.getString(
                    KEY_WALLPAPER_MODE,
                    if (store.getBoolean(KEY_WALLPAPER, false)) WallpaperMode.System.name else WallpaperMode.Aurora.name,
                )!!,
            )
        }.getOrDefault(WallpaperMode.Aurora)
    )
        private set

    var cachedWallpaperPath by mutableStateOf(store.getString(KEY_WALLPAPER_PATH, null))
        private set

    var networkWallpaperUrl by mutableStateOf(store.getString(KEY_WALLPAPER_URL, "").orEmpty())
        private set

    var wallpaperRevision by mutableIntStateOf(0)
        private set

    val wallpaperBackground: Boolean get() = wallpaperMode != WallpaperMode.Aurora

    var glassBlurRadiusDp by mutableFloatStateOf(store.getFloat(KEY_GLASS_BLUR, 0f))
        private set
    var glassRefractionHeightDp by mutableFloatStateOf(store.getFloat(KEY_REFRACTION_HEIGHT, 15f))
        private set
    var glassRefractionAmountDp by mutableFloatStateOf(store.getFloat(KEY_REFRACTION_AMOUNT, 20f))
        private set
    var glassTintAlpha by mutableFloatStateOf(store.getFloat(KEY_TINT_ALPHA, 0.08f))
        private set
    var glassBorderAlpha by mutableFloatStateOf(store.getFloat(KEY_BORDER_ALPHA, 0.20f))
        private set
    var glassVibrancyEnabled by mutableStateOf(store.getBoolean(KEY_VIBRANCY, true))
        private set
    var glassChromaticAberration by mutableStateOf(store.getBoolean(KEY_CHROMATIC, true))
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
        updateWallpaperMode(if (value) WallpaperMode.System else WallpaperMode.Aurora)
    }

    fun updateWallpaperMode(value: WallpaperMode) {
        wallpaperMode = value
        store.edit().putString(KEY_WALLPAPER_MODE, value.name).apply()
    }

    fun updateCachedWallpaper(path: String?, sourceUrl: String = networkWallpaperUrl) {
        cachedWallpaperPath = path
        networkWallpaperUrl = sourceUrl
        wallpaperRevision++
        store.edit()
            .putString(KEY_WALLPAPER_PATH, path)
            .putString(KEY_WALLPAPER_URL, sourceUrl)
            .apply()
        if (!path.isNullOrBlank()) updateWallpaperMode(WallpaperMode.Cached)
    }

    fun updateNetworkWallpaperUrl(value: String) {
        networkWallpaperUrl = value
        store.edit().putString(KEY_WALLPAPER_URL, value).apply()
    }

    fun updateGlassBlurRadiusDp(value: Float) {
        glassBlurRadiusDp = value.coerceIn(0f, 40f)
        store.edit().putFloat(KEY_GLASS_BLUR, glassBlurRadiusDp).apply()
    }

    fun updateGlassRefractionHeightDp(value: Float) {
        glassRefractionHeightDp = value.coerceIn(0f, 40f)
        store.edit().putFloat(KEY_REFRACTION_HEIGHT, glassRefractionHeightDp).apply()
    }

    fun updateGlassRefractionAmountDp(value: Float) {
        glassRefractionAmountDp = value.coerceIn(0f, 60f)
        store.edit().putFloat(KEY_REFRACTION_AMOUNT, glassRefractionAmountDp).apply()
    }

    fun updateGlassTintAlpha(value: Float) {
        glassTintAlpha = value.coerceIn(0f, 0.35f)
        store.edit().putFloat(KEY_TINT_ALPHA, glassTintAlpha).apply()
    }

    fun updateGlassBorderAlpha(value: Float) {
        glassBorderAlpha = value.coerceIn(0f, 0.45f)
        store.edit().putFloat(KEY_BORDER_ALPHA, glassBorderAlpha).apply()
    }

    fun updateGlassVibrancyEnabled(value: Boolean) {
        glassVibrancyEnabled = value
        store.edit().putBoolean(KEY_VIBRANCY, value).apply()
    }

    fun updateGlassChromaticAberration(value: Boolean) {
        glassChromaticAberration = value
        store.edit().putBoolean(KEY_CHROMATIC, value).apply()
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
        put(KEY_WALLPAPER_MODE, wallpaperMode.name)
        put(KEY_WALLPAPER_PATH, cachedWallpaperPath)
        put(KEY_WALLPAPER_URL, networkWallpaperUrl)
        put(KEY_GLASS_BLUR, glassBlurRadiusDp)
        put(KEY_REFRACTION_HEIGHT, glassRefractionHeightDp)
        put(KEY_REFRACTION_AMOUNT, glassRefractionAmountDp)
        put(KEY_TINT_ALPHA, glassTintAlpha)
        put(KEY_BORDER_ALPHA, glassBorderAlpha)
        put(KEY_VIBRANCY, glassVibrancyEnabled)
        put(KEY_CHROMATIC, glassChromaticAberration)
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
        updateWallpaperMode(
            runCatching { WallpaperMode.valueOf(data.optString(KEY_WALLPAPER_MODE, wallpaperMode.name)) }
                .getOrDefault(wallpaperMode)
        )
        updateNetworkWallpaperUrl(data.optString(KEY_WALLPAPER_URL, networkWallpaperUrl))
        updateGlassBlurRadiusDp(data.optDouble(KEY_GLASS_BLUR, glassBlurRadiusDp.toDouble()).toFloat())
        updateGlassRefractionHeightDp(data.optDouble(KEY_REFRACTION_HEIGHT, glassRefractionHeightDp.toDouble()).toFloat())
        updateGlassRefractionAmountDp(data.optDouble(KEY_REFRACTION_AMOUNT, glassRefractionAmountDp.toDouble()).toFloat())
        updateGlassTintAlpha(data.optDouble(KEY_TINT_ALPHA, glassTintAlpha.toDouble()).toFloat())
        updateGlassBorderAlpha(data.optDouble(KEY_BORDER_ALPHA, glassBorderAlpha.toDouble()).toFloat())
        updateGlassVibrancyEnabled(data.optBoolean(KEY_VIBRANCY, glassVibrancyEnabled))
        updateGlassChromaticAberration(data.optBoolean(KEY_CHROMATIC, glassChromaticAberration))
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
        updateWallpaperMode(WallpaperMode.Aurora)
        updateGlassBlurRadiusDp(0f)
        updateGlassRefractionHeightDp(15f)
        updateGlassRefractionAmountDp(20f)
        updateGlassTintAlpha(0.08f)
        updateGlassBorderAlpha(0.20f)
        updateGlassVibrancyEnabled(true)
        updateGlassChromaticAberration(true)
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
        const val KEY_WALLPAPER_MODE = "wallpaperMode"
        const val KEY_WALLPAPER_PATH = "cachedWallpaperPath"
        const val KEY_WALLPAPER_URL = "networkWallpaperUrl"
        const val KEY_GLASS_BLUR = "glassBlurRadiusDp"
        const val KEY_REFRACTION_HEIGHT = "glassRefractionHeightDp"
        const val KEY_REFRACTION_AMOUNT = "glassRefractionAmountDp"
        const val KEY_TINT_ALPHA = "glassTintAlpha"
        const val KEY_BORDER_ALPHA = "glassBorderAlpha"
        const val KEY_VIBRANCY = "glassVibrancy"
        const val KEY_CHROMATIC = "glassChromatic"
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
