package com.aditya.smartmedbox.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dose_schedules")
data class DoseScheduleEntity(
    @PrimaryKey val id: String,
    val medicineId: String,
    val medicineName: String = "",
    val times: List<String> = emptyList(),
    val daysOfWeek: List<Int> = emptyList(),
    val gracePeriodMinutes: Int = 30,
    val compartmentBay: String = "Bay 01",
    val isActive: Boolean = true
)
