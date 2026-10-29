package com.aditya.smartmedbox.core.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseApp(@ApplicationContext context: Context): FirebaseApp? {
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
                .setPersistenceEnabled(true)
                .build()
            firestore
        } catch (e: Exception) {
            Timber.w(e, "FirebaseFirestore instance could not be retrieved")
            null
        }
    }
}
