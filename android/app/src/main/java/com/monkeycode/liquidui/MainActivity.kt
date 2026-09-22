package com.monkeycode.liquidui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Color
import com.monkeycode.liquidui.ui.motion.运动
import com.monkeycode.liquidui.ui.navigation.AppRoot
import com.monkeycode.liquidui.ui.screens.rememberAppSettingsState
import com.monkeycode.liquidui.ui.screens.欢迎引导屏幕
import com.monkeycode.liquidui.ui.theme.AppChrome
import com.monkeycode.liquidui.ui.theme.LiquidUITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings = rememberAppSettingsState()
            SideEffect { 运动.motionDamping.floatValue = settings.motionDamping }
            val onboardingCompleted by settings.onboardingCompletedFlow
                .collectAsStateWithLifecycle(initialValue = settings.onboardingCompleted)
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
                if (!onboardingCompleted) {
                    欢迎引导屏幕(onFinish = settings::completeOnboarding)
                } else {
                    AppRoot(settings = settings)
                }
            }
        }
    }
}
