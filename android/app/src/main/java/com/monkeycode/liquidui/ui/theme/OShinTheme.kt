package com.monkeycode.liquidui.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * O神（OShin）风格主题 token —— 功能 F14。
 *
 * 提取自 suqi8/OShin 公开仓库的界面设计语言（其界面层基于 Miuix）：
 * 浅灰页面底 + 白色分组卡片、系统蓝强调色、大标题页头、分组小标题、
 * 悬浮胶囊底栏。**这是按其公开界面的视觉参数独立实现的 token 映射，
 * 没有复制 OShin 的 AGPL-3.0 源码**，来源与许可说明见仓库根 CREDITS.md。
 *
 * 关键参数出处（OShin 仓库 ui 层）：
 * - 底栏强调蓝：浅色 #0088FF / 深色 #0091FF（BottomTabs.kt），容器 #FAFAFA@40% / #121212@40%
 * - Miuix 默认：页面底 浅 #F7F7F7 / 深 #000000，卡片 浅 #FFFFFF / 深 #242424，主色 #3482FF
 * - 卡片：16dp 连续圆角、左右 16dp、组间 6dp（Card.kt CardDefaults）
 * - 页面：大标题 28sp Bold、分组小标题、正文 15-16sp 的 HyperOS 设置页排版
 */
object OShinTokens {
    // 强调蓝（开关、选中态、链接）
    val PrimaryLight = Color(0xFF3482FF)
    val PrimaryDark = Color(0xFF0091FF)
    val BottomAccentLight = Color(0xFF0088FF)
    val BottomAccentDark = Color(0xFF0091FF)

    // 页面与卡片
    val BackgroundLight = Color(0xFFF7F7F7)
    val BackgroundDark = Color(0xFF000000)
    val CardLight = Color(0xFFFFFFFF)
    val CardDark = Color(0xFF242424)
    val CardLowDark = Color(0xFF1C1C1E)

    // 文字
    val OnBackgroundLight = Color(0xFF1A1A1A)
    val OnBackgroundDark = Color(0xFFF5F5F7)
    val SecondaryLight = Color(0xFF8A8A8E)
    val SecondaryDark = Color(0xFF98989F)

    // 分隔与填充
    val OutlineLight = Color(0xFFE5E5EA)
    val OutlineDark = Color(0xFF38383A)
    val FillLight = Color(0xFFF2F2F7)
    val FillDark = Color(0xFF2C2C2E)

    // 底栏容器（半透明，采样/模糊在壳层处理）
    val BottomContainerLight = Color(0xFFFAFAFA).copy(alpha = 0.72f)
    val BottomContainerDark = Color(0xFF121212).copy(alpha = 0.72f)

    // 布局尺度（与 OShin 页面一致）
    val PagePadding = 16.dp
    val CardRadius = 16.dp
    val ControlRadius = 12.dp
    val CardVerticalSpacing = 6.dp
    val GroupSpacing = 24.dp
    val BottomBarSidePadding = 40.dp
    val BottomBarBottomPadding = 16.dp
    val BottomBarHeight = 64.dp
}

/** 把 Miuix/OShin 色板映射到本模板的 Material3 ColorScheme（切换即整页换色）。 */
fun oshinColorScheme(dark: Boolean): ColorScheme = if (dark) {
    darkColorScheme(
        primary = OShinTokens.PrimaryDark,
        onPrimary = Color.White,
        primaryContainer = Color(0xFF0A3D66),
        onPrimaryContainer = Color(0xFFD6ECFF),
        secondary = Color(0xFF64D2FF),
        onSecondary = Color(0xFF00394D),
        secondaryContainer = Color(0xFF1C313D),
        onSecondaryContainer = Color(0xFFC9E9F7),
        tertiary = Color(0xFFBF5AF2),
        background = OShinTokens.BackgroundDark,
        onBackground = OShinTokens.OnBackgroundDark,
        surface = OShinTokens.CardDark,
        onSurface = OShinTokens.OnBackgroundDark,
        surfaceContainer = OShinTokens.CardDark,
        surfaceContainerLow = OShinTokens.BackgroundDark,
        surfaceContainerLowest = Color(0xFF111113),
        surfaceContainerHigh = Color(0xFF2C2C2E),
        surfaceContainerHighest = Color(0xFF3A3A3C),
        surfaceVariant = OShinTokens.FillDark,
        onSurfaceVariant = OShinTokens.SecondaryDark,
        outline = Color(0xFF48484A),
        outlineVariant = OShinTokens.OutlineDark,
        error = Color(0xFFFF453A),
        onError = Color.White
    )
} else {
    lightColorScheme(
        primary = OShinTokens.PrimaryLight,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDCEBFF),
        onPrimaryContainer = Color(0xFF003A75),
        secondary = Color(0xFF007AFF),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE8F2FF),
        onSecondaryContainer = Color(0xFF003A75),
        tertiary = Color(0xFFAF52DE),
        background = OShinTokens.BackgroundLight,
        onBackground = OShinTokens.OnBackgroundLight,
        surface = OShinTokens.CardLight,
        onSurface = OShinTokens.OnBackgroundLight,
        surfaceContainer = OShinTokens.CardLight,
        surfaceContainerLow = OShinTokens.BackgroundLight,
        surfaceContainerLowest = Color.White,
        surfaceContainerHigh = Color(0xFFEFEFF4),
        surfaceContainerHighest = Color(0xFFE5E5EA),
        surfaceVariant = OShinTokens.FillLight,
        onSurfaceVariant = OShinTokens.SecondaryLight,
        outline = Color(0xFFC7C7CC),
        outlineVariant = OShinTokens.OutlineLight,
        error = Color(0xFFFF3B30),
        onError = Color.White
    )
}

/** OShin/Miuix 形状：卡片 16、控件 12、胶囊全圆。与模板玻璃形状（22/17）刻意区分。 */
val OShinShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

fun scaledOShinShapes(scale: Float): Shapes {
    val s = scale.coerceIn(0.5f, 2f)
    return Shapes(
        extraSmall = RoundedCornerShape(8.dp * s),
        small = RoundedCornerShape(12.dp * s),
        medium = RoundedCornerShape(16.dp * s),
        large = RoundedCornerShape(16.dp * s),
        extraLarge = RoundedCornerShape(24.dp * s)
    )
}

/** OShin/Miuix 排版：大标题更紧凑（页面标题 32），正文回到 15-16，分组标题 13。 */
val OShinTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,
        fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.4).sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp, lineHeight = 25.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 23.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 15.sp
    )
)
