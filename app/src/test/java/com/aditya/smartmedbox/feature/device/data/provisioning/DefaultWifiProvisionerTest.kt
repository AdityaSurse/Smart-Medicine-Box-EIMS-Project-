package com.aditya.smartmedbox.feature.device.data.provisioning

import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.device.domain.model.ProvisioningResult
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DefaultWifiProvisionerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
        override val unconfined = testDispatcher
    }

    private lateinit var provisioner: DefaultWifiProvisioner

    @Before
    fun setUp() {
        provisioner = DefaultWifiProvisioner(dispatchers)
    }

    @Test
    fun `scanAvailableNetworks returns list of wifi networks`() = runTest(testDispatcher) {
        val result = provisioner.scanAvailableNetworks()
        assertTrue(result.isSuccess)
        val networks = result.getOrThrow()
        assertTrue(networks.isNotEmpty())
        assertEquals("Apothecary_Care_2.4G", networks.first().ssid)
    }

    @Test
    fun `provisionDevice with blank ssid returns failure`() = runTest(testDispatcher) {
        val result = provisioner.provisionDevice("DG-4081", "8820", "   ", "pass123")
        assertTrue(result.isFailure)
    }

    @Test
    fun `provisionDevice with blank token returns failure`() = runTest(testDispatcher) {
        val result = provisioner.provisionDevice("DG-4081", "   ", "Home_2.4G", "pass123")
        assertTrue(result.isFailure)
    }

    @Test
    fun `provisionDevice with valid credentials completes successfully`() = runTest(testDispatcher) {
        val result = provisioner.provisionDevice("DG-4081", "8820", "Home_2.4G", "pass123")
        assertTrue(result.isSuccess)
        val provResult = result.getOrThrow()
        assertTrue(provResult is ProvisioningResult.Success)
        assertEquals("192.168.1.142", (provResult as ProvisioningResult.Success).ipAddress)
    }
}
