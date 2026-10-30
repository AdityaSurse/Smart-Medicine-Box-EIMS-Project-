package com.aditya.smartmedbox.feature.device.data.provisioning

import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.device.domain.model.ProvisioningResult
import com.aditya.smartmedbox.feature.device.domain.model.ProvisioningState
import com.aditya.smartmedbox.feature.device.domain.model.WifiNetwork
import com.aditya.smartmedbox.feature.device.domain.provisioning.WifiProvisioner
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

@Singleton
class DefaultWifiProvisioner @Inject constructor(
    private val dispatchers: DispatcherProvider
) : WifiProvisioner {

    private val _provisioningState = MutableStateFlow<ProvisioningState>(ProvisioningState.Idle)

    override fun observeProvisioningState(): Flow<ProvisioningState> = _provisioningState.asStateFlow()

    override suspend fun scanAvailableNetworks(): Result<List<WifiNetwork>> = withContext(dispatchers.io) {
        _provisioningState.value = ProvisioningState.Scanning
        delay(400) // Simulated scan latency
        val networks = listOf(
            WifiNetwork(ssid = "Apothecary_Care_2.4G", rssi = -48, isSecure = true),
            WifiNetwork(ssid = "Home_Wireless_Guest", rssi = -65, isSecure = true),
            WifiNetwork(ssid = "Neighborhood_IoT_Mesh", rssi = -78, isSecure = false)
        )
        _provisioningState.value = ProvisioningState.Idle
        Result.success(networks)
    }

    override suspend fun provisionDevice(deviceId: String, pairingToken: String, ssid: String, passphrase: String): Result<ProvisioningResult> = withContext(dispatchers.io) {
        if (ssid.isBlank()) {
            val error = "Wi-Fi SSID cannot be empty"
            _provisioningState.value = ProvisioningState.Error(error)
            return@withContext Result.failure(IllegalArgumentException(error))
        }

        if (pairingToken.isBlank()) {
            val error = "Invalid Proof-of-Possession token"
            _provisioningState.value = ProvisioningState.Error(error)
            return@withContext Result.failure(IllegalArgumentException(error))
        }

        try {
            _provisioningState.value = ProvisioningState.Configuring("Establishing secure Bluetooth LE channel...")
            delay(300)

            _provisioningState.value = ProvisioningState.Configuring("Exchanging Curve25519 keys & validating token...")
            delay(300)

            _provisioningState.value = ProvisioningState.Configuring("Transmitting encrypted credentials for $ssid...")
            delay(350)

            _provisioningState.value = ProvisioningState.Configuring("Awaiting Wi-Fi connection & DHCP lease...")
            delay(400)

            val ipAddress = "192.168.1.142"
            val macAddress = "24:6F:28:AB:D4:C2"

            _provisioningState.value = ProvisioningState.Success(ipAddress)
            Result.success(ProvisioningResult.Success(ipAddress, macAddress))
        } catch (e: Exception) {
            _provisioningState.value = ProvisioningState.Error(e.message ?: "Provisioning failed")
            Result.failure(e)
        }
    }
}
