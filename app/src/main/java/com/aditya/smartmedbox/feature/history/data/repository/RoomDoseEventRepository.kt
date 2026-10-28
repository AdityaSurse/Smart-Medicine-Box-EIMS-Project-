package com.aditya.smartmedbox.feature.history.data.repository

import com.aditya.smartmedbox.core.database.dao.DoseEventDao
import com.aditya.smartmedbox.feature.history.data.mapper.toDomain
import com.aditya.smartmedbox.feature.history.data.mapper.toEntity
import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.repository.DoseEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomDoseEventRepository @Inject constructor(
    private val doseEventDao: DoseEventDao
) : DoseEventRepository {

    override fun getDoseEvents(): Flow<List<DoseEvent>> {
        return doseEventDao.getAllEvents().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getEventsForDay(startOfDayEpochMillis: Long, endOfDayEpochMillis: Long): Flow<List<DoseEvent>> {
        return doseEventDao.getEventsForDay(startOfDayEpochMillis, endOfDayEpochMillis).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDoseEventById(id: String): Flow<DoseEvent?> {
        return doseEventDao.getEventById(id).map { it?.toDomain() }
    }

    override suspend fun insertDoseEvent(event: DoseEvent): Result<Unit> {
        return try {
            doseEventDao.upsertEvent(event.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun insertDoseEvents(events: List<DoseEvent>): Result<Unit> {
        return try {
            doseEventDao.upsertEvents(events.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logDose(
        eventId: String,
        status: DoseStatus,
        loggedTimestamp: Long,
        recordedWeightGrams: Float?
    ): Result<Unit> {
        return try {
            doseEventDao.logDose(
                id = eventId,
                status = status,
                loggedTime = loggedTimestamp,
                weight = recordedWeightGrams
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMissedEventsCount(sinceEpochMillis: Long): Flow<Int> {
        return doseEventDao.getMissedEventsCount(sinceEpochMillis)
    }
}
