package com.monkeycode.liquidui.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.theme.AppIcons

/**
 * The four blank-feature destinations that make up the template shell.
 * Replace the screen bodies; keep this list to change the tab bar.
 *
 * 文案不写在这里：`titleRes` / `subtitleRes` 是 `@StringRes` id，实际文字在
 * `res/values/strings.xml` 的 `nav_*` 一组（10 条）。套壳改底栏文字只需改 XML，
 * 不必碰 Kotlin；渲染点用 `stringResource(screen.titleRes)` 解析。
 */
enum class Screen(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: ImageVector
) {
    Home(
        titleRes = R.string.nav_home,
        subtitleRes = R.string.nav_home_subtitle,
        icon = AppIcons.BlurOn
    ),
    Components(
        titleRes = R.string.nav_components,
        subtitleRes = R.string.nav_components_subtitle,
        icon = AppIcons.Widgets
    ),
    Motion(
        titleRes = R.string.nav_motion,
        subtitleRes = R.string.nav_motion_subtitle,
        icon = AppIcons.Animation
    ),
    Settings(
        titleRes = R.string.nav_settings,
        subtitleRes = R.string.nav_settings_subtitle,
        icon = AppIcons.Settings
    ),
    Gallery(
        titleRes = R.string.nav_gallery,
        subtitleRes = R.string.nav_gallery_subtitle,
        icon = AppIcons.Widgets
    )
}
