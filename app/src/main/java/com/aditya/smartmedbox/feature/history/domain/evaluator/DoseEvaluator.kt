package com.aditya.smartmedbox.feature.history.domain.evaluator

import com.aditya.smartmedbox.feature.history.domain.model.BoxHardwareEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.model.HardwareEventType
import com.aditya.smartmedbox.feature.schedule.domain.model.DoseSchedule
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.math.abs

data class DoseEvaluationResult(
    val status: DoseStatus,
    val loggedTimeEpochMillis: Long? = null,
    val recordedWeightGrams: Float? = null,
    val confidence: EvaluationConfidence = EvaluationConfidence.HIGH,
    val anomalyNote: String? = null
)

enum class EvaluationConfidence {
    HIGH,
    MEDIUM,
    LOW,
    ANOMALY
}

class DoseEvaluator @Inject constructor() {
    private var preWindowMinutes: Long = 15L
    private var defaultToleranceFraction: Float = 0.35f

    constructor(
        preWindowMinutes: Long,
        defaultToleranceFraction: Float = 0.35f
    ) : this() {
        this.preWindowMinutes = preWindowMinutes
        this.defaultToleranceFraction = defaultToleranceFraction
    }

    /**
     * Evaluates the intake state of a scheduled dose given real-time and offline box hardware events.
     *
     * State Transitions from Hardware Poster Contract:
     * - Prescription -> Dose time reached -> Box opened (reed switch) -> Weight decreased (load cell) -> TAKEN
     * - Dose time reached -> Box opened -> NO weight drop -> PENDING during grace, MISSED after grace
     * - Grace period expired with no valid intake -> MISSED
     * - Device offline at dose time -> Reconciled to TAKEN once buffered RTC events sync
     */
    fun evaluate(
        schedule: DoseSchedule,
        scheduledDoseEpochMillis: Long,
        expectedPillWeightGrams: Float,
        boxEvents: List<BoxHardwareEvent>,
        currentEvaluationTimeMillis: Long,
        isDeviceOnline: Boolean = true
    ): DoseEvaluationResult {
        val gracePeriodMillis = schedule.gracePeriodMinutes * 60 * 1000L
        val windowStart = scheduledDoseEpochMillis - (preWindowMinutes * 60 * 1000L)
        val windowEnd = scheduledDoseEpochMillis + gracePeriodMillis

        // 1. Filter relevant events around the dose window
        val windowEvents = boxEvents.filter { event ->
            event.timestampEpochMillis in windowStart..windowEnd ||
                (event.isOfflineRecorded && event.timestampEpochMillis in windowStart..windowEnd)
        }.sortedBy { it.timestampEpochMillis }

        val boxOpenEvents = windowEvents.filter { it.eventType == HardwareEventType.BOX_OPENED }
        val weightDropEvents = windowEvents.filter {
            it.eventType == HardwareEventType.WEIGHT_DROP ||
                (it.eventType == HardwareEventType.BOX_CLOSED && it.deltaWeightGrams < -0.05f) ||
                (it.deltaWeightGrams < -0.05f)
        }

        // 2. Identify if an extraction occurred matching compartment or weight
        val matchingWeightDrop = weightDropEvents.firstOrNull { drop ->
            val dropMagnitude = abs(drop.deltaWeightGrams)
            val minWeight = (expectedPillWeightGrams * (1f - defaultToleranceFraction)).coerceAtLeast(0.1f)
            val isWeightMatch = dropMagnitude >= minWeight

            val isBayMatch = if (drop.trayBay.isNotBlank()) {
                drop.trayBay.equals(schedule.compartmentBay, ignoreCase = true)
            } else {
                true // Global load cell without per-bay localization
            }

            isBayMatch && isWeightMatch
        }

        val hasBoxOpen = boxOpenEvents.isNotEmpty()
        val hasWeightDrop = matchingWeightDrop != null

        // Happy Path: Box opened and confirmed weight drop
        if (hasBoxOpen && hasWeightDrop) {
            val drop = matchingWeightDrop!!
            val dropMagnitude = abs(drop.deltaWeightGrams)
            val weightDifference = abs(dropMagnitude - expectedPillWeightGrams)
            val isWithinTightTolerance = weightDifference <= (expectedPillWeightGrams * defaultToleranceFraction)

            val confidence = if (isWithinTightTolerance) {
                EvaluationConfidence.HIGH
            } else {
                EvaluationConfidence.MEDIUM
            }

            val note = if (drop.isOfflineRecorded) {
                "Intake confirmed via offline hardware RTC event log"
            } else if (!isWithinTightTolerance) {
                "Weight drop detected ($dropMagnitude g) differed from unit pill target ($expectedPillWeightGrams g)"
            } else {
                null
            }

            return DoseEvaluationResult(
                status = DoseStatus.TAKEN,
                loggedTimeEpochMillis = drop.timestampEpochMillis,
                recordedWeightGrams = dropMagnitude,
                confidence = confidence,
                anomalyNote = note
            )
        }

        // Edge Case: Weight drop occurred WITHOUT box opening (anomalous sensor drop / jarred box)
        if (!hasBoxOpen && hasWeightDrop) {
            val drop = matchingWeightDrop!!
            val dropMagnitude = abs(drop.deltaWeightGrams)
            return DoseEvaluationResult(
                status = DoseStatus.TAKEN,
                loggedTimeEpochMillis = drop.timestampEpochMillis,
                recordedWeightGrams = dropMagnitude,
                confidence = EvaluationConfidence.ANOMALY,
                anomalyNote = "Weight drop recorded without detected lid opening"
            )
        }

        // Edge Case: Box opened but NO weight drop detected
        if (hasBoxOpen && !hasWeightDrop) {
            val isStillWithinGrace = currentEvaluationTimeMillis <= windowEnd
            return if (isStillWithinGrace) {
                DoseEvaluationResult(
                    status = DoseStatus.PENDING,
                    confidence = EvaluationConfidence.MEDIUM,
                    anomalyNote = "Lid opened but no pill extraction registered on load cell"
                )
            } else {
                DoseEvaluationResult(
                    status = DoseStatus.MISSED,
                    confidence = EvaluationConfidence.HIGH,
                    anomalyNote = "Grace period elapsed; lid was opened with 0 weight reduction"
                )
            }
        }

        // Edge Case: No box activity recorded at all
        val isGraceExpired = currentEvaluationTimeMillis > windowEnd
        return if (isGraceExpired) {
            val note = if (!isDeviceOnline) {
                "Device offline; pending hardware event buffer reconciliation"
            } else {
                "Dose window elapsed without lid opening"
            }
            DoseEvaluationResult(
                status = DoseStatus.MISSED,
                confidence = if (isDeviceOnline) EvaluationConfidence.HIGH else EvaluationConfidence.LOW,
                anomalyNote = note
            )
        } else {
            DoseEvaluationResult(
                status = DoseStatus.PENDING,
                confidence = EvaluationConfidence.HIGH,
                anomalyNote = if (!isDeviceOnline) "Device offline at scheduled dose time" else null
            )
        }
    }

    /**
     * Accurately calculates epoch milliseconds for a scheduled time string (e.g. "08:00 AM", "8:00 PM")
     * taking into account local timezone and Daylight Saving Time (DST) offsets.
     */
    fun computeDoseEpochForDate(timeStr: String, date: LocalDate, zoneId: ZoneId): Long {
        val parts = timeStr.trim().split(" ", ":")
        var hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val isPm = timeStr.contains("PM", ignoreCase = true)
        val isAm = timeStr.contains("AM", ignoreCase = true)

        if (isPm && hour < 12) hour += 12
        if (isAm && hour == 12) hour = 0

        val localTime = LocalTime.of(hour, minute, 0)
        val zonedDateTime = ZonedDateTime.of(date, localTime, zoneId)
        return zonedDateTime.toInstant().toEpochMilli()
    }
}
