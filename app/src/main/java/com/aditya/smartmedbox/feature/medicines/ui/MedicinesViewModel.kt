package com.aditya.smartmedbox.feature.medicines.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.smartmedbox.feature.medicines.domain.repository.MedicinesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MedicinesViewModel @Inject constructor(
    private val medicinesRepository: MedicinesRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val filterLowStock = MutableStateFlow(false)

    val uiState: StateFlow<MedicinesUiState> = combine(
        medicinesRepository.getMedicines(),
        searchQuery,
        filterLowStock
    ) { medicines, query, onlyLowStock ->
        if (medicines.isEmpty()) {
            MedicinesUiState.Empty(isSearchResult = false)
        } else {
            val filtered = medicines.filter { med ->
                val matchesQuery = query.isBlank() ||
                    med.name.contains(query, ignoreCase = true) ||
                    med.dosageDescription.contains(query, ignoreCase = true) ||
                    med.compartmentLabel.contains(query, ignoreCase = true)
                val matchesStock = !onlyLowStock || med.currentStock <= 5
                matchesQuery && matchesStock
            }

            if (filtered.isEmpty() && (query.isNotBlank() || onlyLowStock)) {
                MedicinesUiState.Empty(isSearchResult = true)
            } else {
                MedicinesUiState.Success(
                    medicines = medicines,
                    filteredMedicines = filtered,
                    searchQuery = query,
                    filterLowStock = onlyLowStock
                )
            }
        }
    }.catch { error ->
        emit(MedicinesUiState.Error(error.localizedMessage ?: "Failed to load medications"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MedicinesUiState.Loading
    )

    fun onEvent(event: MedicinesUiEvent) {
        when (event) {
            is MedicinesUiEvent.Search -> {
                searchQuery.value = event.query
            }
            is MedicinesUiEvent.ToggleFilterLowStock -> {
                filterLowStock.value = !filterLowStock.value
            }
            is MedicinesUiEvent.ToggleActive -> {
                viewModelScope.launch {
                    medicinesRepository.toggleActive(event.id)
                }
            }
            is MedicinesUiEvent.DeleteMedicine -> {
                viewModelScope.launch {
                    medicinesRepository.deleteMedicine(event.id)
                }
            }
            is MedicinesUiEvent.Refresh, MedicinesUiEvent.LoadMedicines -> {
                viewModelScope.launch {
                    medicinesRepository.refresh()
                }
            }
        }
    }
}
