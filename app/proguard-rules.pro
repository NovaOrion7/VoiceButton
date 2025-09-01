# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Hide the original source file name.
-renamesourcefileattribute SourceFile

# Keep crash reporting attributes
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class kotlin.** { *; }
-dontwarn androidx.compose.**

# Keep our main classes
-keep class com.example.voicebutton.** { *; }
-keep class com.novaorion.volumecontrol.** { *; }

# Keep BuildConfig fields
-keep class com.novaorion.volumecontrol.BuildConfig { *; }
-keepclassmembers class com.novaorion.volumecontrol.BuildConfig {
    public static final boolean USE_REAL_ADS;
}

# Keep Service classes
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.appwidget.AppWidgetProvider

# Keep AudioManager related classes
-keep class android.media.** { *; }

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}