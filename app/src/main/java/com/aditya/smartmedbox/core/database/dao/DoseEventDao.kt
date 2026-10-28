package com.aditya.smartmedbox.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aditya.smartmedbox.core.database.entity.DoseEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseEventDao {
    @Query("SELECT * FROM dose_events ORDER BY scheduledTimeEpochMillis DESC")
    fun getAllEvents(): Flow<List<DoseEventEntity>>

    @Query("SELECT * FROM dose_events WHERE scheduledTimeEpochMillis BETWEEN :startMillis AND :endMillis ORDER BY scheduledTimeEpochMillis ASC")
    fun getEventsForDay(startMillis: Long, endMillis: Long): Flow<List<DoseEventEntity>>

    @Query("SELECT * FROM dose_events WHERE id = :id")
    fun getEventById(id: String): Flow<DoseEventEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvent(event: DoseEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvents(events: List<DoseEventEntity>)

    @Query("UPDATE dose_events SET status = :status, loggedTimeEpochMillis = :loggedTime, recordedWeightGrams = :weight WHERE id = :id")
    suspend fun logDose(id: String, status: String, loggedTime: Long, weight: Float?)

    @Query("SELECT COUNT(*) FROM dose_events WHERE status = 'MISSED' AND scheduledTimeEpochMillis >= :sinceMillis")
    fun getMissedEventsCount(sinceMillis: Long): Flow<Int>

    @Query("DELETE FROM dose_events")
    suspend fun deleteAllEvents()
}
