package com.aditya.smartmedbox.feature.medicines.ui

sealed interface MedicinesUiState {
    data object Loading : MedicinesUiState

    data class Success(val medicines: List<String> = emptyList()) : MedicinesUiState
}

sealed interface MedicinesUiEvent {
    data object LoadMedicines : MedicinesUiEvent
}
