package com.monkeycode.liquidui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 多轨关键帧时间轴的单元测试（批 A）。
 * 重点是边界：游标在首帧前／末帧后／区间端点／零长区间，以及三种插值各自的性质。
 */
class KeyframeTrackTest {

    private fun linearTrack() = Track(
        "translate",
        listOf(Keyframe(0, 0f), Keyframe(100, 10f)),
    )

    private fun threeKeyTrack(mode: KeyframeMode) = Track(
        "alpha",
        listOf(Keyframe(0, 0f, mode), Keyframe(100, 1f, mode), Keyframe(200, 0.4f, mode)),
    )

    @Test
    fun valueBeforeFirstKeyframeIsFirstValue() {
        val track = linearTrack()
        assertEquals(0f, track.valueAt(-500), 1e-6f)
        assertEquals(0f, track.valueAt(0), 1e-6f)
    }

    @Test
    fun valueAfterLastKeyframeIsLastValue() {
        val track = linearTrack()
        assertEquals(10f, track.valueAt(100), 1e-6f)
        assertEquals(10f, track.valueAt(9999), 1e-6f)
    }

    @Test
    fun linearInterpolationHitsTheMidpoint() {
        val track = linearTrack()
        assertEquals(5f, track.valueAt(50), 1e-6f)
        assertEquals(2.5f, track.valueAt(25), 1e-6f)
        assertEquals(7.5f, track.valueAt(75), 1e-6f)
    }

    @Test
    fun stepModeHoldsTheLeftValueUntilTheNextKey() {
        val track = Track("step", listOf(Keyframe(0, 3f, KeyframeMode.STEP), Keyframe(100, 9f)))
        assertEquals(3f, track.valueAt(1), 1e-6f)
        assertEquals(3f, track.valueAt(99), 1e-6f)
        assertEquals(9f, track.valueAt(100), 1e-6f)
    }

    @Test
    fun smoothModeIsSymmetricAroundTheMidpoint() {
        val track = threeKeyTrack(KeyframeMode.SMOOTH)
        assertEquals(0.5f, track.valueAt(50), 1e-6f)
        // smoothstep 两端速度为 0：靠近端点时推进比线性慢
        assertTrue(track.valueAt(25) < 0.25f)
        assertTrue(track.valueAt(75) > 0.75f)
    }

    @Test
    fun smoothstepHasTheExpectedEndsAndShape() {
        assertEquals(0f, smoothstep(0f), 1e-6f)
        assertEquals(1f, smoothstep(1f), 1e-6f)
        assertEquals(0.5f, smoothstep(0.5f), 1e-6f)
        assertTrue(smoothstep(-1f) == 0f)
        assertTrue(smoothstep(2f) == 1f)
        // 单调不减
        var previous = -1f
        for (i in 0..100) {
            val v = smoothstep(i / 100f)
            assertTrue("smoothstep must be monotonic at $i", v >= previous)
            previous = v
        }
    }

    @Test
    fun zeroLengthSegmentDoesNotDivideByZero() {
        val track = Track(
            "hold",
            listOf(
                Keyframe(0, 0f),
                Keyframe(50, 5f),
                Keyframe(50, 9f),
                Keyframe(100, 10f),
            ),
        )
        assertEquals(0f, track.valueAt(0), 1e-6f)
        assertEquals(9.5f, track.valueAt(75), 1e-4f)
        assertEquals(10f, track.valueAt(100), 1e-6f)
        // 时间 50 是区间 [50,100] 的左端，进度为 0（不是 1）
        assertEquals(0f, track.segmentProgressAt(50), 1e-6f)
        assertTrue(track.valueAt(60).isFinite())
    }

    @Test
    fun progressIsNormalisedAndClamped() {
        val track = threeKeyTrack(KeyframeMode.LINEAR)
        assertEquals(200, track.durationMs)
        assertEquals(0f, track.progressAt(-10), 1e-6f)
        assertEquals(0.25f, track.progressAt(50), 1e-6f)
        assertEquals(1f, track.progressAt(999), 1e-6f)
        assertEquals(1f, Track("single", listOf(Keyframe(5, 1f))).progressAt(5), 1e-6f)
    }

    @Test
    fun segmentIndexReportsPositionCorrectly() {
        val track = threeKeyTrack(KeyframeMode.LINEAR)
        assertEquals(-1, track.segmentIndexOf(-1))
        assertEquals(0, track.segmentIndexOf(0))
        assertEquals(0, track.segmentIndexOf(99))
        assertEquals(1, track.segmentIndexOf(100))
        assertEquals(1, track.segmentIndexOf(999))
        assertEquals(2, track.segmentCount)
        assertEquals(3, track.keyframeCount)
    }

    @Test
    fun passedKeyframeCountIsInclusive() {
        val track = threeKeyTrack(KeyframeMode.LINEAR)
        assertEquals(0, track.passedKeyframesAt(-5))
        assertEquals(1, track.passedKeyframesAt(0))
        assertEquals(2, track.passedKeyframesAt(100))
        assertEquals(3, track.passedKeyframesAt(201))
    }

    @Test
    fun timelineSamplesEveryTrackAtTheCursor() {
        val timeline = Timeline(
            listOf(
                Track("translate", listOf(Keyframe(0, 0f), Keyframe(100, 10f))),
                Track("alpha", listOf(Keyframe(0, 1f), Keyframe(100, 0f))),
                Track("scale", listOf(Keyframe(50, 1f), Keyframe(150, 1.5f))),
            ),
        )
        assertEquals(3, timeline.trackCount)
        assertEquals(0, timeline.startMs)
        assertEquals(150, timeline.endMs)
        assertEquals(150, timeline.durationMs)
        assertEquals(6, timeline.keyframeTotal)

        val sample = timeline.sampleAt(50)
        assertEquals(3, sample.size)
        assertEquals(5f, sample.getValue("translate"), 1e-6f)
        assertEquals(0.5f, sample.getValue("alpha"), 1e-6f)
        // scale 轨道在首帧之前 → 取首值，不外推
        assertEquals(1f, sample.getValue("scale"), 1e-6f)

        assertEquals(150, timeline.clampTime(900))
        assertEquals(0, timeline.clampTime(-900))
        assertEquals(0.5f, timeline.progressAt(75), 1e-6f)
    }

    @Test
    fun movingTrackCountIgnoresStepSegments() {
        val timeline = Timeline(
            listOf(
                Track("moving", listOf(Keyframe(0, 0f), Keyframe(100, 1f))),
                Track("held", listOf(Keyframe(0, 0f, KeyframeMode.STEP), Keyframe(100, 1f))),
                Track("flat", listOf(Keyframe(0, 0.5f), Keyframe(100, 0.5f))),
            ),
        )
        assertEquals(1, timeline.movingTrackCountAt(50))
        assertEquals(0, timeline.movingTrackCountAt(500))
    }

    @Test
    fun timelineSegmentsReportPerTrackIndex() {
        val timeline = Timeline(
            listOf(
                Track("a", listOf(Keyframe(0, 0f), Keyframe(100, 1f), Keyframe(200, 0f))),
                Track("b", listOf(Keyframe(50, 0f), Keyframe(150, 1f))),
            ),
        )
        val segments = timeline.segmentsAt(60)
        assertEquals(0, segments.getValue("a"))
        assertEquals(0, segments.getValue("b"))
        assertEquals(-1, timeline.segmentsAt(10).getValue("b"))
        assertEquals(1, timeline.segmentsAt(160).getValue("a"))
    }

    @Test
    fun trackOfFindsByKey() {
        val timeline = Timeline(listOf(Track("only", listOf(Keyframe(0, 1f)))))
        assertEquals("only", timeline.trackOf("only")?.key)
        assertEquals(null, timeline.trackOf("missing"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun trackRejectsEmptyKeyframes() {
        Track("empty", emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun trackRejectsUnsortedKeyframes() {
        Track("bad", listOf(Keyframe(100, 1f), Keyframe(0, 0f)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun timelineRejectsDuplicateTrackKeys() {
        Timeline(
            listOf(
                Track("same", listOf(Keyframe(0, 0f))),
                Track("same", listOf(Keyframe(0, 1f))),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun timelineRejectsNoTracks() {
        Timeline(emptyList())
    }
}
