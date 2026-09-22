# LiquidUI — ProGuard rules for release build

# Keep application class and entry points
-keep class com.monkeycode.liquidui.** { *; }
-keep class com.monkeycode.liquidui.MainActivity { *; }

# Compose — keep all @Composable functions and their metadata
-keep class * extends androidx.compose.runtime.ComposeVersion
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep Compose generated code and adapters
-keep class **.Adapter { *; }
-keep class **.Composer { *; }

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep data classes used in Parcelable / Serializable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep classes referenced from XML layouts
-keep class * extends android.view.View { *; }

# Keep Backdrop library
-keep class com.kyant.** { *; }

# Keep Shapes library
-keep class io.github.kyant0.** { *; }

# General Android
-dontwarn android.**
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
