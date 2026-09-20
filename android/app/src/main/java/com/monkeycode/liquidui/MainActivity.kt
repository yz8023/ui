package com.monkeycode.liquidui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.monkeycode.liquidui.ui.motion.运动
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
            SideEffect { 运动.motionDamping.floatValue = settings.motionDamping }
            LiquidUITheme(
                themeMode = settings.themeMode,
                visualMode = settings.visualMode,
                palette = settings.palette,
                customSeed = Color(settings.customSeed),
                customShade = settings.customShade,
                chrome = AppChrome(
                    visualMode = settings.visualMode,
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
