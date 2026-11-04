package com.monkeycode.liquidui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * BlurHash 编解码的单元测试（批 A）。跑在纯 JVM 上：领域层不 import 任何 Android 类型，
 * 所以不需要 Robolectric、不需要模拟器。CI 用 `:app:testDebugUnitTest` 强制执行。
 */
class BlurHashTest {

    private fun solid(argb: Int, width: Int = 8, height: Int = 8) =
        BlurHash.encode(width, height) { _, _ -> argb }

    private fun gradient(width: Int = 16, height: Int = 12): IntArray {
        val out = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val r = x * 255 / (width - 1)
                val g = y * 255 / (height - 1)
                val b = (x + y) * 255 / (width + height - 2)
                out[y * width + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        return out
    }

    @Test
    fun base83CharacterSetIsExactly83UniqueChars() {
        assertEquals(83, BlurHash.DIGITS.length)
        assertEquals(83, BlurHash.DIGITS.toSet().size)
    }

    @Test
    fun base83RoundTripsWithinCapacity() {
        val cases = listOf(
            0 to 1, 1 to 1, 82 to 1,
            83 to 2, 6888 to 2, 3429 to 2,
            0 to 4, 16777215 to 4, 47458320 to 4,
        )
        cases.forEach { pair ->
            val value = pair.first
            val length = pair.second
            val text = BlurHash.encode83(value, length)
            assertEquals(length, text.length)
            assertEquals("value=$value length=$length", value, BlurHash.decode83(text))
        }
    }

    @Test
    fun decode83RejectsCharsOutsideTheSet() {
        assertEquals(-1, BlurHash.decode83("0!0"))
        assertEquals(-1, BlurHash.decode83("abc "))
        assertTrue(BlurHash.decode83("00") >= 0)
    }

    @Test
    fun sizeFlagEncodesBothComponentCounts() {
        for (x in 1..9) {
            for (y in 1..9) {
                val flag = BlurHash.sizeFlag(x, y)
                assertEquals("x=$x y=$y", x, flag % 9 + 1)
                assertEquals("x=$x y=$y", y, flag / 9 + 1)
            }
        }
    }

    @Test
    fun encodedLengthMatchesSpec() {
        // 规范：1 字符尺寸标志 + 1 字符最大 AC 量化值 + 4 字符 DC + 每个 AC 分量 2 字符
        for (x in 1..5) {
            for (y in 1..4) {
                val hash = BlurHash.encode(6, 5, x, y) { _, _ -> 0xFF204060.toInt() }
                assertTrue("hash should not be null for x=$x y=$y", hash != null)
                assertEquals(6 + 2 * (x * y - 1), hash!!.length)
                assertEquals(BlurHash.expectedLength(x, y), hash.length)
            }
        }
        assertEquals(28, BlurHash.expectedLength(4, 3))
    }

    @Test
    fun componentsAreRecoveredFromTheHash() {
        for (x in 1..6) {
            for (y in 1..5) {
                val hash = BlurHash.encode(5, 4, x, y) { _, _ -> 0xFF102030.toInt() }!!
                val components = BlurHash.componentsOf(hash)
                assertTrue("componentsOf should parse a valid hash", components != null)
                assertEquals(x, components!![0])
                assertEquals(y, components[1])
            }
        }
    }

    @Test
    fun solidColorKeepsDcExactlyButStillCarriesAcComponents() {
        val color = 0xFF3E8FE0.toInt()
        val hash = solid(color)!!
        // 实测 2026-11-04：hash = LU7EDUpMfQpMpMkDfQkDfQfQfQfQ（28 字符）
        assertEquals(28, hash.length)
        assertEquals(BlurHash.sizeFlag(4, 3), BlurHash.decode83(hash.substring(0, 1)))
        // 反直觉但真实：纯色图的 AC 分量并不为 0。规范用的基函数 cos(pi*i*x/w) 在 x=0..w-1 上
        // 并不正交（sum of cos(pi*x/8) for x in 0..7 等于 1，不是 0），所以纯色也会投出交流分量。
        // 这条被写成断言，是为了防止有人"顺手修正"基函数而偏离规范。
        assertTrue("solid color still carries AC energy: maxAcChar=${hash[1]}", hash[1] != '0')
        assertEquals(30, BlurHash.decode83(hash.substring(1, 2)))
        // DC 是精确的：规范保证平均色可以单独取用，不必解码整张图
        assertEquals(color, BlurHash.dcColor(hash))
    }

    @Test
    fun singleComponentHashIsLosslessForSolidColor() {
        val color = 0xFF3E8FE0.toInt()
        val hash = BlurHash.encode(8, 8, 1, 1) { _, _ -> color }!!
        assertEquals(6, hash.length)
        assertEquals('0', hash[0])
        assertEquals('0', hash[1])
        val decoded = BlurHash.decode(hash, 8, 8)!!
        for (pixel in decoded) assertEquals(color, pixel)
        assertEquals(0f, BlurHash.meanAbsoluteError(IntArray(64) { color }, decoded), 0f)
    }

    @Test
    fun moreComponentsDoNotAlwaysMeanLessError() {
        val color = 0xFF3E8FE0.toInt()
        val original = IntArray(64) { color }
        fun mae(x: Int, y: Int): Float {
            val hash = BlurHash.encode(8, 8, x, y) { _, _ -> color }!!
            return BlurHash.meanAbsoluteError(original, BlurHash.decode(hash, 8, 8)!!)
        }
        // 实测：纯色图 1×1 = 0.00、2×2 = 11.64、4×3 = 13.53、6×5 = 15.10、9×9 = 7.93
        // 即「组件数越多越清晰」这句话对纯色图是错的，S19 页面把这张表原样展示出来
        assertEquals(0f, mae(1, 1), 0f)
        assertTrue("4x3 should be worse than 1x1 for a solid color", mae(1, 1) < mae(4, 3))
        assertTrue("measured 4x3 MAE was 13.53", mae(4, 3) in 5f..25f)
        assertTrue("measured 6x5 MAE was 15.10", mae(6, 5) > mae(4, 3))
    }

    @Test
    fun dcColorMatchesSolidColor() {
        val color = 0xFF00FF00.toInt()
        val hash = solid(color, 4, 4)!!
        assertEquals(color, BlurHash.dcColor(hash))
    }

    @Test
    fun samplerEntryAndPixelArrayEntryAgree() {
        val width = 16
        val height = 12
        val pixels = gradient(width, height)
        val fromArray = BlurHash.encode(pixels, width, height, 4, 3)
        val fromSampler = BlurHash.encode(width, height, 4, 3) { x, y -> pixels[y * width + x] }
        assertEquals(fromArray, fromSampler)
        assertTrue(fromArray != null)
    }

    @Test
    fun decodeRejectsMalformedHashes() {
        assertNull(BlurHash.decode("", 8, 8))
        assertNull(BlurHash.decode("abc", 8, 8))
        val valid = solid(0xFF123456.toInt(), 6, 6)!!
        assertNull("truncated hash must be rejected", BlurHash.decode(valid.dropLast(1), 8, 8))
        assertNull("illegal char must be rejected", BlurHash.decode("!" + valid.drop(1), 8, 8))
        assertNull(BlurHash.decode(valid, 0, 8))
    }

    @Test
    fun componentsOfRejectsLengthMismatch() {
        // 尺寸标志说 4×3（长度应为 28），实际给了 20 个字符 → 不自洽
        val flag = BlurHash.encode83(BlurHash.sizeFlag(4, 3), 1)
        assertNull(BlurHash.componentsOf(flag + "0" + "0000" + "00".repeat(6)))
    }

    @Test
    fun decodedGradientIsCloseToOriginalAverage() {
        val width = 16
        val height = 12
        val pixels = gradient(width, height)
        val hash = BlurHash.encode(pixels, width, height, 4, 3)!!
        val decoded = BlurHash.decode(hash, width, height)!!
        assertEquals(pixels.size, decoded.size)
        // 4×3 分量对 16×12 的平滑渐变应当保住大部分信息：平均绝对误差远小于半个色阶跨度
        val error = BlurHash.meanAbsoluteError(pixels, decoded)
        assertTrue("mean absolute error too high: $error", error < 24f)
    }

    @Test
    fun meanAbsoluteErrorHandlesMismatchedInputs() {
        assertTrue(BlurHash.meanAbsoluteError(IntArray(0), IntArray(0)).isNaN())
        assertTrue(BlurHash.meanAbsoluteError(IntArray(4), IntArray(3)).isNaN())
    }

    @Test
    fun segmentsCoverTheWholeHash() {
        val hash = solid(0xFF112233.toInt(), 6, 6)!!
        val segments = BlurHash.segmentsOf(hash)
        assertEquals(3 + (4 * 3 - 1), segments.size)
        assertEquals(0, segments.first()[0])
        assertEquals(hash.length, segments.last()[1])
        assertEquals(BlurHash.ROLE_SIZE, segments[0][2])
        assertEquals(BlurHash.ROLE_MAX_AC, segments[1][2])
        assertEquals(BlurHash.ROLE_DC, segments[2][2])
        assertEquals(BlurHash.ROLE_AC, segments[3][2])
        // 段与段必须首尾相接、不重叠
        for (i in 1 until segments.size) assertEquals(segments[i - 1][1], segments[i][0])
        assertTrue(BlurHash.segmentsOf("bad").isEmpty())
    }

    @Test
    fun acQuantisationRoundTripsWithinTolerance() {
        listOf(-1.0, -0.25, 0.0, 0.25, 1.0).forEach { value ->
            assertEquals(value, BlurHash.roundTripAc(value), 1e-9)
        }
    }

    @Test
    fun srgbConversionIsMonotonicAndRoundTrips() {
        var previous = -1.0
        for (v in 0..255) {
            val linear = BlurHash.srgbToLinear(v)
            assertTrue("srgbToLinear must be monotonic at $v", linear >= previous)
            previous = linear
            assertTrue("srgb round trip at $v drifted", kotlin.math.abs(v - BlurHash.linearToSrgb(linear)) <= 1)
        }
        assertEquals(0.0, BlurHash.srgbToLinear(0), 1e-12)
        assertEquals(1.0, BlurHash.srgbToLinear(255), 1e-12)
        // NaN 与越界输入不许炸
        assertEquals(0, BlurHash.linearToSrgb(Double.NaN))
        assertEquals(0, BlurHash.linearToSrgb(-5.0))
        assertEquals(255, BlurHash.linearToSrgb(5.0))
    }

    @Test
    fun encodeRejectsInvalidGeometryAndComponents() {
        assertNull(BlurHash.encode(0, 8) { _, _ -> 0 })
        assertNull(BlurHash.encode(8, 0) { _, _ -> 0 })
        assertNull(BlurHash.encode(8, 8, 0, 3) { _, _ -> 0 })
        assertNull(BlurHash.encode(8, 8, 4, 10) { _, _ -> 0 })
        val pixels = IntArray(12)
        assertNull("pixel array size must match width*height", BlurHash.encode(pixels, 4, 4))
    }

    @Test
    fun blurHashOfHelperMatchesObjectEntry() {
        val direct = BlurHash.encode(9, 7, 3, 2) { x, y -> (x * 7 + y * 13) shl 8 }
        val helper = blurHashOf(9, 7, 3, 2) { x, y -> (x * 7 + y * 13) shl 8 }
        assertEquals(direct, helper)
    }
}
