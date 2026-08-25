############################################################
# HD Doc Scanner - R8 / ProGuard Rules
############################################################

############################################################
# Kotlin metadata / generic signatures
############################################################

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod


############################################################
# App classes
############################################################

# Keep app models if fields/classes are accessed through
# reflection or serialization.
-keep class com.bugbd.pdfocr.model.** { *; }

# Keep helper/local classes only if accessed through reflection.
-keep class com.bugbd.pdfocr.helper.** { *; }
-keep class com.bugbd.pdfocr.local_bd.** { *; }


############################################################
# Kotlin
############################################################

-keepclassmembers class kotlin.Metadata {
    *;
}

-dontwarn kotlin.**
-dontwarn kotlinx.**
-dontwarn org.jetbrains.annotations.**


############################################################
# Room
############################################################

# Room normally supplies its own R8 rules.
# Keep only if your entities/DAO implementations are accessed
# reflectively in your project.

-keep class * extends androidx.room.RoomDatabase { *; }

-keep @androidx.room.Entity class * { *; }

-keepclassmembers class * {
    @androidx.room.PrimaryKey <fields>;
    @androidx.room.ColumnInfo <fields>;
}


############################################################
# Firebase
############################################################

# Firebase libraries generally provide their own R8 rules.

# Your custom FirebaseMessagingService
-keep class com.bugbd.pdfocr.notification.NotificationSystem {
    *;
}


############################################################
# Google ML Kit
############################################################

# ML Kit libraries provide required R8 rules themselves.
# No blanket -keep rules are normally required here.


############################################################
# Google Code Scanner
############################################################

# No blanket keep rules required.


############################################################
# Google Document Scanner
############################################################

# No blanket keep rules required.


############################################################
# AdMob / Google Mobile Ads
############################################################

# Google Mobile Ads SDK provides its own R8 configuration.
# Do NOT keep the entire ads package manually.


############################################################
# Glide
############################################################

# Glide provides its own consumer ProGuard/R8 rules.

-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule

############################################################
# Lottie
############################################################

# Lottie provides required rules.
# No blanket keep required.


############################################################
# Coil
############################################################

# Coil generally does not require keeping the entire package.

-dontwarn coil.**


############################################################
# AndroidX Navigation
############################################################

# AndroidX provides required rules.
# No blanket keep required.

-dontwarn androidx.navigation.**


############################################################
# Dots Indicator
############################################################

# Usually no keep rule required unless your app accesses
# classes through reflection.

############################################################
# Parcelable
############################################################

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}


############################################################
# Application / Activity / Fragment
############################################################

# Android components referenced from AndroidManifest are
# automatically handled by R8/AGP.
# Do NOT keep every Activity/Fragment unless required.


############################################################
# Warnings
############################################################

-dontwarn okio.**