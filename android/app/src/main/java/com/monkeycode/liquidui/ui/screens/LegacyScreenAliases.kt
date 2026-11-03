package com.monkeycode.liquidui.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 过渡用的中文页面别名 —— 与 `ui/motion/LegacyMotionAliases.kt` 同一批，
 * 让按 v2.9.x 及更早版本抄走的宿主代码不会立刻编译失败。
 *
 * **新代码一律不要用**，计划在 v3.0 整体删除本文件。届时宿主做一次全局替换即可：
 *
 * | 旧名（中文） | 新名（英文） |
 * | --- | --- |
 * | `素材浏览器()` | [MaterialBrowser] |
 * | `运动屏幕()` | [MotionScreen] |
 * | `欢迎引导屏幕()` | [WelcomeGuideScreen] |
 *
 * 注意别和 [OnboardingScreen] 混了：那是 F07 的首次引导页，[WelcomeGuideScreen] 是
 * 展示 App 自带的欢迎/免责声明页，两者一直是两个不同的页面。
 */

@Deprecated(
    "改用 MaterialBrowser；中文名将在 v3.0 移除",
    ReplaceWith("MaterialBrowser(onOpenFeature)")
)
@Composable
fun 素材浏览器(onOpenFeature: (FeatureTarget) -> Unit) = MaterialBrowser(onOpenFeature)

@Deprecated(
    "改用 MotionScreen；中文名将在 v3.0 移除",
    ReplaceWith("MotionScreen(settings)")
)
@Composable
fun 运动屏幕(settings: AppSettingsState) = MotionScreen(settings)

@Deprecated(
    "改用 WelcomeGuideScreen；中文名将在 v3.0 移除",
    ReplaceWith("WelcomeGuideScreen(onFinish, modifier)")
)
@Composable
fun 欢迎引导屏幕(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) = WelcomeGuideScreen(onFinish = onFinish, modifier = modifier)
