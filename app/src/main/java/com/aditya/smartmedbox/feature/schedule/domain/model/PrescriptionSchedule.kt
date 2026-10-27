package com.aditya.smartmedbox.feature.schedule.domain.model

data class PrescriptionSchedule(
    val id: String,
    val medicineName: String,
    val scheduledHour: Int,
    val scheduledMinute: Int,
    val targetWeightDeltaGrams: Float,
    val isActive: Boolean = true
)
