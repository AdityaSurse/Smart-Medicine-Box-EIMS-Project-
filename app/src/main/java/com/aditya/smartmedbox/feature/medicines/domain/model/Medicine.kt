package com.aditya.smartmedbox.feature.medicines.domain.model

data class Medicine(
    val id: String,
    val name: String,
    val dosageDescription: String,
    val strengthMg: Int = 20,
    val formFactor: String = "Coated Pill",
    val compartmentIndex: Int = 1,
    val compartmentLabel: String = "Bay 01",
    val timings: List<String> = listOf("08:00 AM", "08:00 PM"),
    val repeatDays: List<String> = listOf("M", "T", "W", "T", "F"),
    val currentStock: Int = 18,
    val totalStock: Int = 30,
    val lowStockThreshold: Int = 5,
    val estimatedUnitWeightGrams: Float = 0.5f,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
