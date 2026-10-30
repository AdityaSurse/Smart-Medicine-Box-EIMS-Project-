package com.aditya.smartmedbox.feature.device.domain.provisioning

import com.aditya.smartmedbox.feature.device.domain.model.ProvisioningResult
import com.aditya.smartmedbox.feature.device.domain.model.ProvisioningState
import com.aditya.smartmedbox.feature.device.domain.model.WifiNetwork
import kotlinx.coroutines.flow.Flow

interface WifiProvisioner {
    fun observeProvisioningState(): Flow<ProvisioningState>
    suspend fun scanAvailableNetworks(): Result<List<WifiNetwork>>
    suspend fun provisionDevice(deviceId: String, pairingToken: String, ssid: String, passphrase: String): Result<ProvisioningResult>
}
