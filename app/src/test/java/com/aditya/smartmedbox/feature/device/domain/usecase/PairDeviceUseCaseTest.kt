package com.aditya.smartmedbox.feature.device.domain.usecase

import com.aditya.smartmedbox.feature.device.domain.repository.DeviceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PairDeviceUseCaseTest {

    private val repository: DeviceRepository = mockk()
    private lateinit var useCase: PairDeviceUseCase

    @Before
    fun setUp() {
        useCase = PairDeviceUseCase(repository)
    }

    @Test
    fun `invoke with valid credentials calls repository pairDevice and succeeds`() = runTest {
        coEvery { repository.pairDevice("DG-4081", "8820") } returns Result.success(Unit)

        val result = useCase("DG-4081", "8820")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.pairDevice("DG-4081", "8820") }
    }

    @Test
    fun `invoke with blank deviceId returns failure without calling repository`() = runTest {
        val result = useCase("   ", "8820")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.pairDevice(any(), any()) }
    }

    @Test
    fun `invoke when repository fails returns failure`() = runTest {
        coEvery { repository.pairDevice("DG-4081", "8820") } throws RuntimeException("Network error")

        val result = useCase("DG-4081", "8820")

        assertTrue(result.isFailure)
    }
}
