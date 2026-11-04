package com.monkeycode.liquidui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 设置项取值规则的单元测试（批 A）。
 *
 * 这里最有价值的断言不是「夹取函数会夹取」，而是三类**回归锁**：
 * ① 存储键与 prefs 文件名的字面值不能变（变了老用户的设置就丢了）；
 * ② 每个出厂默认值都必须落在自己的夹取区间内（否则刚装好就显示异常，用户改一下又好了，极难复现）；
 * ③ 导出 JSON 的字段集合不能漏、不能重（漏了主题文件互相导入就会静默丢参数）。
 */
class SettingsRulesTest {

    @Test
    fun clampUnitCoversBoundsAndNaN() {
        assertEquals(0f, SettingsRules.clampUnit(-1f), 0f)
        assertEquals(1f, SettingsRules.clampUnit(2f), 0f)
        assertEquals(0.5f, SettingsRules.clampUnit(0.5f), 0f)
        assertEquals(0f, SettingsRules.clampUnit(Float.NaN), 0f)
        assertEquals(0f, SettingsRules.clampUnit(Float.NEGATIVE_INFINITY), 0f)
        assertEquals(1f, SettingsRules.clampUnit(Float.POSITIVE_INFINITY), 0f)
    }

    @Test
    fun clampDampingFallsBackToTheNeutralValue() {
        assertEquals(SettingsRules.DEFAULT_DAMPING, SettingsRules.clampDamping(Float.NaN), 0f)
        assertEquals(1f, SettingsRules.clampDamping(3f), 0f)
        assertEquals(0f, SettingsRules.clampDamping(-0.2f), 0f)
    }

    @Test
    fun clampCornerScaleUsesItsOwnRange() {
        assertEquals(0.8f, SettingsRules.clampCornerScale(0.5f), 0f)
        assertEquals(1.5f, SettingsRules.clampCornerScale(2f), 0f)
        assertEquals(1f, SettingsRules.clampCornerScale(Float.NaN), 0f)
        assertEquals(1.2f, SettingsRules.clampCornerScale(1.2f), 0f)
    }

    @Test
    fun clampingIsIdempotent() {
        val inputs = listOf(-5f, -0.001f, 0f, 0.3f, 0.79f, 0.8f, 1f, 1.2f, 1.5f, 9f, Float.NaN)
        inputs.forEach { value ->
            assertEquals(SettingsRules.clampUnit(value), SettingsRules.clampUnit(SettingsRules.clampUnit(value)), 0f)
            assertEquals(
                SettingsRules.clampCornerScale(value),
                SettingsRules.clampCornerScale(SettingsRules.clampCornerScale(value)),
                0f,
            )
        }
        // NaN 夹一次之后必须变成正常数，否则第二次仍是 NaN，UI 层永远修不好
        assertFalse(SettingsRules.clampUnit(Float.NaN).isNaN())
        assertFalse(SettingsRules.clampCornerScale(Float.NaN).isNaN())
        assertFalse(SettingsRules.clampDamping(Float.NaN).isNaN())
    }

    @Test
    fun clampByKeyAppliesTheRightRuleAndInventsNothing() {
        assertEquals(1f, SettingsRules.clampByKey(SettingsRules.KEY_OPACITY, 7f), 0f)
        assertEquals(1f, SettingsRules.clampByKey(SettingsRules.KEY_REFRACTION, 7f), 0f)
        assertEquals(0.5f, SettingsRules.clampByKey(SettingsRules.KEY_DAMPING, 0.5f), 0f)
        assertEquals(1.5f, SettingsRules.clampByKey(SettingsRules.KEY_CORNER, 9f), 0f)
        // 重构前只有导入路径会夹取的五个键，现在手动改值走同一条规则
        assertEquals(1f, SettingsRules.clampByKey(SettingsRules.KEY_CUSTOM_SHADE, 7f), 0f)
        // 没有规则的键原样返回：不擅自发明一个区间
        assertEquals(42f, SettingsRules.clampByKey("unknownKey", 42f), 0f)
        assertEquals(42f, SettingsRules.clampByKey(SettingsRules.KEY_THEME.let { "notAFloatKey" }, 42f), 0f)
        assertFalse(SettingsRules.hasRule("unknownKey"))
    }

    @Test
    fun legacyVisualModeMappingTruthTable() {
        assertEquals(SettingsRules.VISUAL_NORMAL, SettingsRules.legacyVisualModeKey(false, false))
        assertEquals(SettingsRules.VISUAL_GLASS, SettingsRules.legacyVisualModeKey(true, false))
        // 动态色优先级高于玻璃：两个开关都开时按动态色走
        assertEquals(SettingsRules.VISUAL_MD3, SettingsRules.legacyVisualModeKey(false, true))
        assertEquals(SettingsRules.VISUAL_MD3, SettingsRules.legacyVisualModeKey(true, true))
    }

    @Test
    fun visualKeysAreStableAndCoverOnlyTheLegacyCodomain() {
        assertEquals(listOf("glass", "normal", "md3"), SettingsRules.VISUAL_KEYS)
        assertTrue(SettingsRules.VISUAL_KEYS.all { SettingsRules.isVisualKey(it) })
        assertFalse(SettingsRules.isVisualKey("glassy"))
        assertFalse(SettingsRules.isVisualKey(""))
        // oshin 是后来加的第四种视觉模式，老版本的两个开关推不出它，所以不在兼容映射的值域里
        assertFalse(SettingsRules.isVisualKey("oshin"))
    }

    @Test
    fun seedHexHandlesNegativeArgbAsUnsigned() {
        assertEquals("#FF3E8FE0", SettingsRules.customSeedHex(SettingsRules.DEFAULT_SEED))
        assertEquals("#00000000", SettingsRules.customSeedHex(0))
        assertEquals("#FFFFFFFF", SettingsRules.customSeedHex(-1))
        assertEquals("#7FFFFFFF", SettingsRules.customSeedHex(Int.MAX_VALUE))
        assertEquals("#80000000", SettingsRules.customSeedHex(Int.MIN_VALUE))
        assertFalse(
            "hex must never contain a minus sign",
            SettingsRules.customSeedHex(SettingsRules.DEFAULT_SEED).contains("-"),
        )
    }

    @Test
    fun seedHexParsingRoundTrips() {
        listOf(0, 1, -1, Int.MAX_VALUE, Int.MIN_VALUE, SettingsRules.DEFAULT_SEED, 0xFF00FF00.toInt()).forEach { seed ->
            val hex = SettingsRules.customSeedHex(seed)
            assertEquals("round trip for $hex", seed, SettingsRules.parseCustomSeedHex(hex))
            assertEquals("lowercase must parse too", seed, SettingsRules.parseCustomSeedHex(hex.lowercase()))
        }
    }

    @Test
    fun seedHexParsingAcceptsSixDigitsAndRejectsJunk() {
        assertEquals(0xFF3E8FE0.toInt(), SettingsRules.parseCustomSeedHex("#3E8FE0"))
        assertEquals(0xFFFFFFFF.toInt(), SettingsRules.parseCustomSeedHex("ffffff"))
        assertNull(SettingsRules.parseCustomSeedHex(""))
        assertNull(SettingsRules.parseCustomSeedHex("#"))
        assertNull(SettingsRules.parseCustomSeedHex("#GGGGGGGG"))
        assertNull(SettingsRules.parseCustomSeedHex("#12345"))
        assertNull(SettingsRules.parseCustomSeedHex("#123456789"))
        assertNull(SettingsRules.parseCustomSeedHex("not a colour"))
    }

    @Test
    fun everyDefaultIsAFixedPointOfItsOwnClampRule() {
        SettingsRules.DEFAULT_FLOATS.forEach { entry ->
            val key = entry.key
            val default = entry.value
            assertEquals("default for $key violates its own clamp rule", default, SettingsRules.clampByKey(key, default), 0f)
            assertFalse("default for $key must not be NaN", default.isNaN())
        }
        assertEquals(0.5f, SettingsRules.DEFAULT_DAMPING, 0f)
        assertEquals(0.55f, SettingsRules.DEFAULT_OPACITY, 0f)
        assertEquals(1f, SettingsRules.DEFAULT_CORNER_SCALE, 0f)
        assertEquals("#FF3E8FE0", SettingsRules.customSeedHex(SettingsRules.DEFAULT_SEED))
    }

    @Test
    fun eachGlassKnobKeepsItsOwnHistoricRange() {
        // 这五个区间是重构前各自 updateXxx 里写死的字面值（40/40/60/0.35/0.45），搬进规则层时不许顺手改手感
        assertEquals(40f, SettingsRules.clampBlurRadiusDp(999f), 0f)
        assertEquals(0f, SettingsRules.clampBlurRadiusDp(-3f), 0f)
        assertEquals(40f, SettingsRules.clampRefractionHeightDp(999f), 0f)
        assertEquals(60f, SettingsRules.clampRefractionAmountDp(999f), 0f)
        assertEquals(0.35f, SettingsRules.clampTintAlpha(9f), 0f)
        assertEquals(0.45f, SettingsRules.clampBorderAlpha(9f), 0f)
        // 区间各不相同这件事本身要锁住：曾经有人想把它们统一成 0..1，那会直接改掉模糊与折射的手感
        assertFalse(SettingsRules.BLUR_MAX_DP == SettingsRules.REFRACTION_AMOUNT_MAX_DP)
        assertFalse(SettingsRules.TINT_ALPHA_MAX == SettingsRules.BORDER_ALPHA_MAX)
        // NaN 一律回落到各自默认值，不能让 NaN 渗进 graphicsLayer
        assertEquals(SettingsRules.DEFAULT_BLUR_RADIUS_DP, SettingsRules.clampBlurRadiusDp(Float.NaN), 0f)
        assertEquals(SettingsRules.DEFAULT_TINT_ALPHA, SettingsRules.clampTintAlpha(Float.NaN), 0f)
        assertEquals(SettingsRules.DEFAULT_BORDER_ALPHA, SettingsRules.clampBorderAlpha(Float.NaN), 0f)
    }

    @Test
    fun rangeOfAsksTheRuleInsteadOfCopyingATable() {
        // UI 显示的滑杆范围靠 ±∞ 探规则本身，不是手抄一份区间表 —— 手抄的表迟早会和真规则漂移
        val blurRange = SettingsRules.rangeOf(SettingsRules.KEY_GLASS_BLUR)
        assertEquals(0f, blurRange[0], 0f)
        assertEquals(40f, blurRange[1], 0f)
        val cornerRange = SettingsRules.rangeOf(SettingsRules.KEY_CORNER)
        assertEquals(0.8f, cornerRange[0], 0f)
        assertEquals(1.5f, cornerRange[1], 0f)
        val amountRange = SettingsRules.rangeOf(SettingsRules.KEY_REFRACTION_AMOUNT)
        assertEquals(60f, amountRange[1], 0f)
        // 没有规则的键必须报告「无界」，UI 才知道不该画滑杆
        val unknown = SettingsRules.rangeOf("unknownKey")
        assertEquals(Float.NEGATIVE_INFINITY, unknown[0], 0f)
        assertEquals(Float.POSITIVE_INFINITY, unknown[1], 0f)
        // 探出来的区间端点必须是自身规则的不动点，否则 rangeOf 与 clampByKey 说的不是同一件事
        SettingsRules.CLAMP_RULES.keys.forEach { key ->
            val range = SettingsRules.rangeOf(key)
            assertTrue("range of $key must be ordered", range[0] <= range[1])
            assertEquals("min of $key is not a fixed point", range[0], SettingsRules.clampByKey(key, range[0]), 0f)
            assertEquals("max of $key is not a fixed point", range[1], SettingsRules.clampByKey(key, range[1]), 0f)
        }
    }

    @Test
    fun theFiveFormerlyUnclampedKnobsNowHaveRules() {
        // 回归锁：重构前这五个键只在 importThemeJson 里夹取，用户在设置页拖滑杆是直接赋值。
        // 症状是导入的主题合法、手动拖出来的值却能越界存盘，重启后读回来越界值直接进渲染层。
        listOf(
            SettingsRules.KEY_DAMPING, SettingsRules.KEY_OPACITY, SettingsRules.KEY_REFRACTION,
            SettingsRules.KEY_CORNER, SettingsRules.KEY_CUSTOM_SHADE,
        ).forEach { key ->
            assertTrue("$key lost its clamp rule", SettingsRules.hasRule(key))
            assertEquals("$key must clamp an absurd value", SettingsRules.clampByKey(key, 99f),
                SettingsRules.rangeOf(key)[1], 0f)
        }
    }

    @Test
    fun storageIdentityIsLocked() {
        // 这两条一旦变化，老用户升级后设置会全部丢失、导出的主题文件也互不兼容 —— 必须显式锁住
        assertEquals("liquidui.settings", SettingsRules.PREFS_FILE)
        assertEquals(1, SettingsRules.SCHEMA_VERSION)
    }

    @Test
    fun preferenceKeysAreUnique() {
        val keys = listOf(
            SettingsRules.KEY_THEME, SettingsRules.KEY_PALETTE, SettingsRules.KEY_VISUAL_MODE,
            SettingsRules.KEY_GLASS_ENABLED, SettingsRules.KEY_DYNAMIC, SettingsRules.KEY_REDUCED,
            SettingsRules.KEY_DAMPING, SettingsRules.KEY_WALLPAPER, SettingsRules.KEY_WALLPAPER_MODE,
            SettingsRules.KEY_WALLPAPER_PATH, SettingsRules.KEY_WALLPAPER_URL, SettingsRules.KEY_GLASS_BLUR,
            SettingsRules.KEY_REFRACTION_HEIGHT, SettingsRules.KEY_REFRACTION_AMOUNT, SettingsRules.KEY_TINT_ALPHA,
            SettingsRules.KEY_BORDER_ALPHA, SettingsRules.KEY_VIBRANCY, SettingsRules.KEY_CHROMATIC,
            SettingsRules.KEY_FROST, SettingsRules.KEY_OPACITY, SettingsRules.KEY_REFRACTION,
            SettingsRules.KEY_CORNER, SettingsRules.KEY_CUSTOM_SEED, SettingsRules.KEY_CUSTOM_SHADE,
            SettingsRules.KEY_ONBOARDING, SettingsRules.KEY_SEED_HEX,
        )
        assertEquals("duplicate preference keys would silently overwrite each other", keys.size, keys.toSet().size)
        assertTrue(keys.all { it.matches(Regex("[A-Za-z]+")) })
    }

    @Test
    fun exportFieldsCoverEveryRuleAndDefault() {
        val fields = SettingsRules.EXPORT_FIELDS
        assertEquals(fields.size, fields.toSet().size)
        assertTrue(fields.contains("schema"))
        assertTrue(fields.contains(SettingsRules.KEY_SEED_HEX))
        assertTrue(fields.contains(SettingsRules.KEY_CUSTOM_SEED))
        // 有夹取规则的键、有默认值的键，都必须出现在导出字段里，否则主题文件会静默丢参数
        SettingsRules.CLAMP_RULES.keys.forEach { key ->
            assertTrue("clamp rule for $key is not exported", fields.contains(key))
        }
        SettingsRules.DEFAULT_FLOATS.keys.forEach { key ->
            assertTrue("default for $key is not exported", fields.contains(key))
        }
        // 每个浮点设置项都必须同时有默认值和夹取规则；两边键集合一旦漂移就是漏改
        assertEquals(SettingsRules.DEFAULT_FLOATS.keys, SettingsRules.CLAMP_RULES.keys)
        assertEquals(10, SettingsRules.CLAMP_RULES.size)
    }
}
