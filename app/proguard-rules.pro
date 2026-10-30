# R8 / Proguard rules for Smart Medicine Box

# Keep Kotlinx Serialization & Attributes
-keepattributes *Annotation*,InnerClasses,Signature,EnclosingMethod
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Navigation Routes (Serializable)
-keep @kotlinx.serialization.Serializable class com.aditya.smartmedbox.navigation.** { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Keep Firebase Firestore DTOs & Models
-keep class com.aditya.smartmedbox.core.firebase.dto.** { *; }
-keepclassmembers class com.aditya.smartmedbox.core.firebase.dto.** {
    <fields>;
    <init>(...);
}

# Keep Domain Models
-keep class com.aditya.smartmedbox.feature.**.domain.model.** { *; }

# Keep Hilt & Dagger
-keep class * extends dagger.hilt.internal.GeneratedComponentManager
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}

# Keep WorkManager Workers
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Compose Platform rules
-keep class androidx.compose.ui.platform.** { *; }
-dontwarn androidx.compose.**

# Coroutines rules
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { *; }
