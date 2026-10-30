package com.aditya.smartmedbox.feature.device.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class QrPairingParserTest {

    @Test
    fun `parse parses json format correctly`() {
        val json = """{"deviceId":"DG-4081","token":"8820"}"""
        val result = QrPairingParser.parse(json)
        assertEquals("DG-4081", result.deviceId)
        assertEquals("8820", result.pairingToken)
    }

    @Test
    fun `parse parses uri scheme correctly`() {
        val uri = "smartmedbox://pair?deviceId=DG-4081&token=8820"
        val result = QrPairingParser.parse(uri)
        assertEquals("DG-4081", result.deviceId)
        assertEquals("8820", result.pairingToken)
    }

    @Test
    fun `parse parses colon-delimited string correctly`() {
        val input = "DG-4081:8820"
        val result = QrPairingParser.parse(input)
        assertEquals("DG-4081", result.deviceId)
        assertEquals("8820", result.pairingToken)
    }

    @Test
    fun `parse parses plain device id correctly`() {
        val input = "DG-4081"
        val result = QrPairingParser.parse(input)
        assertEquals("DG-4081", result.deviceId)
        assertEquals("", result.pairingToken)
    }
}
