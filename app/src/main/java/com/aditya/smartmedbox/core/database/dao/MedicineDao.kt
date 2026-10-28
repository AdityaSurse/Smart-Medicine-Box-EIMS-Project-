package com.aditya.smartmedbox.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aditya.smartmedbox.core.database.entity.MedicineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines ORDER BY compartmentIndex ASC")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE id = :id")
    fun getMedicineById(id: String): Flow<MedicineEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMedicine(medicine: MedicineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMedicines(medicines: List<MedicineEntity>)

    @Query("UPDATE medicines SET currentStock = :newStock WHERE id = :id")
    suspend fun updateStock(id: String, newStock: Int)

    @Query("UPDATE medicines SET isActive = NOT isActive WHERE id = :id")
    suspend fun toggleActive(id: String)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteMedicine(id: String)

    @Query("DELETE FROM medicines")
    suspend fun deleteAllMedicines()
}

