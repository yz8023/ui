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
import com.monkeycode.liquidui.data.SettingsRepository
import com.monkeycode.liquidui.domain.SettingsRules
import androidx.compose.ui.platform.LocalContext
import androidx.annotation.StringRes
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.theme.PaletteId
import com.monkeycode.liquidui.ui.theme.ThemeMode
import com.monkeycode.liquidui.ui.theme.VisualMode
import androidx.compose.runtime.snapshotFlow
import org.json.JSONObject

enum class WallpaperMode(@StringRes val labelRes: Int) {
    Aurora(R.string.wallpaper_mode_aurora),
    System(R.string.wallpaper_mode_system),
    Cached(R.string.wallpaper_mode_cached)
}

/**
 * Every user-facing knob the template exposes, persisted so a fresh launch
 * restores the exact look the user configured. This is the only place the app
 * writes to disk — business features added on top should follow the same pattern
 * (one state holder, one preferences file).
 */
@Stable
class AppSettingsState(private val repo: SettingsRepository) {

    var themeMode by mutableStateOf(
        runCatching { ThemeMode.valueOf(repo.readString(KEY_THEME, ThemeMode.System.name)) }
            .getOrDefault(ThemeMode.System)
    )
        private set

    var palette by mutableStateOf(
        runCatching { PaletteId.valueOf(repo.readString(KEY_PALETTE, PaletteId.Aurora.name)) }
            .getOrDefault(PaletteId.Aurora)
    )
        private set

    var visualMode by mutableStateOf(
        runCatching { VisualMode.valueOf(repo.readString(KEY_VISUAL_MODE, legacyVisualMode().name)) }
            .getOrDefault(legacyVisualMode())
    )
        private set

    /**
     * 老版本是两个独立开关（glassEnabled／dynamicColor），现在是一个 VisualMode 枚举。
     * 优先级判定本身在 [SettingsRules.legacyVisualModeKey]（纯 Kotlin、有单元测试），
     * 这里只做「稳定英文键 → 枚举」——枚举属于 UI 层，不许下沉到领域层。
     */
    private fun legacyVisualMode(): VisualMode {
        val key = SettingsRules.legacyVisualModeKey(
            glassEnabled = repo.readBoolean(KEY_GLASS_ENABLED, true),
            dynamicColor = repo.readBoolean(KEY_DYNAMIC, false),
        )
        return when (key) {
            SettingsRules.VISUAL_MD3 -> VisualMode.Md3
            SettingsRules.VISUAL_GLASS -> VisualMode.Glass
            else -> VisualMode.Normal
        }
    }

    var reducedMotion by mutableStateOf(repo.readBoolean(KEY_REDUCED, false))
        private set

    /** True once the user has completed the first-launch onboarding flow. */
    var onboardingCompleted by mutableStateOf(repo.readBoolean(KEY_ONBOARDING, false))
        private set

    var motionDamping by mutableFloatStateOf(repo.readFloat(KEY_DAMPING, 0.5f))
        private set

    var wallpaperMode by mutableStateOf(
        runCatching {
            WallpaperMode.valueOf(
                repo.readString(
                    KEY_WALLPAPER_MODE,
                    if (repo.readBoolean(KEY_WALLPAPER, false)) WallpaperMode.System.name else WallpaperMode.Aurora.name,
                )!!,
            )
        }.getOrDefault(WallpaperMode.Aurora)
    )
        private set

    var cachedWallpaperPath by mutableStateOf(repo.readNullableString(KEY_WALLPAPER_PATH, null))
        private set

    var networkWallpaperUrl by mutableStateOf(repo.readString(KEY_WALLPAPER_URL, "").orEmpty())
        private set

    var wallpaperRevision by mutableIntStateOf(0)
        private set

    val wallpaperBackground: Boolean get() = wallpaperMode != WallpaperMode.Aurora

    var glassBlurRadiusDp by mutableFloatStateOf(repo.readFloat(KEY_GLASS_BLUR, 0f))
        private set
    var glassRefractionHeightDp by mutableFloatStateOf(repo.readFloat(KEY_REFRACTION_HEIGHT, 15f))
        private set
    var glassRefractionAmountDp by mutableFloatStateOf(repo.readFloat(KEY_REFRACTION_AMOUNT, 20f))
        private set
    var glassTintAlpha by mutableFloatStateOf(repo.readFloat(KEY_TINT_ALPHA, 0.08f))
        private set
    var glassBorderAlpha by mutableFloatStateOf(repo.readFloat(KEY_BORDER_ALPHA, 0.20f))
        private set
    var glassVibrancyEnabled by mutableStateOf(repo.readBoolean(KEY_VIBRANCY, true))
        private set
    var glassChromaticAberration by mutableStateOf(repo.readBoolean(KEY_CHROMATIC, true))
        private set

    var glassOpacity by mutableFloatStateOf(
        if (repo.contains(KEY_OPACITY)) repo.readFloat(KEY_OPACITY, 0.55f)
        else repo.readFloat(KEY_FROST, 0.55f)
    )
        private set

    var glassRefraction by mutableFloatStateOf(repo.readFloat(KEY_REFRACTION, 1f))
        private set

    var cornerScale by mutableFloatStateOf(repo.readFloat(KEY_CORNER, 1f))
        private set

    /** Free-form accent seed colour used when [PaletteId.Custom] is active. */
    var customSeed by mutableIntStateOf(repo.readInt(KEY_CUSTOM_SEED, SettingsRules.DEFAULT_SEED))
        private set

    /** 0f..1f — how deep/light the custom background reads. */
    var customShade by mutableFloatStateOf(repo.readFloat(KEY_CUSTOM_SHADE, 0.5f))
        private set

    fun updateOnboardingCompleted(value: Boolean) {
        onboardingCompleted = value
        repo.writeBoolean(KEY_ONBOARDING, value)
    }

    fun updateThemeMode(value: ThemeMode) {
        themeMode = value
        repo.writeString(KEY_THEME, value.name)
    }

    fun updatePalette(value: PaletteId) {
        palette = value
        repo.writeString(KEY_PALETTE, value.name)
    }

    fun updateVisualMode(value: VisualMode) {
        visualMode = value
        repo.writeString(KEY_VISUAL_MODE, value.name)
    }

    fun updateReducedMotion(value: Boolean) {
        reducedMotion = value
        repo.writeBoolean(KEY_REDUCED, value)
    }

    /** Flow that emits the latest onboarding-completed flag whenever it changes. */
    val onboardingCompletedFlow = snapshotFlow { onboardingCompleted }

    fun completeOnboarding() {
        onboardingCompleted = true
        repo.writeBoolean(KEY_ONBOARDING, true)
    }

    /** Reset onboarding so the next launch shows the welcome screen again. */
    fun resetOnboarding() {
        onboardingCompleted = false
        repo.writeBoolean(KEY_ONBOARDING, false)
    }

    fun updateMotionDamping(value: Float) {
        motionDamping = SettingsRules.clampDamping(value)
        repo.writeFloat(KEY_DAMPING, motionDamping)
    }

    fun updateWallpaperBackground(value: Boolean) {
        updateWallpaperMode(if (value) WallpaperMode.System else WallpaperMode.Aurora)
    }

    fun updateWallpaperMode(value: WallpaperMode) {
        wallpaperMode = value
        repo.writeString(KEY_WALLPAPER_MODE, value.name)
    }

    /** Forces wallpaper consumers (and settings-page probes) to re-read. */
    fun bumpWallpaperRevision() {
        wallpaperRevision++
    }

    fun updateCachedWallpaper(path: String?, sourceUrl: String = networkWallpaperUrl) {
        cachedWallpaperPath = path
        networkWallpaperUrl = sourceUrl
        wallpaperRevision++
        repo.writeStrings(KEY_WALLPAPER_PATH to path, KEY_WALLPAPER_URL to sourceUrl)
        if (!path.isNullOrBlank()) updateWallpaperMode(WallpaperMode.Cached)
    }

    fun updateNetworkWallpaperUrl(value: String) {
        networkWallpaperUrl = value
        repo.writeString(KEY_WALLPAPER_URL, value)
    }

    fun updateGlassBlurRadiusDp(value: Float) {
        glassBlurRadiusDp = SettingsRules.clampBlurRadiusDp(value)
        repo.writeFloat(KEY_GLASS_BLUR, glassBlurRadiusDp)
    }

    fun updateGlassRefractionHeightDp(value: Float) {
        glassRefractionHeightDp = SettingsRules.clampRefractionHeightDp(value)
        repo.writeFloat(KEY_REFRACTION_HEIGHT, glassRefractionHeightDp)
    }

    fun updateGlassRefractionAmountDp(value: Float) {
        glassRefractionAmountDp = SettingsRules.clampRefractionAmountDp(value)
        repo.writeFloat(KEY_REFRACTION_AMOUNT, glassRefractionAmountDp)
    }

    fun updateGlassTintAlpha(value: Float) {
        glassTintAlpha = SettingsRules.clampTintAlpha(value)
        repo.writeFloat(KEY_TINT_ALPHA, glassTintAlpha)
    }

    fun updateGlassBorderAlpha(value: Float) {
        glassBorderAlpha = SettingsRules.clampBorderAlpha(value)
        repo.writeFloat(KEY_BORDER_ALPHA, glassBorderAlpha)
    }

    fun updateGlassVibrancyEnabled(value: Boolean) {
        glassVibrancyEnabled = value
        repo.writeBoolean(KEY_VIBRANCY, value)
    }

    fun updateGlassChromaticAberration(value: Boolean) {
        glassChromaticAberration = value
        repo.writeBoolean(KEY_CHROMATIC, value)
    }

    fun updateGlassOpacity(value: Float) {
        glassOpacity = SettingsRules.clampUnit(value)
        repo.writeFloat(KEY_OPACITY, glassOpacity)
    }

    fun updateGlassRefraction(value: Float) {
        glassRefraction = SettingsRules.clampUnit(value)
        repo.writeFloat(KEY_REFRACTION, glassRefraction)
    }

    fun updateCornerScale(value: Float) {
        cornerScale = SettingsRules.clampCornerScale(value)
        repo.writeFloat(KEY_CORNER, cornerScale)
    }

    fun updateCustomSeed(value: Color) {
        customSeed = value.toArgb()
        repo.writeInt(KEY_CUSTOM_SEED, value.toArgb())
    }

    fun updateCustomShade(value: Float) {
        customShade = SettingsRules.clampUnit(value)
        repo.writeFloat(KEY_CUSTOM_SHADE, customShade)
    }


    fun exportThemeJson(): String = JSONObject().apply {
        put("schema", SettingsRules.SCHEMA_VERSION)
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
        put(KEY_SEED_HEX, SettingsRules.customSeedHex(customSeed))
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
        updateMotionDamping(data.optDouble(KEY_DAMPING, motionDamping.toDouble()).toFloat())
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
        updateGlassOpacity(data.optDouble(KEY_OPACITY, glassOpacity.toDouble()).toFloat())
        updateGlassRefraction(data.optDouble(KEY_REFRACTION, glassRefraction.toDouble()).toFloat())
        updateCornerScale(data.optDouble(KEY_CORNER, cornerScale.toDouble()).toFloat())
        if (data.has(KEY_CUSTOM_SEED)) {
            updateCustomSeed(Color(data.optInt(KEY_CUSTOM_SEED, customSeed)))
        } else {
            // 只写了 customSeedHex 的主题文件（手写的、或从别处导出的）过去会被静默忽略
            SettingsRules.parseCustomSeedHex(data.optString(KEY_SEED_HEX, ""))?.let { updateCustomSeed(Color(it)) }
        }
        updateCustomShade(data.optDouble(KEY_CUSTOM_SHADE, customShade.toDouble()).toFloat())
        true
    }.getOrDefault(false)

    fun reset() {
        updateThemeMode(ThemeMode.System)
        updatePalette(PaletteId.Aurora)
        updateVisualMode(VisualMode.Glass)
        updateReducedMotion(false)
        updateMotionDamping(SettingsRules.DEFAULT_DAMPING)
        updateWallpaperMode(WallpaperMode.Aurora)
        updateGlassBlurRadiusDp(SettingsRules.DEFAULT_BLUR_RADIUS_DP)
        updateGlassRefractionHeightDp(SettingsRules.DEFAULT_REFRACTION_HEIGHT_DP)
        updateGlassRefractionAmountDp(SettingsRules.DEFAULT_REFRACTION_AMOUNT_DP)
        updateGlassTintAlpha(SettingsRules.DEFAULT_TINT_ALPHA)
        updateGlassBorderAlpha(SettingsRules.DEFAULT_BORDER_ALPHA)
        updateGlassVibrancyEnabled(true)
        updateGlassChromaticAberration(true)
        updateGlassOpacity(SettingsRules.DEFAULT_OPACITY)
        updateGlassRefraction(SettingsRules.DEFAULT_REFRACTION)
        updateCornerScale(SettingsRules.DEFAULT_CORNER_SCALE)
        updateCustomSeed(Color(SettingsRules.DEFAULT_SEED))
        updateCustomShade(SettingsRules.DEFAULT_CUSTOM_SHADE)
        onboardingCompleted = false
        repo.writeBoolean(KEY_ONBOARDING, false)
    }

    private companion object {
        // 键的字面值定义在 domain/SettingsRules（批 A 有测试锁住），这里只是就近取用
        const val KEY_THEME = SettingsRules.KEY_THEME
        const val KEY_PALETTE = SettingsRules.KEY_PALETTE
        const val KEY_VISUAL_MODE = SettingsRules.KEY_VISUAL_MODE
        const val KEY_GLASS_ENABLED = SettingsRules.KEY_GLASS_ENABLED
        const val KEY_DYNAMIC = SettingsRules.KEY_DYNAMIC
        const val KEY_REDUCED = SettingsRules.KEY_REDUCED
        const val KEY_DAMPING = SettingsRules.KEY_DAMPING
        const val KEY_WALLPAPER = SettingsRules.KEY_WALLPAPER
        const val KEY_WALLPAPER_MODE = SettingsRules.KEY_WALLPAPER_MODE
        const val KEY_WALLPAPER_PATH = SettingsRules.KEY_WALLPAPER_PATH
        const val KEY_WALLPAPER_URL = SettingsRules.KEY_WALLPAPER_URL
        const val KEY_GLASS_BLUR = SettingsRules.KEY_GLASS_BLUR
        const val KEY_REFRACTION_HEIGHT = SettingsRules.KEY_REFRACTION_HEIGHT
        const val KEY_REFRACTION_AMOUNT = SettingsRules.KEY_REFRACTION_AMOUNT
        const val KEY_TINT_ALPHA = SettingsRules.KEY_TINT_ALPHA
        const val KEY_BORDER_ALPHA = SettingsRules.KEY_BORDER_ALPHA
        const val KEY_VIBRANCY = SettingsRules.KEY_VIBRANCY
        const val KEY_CHROMATIC = SettingsRules.KEY_CHROMATIC
        const val KEY_FROST = SettingsRules.KEY_FROST
        const val KEY_OPACITY = SettingsRules.KEY_OPACITY
        const val KEY_REFRACTION = SettingsRules.KEY_REFRACTION
        const val KEY_CORNER = SettingsRules.KEY_CORNER
        const val KEY_CUSTOM_SEED = SettingsRules.KEY_CUSTOM_SEED
        const val KEY_CUSTOM_SHADE = SettingsRules.KEY_CUSTOM_SHADE
        const val KEY_ONBOARDING = SettingsRules.KEY_ONBOARDING
    }
}

@Composable
fun rememberAppSettingsState(): AppSettingsState {
    val context = LocalContext.current.applicationContext
    // prefs 只在构造仓库那一刻用到；状态持有者此后只认 SettingsRepository，再也见不到 Context
    // —— 这就是批 F 的 S18 列为唯一真疑点、批 D 收窄掉的那处
    return remember(context) {
        AppSettingsState(
            SettingsRepository(
                context.getSharedPreferences(SettingsRepository.FILE_NAME, Context.MODE_PRIVATE),
            ),
        )
    }
}
