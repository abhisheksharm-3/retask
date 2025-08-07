# This file contains ProGuard/R8 rules for the ReTask application.
# These rules are essential when isMinifyEnabled is set to true for release builds.

# Keep this for better debugging of crashes in your release app. It preserves
# line numbers in stack traces, which is incredibly helpful.
-keepattributes SourceFile,LineNumberTable

#================================================================================
# Kotlin Coroutines
#================================================================================
# This rule preserves debugging metadata for coroutines, which helps in analyzing
# stack traces from crashes involving suspend functions.
-keep
-keepclassmembernames class kotlinx.coroutines.internal.MainDispatcherFactory {
    private final java.lang.String errorMessage;
}


#================================================================================
# Jetpack Compose
#================================================================================
# These are the standard, recommended rules for Jetpack Compose. They prevent R8
# from removing composable functions and other code that the Compose compiler
# relies on at runtime. Without these, your Compose UI will likely crash.
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keep class androidx.compose.runtime.Composer
-keep class androidx.compose.runtime.Recomposer
-keep class androidx.compose.runtime.Composition
-keep class androidx.compose.runtime.ComposerKt
-keepclassmembers class androidx.compose.runtime.internal.ComposableLambda {
    <methods>;
}
-keepclassmembers class androidx.compose.runtime.internal.ComposableLambdaImpl {
    <methods>;
}
-keepclassmembers class androidx.compose.runtime.internal.ComposableLambdaN {
    <methods>;
}

#================================================================================
# Downloadable Google Fonts
#================================================================================
# This rule is CRUCIAL. It prevents R8's resource shrinker from removing the
# security certificates required to download fonts from Google Fonts.
# NOTE: The IDE may show a "matches no class members" warning on the rule below.
# This is a known issue and a false positive. This rule is necessary and must be kept.
-keep class com.google.android.gms.fonts.**
-keep class in.xroden.retask.R$array {
    public static final int com_google_android_gms_fonts_certs;
}

#================================================================================
# Data Models (Room Entities)
#================================================================================
# This rule ensures that your data classes (like `Task.kt`) are not altered
# in a way that breaks Room's ability to map database columns to class properties.
# It keeps the class and all its members, preventing them from being removed or renamed.
-keep class in.xroden.retask.data.model.** { *; }