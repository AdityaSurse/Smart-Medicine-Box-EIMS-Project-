package com.aditya.smartmedbox.feature.history.domain.evaluator

import com.aditya.smartmedbox.feature.history.domain.model.BoxHardwareEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.model.HardwareEventType
import com.aditya.smartmedbox.feature.schedule.domain.model.DoseSchedule
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DoseEvaluatorTest {

    private lateinit var evaluator: DoseEvaluator

    private val sampleSchedule = DoseSchedule(
        id = "sch_amox",
        medicineId = "med_amox",
        medicineName = "Amoxicillin",
        times = listOf("08:00 AM"),
        daysOfWeek = listOf(1, 2, 3, 4, 5),
        gracePeriodMinutes = 30,
        compartmentBay = "Bay 01",
        isActive = true
    )

    private val scheduledEpoch = 1729000000000L // Reference: 8:00 AM

    @Before
    fun setUp() {
        evaluator = DoseEvaluator(preWindowMinutes = 15L, defaultToleranceFraction = 0.35f)
    }

    // --- Table-Driven Test Matrix ---
    data class TestCase(
        val description: String,
        val scheduledEpoch: Long,
        val pillWeightGrams: Float,
        val events: List<BoxHardwareEvent>,
        val evaluationTime: Long,
        val isDeviceOnline: Boolean,
        val expectedStatus: DoseStatus,
        val expectedConfidence: EvaluationConfidence
    )

    @Test
    fun runTableDrivenDoseEvaluationSuite() {
        val testCases = listOf(
            TestCase(
                description = "Standard Happy Path: Box opened and pill weight removed within grace period",
                scheduledEpoch = scheduledEpoch,
                pillWeightGrams = 0.55f,
                events = listOf(
                    BoxHardwareEvent(
                        eventId = "evt_01",
                        eventType = HardwareEventType.BOX_OPENED,
                        timestampEpochMillis = scheduledEpoch + 5 * 60 * 1000L,
                        trayBay = "Bay 01"
                    ),
                    BoxHardwareEvent(
                        eventId = "evt_02",
                        eventType = HardwareEventType.WEIGHT_DROP,
                        timestampEpochMillis = scheduledEpoch + 5 * 60 * 1000L + 2000L,
                        deltaWeightGrams = -0.55f,
                        trayBay = "Bay 01"
                    ),
                    BoxHardwareEvent(
                        eventId = "evt_03",
                        eventType = HardwareEventType.BOX_CLOSED,
                        timestampEpochMillis = scheduledEpoch + 5 * 60 * 1000L + 8000L,
                        trayBay = "Bay 01"
                    )
                ),
                evaluationTime = scheduledEpoch + 10 * 60 * 1000L,
                isDeviceOnline = true,
                expectedStatus = DoseStatus.TAKEN,
                expectedConfidence = EvaluationConfidence.HIGH
            ),
            TestCase(
                description = "Box opened but NO weight drop: Evaluated DURING grace period -> PENDING",
                scheduledEpoch = scheduledEpoch,
                pillWeightGrams = 0.55f,
                events = listOf(
                    BoxHardwareEvent(
                        eventId = "evt_open_only",
                        eventType = HardwareEventType.BOX_OPENED,
                        timestampEpochMillis = scheduledEpoch + 2 * 60 * 1000L,
                        trayBay = "Bay 01"
                    ),
                    BoxHardwareEvent(
                        eventId = "evt_closed_no_drop",
                        eventType = HardwareEventType.BOX_CLOSED,
                        timestampEpochMillis = scheduledEpoch + 2 * 60 * 1000L + 5000L,
                        deltaWeightGrams = 0.0f,
                        trayBay = "Bay 01"
                    )
                ),
                evaluationTime = scheduledEpoch + 10 * 60 * 1000L, // Still in 30m grace
                isDeviceOnline = true,
                expectedStatus = DoseStatus.PENDING,
                expectedConfidence = EvaluationConfidence.MEDIUM
            ),
            TestCase(
                description = "Box opened but NO weight drop: Evaluated AFTER grace period -> MISSED",
                scheduledEpoch = scheduledEpoch,
                pillWeightGrams = 0.55f,
                events = listOf(
                    BoxHardwareEvent(
                        eventId = "evt_open_only",
                        eventType = HardwareEventType.BOX_OPENED,
                        timestampEpochMillis = scheduledEpoch + 2 * 60 * 1000L,
                        trayBay = "Bay 01"
                    )
                ),
                evaluationTime = scheduledEpoch + 45 * 60 * 1000L, // 45m > 30m grace
                isDeviceOnline = true,
                expectedStatus = DoseStatus.MISSED,
                expectedConfidence = EvaluationConfidence.HIGH
            ),
            TestCase(
                description = "Sensor Anomaly: Weight drop occurred without lid opening detected",
                scheduledEpoch = scheduledEpoch,
                pillWeightGrams = 0.55f,
                events = listOf(
                    BoxHardwareEvent(
                        eventId = "evt_jarred_drop",
                        eventType = HardwareEventType.WEIGHT_DROP,
                        timestampEpochMillis = scheduledEpoch + 6 * 60 * 1000L,
                        deltaWeightGrams = -0.55f,
                        trayBay = "Bay 01"
                    )
                ),
                evaluationTime = scheduledEpoch + 15 * 60 * 1000L,
                isDeviceOnline = true,
                expectedStatus = DoseStatus.TAKEN,
                expectedConfidence = EvaluationConfidence.ANOMALY
            ),
            TestCase(
                description = "Grace period elapsed with zero box events -> MISSED",
                scheduledEpoch = scheduledEpoch,
                pillWeightGrams = 0.55f,
                events = emptyList(),
                evaluationTime = scheduledEpoch + 40 * 60 * 1000L,
                isDeviceOnline = true,
                expectedStatus = DoseStatus.MISSED,
                expectedConfidence = EvaluationConfidence.HIGH
            ),
            TestCase(
                description = "Device offline at dose time and grace expired -> MISSED (pending sync)",
                scheduledEpoch = scheduledEpoch,
                pillWeightGrams = 0.55f,
                events = emptyList(),
                evaluationTime = scheduledEpoch + 35 * 60 * 1000L,
                isDeviceOnline = false,
                expectedStatus = DoseStatus.MISSED,
                expectedConfidence = EvaluationConfidence.LOW
            ),
            TestCase(
                description = "Device offline at dose time, then syncs buffered RTC events -> Reconciled to TAKEN",
                scheduledEpoch = scheduledEpoch,
                pillWeightGrams = 0.55f,
                events = listOf(
                    BoxHardwareEvent(
                        eventId = "evt_rtc_open",
                        eventType = HardwareEventType.BOX_OPENED,
                        timestampEpochMillis = scheduledEpoch + 8 * 60 * 1000L,
                        isOfflineRecorded = true,
                        trayBay = "Bay 01"
                    ),
                    BoxHardwareEvent(
                        eventId = "evt_rtc_drop",
                        eventType = HardwareEventType.WEIGHT_DROP,
                        timestampEpochMillis = scheduledEpoch + 8 * 60 * 1000L + 2000L,
                        deltaWeightGrams = -0.55f,
                        isOfflineRecorded = true,
                        trayBay = "Bay 01"
                    )
                ),
                evaluationTime = scheduledEpoch + 120 * 60 * 1000L, // 2 hours later when online
                isDeviceOnline = true,
                expectedStatus = DoseStatus.TAKEN,
                expectedConfidence = EvaluationConfidence.HIGH
            )
        )

        for (testCase in testCases) {
            val result = evaluator.evaluate(
                schedule = sampleSchedule,
                scheduledDoseEpochMillis = testCase.scheduledEpoch,
                expectedPillWeightGrams = testCase.pillWeightGrams,
                boxEvents = testCase.events,
                currentEvaluationTimeMillis = testCase.evaluationTime,
                isDeviceOnline = testCase.isDeviceOnline
            )

            assertEquals(
                "Failed test: ${testCase.description}",
                testCase.expectedStatus,
                result.status
            )
            assertEquals(
                "Failed confidence on: ${testCase.description}",
                testCase.expectedConfidence,
                result.confidence
            )
        }
    }

    @Test
    fun evaluate_handlesMultipleDosesInDifferentBays() {
        val scheduleBay2 = sampleSchedule.copy(
            id = "sch_metf",
            medicineId = "med_metf",
            medicineName = "Metformin",
            compartmentBay = "Bay 02"
        )

        val events = listOf(
            BoxHardwareEvent(
                eventId = "evt_b1",
                eventType = HardwareEventType.BOX_OPENED,
                timestampEpochMillis = scheduledEpoch + 60000L,
                trayBay = "Bay 01"
            ),
            BoxHardwareEvent(
                eventId = "evt_b1_drop",
                eventType = HardwareEventType.WEIGHT_DROP,
                timestampEpochMillis = scheduledEpoch + 62000L,
                deltaWeightGrams = -0.55f,
                trayBay = "Bay 01"
            )
        )

        val bay1Result = evaluator.evaluate(
            schedule = sampleSchedule,
            scheduledDoseEpochMillis = scheduledEpoch,
            expectedPillWeightGrams = 0.55f,
            boxEvents = events,
            currentEvaluationTimeMillis = scheduledEpoch + 120000L
        )

        val bay2Result = evaluator.evaluate(
            schedule = scheduleBay2,
            scheduledDoseEpochMillis = scheduledEpoch,
            expectedPillWeightGrams = 0.85f,
            boxEvents = events,
            currentEvaluationTimeMillis = scheduledEpoch + 120000L
        )

        assertEquals(DoseStatus.TAKEN, bay1Result.status)
        assertEquals(DoseStatus.PENDING, bay2Result.status)
    }

    @Test
    fun computeDoseEpochForDate_preservesLocalWallClockAcrossTimezones() {
        val zoneNy = ZoneId.of("America/New_York")
        val dateStandard = LocalDate.of(2026, 1, 15) // Winter (EST, UTC-5)
        val dateDst = LocalDate.of(2026, 7, 15) // Summer (EDT, UTC-4)

        val epochWinter = evaluator.computeDoseEpochForDate("08:00 AM", dateStandard, zoneNy)
        val epochSummer = evaluator.computeDoseEpochForDate("08:00 AM", dateDst, zoneNy)

        assertNotNull(epochWinter)
        assertNotNull(epochSummer)
        assertTrue(epochSummer > epochWinter)

        // Verify wall clock hour resolves to 8:00 AM in New York
        val zdtWinter = java.time.Instant.ofEpochMilli(epochWinter).atZone(zoneNy)
        val zdtSummer = java.time.Instant.ofEpochMilli(epochSummer).atZone(zoneNy)
        assertEquals(8, zdtWinter.hour)
        assertEquals(0, zdtWinter.minute)
        assertEquals(8, zdtSummer.hour)
        assertEquals(0, zdtSummer.minute)
    }
}
