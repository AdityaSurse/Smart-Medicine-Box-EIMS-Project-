package com.aditya.smartmedbox.feature.history.domain.util

import com.aditya.smartmedbox.core.designsystem.component.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.model.IntakeLog
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryExporterTest {

    private val sampleLogs = listOf(
        IntakeLog(
            id = "log_1",
            medicineName = "Metformin",
            dosageInstructions = "500 mg",
            timeAndBay = "08:00 AM • Bay 01",
            dateGroup = "Today",
            timestamp = 1700000000L,
            measuredDeltaWeightGrams = 0.5f,
            status = DoseStatus.TAKEN
        ),
        IntakeLog(
            id = "log_2",
            medicineName = "Lisinopril",
            dosageInstructions = "10 mg",
            timeAndBay = "08:00 PM • Bay 02",
            dateGroup = "Yesterday",
            timestamp = 1699900000L,
            measuredDeltaWeightGrams = 0.0f,
            status = DoseStatus.MISSED
        )
    )

    @Test
    fun exportToCsv_generatesHeadersAndValidRows() {
        val csv = HistoryExporter.exportToCsv(sampleLogs)

        assertTrue(csv.contains("LogID,DateGroup,MedicineName,DosageInstructions,TimeAndBay,Status,DeltaWeightGrams,TimestampEpoch"))
        assertTrue(csv.contains("\"log_1\",\"Today\",\"Metformin\",\"500 mg\",\"08:00 AM • Bay 01\",\"TAKEN\",0.5,1700000000"))
        assertTrue(csv.contains("\"log_2\",\"Yesterday\",\"Lisinopril\",\"10 mg\",\"08:00 PM • Bay 02\",\"MISSED\",0.0,1699900000"))
    }

    @Test
    fun exportToPrintableSummary_calculatesAdherenceRate() {
        val summary = HistoryExporter.exportToPrintableSummary(sampleLogs, "Eleanor Vance")

        assertTrue(summary.contains("Patient Name: Eleanor Vance"))
        assertTrue(summary.contains("Adherence Rate: 50% (1 taken, 1 missed of 2 scheduled)"))
        assertTrue(summary.contains("Metformin"))
        assertTrue(summary.contains("Lisinopril"))
    }
}
