package com.aditya.smartmedbox.core.firebase.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreDtosTest {

    @Test
    fun firestoreTelemetryDto_defaultValues_areValid() {
        val telemetry = FirestoreTelemetryDto()
        assertEquals(21.4, telemetry.temp, 0.01)
        assertEquals(46.0, telemetry.humidity, 0.01)
        assertEquals(14.8, telemetry.weightGrams, 0.01)
        assertFalse(telemetry.boxOpen)
        assertEquals(84, telemetry.batteryPercent)
    }

    @Test
    fun firestoreEventDto_creation_preservesFields() {
        val event = FirestoreEventDto(
            eventId = "evt_001",
            eventType = "WEIGHT_DROP",
            deltaWeightGrams = -0.55,
            currentWeightGrams = 14.25,
            trayBay = "Bay 01",
            details = "Pill extracted"
        )
        assertEquals("evt_001", event.eventId)
        assertEquals("WEIGHT_DROP", event.eventType)
        assertEquals(-0.55, event.deltaWeightGrams, 0.01)
        assertEquals(14.25, event.currentWeightGrams, 0.01)
        assertEquals("Bay 01", event.trayBay)
    }

    @Test
    fun firestoreMedicineDto_mapsCorrectly() {
        val medicine = FirestoreMedicineDto(
            id = "med_amox",
            name = "Amoxicillin",
            dosageDescription = "500 mg capsule",
            strengthMg = 500,
            formFactor = "Capsule",
            compartmentIndex = 1,
            compartmentLabel = "Bay 01",
            timings = listOf("08:00 AM", "08:00 PM"),
            repeatDays = listOf("M", "T", "W"),
            currentStock = 18,
            totalStock = 30,
            lowStockThreshold = 5,
            estimatedUnitWeightGrams = 0.55,
            isActive = true
        )
        assertEquals("Amoxicillin", medicine.name)
        assertEquals(18, medicine.currentStock)
        assertEquals(2, medicine.timings.size)
        assertTrue(medicine.isActive)
    }

    @Test
    fun firestoreDoseLogDto_validatesStatus() {
        val log = FirestoreDoseLogDto(
            id = "log_001",
            scheduleId = "sch_001",
            medicineId = "med_001",
            medicineName = "Amoxicillin",
            status = "TAKEN",
            scheduledTimeEpochMillis = 1729000000000L,
            loggedTimeEpochMillis = 1729000120000L,
            recordedWeightGrams = 0.55
        )
        assertEquals("TAKEN", log.status)
        assertEquals("Amoxicillin", log.medicineName)
        assertEquals(0.55, log.recordedWeightGrams ?: 0.0, 0.01)
    }
}
