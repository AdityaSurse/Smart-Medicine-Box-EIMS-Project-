package com.aditya.smartmedbox.feature.schedule.domain.repository

import com.aditya.smartmedbox.feature.schedule.domain.model.PrescriptionSchedule
import com.aditya.smartmedbox.feature.schedule.domain.model.ScheduleDay
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun getScheduleDays(): Flow<List<ScheduleDay>>
    fun getSchedulesForDay(dayNumber: Int): Flow<List<PrescriptionSchedule>>
    suspend fun toggleScheduleActive(scheduleId: String, isActive: Boolean): Result<Unit>
    suspend fun addSchedule(schedule: PrescriptionSchedule): Result<Unit>
}
