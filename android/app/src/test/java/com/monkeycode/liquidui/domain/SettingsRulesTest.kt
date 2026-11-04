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
        // 没有规则的键原样返回：模糊半径、折射高度从来没有区间，不擅自发明一个
        assertEquals(120f, SettingsRules.clampByKey(SettingsRules.KEY_GLASS_BLUR, 120f), 0f)
        assertEquals(15f, SettingsRules.clampByKey(SettingsRules.KEY_REFRACTION_HEIGHT, 15f), 0f)
        assertEquals(42f, SettingsRules.clampByKey("unknownKey", 42f), 0f)
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
        assertEquals(2, SettingsRules.CLAMP_RULES.values.toSet().size)
        assertTrue(SettingsRules.CLAMP_RULES.values.all { it == SettingsRules.RULE_UNIT || it == SettingsRules.RULE_CORNER })
    }
}
