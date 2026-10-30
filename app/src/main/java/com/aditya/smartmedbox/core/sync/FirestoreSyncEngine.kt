package com.aditya.smartmedbox.core.sync

import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.core.database.dao.DoseEventDao
import com.aditya.smartmedbox.core.database.dao.DoseScheduleDao
import com.aditya.smartmedbox.core.database.dao.MedicineDao
import com.aditya.smartmedbox.core.database.entity.DeviceStatusEntity
import com.aditya.smartmedbox.core.database.entity.DoseEventEntity
import com.aditya.smartmedbox.core.database.entity.DoseScheduleEntity
import com.aditya.smartmedbox.core.database.entity.MedicineEntity
import com.aditya.smartmedbox.core.firebase.datasource.FirestoreDeviceDataSource
import com.aditya.smartmedbox.core.firebase.datasource.FirestoreDoseLogDataSource
import com.aditya.smartmedbox.core.firebase.datasource.FirestoreMedicinesDataSource
import com.aditya.smartmedbox.core.firebase.datasource.FirestoreScheduleDataSource
import com.aditya.smartmedbox.core.firebase.dto.FirestoreDoseLogDto
import com.aditya.smartmedbox.core.firebase.dto.FirestoreMedicineDto
import com.aditya.smartmedbox.core.firebase.dto.FirestoreScheduleDto
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.auth.domain.repository.AuthRepository
import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine
import com.aditya.smartmedbox.feature.schedule.domain.model.DoseSchedule
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber

@Singleton
class FirestoreSyncEngine @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreDeviceDataSource: FirestoreDeviceDataSource,
    private val firestoreMedicinesDataSource: FirestoreMedicinesDataSource,
    private val firestoreScheduleDataSource: FirestoreScheduleDataSource,
    private val firestoreDoseLogDataSource: FirestoreDoseLogDataSource,
    private val medicineDao: MedicineDao,
    private val doseScheduleDao: DoseScheduleDao,
    private val doseEventDao: DoseEventDao,
    private val deviceStatusDao: DeviceStatusDao,
    private val dispatchers: DispatcherProvider
) {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private var syncJob: Job? = null

    init {
        startSync()
    }

    fun startSync() {
        syncJob?.cancel()
        syncJob = scope.launch {
            authRepository.getAuthState().collect { user ->
                if (user != null) {
                    val uid = user.uid
                    val deviceId = user.linkedDeviceId ?: "DG-4081"

                    Timber.d("Starting real-time Firestore sync for user $uid on device $deviceId")

                    // 1. Observe Remote Telemetry
                    launch {
                        firestoreDeviceDataSource.observeLatestTelemetry(uid, deviceId).collect { telemetryDto ->
                            if (telemetryDto != null) {
                                val current = deviceStatusDao.getDeviceStatus(deviceId).firstOrNull()
                                val updated = (
                                    current ?: DeviceStatusEntity(
                                        deviceId = deviceId,
                                        temperatureCelsius = 21.4f,
                                        humidityPercent = 46.0f,
                                        weightGrams = 14.8f,
                                        lastSeenTimestamp = System.currentTimeMillis(),
                                        isBoxOpen = false,
                                        batteryPercent = 84,
                                        isOnline = true
                                    )
                                    ).copy(
                                    deviceId = deviceId,
                                    temperatureCelsius = telemetryDto.temp.toFloat(),
                                    humidityPercent = telemetryDto.humidity.toFloat(),
                                    weightGrams = telemetryDto.weightGrams.toFloat(),
                                    isBoxOpen = telemetryDto.boxOpen,
                                    batteryPercent = telemetryDto.batteryPercent,
                                    lastSeenTimestamp = System.currentTimeMillis(),
                                    isOnline = true
                                )
                                deviceStatusDao.upsertDeviceStatus(updated)
                            }
                        }
                    }

                    // 2. Observe Remote Medicines
                    launch {
                        firestoreMedicinesDataSource.observeMedicines(uid).collect { dtoList ->
                            if (dtoList.isNotEmpty()) {
                                val entities = dtoList.map { dto ->
                                    MedicineEntity(
                                        id = dto.id,
                                        name = dto.name,
                                        dosageDescription = dto.dosageDescription,
                                        strengthMg = dto.strengthMg,
                                        formFactor = dto.formFactor,
                                        compartmentIndex = dto.compartmentIndex,
                                        compartmentLabel = dto.compartmentLabel,
                                        timings = dto.timings,
                                        repeatDays = dto.repeatDays,
                                        currentStock = dto.currentStock,
                                        totalStock = dto.totalStock,
                                        lowStockThreshold = dto.lowStockThreshold,
                                        estimatedUnitWeightGrams = dto.estimatedUnitWeightGrams.toFloat(),
                                        isActive = dto.isActive,
                                        createdAt = System.currentTimeMillis()
                                    )
                                }
                                medicineDao.upsertMedicines(entities)
                            }
                        }
                    }

                    // 3. Observe Remote Schedules
                    launch {
                        firestoreScheduleDataSource.observeSchedules(uid).collect { dtoList ->
                            if (dtoList.isNotEmpty()) {
                                val entities = dtoList.map { dto ->
                                    DoseScheduleEntity(
                                        id = dto.id,
                                        medicineId = dto.medicineId,
                                        medicineName = dto.medicineName,
                                        times = dto.times,
                                        daysOfWeek = dto.daysOfWeek,
                                        gracePeriodMinutes = dto.gracePeriodMinutes,
                                        compartmentBay = dto.compartmentBay,
                                        isActive = dto.isActive
                                    )
                                }
                                doseScheduleDao.upsertSchedules(entities)
                            }
                        }
                    }

                    // 4. Observe Remote Dose Logs
                    launch {
                        firestoreDoseLogDataSource.observeDoseLogs(uid).collect { dtoList ->
                            if (dtoList.isNotEmpty()) {
                                val entities = dtoList.map { dto ->
                                    val status = try {
                                        DoseStatus.valueOf(dto.status)
                                    } catch (_: Exception) {
                                        DoseStatus.PENDING
                                    }
                                    DoseEventEntity(
                                        id = dto.id,
                                        scheduleId = dto.scheduleId,
                                        medicineId = dto.medicineId,
                                        medicineName = dto.medicineName,
                                        dosage = dto.dosage,
                                        scheduledTimeEpochMillis = dto.scheduledTimeEpochMillis,
                                        status = status,
                                        loggedTimeEpochMillis = dto.loggedTimeEpochMillis,
                                        recordedWeightGrams = dto.recordedWeightGrams?.toFloat(),
                                        bayLabel = dto.bayLabel
                                    )
                                }
                                doseEventDao.upsertEvents(entities)
                            }
                        }
                    }
                }
            }
        }
    }

    // Outward Push Methods: Room -> Remote Firestore
    fun pushMedicine(medicine: Medicine) {
        scope.launch {
            val user = authRepository.getAuthState().firstOrNull() ?: return@launch
            val dto = FirestoreMedicineDto(
                id = medicine.id,
                name = medicine.name,
                dosageDescription = medicine.dosageDescription,
                strengthMg = medicine.strengthMg,
                formFactor = medicine.formFactor,
                compartmentIndex = medicine.compartmentIndex,
                compartmentLabel = medicine.compartmentLabel,
                timings = medicine.timings,
                repeatDays = medicine.repeatDays,
                currentStock = medicine.currentStock,
                totalStock = medicine.totalStock,
                lowStockThreshold = medicine.lowStockThreshold,
                estimatedUnitWeightGrams = medicine.estimatedUnitWeightGrams.toDouble(),
                isActive = medicine.isActive
            )
            firestoreMedicinesDataSource.upsertMedicine(user.uid, dto)
        }
    }

    fun deleteMedicine(medicineId: String) {
        scope.launch {
            val user = authRepository.getAuthState().firstOrNull() ?: return@launch
            firestoreMedicinesDataSource.deleteMedicine(user.uid, medicineId)
        }
    }

    fun pushSchedule(schedule: DoseSchedule) {
        scope.launch {
            val user = authRepository.getAuthState().firstOrNull() ?: return@launch
            val dto = FirestoreScheduleDto(
                id = schedule.id,
                medicineId = schedule.medicineId,
                medicineName = schedule.medicineName,
                times = schedule.times,
                daysOfWeek = schedule.daysOfWeek,
                gracePeriodMinutes = schedule.gracePeriodMinutes,
                compartmentBay = schedule.compartmentBay,
                isActive = schedule.isActive
            )
            firestoreScheduleDataSource.upsertSchedule(user.uid, dto)
        }
    }

    fun deleteSchedule(scheduleId: String) {
        scope.launch {
            val user = authRepository.getAuthState().firstOrNull() ?: return@launch
            firestoreScheduleDataSource.deleteSchedule(user.uid, scheduleId)
        }
    }

    fun pushDoseLog(event: DoseEvent) {
        scope.launch {
            val user = authRepository.getAuthState().firstOrNull() ?: return@launch
            val dto = FirestoreDoseLogDto(
                id = event.id,
                scheduleId = event.scheduleId,
                medicineId = event.medicineId,
                medicineName = event.medicineName,
                dosage = event.dosage,
                scheduledTimeEpochMillis = event.scheduledTimeEpochMillis,
                status = event.status.name,
                loggedTimeEpochMillis = event.loggedTimeEpochMillis,
                recordedWeightGrams = event.recordedWeightGrams?.toDouble(),
                bayLabel = event.bayLabel,
                source = "MANUAL_APP"
            )
            firestoreDoseLogDataSource.upsertDoseLog(user.uid, dto)
        }
    }
}
