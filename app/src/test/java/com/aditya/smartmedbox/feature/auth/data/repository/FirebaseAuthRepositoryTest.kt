package com.aditya.smartmedbox.feature.auth.data.repository

import com.aditya.smartmedbox.core.util.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAuthRepositoryTest {

    private lateinit var testDispatcher: CoroutineDispatcher
    private lateinit var dispatcherProvider: DispatcherProvider
    private lateinit var repository: FirebaseAuthRepository

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        dispatcherProvider = object : DispatcherProvider {
            override val main: CoroutineDispatcher = testDispatcher
            override val io: CoroutineDispatcher = testDispatcher
            override val default: CoroutineDispatcher = testDispatcher
            override val unconfined: CoroutineDispatcher = testDispatcher
        }
        // Test repository with null Firebase (offline fallback mode)
        repository = FirebaseAuthRepository(
            firebaseAuth = null,
            dispatchers = dispatcherProvider
        )
    }

    @Test
    fun getAuthState_initiallyReturnsDefaultOfflineUser() = runTest {
        val user = repository.getAuthState().first()
        assertNotNull(user)
        assertEquals("user_eleanor_8820", user?.uid)
        assertEquals("eleanor.vance@apothecary.care", user?.email)
    }

    @Test
    fun signIn_updatesUserSession() = runTest {
        val result = repository.signIn("patient@medbox.care", "securePass123")
        assertTrue(result.isSuccess)
        val user = repository.getAuthState().first()
        assertEquals("patient@medbox.care", user?.email)
    }

    @Test
    fun signUp_createsNewUser() = runTest {
        val result = repository.signUp("newuser@medbox.care", "securePass123")
        assertTrue(result.isSuccess)
        val user = repository.getAuthState().first()
        assertEquals("newuser@medbox.care", user?.email)
    }

    @Test
    fun signInWithGoogle_authenticatesSuccessfully() = runTest {
        val result = repository.signInWithGoogle("mock_token")
        assertTrue(result.isSuccess)
        val user = repository.getAuthState().first()
        assertEquals("eleanor.vance@gmail.com", user?.email)
    }

    @Test
    fun signOut_clearsUserSession() = runTest {
        val result = repository.signOut()
        assertTrue(result.isSuccess)
        val user = repository.getAuthState().first()
        assertNull(user)
    }
}

