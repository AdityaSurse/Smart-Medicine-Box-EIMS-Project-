package com.aditya.smartmedbox.feature.schedule.domain.repository

import com.aditya.smartmedbox.feature.schedule.domain.model.DoseSchedule
import kotlinx.coroutines.flow.Flow

interface DoseScheduleRepository {
    fun getSchedules(): Flow<List<DoseSchedule>>
    fun getSchedulesForMedicine(medicineId: String): Flow<List<DoseSchedule>>
    fun getScheduleById(scheduleId: String): Flow<DoseSchedule?>
    suspend fun saveSchedule(schedule: DoseSchedule): Result<Unit>
    suspend fun deleteSchedule(scheduleId: String): Result<Unit>
    suspend fun toggleScheduleActive(scheduleId: String, isActive: Boolean): Result<Unit>
}
