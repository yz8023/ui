package com.monkeycode.liquidui.domain

import java.util.Locale

/**
 * 设置项的取值规则（纯 Kotlin，零 Android/Compose 依赖 → 可在 JVM 上被单元测试覆盖）。
 *
 * 批 D-架构的动机是两处真实的规则不一致：
 * ① `motionDamping`／`glassOpacity`／`glassRefraction`／`cornerScale`／`customShade` 这五个值
 *    **只有导入主题 JSON 时才夹取**，用户在设置页拖滑杆走的是 `updateXxx`，直接赋值不夹取；
 * ② `glassBlurRadiusDp`／`glassRefractionHeightDp`／`glassRefractionAmountDp`／`glassTintAlpha`／
 *    `glassBorderAlpha` 这五个值反过来：夹取写在 `updateXxx` 里（区间各不相同：40／40／60／0.35／0.45），
 *    导入时靠调用 `updateXxx` 间接生效。
 * 十条区间集中到这里之后，导入与手动修改走同一套规则，且规则本身可以被测试。
 *
 * 依赖方向约束：本文件不许 import 任何 `android.*`／`androidx.*`／Compose 类型，
 * 枚举也不许进来——UI 层负责「稳定英文键 ↔ 枚举」的映射（沿用批 C 定的惯例）。
 */
object SettingsRules {

    // ---------- 取值区间（字面值照抄重构前的行为，不改既有手感） ----------

    const val UNIT_MIN = 0f
    const val UNIT_MAX = 1f
    const val CORNER_MIN = 0.8f
    const val CORNER_MAX = 1.5f
    const val BLUR_MAX_DP = 40f
    const val REFRACTION_HEIGHT_MAX_DP = 40f
    const val REFRACTION_AMOUNT_MAX_DP = 60f
    const val TINT_ALPHA_MAX = 0.35f
    const val BORDER_ALPHA_MAX = 0.45f

    /**
     * 0..1 的比值类参数（透明度、折射强度、深浅）共用这一条。
     * NaN 归 0：NaN 一旦渗进 `graphicsLayer` 的 alpha/scale，整层会直接不绘制，
     * 而这种 bug 在真机上表现为「东西凭空消失」，极难定位，所以在规则层就掐掉。
     */
    fun clampUnit(value: Float): Float = if (value.isNaN()) UNIT_MIN else value.coerceIn(UNIT_MIN, UNIT_MAX)

    /** 动效阻尼 0..1。NaN 归中性值 0.5（既不完全抑制也不完全放开）。 */
    fun clampDamping(value: Float): Float = if (value.isNaN()) DEFAULT_DAMPING else value.coerceIn(UNIT_MIN, UNIT_MAX)

    /** 圆角缩放 0.8..1.5。NaN 归 1（不缩放）。 */
    fun clampCornerScale(value: Float): Float =
        if (value.isNaN()) DEFAULT_CORNER_SCALE else value.coerceIn(CORNER_MIN, CORNER_MAX)

    /** 0..上限 且不允许负值的一类（dp 尺寸、alpha 上限各不相同）。NaN 归各自的默认值。 */
    fun clampBlurRadiusDp(value: Float): Float =
        if (value.isNaN()) DEFAULT_BLUR_RADIUS_DP else value.coerceIn(0f, BLUR_MAX_DP)

    fun clampRefractionHeightDp(value: Float): Float =
        if (value.isNaN()) DEFAULT_REFRACTION_HEIGHT_DP else value.coerceIn(0f, REFRACTION_HEIGHT_MAX_DP)

    fun clampRefractionAmountDp(value: Float): Float =
        if (value.isNaN()) DEFAULT_REFRACTION_AMOUNT_DP else value.coerceIn(0f, REFRACTION_AMOUNT_MAX_DP)

    fun clampTintAlpha(value: Float): Float =
        if (value.isNaN()) DEFAULT_TINT_ALPHA else value.coerceIn(0f, TINT_ALPHA_MAX)

    fun clampBorderAlpha(value: Float): Float =
        if (value.isNaN()) DEFAULT_BORDER_ALPHA else value.coerceIn(0f, BORDER_ALPHA_MAX)

    /** 键 → 夹取规则。没有出现在这里的键就是**故意不设区间**，`clampByKey` 原样返回，不擅自发明。 */
    val CLAMP_RULES: Map<String, (Float) -> Float> = mapOf(
        KEY_DAMPING to { clampDamping(it) },
        KEY_OPACITY to { clampUnit(it) },
        KEY_REFRACTION to { clampUnit(it) },
        KEY_CUSTOM_SHADE to { clampUnit(it) },
        KEY_CORNER to { clampCornerScale(it) },
        KEY_GLASS_BLUR to { clampBlurRadiusDp(it) },
        KEY_REFRACTION_HEIGHT to { clampRefractionHeightDp(it) },
        KEY_REFRACTION_AMOUNT to { clampRefractionAmountDp(it) },
        KEY_TINT_ALPHA to { clampTintAlpha(it) },
        KEY_BORDER_ALPHA to { clampBorderAlpha(it) },
    )

    fun hasRule(key: String): Boolean = CLAMP_RULES.containsKey(key)

    fun clampByKey(key: String, value: Float): Float = CLAMP_RULES[key]?.invoke(value) ?: value

    /**
     * 反查某个键的实际区间：拿 ±∞ 去问规则本身，而不是手抄一份区间表。
     * 这样 UI 上显示的滑杆范围与真正生效的夹取**不可能不一致**（手抄的表迟早会漂）。
     * @return `[min, max]`；没有规则的键返回 `[-∞, +∞]`。
     */
    fun rangeOf(key: String): FloatArray = floatArrayOf(
        clampByKey(key, Float.NEGATIVE_INFINITY),
        clampByKey(key, Float.POSITIVE_INFINITY),
    )

    // ---------- 旧版视觉模式的兼容映射 ----------

    const val VISUAL_GLASS = "glass"
    const val VISUAL_NORMAL = "normal"
    const val VISUAL_MD3 = "md3"

    /** 稳定英文键集合（老版本两个开关能推出的值域）。UI 层用它做「键 → VisualMode 枚举」的映射。 */
    val VISUAL_KEYS: List<String> = listOf(VISUAL_GLASS, VISUAL_NORMAL, VISUAL_MD3)

    fun isVisualKey(key: String): Boolean = key in VISUAL_KEYS

    /**
     * 老版本是两个独立开关（`glassEnabled`／`dynamicColor`），现在是一个 `VisualMode` 枚举。
     * 优先级：动态色 > 玻璃 > 普通。这条映射原先是 `AppSettingsState.legacyVisualMode()` 的私有逻辑，
     * 抽出来之后它能被测试，而且 UI 层不再需要知道历史键名。
     * 注意 `oshin` 不在这个值域里：它是后来加的第四种视觉模式，老开关推不出来。
     */
    fun legacyVisualModeKey(glassEnabled: Boolean, dynamicColor: Boolean): String = when {
        dynamicColor -> VISUAL_MD3
        glassEnabled -> VISUAL_GLASS
        else -> VISUAL_NORMAL
    }

    // ---------- 自定义种子色 ----------

    /** 默认种子色 `#FF3E8FE0`（与既有 `PaletteId.Custom` 的出厂值一致）。 */
    const val DEFAULT_SEED = 0xFF3E8FE0.toInt()

    /** ARGB → `#AARRGGBB`。必须按无符号处理，否则负数 Int 会打印成 `#-1C701F0` 这种废串。 */
    fun customSeedHex(argb: Int): String =
        String.format(Locale.US, "#%08X", argb.toLong() and 0xFFFFFFFFL)

    /**
     * `#AARRGGBB`／`#RRGGBB` → ARGB Int；解析不出来返回 null（调用方保留当前值，不猜）。
     * 导出 JSON 里一直带着 `customSeedHex`，但导入过去只认整数字段——补上这条后，
     * 只给了十六进制的主题文件也能正确导入。
     */
    fun parseCustomSeedHex(text: String): Int? {
        val body = text.trim().removePrefix("#")
        if (body.length != 8 && body.length != 6) return null
        if (body.any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }) return null
        val value = body.toLongOrNull(16) ?: return null
        return if (body.length == 6) (0xFF000000L or value).toInt() else value.toInt()
    }

    // ---------- 出厂默认值（与 AppSettingsState 的初值、reset() 一一对应） ----------

    const val DEFAULT_DAMPING = 0.5f
    const val DEFAULT_OPACITY = 0.55f
    const val DEFAULT_REFRACTION = 1f
    const val DEFAULT_CORNER_SCALE = 1f
    const val DEFAULT_CUSTOM_SHADE = 0.5f
    const val DEFAULT_BLUR_RADIUS_DP = 0f
    const val DEFAULT_REFRACTION_HEIGHT_DP = 15f
    const val DEFAULT_REFRACTION_AMOUNT_DP = 20f
    const val DEFAULT_TINT_ALPHA = 0.08f
    const val DEFAULT_BORDER_ALPHA = 0.20f

    /**
     * 浮点默认值表：键就是 prefs 键。
     * 批 A 用它断言「每个默认值都是自身夹取规则的不动点」——默认值违反自己的规则是一类真实 bug
     * （表现为刚装好就显示异常，而用户改一下又好了，很难复现）。
     */
    val DEFAULT_FLOATS: Map<String, Float> = mapOf(
        KEY_DAMPING to DEFAULT_DAMPING,
        KEY_OPACITY to DEFAULT_OPACITY,
        KEY_REFRACTION to DEFAULT_REFRACTION,
        KEY_CORNER to DEFAULT_CORNER_SCALE,
        KEY_CUSTOM_SHADE to DEFAULT_CUSTOM_SHADE,
        KEY_GLASS_BLUR to DEFAULT_BLUR_RADIUS_DP,
        KEY_REFRACTION_HEIGHT to DEFAULT_REFRACTION_HEIGHT_DP,
        KEY_REFRACTION_AMOUNT to DEFAULT_REFRACTION_AMOUNT_DP,
        KEY_TINT_ALPHA to DEFAULT_TINT_ALPHA,
        KEY_BORDER_ALPHA to DEFAULT_BORDER_ALPHA,
    )

    // ---------- 存储键（字面值是跨版本契约，批 A 有测试锁住） ----------

    const val PREFS_FILE = "liquidui.settings"
    const val SCHEMA_VERSION = 1

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
    const val KEY_SEED_HEX = "customSeedHex"

    /** 导出 JSON 的全部字段（含 schema），S22 用它说明「哪些键跨版本不能改」。 */
    val EXPORT_FIELDS: List<String> = listOf(
        "schema", KEY_THEME, KEY_PALETTE, KEY_VISUAL_MODE, KEY_REDUCED, KEY_DAMPING,
        KEY_WALLPAPER, KEY_WALLPAPER_MODE, KEY_WALLPAPER_PATH, KEY_WALLPAPER_URL,
        KEY_GLASS_BLUR, KEY_REFRACTION_HEIGHT, KEY_REFRACTION_AMOUNT, KEY_TINT_ALPHA,
        KEY_BORDER_ALPHA, KEY_VIBRANCY, KEY_CHROMATIC, KEY_OPACITY, KEY_REFRACTION,
        KEY_CORNER, KEY_CUSTOM_SEED, KEY_SEED_HEX, KEY_CUSTOM_SHADE,
    )
}
