package com.aditya.smartmedbox.feature.medicines.data.repository

import com.aditya.smartmedbox.core.database.dao.MedicineDao
import com.aditya.smartmedbox.core.sync.FirestoreSyncEngine
import com.aditya.smartmedbox.feature.medicines.data.mapper.toDomain
import com.aditya.smartmedbox.feature.medicines.data.mapper.toEntity
import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine
import com.aditya.smartmedbox.feature.medicines.domain.repository.MedicinesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

@Singleton
class RoomMedicinesRepository @Inject constructor(
    private val medicineDao: MedicineDao,
    private val firestoreSyncEngine: FirestoreSyncEngine? = null
) : MedicinesRepository {

    override fun getMedicines(): Flow<List<Medicine>> {
        return medicineDao.getAllMedicines().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMedicineById(id: String): Flow<Medicine?> {
        return medicineDao.getMedicineById(id).map { it?.toDomain() }
    }

    override suspend fun saveMedicine(medicine: Medicine): Result<Unit> {
        return try {
            medicineDao.upsertMedicine(medicine.toEntity())
            firestoreSyncEngine?.pushMedicine(medicine)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleActive(id: String): Result<Unit> {
        return try {
            medicineDao.toggleActive(id)
            medicineDao.getMedicineById(id).firstOrNull()?.let {
                firestoreSyncEngine?.pushMedicine(it.toDomain())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMedicine(id: String): Result<Unit> {
        return try {
            medicineDao.deleteMedicine(id)
            firestoreSyncEngine?.deleteMedicine(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStock(medicineId: String, newStock: Int): Result<Unit> {
        return try {
            medicineDao.updateStock(medicineId, newStock)
            medicineDao.getMedicineById(medicineId).firstOrNull()?.let {
                firestoreSyncEngine?.pushMedicine(it.toDomain())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refresh(): Result<Unit> {
        return Result.success(Unit)
    }
}
