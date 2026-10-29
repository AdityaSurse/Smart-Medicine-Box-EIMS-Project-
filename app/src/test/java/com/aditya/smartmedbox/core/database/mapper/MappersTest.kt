package com.aditya.smartmedbox.core.database.mapper

import com.aditya.smartmedbox.feature.dashboard.data.mapper.toDomain
import com.aditya.smartmedbox.feature.dashboard.data.mapper.toEntity
import com.aditya.smartmedbox.feature.dashboard.domain.model.DeviceStatus
import com.aditya.smartmedbox.feature.history.data.mapper.toDomain
import com.aditya.smartmedbox.feature.history.data.mapper.toEntity
import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.medicines.data.mapper.toDomain
import com.aditya.smartmedbox.feature.medicines.data.mapper.toEntity
import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine
import com.aditya.smartmedbox.feature.schedule.data.mapper.toDomain
import com.aditya.smartmedbox.feature.schedule.data.mapper.toEntity
import com.aditya.smartmedbox.feature.schedule.domain.model.DoseSchedule
import org.junit.Assert.assertEquals
import org.junit.Test

class MappersTest {

    @Test
    fun `Medicine toEntity and toDomain bi-directional mapping`() {
        val domain = Medicine(
            id = "med_1",
            name = "Amoxicillin",
            dosageDescription = "500 mg",
            strengthMg = 500,
            formFactor = "Antibiotic capsule",
            compartmentIndex = 1,
            compartmentLabel = "Bay 01",
            timings = listOf("08:00 AM", "08:00 PM"),
            repeatDays = listOf("M", "T", "W"),
            currentStock = 20,
            totalStock = 30,
            lowStockThreshold = 5,
            estimatedUnitWeightGrams = 0.5f,
            isActive = true
        )

        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain.id, mappedBack.id)
        assertEquals(domain.name, mappedBack.name)
        assertEquals(domain.timings, mappedBack.timings)
        assertEquals(domain.currentStock, mappedBack.currentStock)
    }

    @Test
    fun `DoseSchedule toEntity and toDomain bi-directional mapping`() {
        val domain = DoseSchedule(
            id = "sch_1",
            medicineId = "med_1",
            medicineName = "Amoxicillin",
            times = listOf("08:00 AM", "08:00 PM"),
            daysOfWeek = listOf(1, 2, 3, 4, 5),
            gracePeriodMinutes = 45,
            compartmentBay = "Bay 01",
            isActive = true
        )

        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain.id, mappedBack.id)
        assertEquals(domain.times, mappedBack.times)
        assertEquals(domain.gracePeriodMinutes, mappedBack.gracePeriodMinutes)
    }

    @Test
    fun `DoseEvent toEntity and toDomain bi-directional mapping`() {
        val domain = DoseEvent(
            id = "evt_1",
            scheduleId = "sch_1",
            medicineId = "med_1",
            medicineName = "Amoxicillin",
            dosage = "500 mg",
            scheduledTimeEpochMillis = 1700000000L,
            status = DoseStatus.TAKEN,
            loggedTimeEpochMillis = 1700000050L,
            recordedWeightGrams = 0.55f,
            bayLabel = "Bay 01"
        )

        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain.id, mappedBack.id)
        assertEquals(domain.status, mappedBack.status)
        assertEquals(domain.recordedWeightGrams, mappedBack.recordedWeightGrams)
    }

    @Test
    fun `DeviceStatus toEntity and toDomain bi-directional mapping`() {
        val domain = DeviceStatus(
            deviceId = "DG_BOX_4081",
            temperatureCelsius = 22.5f,
            humidityPercent = 48.0f,
            weightGrams = 15.2f,
            lastSeenTimestamp = 1700000000L,
            isBoxOpen = false,
            batteryPercent = 90,
            isOnline = true
        )

        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain.deviceId, mappedBack.deviceId)
        assertEquals(domain.temperatureCelsius, mappedBack.temperatureCelsius)
        assertEquals(domain.batteryPercent, mappedBack.batteryPercent)
    }
}
