package com.aditya.smartmedbox.feature.schedule.domain.model

data class PrescriptionSchedule(
    val id: String,
    val medicineName: String,
    val dosageStrength: String = "500 mg",
    val formFactor: String = "Antibiotic capsule",
    val trayBay: String = "Tray 01",
    val dispenseTimes: List<String> = listOf("8:00 AM", "8:00 PM"),
    val daysOfWeek: List<String> = listOf("M", "T", "W", "T", "F"),
    val status: String = "Dispensing",
    val scheduledHour: Int = 8,
    val scheduledMinute: Int = 0,
    val targetWeightDeltaGrams: Float = 0.5f,
    val totalSupply: Int = 30,
    val isActive: Boolean = true
)

data class ScheduleDay(
    val dayLetter: String,
    val dayNumber: Int,
    val isToday: Boolean = false,
    val isSelected: Boolean = false
)
