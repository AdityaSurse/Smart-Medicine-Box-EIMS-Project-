package com.aditya.smartmedbox.feature.device.ui

import app.cash.turbine.test
import com.aditya.smartmedbox.feature.device.domain.model.ProvisioningResult
import com.aditya.smartmedbox.feature.device.domain.model.ProvisioningState
import com.aditya.smartmedbox.feature.device.domain.model.SmartBoxDevice
import com.aditya.smartmedbox.feature.device.domain.model.WifiNetwork
import com.aditya.smartmedbox.feature.device.domain.provisioning.WifiProvisioner
import com.aditya.smartmedbox.feature.device.domain.repository.DeviceRepository
import com.aditya.smartmedbox.feature.device.domain.usecase.ObserveDeviceUseCase
import com.aditya.smartmedbox.feature.device.domain.usecase.PairDeviceUseCase
import com.aditya.smartmedbox.feature.device.domain.usecase.UnpairDeviceUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val deviceRepository: DeviceRepository = mockk()
    private val pairDeviceUseCase: PairDeviceUseCase = mockk()
    private val unpairDeviceUseCase: UnpairDeviceUseCase = mockk()
    private val observeDeviceUseCase: ObserveDeviceUseCase = mockk()
    private val fakeWifiProvisioner = FakeWifiProvisioner()

    private val observedDeviceFlow = MutableStateFlow<SmartBoxDevice?>(null)

    private lateinit var viewModel: DeviceViewModel

    private class FakeWifiProvisioner : WifiProvisioner {
        val provisioningStateFlow = MutableStateFlow<ProvisioningState>(ProvisioningState.Idle)
        var networks = listOf(WifiNetwork("Test_WiFi", -50))

        override fun observeProvisioningState(): Flow<ProvisioningState> = provisioningStateFlow

        override suspend fun scanAvailableNetworks(): Result<List<WifiNetwork>> = Result.success(networks)

        override suspend fun provisionDevice(deviceId: String, pairingToken: String, ssid: String, passphrase: String): Result<ProvisioningResult> = Result.success(
            ProvisioningResult.Success("192.168.1.142", "24:6F:28:AB:D4:C2")
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { observeDeviceUseCase() } returns observedDeviceFlow
        coEvery { deviceRepository.startScan() } returns Result.success(Unit)
        coEvery { pairDeviceUseCase(any(), any()) } returns Result.success(Unit)
        coEvery { unpairDeviceUseCase(any()) } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DeviceViewModel {
        return DeviceViewModel(
            deviceRepository = deviceRepository,
            pairDeviceUseCase = pairDeviceUseCase,
            unpairDeviceUseCase = unpairDeviceUseCase,
            observeDeviceUseCase = observeDeviceUseCase,
            wifiProvisioner = fakeWifiProvisioner
        )
    }

    @Test
    fun `uiState emits Empty when no device is paired`() = runTest(testDispatcher) {
        observedDeviceFlow.value = null
        viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(DeviceUiState.Loading, awaitItem())
            val item = awaitItem()
            assertTrue(item is DeviceUiState.Empty)
        }
    }

    @Test
    fun `uiState emits Success when device is paired`() = runTest(testDispatcher) {
        val testDevice = SmartBoxDevice(deviceId = "DG-4081")
        observedDeviceFlow.value = testDevice
        viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(DeviceUiState.Loading, awaitItem())
            val item = awaitItem()
            assertTrue(item is DeviceUiState.Success)
            assertEquals("DG-4081", (item as DeviceUiState.Success).device.deviceId)
        }
    }

    @Test
    fun `onEvent PairDevice executes pairDeviceUseCase and navigates to add medicine`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.onEvent(DeviceUiEvent.PairDevice("DG-4081", "8820"))

            val effect1 = awaitItem()
            assertTrue(effect1 is DeviceUiEffect.ShowSnackbar)
            val effect2 = awaitItem()
            assertEquals(DeviceUiEffect.NavigateToAddMedicine, effect2)
        }
        coVerify(exactly = 1) { pairDeviceUseCase("DG-4081", "8820") }
    }

    @Test
    fun `onEvent UnpairDevice executes unpairDeviceUseCase and shows snackbar`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.onEvent(DeviceUiEvent.UnpairDevice("DG-4081"))

            val effect = awaitItem()
            assertTrue(effect is DeviceUiEffect.ShowSnackbar)
            assertTrue((effect as DeviceUiEffect.ShowSnackbar).message.contains("unpaired"))
        }
        coVerify(exactly = 1) { unpairDeviceUseCase("DG-4081") }
    }

    @Test
    fun `onEvent ScanQrCode parses payload and triggers pairing`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        val qrPayload = "smartmedbox://pair?deviceId=DG-9999&token=1234"
        viewModel.onEvent(DeviceUiEvent.ScanQrCode(qrPayload))

        testScheduler.advanceUntilIdle()
        coVerify(exactly = 1) { pairDeviceUseCase("DG-9999", "1234") }
    }

    @Test
    fun `onEvent StartWifiProvisioning scans available networks`() = runTest(testDispatcher) {
        val testDevice = SmartBoxDevice(deviceId = "DG-4081")
        observedDeviceFlow.value = testDevice
        viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(DeviceUiState.Loading, awaitItem())
            testScheduler.advanceUntilIdle()
            val initial = awaitItem() as DeviceUiState.Success
            assertEquals("DG-4081", initial.device.deviceId)

            viewModel.onEvent(DeviceUiEvent.StartWifiProvisioning)
            testScheduler.advanceUntilIdle()

            var updated = awaitItem() as DeviceUiState.Success
            while (updated.availableWifiNetworks.isEmpty()) {
                updated = awaitItem() as DeviceUiState.Success
            }
            assertTrue(updated.isWifiProvisioningActive)
            assertEquals(1, updated.availableWifiNetworks.size)
            assertEquals("Test_WiFi", updated.availableWifiNetworks.first().ssid)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
