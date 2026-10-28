package com.aditya.smartmedbox.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aditya.smartmedbox.core.database.entity.DoseScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseScheduleDao {
    @Query("SELECT * FROM dose_schedules")
    fun getAllSchedules(): Flow<List<DoseScheduleEntity>>

    @Query("SELECT * FROM dose_schedules WHERE medicineId = :medicineId")
    fun getSchedulesForMedicine(medicineId: String): Flow<List<DoseScheduleEntity>>

    @Query("SELECT * FROM dose_schedules WHERE id = :id")
    fun getScheduleById(id: String): Flow<DoseScheduleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedule(schedule: DoseScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedules(schedules: List<DoseScheduleEntity>)

    @Query("UPDATE dose_schedules SET isActive = :isActive WHERE id = :id")
    suspend fun toggleActive(id: String, isActive: Boolean)

    @Query("DELETE FROM dose_schedules WHERE id = :id")
    suspend fun deleteSchedule(id: String)

    @Query("DELETE FROM dose_schedules")
    suspend fun deleteAllSchedules()
}

