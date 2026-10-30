package com.aditya.smartmedbox.core.firebase.datasource

import android.os.Build
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreFcmDataSource @Inject constructor(
    private val firestore: FirebaseFirestore?
) {

    suspend fun saveFcmToken(uid: String, token: String, deviceModel: String = Build.MODEL): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val docRef = db.collection("users").document(uid)
                .collection("fcmTokens").document(token)
            val data = hashMapOf(
                "token" to token,
                "deviceModel" to deviceModel,
                "platform" to "android",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFcmToken(uid: String, token: String): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection("users").document(uid)
                .collection("fcmTokens").document(token)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
