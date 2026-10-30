package com.aditya.smartmedbox.feature.device.domain.usecase

import app.cash.turbine.test
import com.aditya.smartmedbox.feature.device.domain.model.SmartBoxDevice
import com.aditya.smartmedbox.feature.device.domain.repository.DeviceRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveDeviceUseCaseTest {

    private val repository: DeviceRepository = mockk()
    private lateinit var useCase: ObserveDeviceUseCase

    @Before
    fun setUp() {
        useCase = ObserveDeviceUseCase(repository)
    }

    @Test
    fun `invoke emits device from repository`() = runTest {
        val testDevice = SmartBoxDevice(
            deviceId = "DG-4081",
            serialNumber = "#DG-4081",
            modelName = "Apothecary Smart Box",
            isPaired = true
        )
        every { repository.observeDiscoveredDevice() } returns flowOf(testDevice)

        useCase().test {
            val item = awaitItem()
            assertEquals("DG-4081", item?.deviceId)
            assertEquals("Apothecary Smart Box", item?.modelName)
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits null when device is not paired`() = runTest {
        every { repository.observeDiscoveredDevice() } returns flowOf(null)

        useCase().test {
            val item = awaitItem()
            assertEquals(null, item)
            awaitComplete()
        }
    }
}
