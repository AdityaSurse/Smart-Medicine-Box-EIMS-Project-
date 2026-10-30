package com.aditya.smartmedbox.core.firebase.fcm

import com.aditya.smartmedbox.core.firebase.datasource.FirestoreFcmDataSource
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.auth.domain.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

@Singleton
class FcmTokenManager @Inject constructor(
    private val firestoreFcmDataSource: FirestoreFcmDataSource,
    private val authRepository: AuthRepository,
    private val dispatchers: DispatcherProvider
) {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private var currentCachedToken: String? = null

    init {
        scope.launch {
            authRepository.getAuthState().collect { user ->
                if (user != null) {
                    val token = currentCachedToken ?: fetchFcmTokenSafely()
                    if (token != null) {
                        syncTokenWithFirestore(user.uid, token)
                    }
                }
            }
        }
    }

    fun onNewToken(token: String) {
        currentCachedToken = token
        scope.launch {
            val user = authRepository.getAuthState().firstOrNull()
            if (user != null) {
                syncTokenWithFirestore(user.uid, token)
            }
        }
    }

    suspend fun syncCurrentToken(): Result<Unit> {
        val user = authRepository.getAuthState().firstOrNull() ?: return Result.success(Unit)
        val token = currentCachedToken ?: fetchFcmTokenSafely() ?: return Result.success(Unit)
        return firestoreFcmDataSource.saveFcmToken(user.uid, token)
    }

    private suspend fun syncTokenWithFirestore(uid: String, token: String) {
        val result = firestoreFcmDataSource.saveFcmToken(uid, token)
        if (result.isSuccess) {
            Timber.d("Successfully synchronized FCM token with Firestore for user $uid")
        } else {
            Timber.w(result.exceptionOrNull(), "Failed to sync FCM token with Firestore")
        }
    }

    private suspend fun fetchFcmTokenSafely(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            currentCachedToken = token
            token
        } catch (e: Exception) {
            Timber.w(e, "FirebaseMessaging.getInstance().token failed (e.g. offline/no Google Play Services)")
            null
        }
    }
}
