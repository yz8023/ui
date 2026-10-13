# LiquidUI release rules.
# Compose, AndroidX, backdrop-android and shapes publish consumer rules and do not
# require broad -keep directives. Keeping the whole app/library would defeat R8.

# Preserve generic signatures and runtime annotations used by Android libraries.
-keepattributes Signature,*Annotation*

# org.json is supplied by Android at runtime.
-dontwarn org.json.**
