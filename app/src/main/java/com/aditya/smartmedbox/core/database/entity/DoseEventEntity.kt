package com.aditya.smartmedbox.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus

@Entity(tableName = "dose_events")
data class DoseEventEntity(
    @PrimaryKey val id: String,
    val scheduleId: String,
    val medicineId: String,
    val medicineName: String,
    val dosage: String,
    val scheduledTimeEpochMillis: Long,
    val status: DoseStatus = DoseStatus.PENDING,
    val loggedTimeEpochMillis: Long? = null,
    val recordedWeightGrams: Float? = null,
    val bayLabel: String = "Bay 01"
)
