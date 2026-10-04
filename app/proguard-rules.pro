# Project-specific R8 rules. Most libraries (Room, WorkManager, Firebase, AdMob, OkHttp,
# Koin, Compose) ship their own consumer rules; only the gaps are covered here.

# Keep line numbers so Crashlytics stack traces stay readable. The Crashlytics Gradle
# plugin uploads the mapping file on release builds to de-obfuscate them.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Retrofit 2.9 --------------------------------------------------------------
# 2.9.0 predates the rules needed under R8 full mode (AGP 8 default): without these,
# suspend API methods lose their generic signatures and fail at runtime. Taken from
# Retrofit's own retrofit2.pro (2.10+); can be dropped after upgrading Retrofit.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# --- TensorFlow Lite -----------------------------------------------------------
# The interpreter is driven from native code via JNI, which looks classes up by name.
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.gpu.**
