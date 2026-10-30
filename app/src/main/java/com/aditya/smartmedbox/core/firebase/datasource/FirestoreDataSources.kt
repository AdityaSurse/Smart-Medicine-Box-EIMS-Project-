package com.aditya.smartmedbox.core.firebase.datasource

import com.aditya.smartmedbox.core.firebase.dto.FirestoreDeviceDto
import com.aditya.smartmedbox.core.firebase.dto.FirestoreDoseLogDto
import com.aditya.smartmedbox.core.firebase.dto.FirestoreEventDto
import com.aditya.smartmedbox.core.firebase.dto.FirestoreMedicineDto
import com.aditya.smartmedbox.core.firebase.dto.FirestoreScheduleDto
import com.aditya.smartmedbox.core.firebase.dto.FirestoreTelemetryDto
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import timber.log.Timber

@Singleton
class FirestoreDeviceDataSource @Inject constructor(
    private val firestore: FirebaseFirestore?
) {
    fun observeLatestTelemetry(uid: String, deviceId: String): Flow<FirestoreTelemetryDto?> {
        val db = firestore ?: return flowOf(null)
        return callbackFlow {
            val docRef = db.collection("users").document(uid)
                .collection("devices").document(deviceId)
                .collection("telemetry").document("latest")

            val registration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.w(error, "Telemetry snapshot listener failed for $deviceId")
                    return@addSnapshotListener
                }
                val dto = snapshot?.toObject(FirestoreTelemetryDto::class.java)
                trySend(dto)
            }
            awaitClose { registration.remove() }
        }
    }

    fun observeDeviceEvents(uid: String, deviceId: String): Flow<List<FirestoreEventDto>> {
        val db = firestore ?: return flowOf(emptyList())
        return callbackFlow {
            val colRef = db.collection("users").document(uid)
                .collection("devices").document(deviceId)
                .collection("events")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)

            val registration = colRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.w(error, "Events snapshot listener failed for $deviceId")
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(FirestoreEventDto::class.java) ?: emptyList()
                trySend(list)
            }
            awaitClose { registration.remove() }
        }
    }

    suspend fun updateTelemetry(uid: String, deviceId: String, telemetry: FirestoreTelemetryDto): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val docRef = db.collection("users").document(uid)
                .collection("devices").document(deviceId)
                .collection("telemetry").document("latest")

            val map = hashMapOf(
                "temp" to telemetry.temp,
                "humidity" to telemetry.humidity,
                "weightGrams" to telemetry.weightGrams,
                "boxOpen" to telemetry.boxOpen,
                "rtcTime" to telemetry.rtcTime,
                "ts" to FieldValue.serverTimestamp(),
                "batteryPercent" to telemetry.batteryPercent,
                "wifiRssi" to telemetry.wifiRssi
            )
            docRef.set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordEvent(uid: String, deviceId: String, event: FirestoreEventDto): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val eventId = event.eventId.ifBlank { "evt_${System.currentTimeMillis()}" }
            val docRef = db.collection("users").document(uid)
                .collection("devices").document(deviceId)
                .collection("events").document(eventId)

            val map = hashMapOf(
                "eventId" to eventId,
                "eventType" to event.eventType,
                "timestamp" to FieldValue.serverTimestamp(),
                "rtcTime" to event.rtcTime,
                "deltaWeightGrams" to event.deltaWeightGrams,
                "currentWeightGrams" to event.currentWeightGrams,
                "trayBay" to event.trayBay,
                "details" to event.details
            )
            docRef.set(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeDeviceMetadata(uid: String, deviceId: String): Flow<FirestoreDeviceDto?> {
        val db = firestore ?: return flowOf(null)
        return callbackFlow {
            val docRef = db.collection("users").document(uid)
                .collection("devices").document(deviceId)

            val registration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.w(error, "Device metadata listener failed for $deviceId")
                    return@addSnapshotListener
                }
                val dto = snapshot?.toObject(FirestoreDeviceDto::class.java)
                trySend(dto)
            }
            awaitClose { registration.remove() }
        }
    }

    suspend fun claimDevice(uid: String, deviceId: String, pairingToken: String, modelName: String = "DoseGuard Bay 4X"): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val docRef = db.collection("users").document(uid)
                .collection("devices").document(deviceId)

            val map = hashMapOf(
                "deviceId" to deviceId,
                "modelName" to modelName,
                "serialNumber" to "#$deviceId",
                "firmwareVersion" to "v3.2",
                "hardwareRev" to "Rev 3.2",
                "macAddress" to "24:6F:28:AB:D4:C2",
                "baysCount" to 4,
                "isOnline" to true,
                "batteryPercent" to 84,
                "wifiSsid" to "Home_WiFi_2G",
                "ipAddress" to "192.168.1.142",
                "pairingToken" to pairingToken,
                "lastSeen" to FieldValue.serverTimestamp(),
                "pairedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unpairDevice(uid: String, deviceId: String): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val docRef = db.collection("users").document(uid)
                .collection("devices").document(deviceId)
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendDeviceCommand(uid: String, deviceId: String, commandType: String, payload: Map<String, Any> = emptyMap()): Result<String> {
        val db = firestore ?: return Result.success("cmd_offline_${System.currentTimeMillis()}")
        return try {
            val cmdId = "cmd_${System.currentTimeMillis()}"
            val docRef = db.collection("users").document(uid)
                .collection("devices").document(deviceId)
                .collection("commands").document(cmdId)

            val map = hashMapOf(
                "commandId" to cmdId,
                "type" to commandType,
                "status" to "PENDING",
                "createdAt" to FieldValue.serverTimestamp(),
                "payload" to payload
            )
            docRef.set(map).await()
            Result.success(cmdId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Singleton
class FirestoreMedicinesDataSource @Inject constructor(
    private val firestore: FirebaseFirestore?
) {
    fun observeMedicines(uid: String): Flow<List<FirestoreMedicineDto>> {
        val db = firestore ?: return flowOf(emptyList())
        return callbackFlow {
            val colRef = db.collection("users").document(uid)
                .collection("medicines")
                .orderBy("compartmentIndex", Query.Direction.ASCENDING)

            val registration = colRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.w(error, "Medicines snapshot listener failed for uid $uid")
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(FirestoreMedicineDto::class.java) ?: emptyList()
                trySend(list)
            }
            awaitClose { registration.remove() }
        }
    }

    suspend fun upsertMedicine(uid: String, medicine: FirestoreMedicineDto): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val docRef = db.collection("users").document(uid)
                .collection("medicines").document(medicine.id)

            val map = hashMapOf(
                "id" to medicine.id,
                "name" to medicine.name,
                "dosageDescription" to medicine.dosageDescription,
                "strengthMg" to medicine.strengthMg,
                "formFactor" to medicine.formFactor,
                "compartmentIndex" to medicine.compartmentIndex,
                "compartmentLabel" to medicine.compartmentLabel,
                "timings" to medicine.timings,
                "repeatDays" to medicine.repeatDays,
                "currentStock" to medicine.currentStock,
                "totalStock" to medicine.totalStock,
                "lowStockThreshold" to medicine.lowStockThreshold,
                "estimatedUnitWeightGrams" to medicine.estimatedUnitWeightGrams,
                "isActive" to medicine.isActive,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMedicine(uid: String, medicineId: String): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection("users").document(uid)
                .collection("medicines").document(medicineId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Singleton
class FirestoreScheduleDataSource @Inject constructor(
    private val firestore: FirebaseFirestore?
) {
    fun observeSchedules(uid: String): Flow<List<FirestoreScheduleDto>> {
        val db = firestore ?: return flowOf(emptyList())
        return callbackFlow {
            val colRef = db.collection("users").document(uid)
                .collection("schedules")

            val registration = colRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.w(error, "Schedules snapshot listener failed for uid $uid")
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(FirestoreScheduleDto::class.java) ?: emptyList()
                trySend(list)
            }
            awaitClose { registration.remove() }
        }
    }

    suspend fun upsertSchedule(uid: String, schedule: FirestoreScheduleDto): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val docRef = db.collection("users").document(uid)
                .collection("schedules").document(schedule.id)

            val map = hashMapOf(
                "id" to schedule.id,
                "medicineId" to schedule.medicineId,
                "medicineName" to schedule.medicineName,
                "times" to schedule.times,
                "daysOfWeek" to schedule.daysOfWeek,
                "gracePeriodMinutes" to schedule.gracePeriodMinutes,
                "compartmentBay" to schedule.compartmentBay,
                "isActive" to schedule.isActive,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSchedule(uid: String, scheduleId: String): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection("users").document(uid)
                .collection("schedules").document(scheduleId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Singleton
class FirestoreDoseLogDataSource @Inject constructor(
    private val firestore: FirebaseFirestore?
) {
    fun observeDoseLogs(uid: String): Flow<List<FirestoreDoseLogDto>> {
        val db = firestore ?: return flowOf(emptyList())
        return callbackFlow {
            val colRef = db.collection("users").document(uid)
                .collection("doseLogs")
                .orderBy("scheduledTimeEpochMillis", Query.Direction.DESCENDING)
                .limit(100)

            val registration = colRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.w(error, "DoseLogs snapshot listener failed for uid $uid")
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(FirestoreDoseLogDto::class.java) ?: emptyList()
                trySend(list)
            }
            awaitClose { registration.remove() }
        }
    }

    suspend fun upsertDoseLog(uid: String, log: FirestoreDoseLogDto): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val docRef = db.collection("users").document(uid)
                .collection("doseLogs").document(log.id)

            val map = hashMapOf(
                "id" to log.id,
                "scheduleId" to log.scheduleId,
                "medicineId" to log.medicineId,
                "medicineName" to log.medicineName,
                "dosage" to log.dosage,
                "scheduledTimeEpochMillis" to log.scheduledTimeEpochMillis,
                "status" to log.status,
                "loggedTimeEpochMillis" to log.loggedTimeEpochMillis,
                "recordedWeightGrams" to log.recordedWeightGrams,
                "bayLabel" to log.bayLabel,
                "source" to log.source,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
