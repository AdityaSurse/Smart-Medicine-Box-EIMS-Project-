package com.aditya.smartmedbox.feature.schedule.domain.model

data class DoseSchedule(
    val id: String,
    val medicineId: String,
    val medicineName: String = "",
    val times: List<String> = listOf("08:00 AM"),
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
    val gracePeriodMinutes: Int = 30,
    val compartmentBay: String = "Bay 01",
    val isActive: Boolean = true
)

