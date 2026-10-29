package com.aditya.smartmedbox.feature.medicines.data.repository

import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine
import com.aditya.smartmedbox.feature.medicines.domain.repository.MedicinesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

@Singleton
class FakeMedicinesRepository @Inject constructor() : MedicinesRepository {

    private val defaultMedicines = listOf(
        Medicine(
            id = "med_1",
            name = "Amoxicillin",
            dosageDescription = "500 mg • Antibiotic capsule",
            strengthMg = 500,
            formFactor = "Capsule",
            compartmentIndex = 1,
            compartmentLabel = "Bay 01",
            timings = listOf("08:00 AM", "08:00 PM"),
            repeatDays = listOf("M", "T", "W", "T", "F", "S", "S"),
            currentStock = 18,
            totalStock = 30,
            isActive = true
        ),
        Medicine(
            id = "med_2",
            name = "Metformin",
            dosageDescription = "850 mg • Blood sugar regulation",
            strengthMg = 850,
            formFactor = "Tablet",
            compartmentIndex = 2,
            compartmentLabel = "Bay 02",
            timings = listOf("12:30 PM"),
            repeatDays = listOf("M", "T", "W", "T", "F"),
            currentStock = 24,
            totalStock = 60,
            isActive = true
        ),
        Medicine(
            id = "med_3",
            name = "Levothyroxine",
            dosageDescription = "75 mcg • Thyroid hormone",
            strengthMg = 75,
            formFactor = "Tablet",
            compartmentIndex = 3,
            compartmentLabel = "Bay 03",
            timings = listOf("08:00 AM"),
            repeatDays = listOf("M", "T", "W", "T", "F", "S", "S"),
            currentStock = 4, // low stock trigger!
            totalStock = 30,
            isActive = true
        ),
        Medicine(
            id = "med_4",
            name = "Atorvastatin",
            dosageDescription = "20 mg • Cholesterol management",
            strengthMg = 20,
            formFactor = "Coated Pill",
            compartmentIndex = 4,
            compartmentLabel = "Bay 04",
            timings = listOf("09:00 PM"),
            repeatDays = listOf("M", "T", "W", "T", "F", "S", "S"),
            currentStock = 28,
            totalStock = 30,
            isActive = true
        )
    )

    private val _medicines = MutableStateFlow(defaultMedicines)

    override fun getMedicines(): Flow<List<Medicine>> = _medicines.asStateFlow()

    override fun getMedicineById(id: String): Flow<Medicine?> = _medicines.map { list -> list.find { it.id == id } }

    override suspend fun saveMedicine(medicine: Medicine): Result<Unit> {
        delay(400)
        val current = _medicines.value.toMutableList()
        val index = current.indexOfFirst { it.id == medicine.id }
        if (index != -1) {
            current[index] = medicine
        } else {
            current.add(medicine)
        }
        _medicines.value = current
        return Result.success(Unit)
    }

    override suspend fun toggleActive(id: String): Result<Unit> {
        val current = _medicines.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val med = current[index]
            current[index] = med.copy(isActive = !med.isActive)
            _medicines.value = current
        }
        return Result.success(Unit)
    }

    override suspend fun deleteMedicine(id: String): Result<Unit> {
        val current = _medicines.value.toMutableList()
        current.removeAll { it.id == id }
        _medicines.value = current
        return Result.success(Unit)
    }

    override suspend fun updateStock(medicineId: String, newStock: Int): Result<Unit> {
        val current = _medicines.value.toMutableList()
        val index = current.indexOfFirst { it.id == medicineId }
        if (index != -1) {
            val med = current[index]
            current[index] = med.copy(currentStock = newStock)
            _medicines.value = current
        }
        return Result.success(Unit)
    }

    override suspend fun refresh(): Result<Unit> {
        delay(400)
        return Result.success(Unit)
    }
}
