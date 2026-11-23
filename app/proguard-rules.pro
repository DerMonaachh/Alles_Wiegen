# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve Line Numbers and Source File for crash report stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Vico Charting Library
-keep class com.patrykandpatrick.vico.** { *; }

# Room Database Entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# Google Play Services Code Scanner & MLKit
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.tasks.** { *; }
