package com.aditya.smartmedbox.feature.medicines.data.repository

import com.aditya.smartmedbox.core.database.dao.MedicineDao
import com.aditya.smartmedbox.core.database.entity.MedicineEntity
import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoomMedicinesRepositoryTest {

    private lateinit var fakeDao: FakeTestMedicineDao
    private lateinit var repository: RoomMedicinesRepository

    @Before
    fun setUp() {
        fakeDao = FakeTestMedicineDao()
        repository = RoomMedicinesRepository(fakeDao)
    }

    @Test
    fun getMedicines_returnsMappedDomainList() = runTest {
        val entity = MedicineEntity(
            id = "med_1",
            name = "Amoxicillin",
            dosageDescription = "500mg capsule",
            strengthMg = 500,
            formFactor = "Capsule",
            compartmentIndex = 1,
            compartmentLabel = "Bay 01",
            timings = listOf("08:00 AM"),
            repeatDays = listOf("M", "T"),
            currentStock = 20,
            totalStock = 30,
            lowStockThreshold = 5,
            estimatedUnitWeightGrams = 0.5f,
            isActive = true
        )
        fakeDao.upsertMedicine(entity)

        val medicines = repository.getMedicines().first()
        assertEquals(1, medicines.size)
        assertEquals("Amoxicillin", medicines[0].name)
        assertEquals(20, medicines[0].currentStock)
    }

    @Test
    fun saveMedicine_insertsEntitySuccessfully() = runTest {
        val domain = Medicine(
            id = "med_new",
            name = "Metformin",
            dosageDescription = "850mg tablet",
            strengthMg = 850,
            formFactor = "Tablet",
            compartmentIndex = 2,
            compartmentLabel = "Bay 02",
            timings = listOf("12:00 PM"),
            repeatDays = listOf("M", "W", "F"),
            currentStock = 60,
            totalStock = 60,
            lowStockThreshold = 10,
            estimatedUnitWeightGrams = 0.8f,
            isActive = true
        )

        val result = repository.saveMedicine(domain)
        assertTrue(result.isSuccess)

        val fetched = repository.getMedicineById("med_new").first()
        assertEquals("Metformin", fetched?.name)
        assertEquals(850, fetched?.strengthMg)
    }

    @Test
    fun updateStock_updatesDaoStock() = runTest {
        val entity = MedicineEntity(
            id = "med_stock",
            name = "Atorvastatin",
            dosageDescription = "20mg",
            strengthMg = 20,
            formFactor = "Pill",
            compartmentIndex = 3,
            compartmentLabel = "Bay 03",
            timings = listOf("08:00 PM"),
            repeatDays = listOf("M"),
            currentStock = 30,
            totalStock = 30,
            lowStockThreshold = 5,
            estimatedUnitWeightGrams = 0.4f,
            isActive = true
        )
        fakeDao.upsertMedicine(entity)

        repository.updateStock("med_stock", 15)

        val updated = repository.getMedicineById("med_stock").first()
        assertEquals(15, updated?.currentStock)
    }
}

private class FakeTestMedicineDao : MedicineDao {
    private val data = MutableStateFlow<Map<String, MedicineEntity>>(emptyMap())

    override fun getAllMedicines(): Flow<List<MedicineEntity>> {
        return data.map { it.values.sortedBy { m -> m.compartmentIndex } }
    }

    override fun getMedicineById(id: String): Flow<MedicineEntity?> {
        return data.map { it[id] }
    }

    override suspend fun upsertMedicine(medicine: MedicineEntity) {
        data.value = data.value + (medicine.id to medicine)
    }

    override suspend fun upsertMedicines(medicines: List<MedicineEntity>) {
        data.value = data.value + medicines.associateBy { it.id }
    }

    override suspend fun updateStock(id: String, newStock: Int) {
        val existing = data.value[id] ?: return
        data.value = data.value + (id to existing.copy(currentStock = newStock))
    }

    override suspend fun toggleActive(id: String) {
        val existing = data.value[id] ?: return
        data.value = data.value + (id to existing.copy(isActive = !existing.isActive))
    }

    override suspend fun deleteMedicine(id: String) {
        data.value = data.value - id
    }

    override suspend fun deleteAllMedicines() {
        data.value = emptyMap()
    }
}

