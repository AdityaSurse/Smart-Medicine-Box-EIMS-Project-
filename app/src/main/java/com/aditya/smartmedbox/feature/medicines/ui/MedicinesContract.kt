package com.aditya.smartmedbox.feature.medicines.ui

import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine

sealed interface MedicinesUiState {
    data object Loading : MedicinesUiState

    data class Success(
        val medicines: List<Medicine>,
        val filteredMedicines: List<Medicine>,
        val searchQuery: String = "",
        val filterLowStock: Boolean = false,
        val isOffline: Boolean = false
    ) : MedicinesUiState

    data class Empty(
        val isSearchResult: Boolean = false
    ) : MedicinesUiState

    data class Error(
        val message: String
    ) : MedicinesUiState
}

sealed interface MedicinesUiEvent {
    data object LoadMedicines : MedicinesUiEvent
    data class Search(val query: String) : MedicinesUiEvent
    data object ToggleFilterLowStock : MedicinesUiEvent
    data class ToggleActive(val id: String) : MedicinesUiEvent
    data class DeleteMedicine(val id: String) : MedicinesUiEvent
    data object Refresh : MedicinesUiEvent
}
