package com.aditya.smartmedbox.feature.dashboard.data.repository

import app.cash.turbine.test
import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.core.database.dao.MedicineDao
import com.aditya.smartmedbox.core.database.entity.DeviceStatusEntity
import com.aditya.smartmedbox.core.database.entity.MedicineEntity
import com.aditya.smartmedbox.core.firebase.datasource.FirestoreDeviceDataSource
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.auth.domain.model.User
import com.aditya.smartmedbox.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TelemetryRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private val deviceStatusDao: DeviceStatusDao = mockk()
    private val medicineDao: MedicineDao = mockk()
    private val authRepository: AuthRepository = mockk()
    private val firestoreDeviceDataSource: FirestoreDeviceDataSource = mockk()

    private val dispatchers = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
        override val unconfined = testDispatcher
    }

    private lateinit var repository: TelemetryRepositoryImpl

    private val deviceStatusFlow = MutableStateFlow<DeviceStatusEntity?>(null)
    private val medicinesFlow = MutableStateFlow<List<MedicineEntity>>(emptyList())
    private val authUserFlow = MutableStateFlow<User?>(
        User(uid = "uid_123", email = "test@care.com", displayName = "Eleanor Vance", linkedDeviceId = "DG-4081")
    )

    @Before
    fun setUp() {
        every { deviceStatusDao.getLatestDeviceStatus() } returns deviceStatusFlow
        every { medicineDao.getAllMedicines() } returns medicinesFlow
        every { authRepository.getAuthState() } returns authUserFlow

        repository = TelemetryRepositoryImpl(
            deviceStatusDao = deviceStatusDao,
            medicineDao = medicineDao,
            authRepository = authRepository,
            firestoreDeviceDataSource = firestoreDeviceDataSource,
            dispatchers = dispatchers
        )
    }

    @Test
    fun `observeTelemetry calculates real tablet count from active medicines`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        deviceStatusFlow.value = DeviceStatusEntity(
            deviceId = "DG-4081",
            temperatureCelsius = 22.5f,
            humidityPercent = 48.0f,
            lastSeenTimestamp = now,
            isOnline = true
        )

        medicinesFlow.value = listOf(
            MedicineEntity(id = "m1", name = "Amox", dosageDescription = "500mg", currentStock = 12, isActive = true),
            MedicineEntity(id = "m2", name = "Vitamin D", dosageDescription = "1000 IU", currentStock = 8, isActive = true),
            MedicineEntity(id = "m3", name = "Old course", dosageDescription = "10mg", currentStock = 5, isActive = false)
        )

        repository.observeTelemetry("DG-4081").test {
            val telemetry = awaitItem()
            assertEquals("DG-4081", telemetry.deviceId)
            assertEquals("Eleanor Vance", telemetry.patientName)
            assertEquals(20, telemetry.tabletsRemaining) // 12 + 8, excluding inactive 5
            assertEquals(22.5f, telemetry.temperatureCelsius)
            assertFalse(telemetry.isStale)
            assertEquals("Just now", telemetry.lastSeenFormatted)
        }
    }

    @Test
    fun `observeTelemetry flags isStale when lastSeen is older than 5 minutes`() = runTest(testDispatcher) {
        val tenMinutesAgo = System.currentTimeMillis() - (10 * 60 * 1000L)
        deviceStatusFlow.value = DeviceStatusEntity(
            deviceId = "DG-4081",
            lastSeenTimestamp = tenMinutesAgo
        )

        repository.observeTelemetry("DG-4081").test {
            val telemetry = awaitItem()
            assertTrue(telemetry.isStale)
            assertEquals("10m ago", telemetry.lastSeenFormatted)
        }
    }

    @Test
    fun `silenceBuzzer writes SILENCE_BUZZER command to firestore`() = runTest(testDispatcher) {
        coEvery {
            firestoreDeviceDataSource.sendDeviceCommand("uid_123", "DG-4081", "SILENCE_BUZZER", emptyMap())
        } returns Result.success("cmd_123")

        val result = repository.silenceBuzzer("DG-4081")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            firestoreDeviceDataSource.sendDeviceCommand("uid_123", "DG-4081", "SILENCE_BUZZER", emptyMap())
        }
    }
}
