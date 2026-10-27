package com.aditya.smartmedbox.feature.medicines.ui.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine
import com.aditya.smartmedbox.feature.medicines.domain.repository.MedicinesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddEditMedicineViewModel @Inject constructor(
    private val medicinesRepository: MedicinesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val medicineId: String? = savedStateHandle["medicineId"]

    private val _uiState = MutableStateFlow(
        AddEditMedicineUiState(
            id = medicineId,
            isEditing = medicineId != null
        )
    )
    val uiState: StateFlow<AddEditMedicineUiState> = _uiState.asStateFlow()

    init {
        if (medicineId != null) {
            loadMedicine(medicineId)
        }
    }

    private fun loadMedicine(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val medicine = medicinesRepository.getMedicineById(id).firstOrNull()
            if (medicine != null) {
                val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
                val activeDays = daysOfWeek.map { medicine.repeatDays.contains(it) }

                _uiState.update {
                    it.copy(
                        id = medicine.id,
                        name = medicine.name,
                        dosageMg = medicine.strengthMg,
                        selectedBayIndex = medicine.compartmentIndex,
                        timings = medicine.timings,
                        activeDays = if (activeDays.any { b -> b }) activeDays else it.activeDays,
                        totalStock = medicine.totalStock,
                        formFactor = medicine.formFactor,
                        isLoading = false,
                        isEditing = true
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onEvent(event: AddEditMedicineUiEvent) {
        when (event) {
            is AddEditMedicineUiEvent.NameChanged -> {
                _uiState.update { it.copy(name = event.name, errorMessage = null) }
            }

            AddEditMedicineUiEvent.DosageIncrement -> {
                _uiState.update { it.copy(dosageMg = (it.dosageMg + 5).coerceAtMost(1000)) }
            }

            AddEditMedicineUiEvent.DosageDecrement -> {
                _uiState.update { it.copy(dosageMg = (it.dosageMg - 5).coerceAtLeast(5)) }
            }

            is AddEditMedicineUiEvent.BaySelected -> {
                _uiState.update { it.copy(selectedBayIndex = event.bayIndex) }
            }

            is AddEditMedicineUiEvent.TimeAdded -> {
                _uiState.update {
                    if (!it.timings.contains(event.time)) {
                        it.copy(timings = it.timings + event.time)
                    } else {
                        it
                    }
                }
            }

            is AddEditMedicineUiEvent.TimeRemoved -> {
                _uiState.update {
                    it.copy(timings = it.timings.filter { t -> t != event.time })
                }
            }

            is AddEditMedicineUiEvent.DayToggled -> {
                _uiState.update {
                    val updated = it.activeDays.toMutableList()
                    if (event.dayIndex in updated.indices) {
                        updated[event.dayIndex] = !updated[event.dayIndex]
                    }
                    it.copy(activeDays = updated)
                }
            }

            is AddEditMedicineUiEvent.TotalStockChanged -> {
                _uiState.update { it.copy(totalStock = event.stock.coerceAtLeast(1)) }
            }

            is AddEditMedicineUiEvent.FormFactorChanged -> {
                _uiState.update { it.copy(formFactor = event.formFactor) }
            }

            AddEditMedicineUiEvent.Save -> {
                val state = _uiState.value
                if (!state.isSaveEnabled) return

                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true) }
                    val daysLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                    val selectedDays = state.activeDays.mapIndexedNotNull { index, active ->
                        if (active) daysLabels[index] else null
                    }

                    val medicine = Medicine(
                        id = state.id ?: UUID.randomUUID().toString(),
                        name = state.name.trim(),
                        dosageDescription = "${state.dosageMg} mg • ${state.formFactor}",
                        strengthMg = state.dosageMg,
                        formFactor = state.formFactor,
                        compartmentIndex = state.selectedBayIndex,
                        compartmentLabel = "Bay 0${state.selectedBayIndex}",
                        timings = state.timings,
                        repeatDays = selectedDays,
                        currentStock = state.totalStock,
                        totalStock = state.totalStock,
                        isActive = true
                    )

                    val result = medicinesRepository.saveMedicine(medicine)
                    if (result.isSuccess) {
                        _uiState.update { it.copy(isLoading = false, isSaved = true) }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to save schedule"
                            )
                        }
                    }
                }
            }

            AddEditMedicineUiEvent.DismissAffirmation -> {
                _uiState.update { it.copy(isSaved = false) }
            }
        }
    }
}
