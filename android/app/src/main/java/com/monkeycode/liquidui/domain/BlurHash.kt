package com.monkeycode.liquidui.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * BlurHash 编解码：把一张图压成 20–30 个字符，解码回来是一张可用的低清占位图。
 *
 * 算法规范取自 woltapp/blurhash（MIT）的公开定义；「先给低清占位、再换清图」的用法取自
 * saket/telephoto（Apache-2.0）。**本仓库不引任何依赖**，这一层是纯 Kotlin：
 * 输入输出都是 ARGB Int，不碰 Bitmap、不碰 Context，所以它能在 JVM 上被单元测试直接覆盖（批 A）。
 *
 * S19（图片变换与占位）用它做真实占位；批 F 的 S16 只做过多趟偏移叠加的近似，那条页面上已如实降级说明。
 */
object BlurHash {

    /** base83 的 83 个字符，顺序即数值，改一个字符整个编码就与规范不兼容。 */
    const val DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz#\$%*+,-.:;=?@[]^_{|}~"

    /** 规范推荐的默认组件数：4×3，共 12 个分量、hash 长度 = 6+2×(12−1) = 28。 */
    const val DEFAULT_X_COMPONENTS = 4
    const val DEFAULT_Y_COMPONENTS = 3

    /** 组件数合法区间（规范限定 1..9）。 */
    fun validComponents(n: Int): Boolean = n in 1..9

    /** 尺寸标志：把两个组件数塞进一个 base83 字符。 */
    fun sizeFlag(xComponents: Int, yComponents: Int): Int = (xComponents - 1) + (yComponents - 1) * 9

    /**
     * 从 hash 反解组件数。
     * @return 长度 2 的数组 `[xComponents, yComponents]`；串太短、含非法字符或长度与组件数不自洽时返回 null。
     */
    fun componentsOf(hash: String): IntArray? {
        if (hash.length < 6) return null
        val flag = decode83(hash.substring(0, 1))
        if (flag < 0) return null
        val x = flag % 9 + 1
        val y = flag / 9 + 1
        if (hash.length != 4 + 2 * x * y) return null
        return intArrayOf(x, y)
    }

    /** hash 长度是否自洽（不校验字符是否合法）。 */
    fun expectedLength(xComponents: Int, yComponents: Int): Int = 4 + 2 * xComponents * yComponents

    /** base83 定长编码：高位在前。 */
    fun encode83(value: Int, length: Int): String {
        val out = StringBuilder(length)
        for (i in 1..length) {
            val digit = (value / pow83(length - i)) % 83
            out.append(DIGITS[digit.coerceIn(0, 82)])
        }
        return out.toString()
    }

    /** base83 解码；遇到字符集外的字符返回 −1（调用方据此判定非法串）。 */
    fun decode83(text: String): Int {
        var value = 0
        for (c in text) {
            val digit = DIGITS.indexOf(c)
            if (digit < 0) return -1
            value = value * 83 + digit
        }
        return value
    }

    private fun pow83(exponent: Int): Int {
        var result = 1
        repeat(exponent) { result *= 83 }
        return result
    }

    /**
     * 编码。像素用采样器给，因此**不需要 Bitmap**：S19 直接用 `(x, y) -> ARGB` 现场生成图案，
     * 单元测试也走这个入口，两边结果必须一致（批 A 有这条断言）。
     *
     * @return hash；尺寸非法或组件数越界时返回 null。
     */
    fun encode(
        width: Int,
        height: Int,
        xComponents: Int = DEFAULT_X_COMPONENTS,
        yComponents: Int = DEFAULT_Y_COMPONENTS,
        sample: (x: Int, y: Int) -> Int,
    ): String? {
        if (width <= 0 || height <= 0) return null
        if (!validComponents(xComponents) || !validComponents(yComponents)) return null

        val total = xComponents * yComponents
        val factors = DoubleArray(total * 3)
        for (j in 0 until yComponents) {
            for (i in 0 until xComponents) {
                // 直流分量权重 1，交流分量权重 2（规范如此，少乘 2 解码出来会偏暗）
                val norm = if (i == 0 && j == 0) 1.0 else 2.0
                var r = 0.0
                var g = 0.0
                var b = 0.0
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val basis = norm * cos(PI * i * x / width) * cos(PI * j * y / height)
                        val argb = sample(x, y)
                        r += basis * srgbToLinear((argb shr 16) and 0xFF)
                        g += basis * srgbToLinear((argb shr 8) and 0xFF)
                        b += basis * srgbToLinear(argb and 0xFF)
                    }
                }
                val scale = 1.0 / (width.toDouble() * height.toDouble())
                val at = (j * xComponents + i) * 3
                factors[at] = r * scale
                factors[at + 1] = g * scale
                factors[at + 2] = b * scale
            }
        }

        var maxAc = 0.0
        for (k in 1 until total) {
            maxAc = max(maxAc, abs(factors[k * 3]))
            maxAc = max(maxAc, abs(factors[k * 3 + 1]))
            maxAc = max(maxAc, abs(factors[k * 3 + 2]))
        }
        val quantisedMax = if (total > 1) floor(maxAc * 166 - 0.5).toInt().coerceIn(0, 82) else 0
        val realMax = (quantisedMax + 1) / 166.0

        val out = StringBuilder(expectedLength(xComponents, yComponents))
        out.append(encode83(sizeFlag(xComponents, yComponents), 1))
        out.append(encode83(quantisedMax, 1))
        out.append(encode83(encodeDc(factors[0], factors[1], factors[2]), 4))
        for (k in 1 until total) {
            out.append(encode83(encodeAc(factors[k * 3], factors[k * 3 + 1], factors[k * 3 + 2], realMax), 2))
        }
        return out.toString()
    }

    /** 像素数组入口：与采样器入口必须给出同一个 hash。 */
    fun encode(
        pixels: IntArray,
        width: Int,
        height: Int,
        xComponents: Int = DEFAULT_X_COMPONENTS,
        yComponents: Int = DEFAULT_Y_COMPONENTS,
    ): String? {
        if (width <= 0 || height <= 0 || pixels.size != width * height) return null
        return encode(width, height, xComponents, yComponents) { x, y -> pixels[y * width + x] }
    }

    /**
     * 解码成 ARGB 像素数组（行主序，长度 width×height）。
     * @return null 表示 hash 非法（长度不自洽或含字符集外的字符）。
     */
    fun decode(hash: String, width: Int, height: Int): IntArray? {
        if (width <= 0 || height <= 0) return null
        val components = componentsOf(hash) ?: return null
        val xComponents = components[0]
        val yComponents = components[1]

        val quantisedMax = decode83(hash.substring(1, 2))
        if (quantisedMax < 0) return null
        val realMax = (quantisedMax + 1) / 166.0

        val dc = decode83(hash.substring(2, 6))
        if (dc < 0) return null

        val total = xComponents * yComponents
        val colors = Array(total) { DoubleArray(3) }
        colors[0][0] = srgbToLinear((dc shr 16) and 0xFF)
        colors[0][1] = srgbToLinear((dc shr 8) and 0xFF)
        colors[0][2] = srgbToLinear(dc and 0xFF)
        for (k in 1 until total) {
            val ac = decode83(hash.substring(4 + k * 2, 6 + k * 2))
            if (ac < 0) return null
            val qr = ac / (19 * 19)
            val qg = (ac / 19) % 19
            val qb = ac % 19
            colors[k][0] = signPow((qr - 9) / 9.0, 2.0) * realMax
            colors[k][1] = signPow((qg - 9) / 9.0, 2.0) * realMax
            colors[k][2] = signPow((qb - 9) / 9.0, 2.0) * realMax
        }

        val out = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var r = 0.0
                var g = 0.0
                var b = 0.0
                for (j in 0 until yComponents) {
                    for (i in 0 until xComponents) {
                        val basis = cos(PI * i * x / width) * cos(PI * j * y / height)
                        val at = j * xComponents + i
                        r += colors[at][0] * basis
                        g += colors[at][1] * basis
                        b += colors[at][2] * basis
                    }
                }
                out[y * width + x] = (0xFF shl 24) or
                    (linearToSrgb(r) shl 16) or
                    (linearToSrgb(g) shl 8) or
                    linearToSrgb(b)
            }
        }
        return out
    }

    /** 解码图与原图的平均绝对误差（0–255），S19 用它当「这串 hash 保住了多少信息」的读数。 */
    fun meanAbsoluteError(pixels: IntArray, decoded: IntArray): Float {
        if (pixels.isEmpty() || pixels.size != decoded.size) return Float.NaN
        var sum = 0L
        for (i in pixels.indices) {
            sum += abs(((pixels[i] shr 16) and 0xFF) - ((decoded[i] shr 16) and 0xFF))
            sum += abs(((pixels[i] shr 8) and 0xFF) - ((decoded[i] shr 8) and 0xFF))
            sum += abs((pixels[i] and 0xFF) - (decoded[i] and 0xFF))
        }
        return sum.toFloat() / (pixels.size * 3)
    }

    /** 把 hash 拆成「这一段是什么」，S19 的逐字符着色用它。返回的每段是 `[起, 止, 角色]`，角色见 ROLE_*。 */
    const val ROLE_SIZE = 0
    const val ROLE_MAX_AC = 1
    const val ROLE_DC = 2
    const val ROLE_AC = 3

    fun segmentsOf(hash: String): List<IntArray> {
        val components = componentsOf(hash) ?: return emptyList()
        val total = components[0] * components[1]
        val out = ArrayList<IntArray>(total + 2)
        out.add(intArrayOf(0, 1, ROLE_SIZE))
        out.add(intArrayOf(1, 2, ROLE_MAX_AC))
        out.add(intArrayOf(2, 6, ROLE_DC))
        for (k in 1 until total) out.add(intArrayOf(4 + k * 2, 6 + k * 2, ROLE_AC))
        return out
    }

    private fun encodeDc(r: Double, g: Double, b: Double): Int =
        (linearToSrgb(r) shl 16) + (linearToSrgb(g) shl 8) + linearToSrgb(b)

    private fun encodeAc(r: Double, g: Double, b: Double, maximum: Double): Int {
        if (maximum <= 0.0) return 0
        val qr = floor(signPow(r / maximum, 0.5) * 9 + 9.5).toInt().coerceIn(0, 18)
        val qg = floor(signPow(g / maximum, 0.5) * 9 + 9.5).toInt().coerceIn(0, 18)
        val qb = floor(signPow(b / maximum, 0.5) * 9 + 9.5).toInt().coerceIn(0, 18)
        return qr * 19 * 19 + qg * 19 + qb
    }

    /** sign(x)·|x|^n：编码用 n=0.5（压缩大值），解码用 n=2（还原）。 */
    private fun signPow(value: Double, n: Double): Double {
        val sign = if (value < 0) -1.0 else 1.0
        return sign * abs(value).pow(n)
    }

    /** 8bit sRGB → 线性光。阈值 0.04045 是规范值，写成 0.0404 会在暗部产生可见偏差。 */
    fun srgbToLinear(value8bit: Int): Double {
        val c = (value8bit and 0xFF) / 255.0
        return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }

    /** 线性光 → 8bit sRGB。先夹到 0..1 再开幂，否则负值会得到 NaN。 */
    fun linearToSrgb(linear: Double): Int {
        val c = linear.coerceIn(0.0, 1.0)
        val v = if (c <= 0.0031308) c * 12.92 else 1.055 * c.pow(1.0 / 2.4) - 0.055
        return (v * 255 + 0.5).toInt().coerceIn(0, 255)
    }

    /** 只取直流分量对应的平均色（ARGB），S19 用它跟原图平均色对账。 */
    fun dcColor(hash: String): Int? {
        if (hash.length < 6) return null
        val dc = decode83(hash.substring(2, 6))
        if (dc < 0) return null
        return (0xFF shl 24) or dc
    }

    /** 供测试用的开方入口：验证 signPow 的性质（signPow(signPow(x,0.5),2) ≈ x）。 */
    fun roundTripAc(value: Double): Double = signPow(signPow(value, 0.5), 2.0)

    init {
        // 自检：字符集必须正好 83 个且不重复，否则整个编码是错的
        check(DIGITS.length == 83) { "base83 digit set must have exactly 83 characters" }
        check(DIGITS.toSet().size == 83) { "base83 digit set must not repeat characters" }
    }
}

/** 采样器版编码的便捷封装：给一个「归一化坐标 → ARGB」的函数即可，无需知道像素尺寸。 */
fun blurHashOf(
    width: Int,
    height: Int,
    xComponents: Int = BlurHash.DEFAULT_X_COMPONENTS,
    yComponents: Int = BlurHash.DEFAULT_Y_COMPONENTS,
    sample: (x: Int, y: Int) -> Int,
): String? = BlurHash.encode(width, height, xComponents, yComponents, sample)

/** 整数开方入口留给测试：确认 sqrt 与 pow(0.5) 在正数上等价（规范用后者）。 */
internal fun sqrtPositive(v: Double): Double = sqrt(abs(v))
