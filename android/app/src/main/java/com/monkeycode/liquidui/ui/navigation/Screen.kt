package com.monkeycode.liquidui.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.monkeycode.liquidui.ui.theme.AppIcons

/**
 * The four blank-feature destinations that make up the template shell.
 * Replace the screen bodies; keep this list to change the tab bar.
 */
enum class Screen(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    Home(
        title = "首页",
        subtitle = "设计系统概览",
        icon = AppIcons.BlurOn
    ),
    Components(
        title = "组件",
        subtitle = "控件与状态",
        icon = AppIcons.Widgets
    ),
    Motion(
        title = "动效",
        subtitle = "交互物理演示",
        icon = AppIcons.Animation
    ),
    Settings(
        title = "设置",
        subtitle = "主题与偏好",
        icon = AppIcons.Settings
    )
}
