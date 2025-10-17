############################################################
# 🌟 Base Rules
############################################################
# Keep annotations, signatures, and metadata
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

# Keep Kotlin metadata (for reflection & coroutines)
-keepclassmembers class kotlin.Metadata { *; }

# Keep generic type information for Room, Gson, etc.
-keepattributes Exceptions, Signature, InnerClasses, EnclosingMethod

# Do not warn about Kotlin internal classes
-dontwarn kotlin.**
-dontwarn kotlinx.**
-dontwarn org.jetbrains.annotations.**

############################################################
# 🧠 ML Kit / Google ML / Vision / Text Recognition
############################################################
-keep class com.google.mlkit.** { *; }
-keep interface com.google.mlkit.** { *; }

# Play Services MLKit dependencies (OCR)
-keep class com.google.android.gms.internal.mlkit_vision_text_common.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_common.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_id.** { *; }
-keep class com.google.android.gms.vision.text.** { *; }

# Text Recognition (Chinese, Japanese, Korean, Devanagari)
-keep class com.google.android.gms.internal.mlkit_vision_text_chinese.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_japanese.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_korean.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_devanagari.** { *; }

############################################################
# 📄 ML Kit Document Scanner
############################################################
-keep class com.google.android.gms.internal.mlkit_vision_documentscanner.** { *; }
-keep class com.google.mlkit.vision.documentscanner.** { *; }
-dontwarn com.google.mlkit.vision.documentscanner.**

############################################################
# 🔍 Code Scanner (Google Play Services)
############################################################
-keep class com.google.android.gms.codescanner.** { *; }
-keep class com.google.android.gms.internal.codescanner.** { *; }

############################################################
# 🔥 Firebase (Analytics + Messaging)
############################################################
-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keepattributes Signature, *Annotation*

# Keep FirebaseMessagingService subclass (your service)
-keep class com.bugbd.pdfocr.notification.NotificationSystem {*;}

############################################################
# 🏠 AndroidX / Lifecycle / ViewModel / LiveData
############################################################
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel { *; }

############################################################
# 🧱 Room Database
############################################################
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
############################################################
# 💫 Lottie Animations
############################################################
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

############################################################
# 🌀 Coil Image Loading
############################################################
-dontwarn coil.**
-keep class coil.** { *; }

############################################################
# 📱 Navigation Components
############################################################
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**


# Retain Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Retain your Activities, Fragments, Services, and BroadcastReceivers
-keep class * extends android.app.Activity { *; }
-keep class * extends androidx.fragment.app.Fragment { *; }
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends android.app.Application { *; }

############################################################
# ✅ Final Optimization
############################################################
-dontwarn com.google.**
