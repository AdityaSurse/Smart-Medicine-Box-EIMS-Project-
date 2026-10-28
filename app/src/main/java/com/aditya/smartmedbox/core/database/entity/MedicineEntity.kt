package com.aditya.smartmedbox.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dosageDescription: String,
    val strengthMg: Int = 20,
    val formFactor: String = "Coated Pill",
    val compartmentIndex: Int = 1,
    val compartmentLabel: String = "Bay 01",
    val timings: List<String> = emptyList(),
    val repeatDays: List<String> = emptyList(),
    val currentStock: Int = 18,
    val totalStock: Int = 30,
    val lowStockThreshold: Int = 5,
    val estimatedUnitWeightGrams: Float = 0.5f,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

