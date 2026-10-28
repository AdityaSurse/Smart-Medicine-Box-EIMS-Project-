package com.aditya.smartmedbox.feature.history.domain.repository

import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import kotlinx.coroutines.flow.Flow

interface DoseEventRepository {
    fun getDoseEvents(): Flow<List<DoseEvent>>
    fun getEventsForDay(startOfDayEpochMillis: Long, endOfDayEpochMillis: Long): Flow<List<DoseEvent>>
    fun getDoseEventById(id: String): Flow<DoseEvent?>
    suspend fun insertDoseEvent(event: DoseEvent): Result<Unit>
    suspend fun insertDoseEvents(events: List<DoseEvent>): Result<Unit>
    suspend fun logDose(eventId: String, status: DoseStatus, loggedTimestamp: Long, recordedWeightGrams: Float?): Result<Unit>
    fun getMissedEventsCount(sinceEpochMillis: Long): Flow<Int>
}

