package com.aditya.smartmedbox.core.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseApp(): FirebaseApp? {
        return try {
            FirebaseApp.getInstance()
        } catch (e: Exception) {
            Timber.w(e, "FirebaseApp not initialized via google-services.json; running in offline/local mode")
            null
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(firebaseApp: FirebaseApp?): FirebaseAuth? {
        if (firebaseApp == null) return null
        return try {
            FirebaseAuth.getInstance(firebaseApp)
        } catch (e: Exception) {
            Timber.w(e, "FirebaseAuth instance could not be retrieved")
            null
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(firebaseApp: FirebaseApp?): FirebaseFirestore? {
        if (firebaseApp == null) return null
        return try {
            val firestore = FirebaseFirestore.getInstance(firebaseApp)
            firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            firestore
        } catch (e: Exception) {
            Timber.w(e, "FirebaseFirestore instance could not be retrieved")
            null
        }
    }
}
