package com.monkeycode.liquidui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import com.monkeycode.liquidui.ui.motion.Motion
import com.monkeycode.liquidui.ui.navigation.AppRoot
import com.monkeycode.liquidui.ui.screens.rememberAppSettingsState
import com.monkeycode.liquidui.ui.theme.AppChrome
import com.monkeycode.liquidui.ui.theme.LiquidUITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings = rememberAppSettingsState()
            SideEffect { Motion.motionDamping.floatValue = settings.motionDamping }
            LiquidUITheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor,
                palette = settings.palette,
                chrome = AppChrome(
                    glassEnabled = settings.glassEnabled,
                    glassOpacity = settings.glassOpacity,
                    glassRefraction = settings.glassRefraction,
                    cornerScale = settings.cornerScale,
                    reducedMotion = settings.reducedMotion,
                    motionDamping = settings.motionDamping,
                    wallpaperBackground = settings.wallpaperBackground
                )
            ) {
                AppRoot(settings = settings)
            }
        }
    }
}
