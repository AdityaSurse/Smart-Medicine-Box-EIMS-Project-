package com.aditya.smartmedbox.feature.alerts.data.repository

import com.aditya.smartmedbox.feature.alerts.domain.model.AdherenceAlert
import com.aditya.smartmedbox.feature.alerts.domain.model.AlertSeverity
import com.aditya.smartmedbox.feature.alerts.domain.model.TrayCompartmentStatus
import com.aditya.smartmedbox.feature.alerts.domain.repository.AlertsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAlertsRepository @Inject constructor() : AlertsRepository {

    private val alertsState = MutableStateFlow(
        listOf(
            AdherenceAlert(
                id = "alert_para_night",
                title = "Dose missed",
                medicineName = "Paracetamol 650mg",
                dosage = "1 tablet with water",
                instructions = "Take after food",
                compartmentTag = "Compartment C-3",
                scheduledTime = "8:00 PM",
                delayMinutes = 42,
                boxSerial = "BOX #DG-4081",
                lastSyncTime = "2 MIN AGO",
                severity = AlertSeverity.CRITICAL,
                trays = listOf(
                    TrayCompartmentStatus(
                        trayId = "TRAY 01",
                        timeOfDay = "Morning",
                        statusText = "Taken 8am",
                        isTaken = true
                    ),
                    TrayCompartmentStatus(
                        trayId = "TRAY 02",
                        timeOfDay = "Noon",
                        statusText = "Taken 1pm",
                        isTaken = true
                    ),
                    TrayCompartmentStatus(
                        trayId = "TRAY 03",
                        timeOfDay = "Night",
                        statusText = "UNOPENED",
                        isMissedOrUnopened = true
                    ),
                    TrayCompartmentStatus(
                        trayId = "TRAY 04",
                        timeOfDay = "Bed",
                        statusText = "10:30 PM"
                    )
                ),
                isChimeActive = true
            )
        )
    )

    override fun getActiveAlerts(): Flow<List<AdherenceAlert>> = alertsState.asStateFlow()

    override suspend fun markDoseTaken(alertId: String): Result<Unit> {
        alertsState.value = alertsState.value.map { alert ->
            if (alert.id == alertId) {
                alert.copy(
                    isChimeActive = false,
                    trays = alert.trays.map { tray ->
                        if (tray.trayId == "TRAY 03") {
                            tray.copy(
                                statusText = "Taken now",
                                isMissedOrUnopened = false,
                                isTaken = true
                            )
                        } else tray
                    }
                )
            } else alert
        }
        return Result.success(Unit)
    }

    override suspend fun snoozeAlert(alertId: String, minutes: Int): Result<Unit> {
        alertsState.value = alertsState.value.map { alert ->
            if (alert.id == alertId) {
                alert.copy(isChimeActive = false)
            } else alert
        }
        return Result.success(Unit)
    }

    override suspend fun skipDose(alertId: String, reason: String?): Result<Unit> {
        alertsState.value = alertsState.value.map { alert ->
            if (alert.id == alertId) {
                alert.copy(
                    isChimeActive = false,
                    trays = alert.trays.map { tray ->
                        if (tray.trayId == "TRAY 03") {
                            tray.copy(
                                statusText = "Skipped",
                                isMissedOrUnopened = false
                            )
                        } else tray
                    }
                )
            } else alert
        }
        return Result.success(Unit)
    }
}

