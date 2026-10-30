package com.aditya.smartmedbox.core.sync

import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.core.database.dao.DoseEventDao
import com.aditya.smartmedbox.core.database.dao.DoseScheduleDao
import com.aditya.smartmedbox.core.database.dao.MedicineDao
import com.aditya.smartmedbox.core.firebase.datasource.FirestoreDeviceDataSource
import com.aditya.smartmedbox.core.firebase.datasource.FirestoreDoseLogDataSource
import com.aditya.smartmedbox.core.firebase.datasource.FirestoreMedicinesDataSource
import com.aditya.smartmedbox.core.firebase.datasource.FirestoreScheduleDataSource
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.auth.domain.model.User
import com.aditya.smartmedbox.feature.auth.domain.repository.AuthRepository
import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine
import com.aditya.smartmedbox.feature.schedule.domain.model.DoseSchedule
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirestoreSyncEngineTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var syncEngine: FirestoreSyncEngine
    private val testDispatcher = UnconfinedTestDispatcher()

    private val dispatcherProvider = object : DispatcherProvider {
        override val main: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
        override val unconfined: CoroutineDispatcher = testDispatcher
    }

    @Before
    fun setUp() {
        authRepository = object : AuthRepository {
            override fun getAuthState(): Flow<User?> = flowOf(User(uid = "test_user_1", email = "test@med.care"))
            override suspend fun signIn(email: String, password: String) = Result.success(User(uid = "test_user_1", email = email))
            override suspend fun signUp(email: String, password: String) = Result.success(User(uid = "test_user_1", email = email))
            override suspend fun signInWithGoogle(idToken: String) = Result.success(User(uid = "test_user_1", email = "test@med.care"))
            override suspend fun updateLinkedDeviceId(deviceId: String?) = Result.success(Unit)
            override suspend fun signOut() = Result.success(Unit)
        }

        val deviceDataSource = FirestoreDeviceDataSource(null)
        val medicinesDataSource = FirestoreMedicinesDataSource(null)
        val scheduleDataSource = FirestoreScheduleDataSource(null)
        val doseLogDataSource = FirestoreDoseLogDataSource(null)

        val medicineDao = mockk<MedicineDao>(relaxed = true)
        val doseScheduleDao = mockk<DoseScheduleDao>(relaxed = true)
        val doseEventDao = mockk<DoseEventDao>(relaxed = true)
        val deviceStatusDao = mockk<DeviceStatusDao>(relaxed = true)

        syncEngine = FirestoreSyncEngine(
            authRepository = authRepository,
            firestoreDeviceDataSource = deviceDataSource,
            firestoreMedicinesDataSource = medicinesDataSource,
            firestoreScheduleDataSource = scheduleDataSource,
            firestoreDoseLogDataSource = doseLogDataSource,
            medicineDao = medicineDao,
            doseScheduleDao = doseScheduleDao,
            doseEventDao = doseEventDao,
            deviceStatusDao = deviceStatusDao,
            dispatchers = dispatcherProvider
        )
    }

    @Test
    fun pushMedicine_runsWithoutErrorInOfflineMode() = runTest {
        val medicine = Medicine(
            id = "med_001",
            name = "Paracetamol",
            dosageDescription = "650mg",
            strengthMg = 650,
            formFactor = "Tablet",
            compartmentIndex = 1,
            compartmentLabel = "Bay 01",
            timings = listOf("08:00 AM"),
            repeatDays = listOf("M", "T"),
            currentStock = 20,
            totalStock = 30,
            lowStockThreshold = 5,
            estimatedUnitWeightGrams = 0.65f,
            isActive = true
        )
        syncEngine.pushMedicine(medicine)
    }

    @Test
    fun pushSchedule_runsWithoutErrorInOfflineMode() = runTest {
        val schedule = DoseSchedule(
            id = "sch_001",
            medicineId = "med_001",
            medicineName = "Paracetamol",
            times = listOf("08:00 AM"),
            daysOfWeek = listOf(1, 2, 3),
            gracePeriodMinutes = 30,
            compartmentBay = "Bay 01",
            isActive = true
        )
        syncEngine.pushSchedule(schedule)
    }

    @Test
    fun pushDoseLog_runsWithoutErrorInOfflineMode() = runTest {
        val event = DoseEvent(
            id = "evt_001",
            scheduleId = "sch_001",
            medicineId = "med_001",
            medicineName = "Paracetamol",
            dosage = "650mg",
            scheduledTimeEpochMillis = 1729000000000L,
            status = DoseStatus.TAKEN,
            bayLabel = "Tray 01"
        )
        syncEngine.pushDoseLog(event)
    }
}
