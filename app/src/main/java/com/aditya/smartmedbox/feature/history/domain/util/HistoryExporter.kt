package com.aditya.smartmedbox.feature.history.domain.util

import com.aditya.smartmedbox.feature.history.domain.model.IntakeLog

object HistoryExporter {

    fun exportToCsv(logs: List<IntakeLog>): String {
        val sb = StringBuilder()
        sb.append("LogID,DateGroup,MedicineName,DosageInstructions,TimeAndBay,Status,DeltaWeightGrams,TimestampEpoch\n")
        logs.forEach { log ->
            sb.append("\"${log.id}\",")
            sb.append("\"${log.dateGroup}\",")
            sb.append("\"${log.medicineName.replace("\"", "\"\"")}\",")
            sb.append("\"${log.dosageInstructions.replace("\"", "\"\"")}\",")
            sb.append("\"${log.timeAndBay.replace("\"", "\"\"")}\",")
            sb.append("\"${log.status.name}\",")
            sb.append("${log.measuredDeltaWeightGrams},")
            sb.append("${log.timestamp}\n")
        }
        return sb.toString()
    }

    fun exportToPrintableSummary(logs: List<IntakeLog>, patientName: String = "Eleanor Vance"): String {
        val total = logs.size
        val taken = logs.count { it.status.name == "TAKEN" }
        val missed = logs.count { it.status.name == "MISSED" }
        val adherenceRate = if (total > 0) (taken * 100) / total else 100

        val sb = StringBuilder()
        sb.append("=========================================================\n")
        sb.append(" SMART MEDICINE BOX — CLINICAL ADHERENCE REPORT\n")
        sb.append("=========================================================\n")
        sb.append("Patient Name: $patientName\n")
        sb.append("Adherence Rate: $adherenceRate% ($taken taken, $missed missed of $total scheduled)\n")
        sb.append("---------------------------------------------------------\n")
        sb.append("RECENT DOSE LEDGER:\n")
        logs.take(20).forEach { log ->
            sb.append("- [${log.status.name}] ${log.dateGroup} at ${log.timeAndBay}: ${log.medicineName} (${log.dosageInstructions})\n")
        }
        sb.append("=========================================================\n")
        sb.append("Caregiver / Clinician Signature: _______________________\n")
        return sb.toString()
    }
}

