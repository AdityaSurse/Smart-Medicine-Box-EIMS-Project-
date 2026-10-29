package com.aditya.smartmedbox.core.firebase.dto

import com.google.firebase.firestore.PropertyName

data class FirestoreTelemetryDto(
    @get:PropertyName("temp") @set:PropertyName("temp") var temp: Double = 21.4,
    @get:PropertyName("humidity") @set:PropertyName("humidity") var humidity: Double = 46.0,
    @get:PropertyName("weightGrams") @set:PropertyName("weightGrams") var weightGrams: Double = 14.8,
    @get:PropertyName("boxOpen") @set:PropertyName("boxOpen") var boxOpen: Boolean = false,
    @get:PropertyName("rtcTime") @set:PropertyName("rtcTime") var rtcTime: String = "",
    @get:PropertyName("ts") @set:PropertyName("ts") var ts: Any? = null,
    @get:PropertyName("batteryPercent") @set:PropertyName("batteryPercent") var batteryPercent: Int = 84,
    @get:PropertyName("wifiRssi") @set:PropertyName("wifiRssi") var wifiRssi: Int = -60
)

data class FirestoreEventDto(
    @get:PropertyName("eventId") @set:PropertyName("eventId") var eventId: String = "",
    @get:PropertyName("eventType") @set:PropertyName("eventType") var eventType: String = "BOX_OPENED",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Any? = null,
    @get:PropertyName("rtcTime") @set:PropertyName("rtcTime") var rtcTime: String = "",
    @get:PropertyName("deltaWeightGrams") @set:PropertyName("deltaWeightGrams") var deltaWeightGrams: Double = 0.0,
    @get:PropertyName("currentWeightGrams") @set:PropertyName("currentWeightGrams") var currentWeightGrams: Double = 0.0,
    @get:PropertyName("trayBay") @set:PropertyName("trayBay") var trayBay: String = "Bay 01",
    @get:PropertyName("details") @set:PropertyName("details") var details: String = ""
)

data class FirestoreDeviceDto(
    @get:PropertyName("deviceId") @set:PropertyName("deviceId") var deviceId: String = "DG-4081",
    @get:PropertyName("modelName") @set:PropertyName("modelName") var modelName: String = "DoseGuard Bay 4X",
    @get:PropertyName("serialNumber") @set:PropertyName("serialNumber") var serialNumber: String = "#DG-4081-B",
    @get:PropertyName("firmwareVersion") @set:PropertyName("firmwareVersion") var firmwareVersion: String = "v2.4.1",
    @get:PropertyName("hardwareRev") @set:PropertyName("hardwareRev") var hardwareRev: String = "Rev 3.2",
    @get:PropertyName("macAddress") @set:PropertyName("macAddress") var macAddress: String = "",
    @get:PropertyName("baysCount") @set:PropertyName("baysCount") var baysCount: Int = 4,
    @get:PropertyName("isOnline") @set:PropertyName("isOnline") var isOnline: Boolean = true,
    @get:PropertyName("batteryPercent") @set:PropertyName("batteryPercent") var batteryPercent: Int = 84,
    @get:PropertyName("wifiSsid") @set:PropertyName("wifiSsid") var wifiSsid: String = "Home_WiFi_2G",
    @get:PropertyName("ipAddress") @set:PropertyName("ipAddress") var ipAddress: String = "192.168.1.142",
    @get:PropertyName("lastSeen") @set:PropertyName("lastSeen") var lastSeen: Any? = null,
    @get:PropertyName("pairedAt") @set:PropertyName("pairedAt") var pairedAt: Any? = null
)

data class FirestoreMedicineDto(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("dosageDescription") @set:PropertyName("dosageDescription") var dosageDescription: String = "",
    @get:PropertyName("strengthMg") @set:PropertyName("strengthMg") var strengthMg: Int = 0,
    @get:PropertyName("formFactor") @set:PropertyName("formFactor") var formFactor: String = "Capsule",
    @get:PropertyName("compartmentIndex") @set:PropertyName("compartmentIndex") var compartmentIndex: Int = 1,
    @get:PropertyName("compartmentLabel") @set:PropertyName("compartmentLabel") var compartmentLabel: String = "Bay 01",
    @get:PropertyName("timings") @set:PropertyName("timings") var timings: List<String> = emptyList(),
    @get:PropertyName("repeatDays") @set:PropertyName("repeatDays") var repeatDays: List<String> = emptyList(),
    @get:PropertyName("currentStock") @set:PropertyName("currentStock") var currentStock: Int = 0,
    @get:PropertyName("totalStock") @set:PropertyName("totalStock") var totalStock: Int = 0,
    @get:PropertyName("lowStockThreshold") @set:PropertyName("lowStockThreshold") var lowStockThreshold: Int = 5,
    @get:PropertyName("estimatedUnitWeightGrams") @set:PropertyName("estimatedUnitWeightGrams") var estimatedUnitWeightGrams: Double = 0.5,
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt") var updatedAt: Any? = null,
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Any? = null
)

data class FirestoreScheduleDto(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("medicineId") @set:PropertyName("medicineId") var medicineId: String = "",
    @get:PropertyName("medicineName") @set:PropertyName("medicineName") var medicineName: String = "",
    @get:PropertyName("times") @set:PropertyName("times") var times: List<String> = emptyList(),
    @get:PropertyName("daysOfWeek") @set:PropertyName("daysOfWeek") var daysOfWeek: List<Int> = emptyList(),
    @get:PropertyName("gracePeriodMinutes") @set:PropertyName("gracePeriodMinutes") var gracePeriodMinutes: Int = 30,
    @get:PropertyName("compartmentBay") @set:PropertyName("compartmentBay") var compartmentBay: String = "Bay 01",
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt") var updatedAt: Any? = null
)

data class FirestoreDoseLogDto(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("scheduleId") @set:PropertyName("scheduleId") var scheduleId: String = "",
    @get:PropertyName("medicineId") @set:PropertyName("medicineId") var medicineId: String = "",
    @get:PropertyName("medicineName") @set:PropertyName("medicineName") var medicineName: String = "",
    @get:PropertyName("dosage") @set:PropertyName("dosage") var dosage: String = "",
    @get:PropertyName("scheduledTimeEpochMillis") @set:PropertyName("scheduledTimeEpochMillis") var scheduledTimeEpochMillis: Long = 0L,
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "PENDING",
    @get:PropertyName("loggedTimeEpochMillis") @set:PropertyName("loggedTimeEpochMillis") var loggedTimeEpochMillis: Long? = null,
    @get:PropertyName("recordedWeightGrams") @set:PropertyName("recordedWeightGrams") var recordedWeightGrams: Double? = null,
    @get:PropertyName("bayLabel") @set:PropertyName("bayLabel") var bayLabel: String = "Tray 01",
    @get:PropertyName("source") @set:PropertyName("source") var source: String = "DEVICE_SENSOR",
    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt") var updatedAt: Any? = null
)

