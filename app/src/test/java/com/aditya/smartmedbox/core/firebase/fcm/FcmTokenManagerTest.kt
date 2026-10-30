package com.aditya.smartmedbox.core.firebase.fcm

import com.aditya.smartmedbox.core.firebase.datasource.FirestoreFcmDataSource
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.auth.domain.model.User
import com.aditya.smartmedbox.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FcmTokenManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val firestoreFcmDataSource: FirestoreFcmDataSource = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private val dispatchers = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
        override val unconfined = testDispatcher
    }

    private lateinit var tokenManager: FcmTokenManager

    @Before
    fun setUp() {
        every { authRepository.getAuthState() } returns flowOf(
            User(
                uid = "test_user_123",
                email = "eleanor@example.com",
                displayName = "Eleanor"
            )
        )
        tokenManager = FcmTokenManager(
            firestoreFcmDataSource = firestoreFcmDataSource,
            authRepository = authRepository,
            dispatchers = dispatchers
        )
    }

    @Test
    fun `onNewToken saves token to Firestore when user is authenticated`() = runTest(testDispatcher) {
        val testToken = "fcm_token_xyz_456"
        coEvery { firestoreFcmDataSource.saveFcmToken("test_user_123", testToken) } returns Result.success(Unit)

        tokenManager.onNewToken(testToken)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(atLeast = 1) { firestoreFcmDataSource.saveFcmToken("test_user_123", testToken, any()) }
    }

    @Test
    fun `syncCurrentToken returns success when no user is authenticated`() = runTest(testDispatcher) {
        every { authRepository.getAuthState() } returns flowOf(null)
        val unauthenticatedManager = FcmTokenManager(
            firestoreFcmDataSource = firestoreFcmDataSource,
            authRepository = authRepository,
            dispatchers = dispatchers
        )

        val result = unauthenticatedManager.syncCurrentToken()
        assertTrue(result.isSuccess)
    }
}
