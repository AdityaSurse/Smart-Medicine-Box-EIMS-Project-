package com.aditya.smartmedbox.feature.alerts.data.repository

import com.aditya.smartmedbox.core.database.dao.DoseEventDao
import com.aditya.smartmedbox.core.database.dao.MedicineDao
import com.aditya.smartmedbox.feature.alerts.domain.model.AdherenceAlert
import com.aditya.smartmedbox.feature.alerts.domain.model.AlertSeverity
import com.aditya.smartmedbox.feature.alerts.domain.model.TrayCompartmentStatus
import com.aditya.smartmedbox.feature.alerts.domain.repository.AlertsRepository
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomAlertsRepository @Inject constructor(
    private val doseEventDao: DoseEventDao,
    private val medicineDao: MedicineDao
) : AlertsRepository {

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    override fun getActiveAlerts(): Flow<List<AdherenceAlert>> {
        return combine(
            doseEventDao.getAllEvents(),
            medicineDao.getAllMedicines()
        ) { events, medicines ->
            val now = System.currentTimeMillis()
            val pendingOrMissed = events.filter { it.status == DoseStatus.MISSED || it.status == DoseStatus.PENDING }

            if (pendingOrMissed.isEmpty()) {
                emptyList()
            } else {
                pendingOrMissed.map { event ->
                    val delayMins = ((now - event.scheduledTimeEpochMillis) / 60000L).coerceAtLeast(0L).toInt()
                    val scheduledTimeStr = timeFormat.format(Date(event.scheduledTimeEpochMillis))

                    AdherenceAlert(
                        id = event.id,
                        title = if (event.status == DoseStatus.MISSED) "Dose missed" else "Dose reminder",
                        medicineName = event.medicineName,
                        dosage = event.dosage,
                        instructions = "Take with water",
                        compartmentTag = event.bayLabel,
                        scheduledTime = scheduledTimeStr,
                        delayMinutes = delayMins,
                        boxSerial = "BOX #DG-4081",
                        lastSyncTime = "JUST NOW",
                        severity = if (event.status == DoseStatus.MISSED) AlertSeverity.CRITICAL else AlertSeverity.WARNING,
                        trays = listOf(
                            TrayCompartmentStatus(
                                trayId = event.bayLabel,
                                timeOfDay = scheduledTimeStr,
                                statusText = if (event.status == DoseStatus.MISSED) "UNOPENED" else "PENDING",
                                isMissedOrUnopened = event.status == DoseStatus.MISSED
                            )
                        ),
                        isChimeActive = event.status == DoseStatus.MISSED
                    )
                }
            }
        }
    }

    override suspend fun markDoseTaken(alertId: String): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            doseEventDao.logDose(
                id = alertId,
                status = DoseStatus.TAKEN,
                loggedTime = now,
                weight = 0.5f
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun snoozeAlert(alertId: String, minutes: Int): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun skipDose(alertId: String, reason: String?): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            doseEventDao.logDose(
                id = alertId,
                status = DoseStatus.MISSED,
                loggedTime = now,
                weight = 0f
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
