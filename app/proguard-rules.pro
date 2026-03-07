# Add project specific ProGuard rules here.
-keep class com.miruai.app.data.api.** { *; }
-keep class com.miruai.app.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
