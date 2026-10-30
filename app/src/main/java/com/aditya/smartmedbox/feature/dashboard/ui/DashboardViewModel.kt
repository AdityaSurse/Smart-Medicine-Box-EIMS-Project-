package com.aditya.smartmedbox.feature.dashboard.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.smartmedbox.core.designsystem.component.DoseStatus as ComponentDoseStatus
import com.aditya.smartmedbox.core.notification.NotificationPermissionChecker
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.dashboard.domain.repository.TelemetryRepository
import com.aditya.smartmedbox.feature.dashboard.domain.usecase.ComputeTimeLeftUseCase
import com.aditya.smartmedbox.feature.dashboard.domain.usecase.GetNextDoseUseCase
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.usecase.GetWeeklyMissedCountUseCase
import com.aditya.smartmedbox.feature.schedule.domain.alarm.DoseAlarmScheduler
import com.aditya.smartmedbox.feature.schedule.domain.usecase.GetTodayScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    private val getTodayScheduleUseCase: GetTodayScheduleUseCase,
    private val getNextDoseUseCase: GetNextDoseUseCase,
    private val getWeeklyMissedCountUseCase: GetWeeklyMissedCountUseCase,
    private val computeTimeLeftUseCase: ComputeTimeLeftUseCase,
    private val doseAlarmScheduler: DoseAlarmScheduler,
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
    private val notificationPermissionChecker: NotificationPermissionChecker
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isExactAlarmBannerDismissed = MutableStateFlow(false)
    private val _isNotificationBannerDismissed = MutableStateFlow(false)

    private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

    private data class DashboardFlags(
        val first: Boolean,
        val second: String?,
        val third: Boolean,
        val fourth: Boolean
    )

    val uiState: StateFlow<DashboardUiState> = combine(
        telemetryRepository.observeTelemetry("DG-4081"),
        getTodayScheduleUseCase(),
        getNextDoseUseCase(),
        getWeeklyMissedCountUseCase(),
        combine(
            _isRefreshing,
            _errorMessage,
            _isExactAlarmBannerDismissed,
            _isNotificationBannerDismissed
        ) { r, e, ea, nb -> DashboardFlags(r, e, ea, nb) }
    ) { telemetry, todayEvents, nextDose, missedCount, flags ->
        val refreshing = flags.first
        val error = flags.second
        val alarmBannerDismissed = flags.third
        val notifBannerDismissed = flags.fourth
        when {
            error != null -> DashboardUiState.Error(error)
            !telemetry.isOnline -> DashboardUiState.Offline
            else -> {
                val now = System.currentTimeMillis()
                val hasMissedDoseToday = todayEvents.any { it.status == DoseStatus.MISSED }
                val hasPendingDueNow = todayEvents.any {
                    it.status == DoseStatus.PENDING && now >= it.scheduledTimeEpochMillis
                }

                val cadenceStatus = when {
                    hasMissedDoseToday || missedCount > 0 -> "Dose missed"
                    hasPendingDueNow -> "Pending"
                    else -> "On time"
                }

                val scheduleLedger = todayEvents.map { event ->
                    DashboardScheduleItem(
                        id = event.id,
                        time = timeFormatter.format(Date(event.scheduledTimeEpochMillis)),
                        tray = event.bayLabel,
                        medicineName = event.medicineName,
                        dosage = event.dosage,
                        status = when (event.status) {
                            DoseStatus.TAKEN -> ComponentDoseStatus.TAKEN
                            DoseStatus.PENDING -> ComponentDoseStatus.PENDING
                            DoseStatus.MISSED -> ComponentDoseStatus.MISSED
                        }
                    )
                }

                val nextDoseTime = nextDose?.let { timeFormatter.format(Date(it.scheduledTimeEpochMillis)) }
                    ?: telemetry.nextDoseTime
                val nextDoseCountdown = nextDose?.let { computeTimeLeftUseCase(it.scheduledTimeEpochMillis) }
                    ?: telemetry.nextDoseCountdown

                val updatedTelemetry = telemetry.copy(
                    cadenceStatus = cadenceStatus,
                    nextDoseMedicine = nextDose?.medicineName ?: telemetry.nextDoseMedicine,
                    nextDoseDosage = nextDose?.dosage ?: telemetry.nextDoseDosage,
                    nextDoseTime = nextDoseTime,
                    nextDoseCountdown = nextDoseCountdown,
                    nextDoseTray = nextDose?.bayLabel ?: telemetry.nextDoseTray
                )

                val totalDosesThisWeek = maxOf(1, todayEvents.size * 7)
                val adherencePercent = ((1.0 - (missedCount.toDouble() / totalDosesThisWeek)) * 100)
                    .toInt()
                    .coerceIn(0, 100)

                val isExactAlarmRequired = !doseAlarmScheduler.canScheduleExactAlarms() && !alarmBannerDismissed
                val isNotificationRequired = !notificationPermissionChecker.hasNotificationPermission(context) && !notifBannerDismissed

                DashboardUiState.Success(
                    telemetry = updatedTelemetry,
                    isRefreshing = refreshing,
                    scheduleLedger = scheduleLedger,
                    weeklyAdherencePercent = adherencePercent,
                    missedThisWeekCount = missedCount,
                    isExactAlarmPermissionRequired = isExactAlarmRequired,
                    isNotificationPermissionRequired = isNotificationRequired
                )
            }
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
            is DashboardUiEvent.RequestExactAlarmPermission -> {
                viewModelScope.launch(dispatchers.main) {
                    _effect.emit(DashboardUiEffect.OpenExactAlarmSettings)
                }
            }
            is DashboardUiEvent.DismissExactAlarmBanner -> {
                _isExactAlarmBannerDismissed.value = true
            }
            is DashboardUiEvent.RequestNotificationPermission -> {
                viewModelScope.launch(dispatchers.main) {
                    _effect.emit(DashboardUiEffect.NavigateToNotificationRationale)
                }
            }
            is DashboardUiEvent.DismissNotificationBanner -> {
                _isNotificationBannerDismissed.value = true
            }
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
