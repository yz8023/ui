package com.monkeycode.liquidui.ui.motion

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset

/**
 * 过渡用的中文别名 —— 让按 v2.9.x 及更早版本抄走的宿主代码不会立刻编译失败。
 *
 * 这里的每个名字都指向同包下的英文实现，**新代码一律不要用**。计划在下一个大版本
 * （v3.0）整体删除本文件；届时宿主只需做一次全局替换：
 *
 * | 旧名（中文） | 新名（英文） |
 * | --- | --- |
 * | `运动` | [Motion] |
 * | `交互高亮` | [InteractiveHighlight] |
 * | `阻尼拖拽动画` | [DampedDragAnimation] |
 * | `交错展示()` | [StaggeredReveal] |
 * | `Modifier.按压缩放()` | [pressScale] |
 * | `Modifier.冲击抖动()` | [impactShake] |
 * | `Modifier.闲置呼吸()` | [idleBreathing] |
 *
 * 为什么 [运动] 写成转发 object 而不是一行 `typealias 运动 = Motion`：
 * Kotlin 的 typealias 能转发**成员**（`运动.snappy(0.01f)` 可以编译），但不能作为
 * **嵌套 classifier 的限定符** —— `运动.Duration.Fast` 会报
 * `unresolved reference 'Duration'`（kotlinc 2.0.21 实测）。本仓库有 14 处这样的嵌套访问，
 * 所以只能把 Duration / Stagger / Follow 一并再暴露一次。
 */

@Deprecated("改用 Motion；中文名将在 v3.0 移除", ReplaceWith("Motion"))
object 运动 {

    val motionDamping: MutableFloatState get() = Motion.motionDamping

    fun DampedRatio(base: Float): Float = Motion.DampedRatio(base)

    /** 转发 [Motion.Duration]：嵌套 object 无法用 typealias 暴露。 */
    object Duration {
        const val Instant = Motion.Duration.Instant
        const val Fast = Motion.Duration.Fast
        const val Standard = Motion.Duration.Standard
        const val Medium = Motion.Duration.Medium
        const val Slow = Motion.Duration.Slow
        const val Cinematic = Motion.Duration.Cinematic
    }

    val EaseOutExpo: Easing get() = Motion.EaseOutExpo
    val EaseInExpo: Easing get() = Motion.EaseInExpo
    val EaseInOutQuart: Easing get() = Motion.EaseInOutQuart
    val EaseOutBack: Easing get() = Motion.EaseOutBack
    val EaseOutCirc: Easing get() = Motion.EaseOutCirc
    val EaseSharp: Easing get() = Motion.EaseSharp
    val EaseExpressive: Easing get() = Motion.EaseExpressive

    fun <T> gentle(visibilityThreshold: T? = null): FiniteAnimationSpec<T> =
        Motion.gentle(visibilityThreshold)

    fun <T> standard(visibilityThreshold: T? = null): FiniteAnimationSpec<T> =
        Motion.standard(visibilityThreshold)

    fun <T> snappy(visibilityThreshold: T? = null): FiniteAnimationSpec<T> =
        Motion.snappy(visibilityThreshold)

    fun <T> bouncy(visibilityThreshold: T? = null): FiniteAnimationSpec<T> =
        Motion.bouncy(visibilityThreshold)

    fun <T> heavy(visibilityThreshold: T? = null): FiniteAnimationSpec<T> =
        Motion.heavy(visibilityThreshold)

    fun <T> stiff(visibilityThreshold: T? = null): FiniteAnimationSpec<T> =
        Motion.stiff(visibilityThreshold)

    val Press: FiniteAnimationSpec<Float> get() = Motion.Press
    val TabSlide: FiniteAnimationSpec<Float> get() = Motion.TabSlide
    val PanelExpand: FiniteAnimationSpec<Float> get() = Motion.PanelExpand
    val HeroReveal: FiniteAnimationSpec<Float> get() = Motion.HeroReveal
    val OffsetSnap: FiniteAnimationSpec<IntOffset> get() = Motion.OffsetSnap

    /** 转发 [Motion.Stagger]。 */
    object Stagger {
        const val TightGrid = Motion.Stagger.TightGrid
        const val StandardList = Motion.Stagger.StandardList
        const val EditorialLine = Motion.Stagger.EditorialLine
        const val DramaticCascade = Motion.Stagger.DramaticCascade
    }

    /** 转发 [Motion.Follow]。 */
    object Follow {
        const val CursorTightness = Motion.Follow.CursorTightness
        const val CameraTightness = Motion.Follow.CameraTightness
        const val RailTightness = Motion.Follow.RailTightness
        const val LevelTightness = Motion.Follow.LevelTightness
    }

    const val CriticalDamping = Motion.CriticalDamping
}

@Deprecated("改用 InteractiveHighlight；中文名将在 v3.0 移除", ReplaceWith("InteractiveHighlight"))
typealias 交互高亮 = InteractiveHighlight

@Deprecated("改用 DampedDragAnimation；中文名将在 v3.0 移除", ReplaceWith("DampedDragAnimation"))
typealias 阻尼拖拽动画 = DampedDragAnimation

@Deprecated(
    "改用 pressScale；中文名将在 v3.0 移除",
    ReplaceWith("pressScale(interactionSource, pressedScale, enabled)")
)
fun Modifier.按压缩放(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.94f,
    enabled: Boolean = true
): Modifier = pressScale(interactionSource, pressedScale, enabled)

@Deprecated(
    "改用 impactShake；中文名将在 v3.0 移除",
    ReplaceWith("impactShake(trigger, amplitude, durationMs)")
)
fun Modifier.冲击抖动(
    trigger: Any?,
    amplitude: Float = 10f,
    durationMs: Int = 300
): Modifier = impactShake(trigger, amplitude, durationMs)

@Deprecated(
    "改用 idleBreathing；中文名将在 v3.0 移除",
    ReplaceWith("idleBreathing(periodMs, amplitudeX, amplitudeY)")
)
@Composable
fun Modifier.闲置呼吸(
    periodMs: Int = 5600,
    amplitudeX: Float = 0.012f,
    amplitudeY: Float = 0.020f
): Modifier = idleBreathing(periodMs, amplitudeX, amplitudeY)

@Deprecated(
    "改用 StaggeredReveal；中文名将在 v3.0 移除",
    ReplaceWith("StaggeredReveal(index, modifier, enabled, staggerMs, startDelayMs, translateYDp, content)")
)
@Composable
fun 交错展示(
    index: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    staggerMs: Int = Motion.Stagger.StandardList,
    startDelayMs: Int = 60,
    translateYDp: Int = 18,
    content: @Composable () -> Unit
) = StaggeredReveal(index, modifier, enabled, staggerMs, startDelayMs, translateYDp, content)
