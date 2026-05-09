-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.lyra.aura.**$$serializer { *; }
-keepclassmembers class com.lyra.aura.** {
    *** Companion;
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Coil
-dontwarn coil.**

# App models
-keep class com.lyra.aura.model.** { *; }
