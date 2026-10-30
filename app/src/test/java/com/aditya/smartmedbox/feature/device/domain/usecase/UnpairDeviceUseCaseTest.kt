package com.aditya.smartmedbox.feature.device.domain.usecase

import com.aditya.smartmedbox.feature.device.domain.repository.DeviceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UnpairDeviceUseCaseTest {

    private val repository: DeviceRepository = mockk()
    private lateinit var useCase: UnpairDeviceUseCase

    @Before
    fun setUp() {
        useCase = UnpairDeviceUseCase(repository)
    }

    @Test
    fun `invoke with valid deviceId calls repository unpairDevice and succeeds`() = runTest {
        coEvery { repository.unpairDevice("DG-4081") } returns Result.success(Unit)

        val result = useCase("DG-4081")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.unpairDevice("DG-4081") }
    }

    @Test
    fun `invoke with blank deviceId returns failure without calling repository`() = runTest {
        val result = useCase("   ")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.unpairDevice(any()) }
    }

    @Test
    fun `invoke when repository fails returns failure`() = runTest {
        coEvery { repository.unpairDevice("DG-4081") } throws RuntimeException("Firestore error")

        val result = useCase("DG-4081")

        assertTrue(result.isFailure)
    }
}
