package com.aditya.smartmedbox.feature.schedule.data.repository

import com.aditya.smartmedbox.core.database.dao.DoseScheduleDao
import com.aditya.smartmedbox.feature.schedule.data.mapper.toDomain
import com.aditya.smartmedbox.feature.schedule.data.mapper.toEntity
import com.aditya.smartmedbox.feature.schedule.domain.model.DoseSchedule
import com.aditya.smartmedbox.feature.schedule.domain.repository.DoseScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomDoseScheduleRepository @Inject constructor(
    private val doseScheduleDao: DoseScheduleDao
) : DoseScheduleRepository {

    override fun getSchedules(): Flow<List<DoseSchedule>> {
        return doseScheduleDao.getAllSchedules().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSchedulesForMedicine(medicineId: String): Flow<List<DoseSchedule>> {
        return doseScheduleDao.getSchedulesForMedicine(medicineId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getScheduleById(scheduleId: String): Flow<DoseSchedule?> {
        return doseScheduleDao.getScheduleById(scheduleId).map { it?.toDomain() }
    }

    override suspend fun saveSchedule(schedule: DoseSchedule): Result<Unit> {
        return try {
            doseScheduleDao.upsertSchedule(schedule.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSchedule(scheduleId: String): Result<Unit> {
        return try {
            doseScheduleDao.deleteSchedule(scheduleId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleScheduleActive(scheduleId: String, isActive: Boolean): Result<Unit> {
        return try {
            doseScheduleDao.toggleActive(scheduleId, isActive)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
