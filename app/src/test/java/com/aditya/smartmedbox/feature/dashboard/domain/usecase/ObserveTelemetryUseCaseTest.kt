package com.aditya.smartmedbox.feature.dashboard.domain.usecase

import app.cash.turbine.test
import com.aditya.smartmedbox.feature.dashboard.domain.model.DeviceTelemetry
import com.aditya.smartmedbox.feature.dashboard.domain.repository.TelemetryRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveTelemetryUseCaseTest {
    private val repository: TelemetryRepository = mockk()
    private val useCase = ObserveTelemetryUseCase(repository)

    @Test
    fun `invoke should emit telemetry flow from repository`() = runTest {
        val expected =
            DeviceTelemetry(
                deviceId = "ESP32_01",
                weightGrams = 120.0f,
                isBoxOpen = false,
                temperatureCelsius = 22.0f,
                humidityPercent = 40.0f,
                timestamp = 1700000000L
            )

        every { repository.observeTelemetry("ESP32_01") } returns flowOf(expected)

        useCase("ESP32_01").test {
            val item = awaitItem()
            assertEquals(expected, item)
            awaitComplete()
        }
    }
}
