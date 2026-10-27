package com.aditya.smartmedbox.feature.medicines.domain.model

data class Medicine(
    val id: String,
    val name: String,
    val dosageDescription: String,
    val estimatedUnitWeightGrams: Float,
    val compartmentIndex: Int
)
