package com.monkeycode.liquidui.domain

import kotlin.math.abs

/**
 * 多轨关键帧时间轴的核心（纯 Kotlin，零 Android/Compose 依赖 → 可在 JVM 上被单元测试覆盖）。
 *
 * 取材边界：多轨／关键帧／时间游标／区间插值取自 airbnb/lottie-android（Apache-2.0）的时间轴口径。
 * **刻意不做曲线库**——缓动曲线、时长档、弹簧参数是既有素材 W02 令牌实验室的地盘（时长 7 档／曲线 9 条／
 * 弹簧 6 组／错峰 5 种），这一层只管「多条动画在时间上怎么排布」，所以插值只有 LINEAR／STEP／SMOOTH 三种。
 */

/** 关键帧到下一帧之间怎么取值。 */
enum class KeyframeMode {
    /** 线性插值。 */
    LINEAR,

    /** 阶梯：整段保持左端值，到下一帧瞬间跳变（Lottie 里的 hold 关键帧）。 */
    STEP,

    /** smoothstep：t²(3−2t)，两端速度为 0。这是唯一一个内建缓动，不是曲线库。 */
    SMOOTH,
}

/** 一个关键帧：时间点、取值、以及「到下一帧之间」的取值方式。 */
data class Keyframe(
    val timeMs: Int,
    val value: Float,
    val mode: KeyframeMode = KeyframeMode.LINEAR,
)

/** smoothstep：在 0.5 处正好等于 0.5，两端导数为 0。批 A 断言的就是这两条性质。 */
fun smoothstep(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

/**
 * 一条轨道。关键帧必须按时间升序（构造时校验），至少一帧。
 * @param key 稳定英文键，不是显示文案；显示文案由 UI 层查资源。
 */
class Track(val key: String, val keys: List<Keyframe>) {

    init {
        require(keys.isNotEmpty()) { "track '$key' needs at least one keyframe" }
        require(keys.zipWithNext().all { it.first.timeMs <= it.second.timeMs }) {
            "track '$key' keyframes must be sorted by timeMs"
        }
    }

    val startMs: Int get() = keys.first().timeMs
    val endMs: Int get() = keys.last().timeMs
    val durationMs: Int get() = endMs - startMs
    val keyframeCount: Int get() = keys.size
    val segmentCount: Int get() = keys.size - 1

    /** 游标落在第几个区间；在首帧之前返回 −1，在末帧之后夹到最后一个区间（不返回越界序号）。 */
    fun segmentIndexOf(timeMs: Int): Int {
        if (timeMs < startMs) return -1
        if (segmentCount == 0) return 0
        var index = 0
        while (index < keys.lastIndex && keys[index + 1].timeMs <= timeMs) index++
        return index.coerceAtMost(segmentCount - 1)
    }

    /** 某个区间的插值方式（由区间左端的关键帧决定）。 */
    fun modeOfSegment(index: Int): KeyframeMode =
        keys[index.coerceIn(0, keys.lastIndex)].mode

    /** 区间内的归一化进度 0..1；零长区间返回 1（已经在右端）。 */
    fun segmentProgressAt(timeMs: Int): Float {
        val index = segmentIndexOf(timeMs).coerceAtLeast(0)
        if (index >= keys.lastIndex) return 1f
        val left = keys[index]
        val right = keys[index + 1]
        val span = right.timeMs - left.timeMs
        if (span <= 0) return 1f
        return ((timeMs - left.timeMs).toFloat() / span).coerceIn(0f, 1f)
    }

    /**
     * 游标处的取值。
     * 首帧之前取首值、末帧之后取末值（不外推）；NaN 输入按 0 处理，避免渗进 graphicsLayer 让整层不绘制。
     */
    fun valueAt(timeMs: Int): Float {
        if (timeMs <= startMs) return keys.first().value
        if (timeMs >= endMs) return keys.last().value
        val index = segmentIndexOf(timeMs)
        val left = keys[index]
        val right = keys[index + 1]
        val span = right.timeMs - left.timeMs
        if (span <= 0) return right.value
        val t = (timeMs - left.timeMs).toFloat() / span
        val result = when (left.mode) {
            KeyframeMode.STEP -> left.value
            KeyframeMode.SMOOTH -> left.value + (right.value - left.value) * smoothstep(t)
            KeyframeMode.LINEAR -> left.value + (right.value - left.value) * t
        }
        return if (result.isNaN()) 0f else result
    }

    /** 游标在整条轨道上的归一化进度 0..1；轨道长度 0 时返回 1。 */
    fun progressAt(timeMs: Int): Float {
        if (durationMs <= 0) return 1f
        return ((timeMs - startMs).toFloat() / durationMs).coerceIn(0f, 1f)
    }

    /** 到游标为止已经过的关键帧数（含首帧），S20 的读数用。 */
    fun passedKeyframesAt(timeMs: Int): Int = keys.count { it.timeMs <= timeMs }
}

/**
 * 时间轴 = 多条轨道共用一个游标。
 * 各轨道的起止可以不同（错峰就靠这个），所以时间轴的区间取所有轨道的并集。
 */
class Timeline(val tracks: List<Track>) {

    init {
        require(tracks.isNotEmpty()) { "timeline needs at least one track" }
        require(tracks.map { it.key }.toSet().size == tracks.size) { "track keys must be unique" }
    }

    val startMs: Int get() = tracks.minOf { it.startMs }
    val endMs: Int get() = tracks.maxOf { it.endMs }
    val durationMs: Int get() = endMs - startMs
    val trackCount: Int get() = tracks.size
    val keyframeTotal: Int get() = tracks.sumOf { it.keyframeCount }

    fun clampTime(timeMs: Int): Int = timeMs.coerceIn(startMs, endMs)

    /** 游标处的全部取值：轨道键 → 值。UI 层按键查资源拿显示名。 */
    fun sampleAt(timeMs: Int): Map<String, Float> = tracks.associate { it.key to it.valueAt(timeMs) }

    /** 游标在整个时间轴上的归一化进度 0..1。 */
    fun progressAt(timeMs: Int): Float {
        if (durationMs <= 0) return 1f
        return ((timeMs - startMs).toFloat() / durationMs).coerceIn(0f, 1f)
    }

    /** 到游标为止，所有轨道一共已过多少个关键帧。 */
    fun passedKeyframesAt(timeMs: Int): Int = tracks.sumOf { it.passedKeyframesAt(timeMs) }

    /** 每条轨道在游标处的区间序号（−1 表示还没开始），S20 用它画「当前落在第几段」。 */
    fun segmentsAt(timeMs: Int): Map<String, Int> = tracks.associate { it.key to it.segmentIndexOf(timeMs) }

    /**
     * 游标处正在动的轨道数。三个条件都要满足：游标落在该轨道自己的时间范围内（未开始或已结束都不算在动）、
     * 所在区间两端值不同、且插值方式不是 STEP（阶梯在整段里是静止的，只在跳变瞬间变化）。
     */
    fun movingTrackCountAt(timeMs: Int): Int = tracks.count { track ->
        if (timeMs < track.startMs || timeMs >= track.endMs) return@count false
        val index = track.segmentIndexOf(timeMs)
        index in 0 until track.keys.lastIndex &&
            abs(track.keys[index].value - track.keys[index + 1].value) > 0.0001f &&
            track.modeOfSegment(index) != KeyframeMode.STEP
    }

    fun trackOf(key: String): Track? = tracks.firstOrNull { it.key == key }
}
