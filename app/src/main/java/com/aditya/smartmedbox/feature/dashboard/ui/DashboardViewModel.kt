package com.aditya.smartmedbox.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.smartmedbox.core.designsystem.component.DoseStatus
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.dashboard.domain.repository.TelemetryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val telemetryRepository: TelemetryRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val defaultLedger = listOf(
        DashboardScheduleItem(
            id = "item_1",
            time = "8:00 AM",
            tray = "Tray 01",
            medicineName = "Levothyroxine 75mcg",
            dosage = "1 tablet • Fasted",
            status = DoseStatus.TAKEN
        ),
        DashboardScheduleItem(
            id = "item_2",
            time = "1:30 PM",
            tray = "Tray 02",
            medicineName = "Vitamin D3 2000 IU",
            dosage = "1 softgel • With lunch",
            status = DoseStatus.PENDING
        ),
        DashboardScheduleItem(
            id = "item_3",
            time = "6:00 PM",
            tray = "Tray 03",
            medicineName = "Omega-3 1000mg",
            dosage = "1 capsule • Dinner",
            status = DoseStatus.MISSED
        )
    )

    val uiState: StateFlow<DashboardUiState> = combine(
        telemetryRepository.observeTelemetry("DG-4081"),
        _isRefreshing,
        _errorMessage
    ) { telemetry, refreshing, error ->
        when {
            error != null -> DashboardUiState.Error(error)
            !telemetry.isOnline -> DashboardUiState.Offline
            else -> DashboardUiState.Success(
                telemetry = telemetry,
                isRefreshing = refreshing,
                scheduleLedger = defaultLedger
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState.Loading
    )

    private val _effect = MutableSharedFlow<DashboardUiEffect>()
    val effect: SharedFlow<DashboardUiEffect> = _effect.asSharedFlow()

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.Refresh -> refresh()
            is DashboardUiEvent.DispenseNow -> dispenseNow(event.tray)
            is DashboardUiEvent.ClearError -> _errorMessage.value = null
        }
    }

    private fun refresh() {
        viewModelScope.launch(dispatchers.io) {
            _isRefreshing.value = true
            val result = telemetryRepository.refreshTelemetry("DG-4081")
            _isRefreshing.value = false
            if (result.isSuccess) {
                _effect.emit(DashboardUiEffect.ShowSnackbar("Smart Box telemetry synchronized"))
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Sync failed"
            }
        }
    }

    private fun dispenseNow(tray: String) {
        viewModelScope.launch(dispatchers.io) {
            val result = telemetryRepository.dispenseDose(tray)
            if (result.isSuccess) {
                _effect.emit(DashboardUiEffect.ShowSnackbar("Physical compartment $tray unlocked"))
            }
        }
    }
}
