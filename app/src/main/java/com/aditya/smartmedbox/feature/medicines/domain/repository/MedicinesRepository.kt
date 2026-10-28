package com.aditya.smartmedbox.feature.medicines.domain.repository

import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine
import kotlinx.coroutines.flow.Flow

interface MedicinesRepository {
    fun getMedicines(): Flow<List<Medicine>>
    fun getMedicineById(id: String): Flow<Medicine?>
    suspend fun saveMedicine(medicine: Medicine): Result<Unit>
    suspend fun toggleActive(id: String): Result<Unit>
    suspend fun deleteMedicine(id: String): Result<Unit>
    suspend fun updateStock(medicineId: String, newStock: Int): Result<Unit>
    suspend fun refresh(): Result<Unit>
}
