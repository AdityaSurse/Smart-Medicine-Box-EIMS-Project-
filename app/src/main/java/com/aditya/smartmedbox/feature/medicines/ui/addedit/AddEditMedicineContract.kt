package com.aditya.smartmedbox.feature.medicines.ui.addedit

data class AddEditMedicineUiState(
    val id: String? = null,
    val name: String = "",
    val dosageMg: Int = 20,
    val selectedBayIndex: Int = 2,
    val timings: List<String> = listOf("08:00 PM"),
    val activeDays: List<Boolean> = listOf(true, true, true, true, true, false, false), // M T W T F S S
    val totalStock: Int = 30,
    val formFactor: String = "Coated Pill",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val isSaveEnabled: Boolean
        get() = name.isNotBlank() && timings.isNotEmpty() && activeDays.any { it } && totalStock > 0
}

sealed interface AddEditMedicineUiEvent {
    data class NameChanged(val name: String) : AddEditMedicineUiEvent
    data object DosageIncrement : AddEditMedicineUiEvent
    data object DosageDecrement : AddEditMedicineUiEvent
    data class BaySelected(val bayIndex: Int) : AddEditMedicineUiEvent
    data class TimeAdded(val time: String) : AddEditMedicineUiEvent
    data class TimeRemoved(val time: String) : AddEditMedicineUiEvent
    data class DayToggled(val dayIndex: Int) : AddEditMedicineUiEvent
    data class TotalStockChanged(val stock: Int) : AddEditMedicineUiEvent
    data class FormFactorChanged(val formFactor: String) : AddEditMedicineUiEvent
    data object Save : AddEditMedicineUiEvent
    data object DismissAffirmation : AddEditMedicineUiEvent
}
