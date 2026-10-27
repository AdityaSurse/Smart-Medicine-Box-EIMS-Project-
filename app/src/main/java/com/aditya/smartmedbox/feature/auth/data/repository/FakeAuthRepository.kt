package com.aditya.smartmedbox.feature.auth.data.repository

import com.aditya.smartmedbox.feature.auth.domain.model.User
import com.aditya.smartmedbox.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {
    private val _currentUser = MutableStateFlow<User?>(
        User(uid = "user_eleanor_8820", email = "eleanor.vance@apothecary.care", linkedDeviceId = "DG-4081")
    )

    override fun getAuthState(): Flow<User?> = _currentUser.asStateFlow()

    override suspend fun signIn(email: String, password: String): Result<User> {
        delay(500)
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        val user = User(uid = "user_${email.hashCode()}", email = email, linkedDeviceId = "DG-4081")
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signUp(email: String, password: String): Result<User> {
        delay(600)
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        val user = User(uid = "user_new_${email.hashCode()}", email = email, linkedDeviceId = null)
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signOut(): Result<Unit> {
        _currentUser.value = null
        return Result.success(Unit)
    }
}
