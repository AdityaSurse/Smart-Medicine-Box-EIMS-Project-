package com.aditya.smartmedbox.feature.auth.data.repository

import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.auth.domain.model.User
import com.aditya.smartmedbox.feature.auth.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth?,
    private val dispatchers: DispatcherProvider
) : AuthRepository {

    // Fallback in-memory state for offline execution when Firebase credentials are not supplied
    private val fallbackUser = MutableStateFlow<User?>(
        User(
            uid = "user_eleanor_8820",
            email = "eleanor.vance@apothecary.care",
            displayName = "Eleanor Vance",
            linkedDeviceId = "DG-4081"
        )
    )

    override fun getAuthState(): Flow<User?> {
        val auth = firebaseAuth ?: return fallbackUser.asStateFlow()

        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { currentAuth ->
                val user = currentAuth.currentUser?.toDomain()
                trySend(user)
            }
            auth.addAuthStateListener(listener)
            trySend(auth.currentUser?.toDomain())
            awaitClose { auth.removeAuthStateListener(listener) }
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return withContext(dispatchers.io) {
            val auth = firebaseAuth
            if (auth == null) {
                // Offline fallback
                val user = User(
                    uid = "user_${email.hashCode()}",
                    email = email,
                    displayName = email.substringBefore("@").replace(".", " ").capitalizeWords(),
                    linkedDeviceId = "DG-4081"
                )
                fallbackUser.value = user
                return@withContext Result.success(user)
            }

            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val user = authResult.user?.toDomain()
                    ?: error("Firebase returned a null authenticated user")
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun signUp(email: String, password: String): Result<User> {
        return withContext(dispatchers.io) {
            val auth = firebaseAuth
            if (auth == null) {
                // Offline fallback
                val user = User(
                    uid = "user_new_${email.hashCode()}",
                    email = email,
                    displayName = email.substringBefore("@").replace(".", " ").capitalizeWords(),
                    linkedDeviceId = "DG-4081"
                )
                fallbackUser.value = user
                return@withContext Result.success(user)
            }

            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user?.toDomain()
                    ?: error("Firebase user creation failed")
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return withContext(dispatchers.io) {
            val auth = firebaseAuth
            if (auth == null) {
                // Offline fallback
                val user = User(
                    uid = "user_google_10294",
                    email = "eleanor.vance@gmail.com",
                    displayName = "Eleanor Vance",
                    linkedDeviceId = "DG-4081"
                )
                fallbackUser.value = user
                return@withContext Result.success(user)
            }

            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user?.toDomain()
                    ?: error("Google authentication returned empty user")
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return withContext(dispatchers.io) {
            val auth = firebaseAuth
            if (auth != null) {
                auth.signOut()
            } else {
                fallbackUser.value = null
            }
            Result.success(Unit)
        }
    }

    private fun FirebaseUser.toDomain(): User {
        return User(
            uid = uid,
            email = email ?: "",
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            linkedDeviceId = "DG-4081"
        )
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}
