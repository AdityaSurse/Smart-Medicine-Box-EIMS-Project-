package com.aditya.smartmedbox.feature.device.domain.util

import com.aditya.smartmedbox.feature.device.domain.model.PairingData

object QrPairingParser {

    fun parse(rawText: String): PairingData {
        val trimmed = rawText.trim()

        // 1. JSON format: {"deviceId":"DG-4081","token":"8820"}
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            val deviceId = extractJsonValue(trimmed, listOf("deviceId", "id"))
            val token = extractJsonValue(trimmed, listOf("pairingToken", "token", "code"))
            if (deviceId.isNotBlank()) {
                return PairingData(deviceId = deviceId, pairingToken = token)
            }
        }

        // 2. URI format: smartmedbox://pair?deviceId=DG-4081&token=8820
        if (trimmed.contains("://") && trimmed.contains("?")) {
            try {
                val query = trimmed.substringAfter("?")
                val queryParams = query.split("&").associate { param ->
                    val parts = param.split("=", limit = 2)
                    parts[0] to (parts.getOrNull(1) ?: "")
                }
                val deviceId = queryParams["deviceId"] ?: queryParams["id"] ?: ""
                val token = queryParams["token"] ?: queryParams["pairingToken"] ?: queryParams["code"] ?: ""
                if (deviceId.isNotBlank()) {
                    return PairingData(deviceId = deviceId, pairingToken = token)
                }
            } catch (_: Exception) {
                // Fallback
            }
        }

        // 3. Delimited: DG-4081:8820 or DG-4081,8820 or DG-4081|8820
        val delimiters = listOf(":", ",", "|")
        for (delim in delimiters) {
            if (trimmed.contains(delim)) {
                val parts = trimmed.split(delim, limit = 2)
                val deviceId = parts[0].trim()
                val token = parts.getOrNull(1)?.trim() ?: ""
                if (deviceId.isNotBlank()) {
                    return PairingData(deviceId = deviceId, pairingToken = token)
                }
            }
        }

        // 4. Fallback: single device ID
        return PairingData(deviceId = trimmed, pairingToken = "")
    }

    private fun extractJsonValue(json: String, keys: List<String>): String {
        for (key in keys) {
            val regex = Regex("""\"$key\"\s*:\s*\"([^\"]+)\"""")
            val match = regex.find(json)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return ""
    }
}
