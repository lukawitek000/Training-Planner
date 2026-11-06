# ProGuard / R8 Configuration for Training Planner

# ----------------------------------------------------------------------------------
# Crash Reporting & Stack Trace Deobfuscation
# ----------------------------------------------------------------------------------
# Preserve source line numbers in crash stack traces so Google Play Console
# and local deobfuscation tools can trace crashes back to exact Kotlin source lines.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve runtime annotations and generic signatures essential for reflection/serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# ----------------------------------------------------------------------------------
# Kotlinx Serialization
# ----------------------------------------------------------------------------------
# Preserve Companion objects, serializer generation methods, and synthetic constructors for @Serializable classes
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    <fields>;
    <methods>;
}
-keepclassmembers class * {
    synthetic <init>(int, ...);
}

# Preserve shared DTO models (allowing shrinking and obfuscation)
-keep,allowobfuscation,allowshrinking class com.lukasz.witkowski.training.planner.dto.** { *; }

# Preserve Navigation 3 NavKey implementations and custom serializers
# Required for polymorphic serialization in rememberTrainingPlannerNavBackStack
# Keep class names intact to prevent deserialization crashes across app updates
-keepnames class com.lukasz.witkowski.training.planner.navigation.** implements androidx.navigation3.runtime.NavKey
-keepclassmembers class com.lukasz.witkowski.training.planner.navigation.** implements androidx.navigation3.runtime.NavKey { *; }
-keep class com.lukasz.witkowski.training.planner.navigation.*Serializer { *; }
-keep class androidx.navigation3.** { *; }

# ----------------------------------------------------------------------------------
# Retrofit 3 & OkHttp 5 (:network)
# ----------------------------------------------------------------------------------
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# ----------------------------------------------------------------------------------
# Room 3 (androidx.room3)
# ----------------------------------------------------------------------------------
-keep class * extends androidx.room3.RoomDatabase
-keep @androidx.room3.Entity class * { *; }
-keepclassmembers class * {
    @androidx.room3.ColumnTypeConverter <methods>;
    @androidx.room3.DaoReturnTypeConverters <methods>;
}
-keep class com.lukasz.witkowski.training.planner.exercise.infrastructure.Converters { *; }
-keep class com.lukasz.witkowski.training.planner.training.infrastructure.Converters { *; }
-keep class com.lukasz.witkowski.training.planner.training.infrastructure.InstantConverters { *; }

# ----------------------------------------------------------------------------------
# Koin DI
# ----------------------------------------------------------------------------------
-keepclassmembers class * {
    @org.koin.core.annotation.* <init>(...);
}
-dontwarn org.koin.**

# ----------------------------------------------------------------------------------
# Third-party UI Libraries
# ----------------------------------------------------------------------------------
-dontwarn com.bytebeats.charts.**
-dontwarn org.calvin.reorderable.**
-dontwarn coil.**
