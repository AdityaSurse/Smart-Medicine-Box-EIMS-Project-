package com.aditya.smartmedbox.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aditya.smartmedbox.core.database.converter.SmartMedBoxTypeConverters
import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.core.database.dao.DoseEventDao
import com.aditya.smartmedbox.core.database.dao.DoseScheduleDao
import com.aditya.smartmedbox.core.database.dao.MedicineDao
import com.aditya.smartmedbox.core.database.entity.DeviceStatusEntity
import com.aditya.smartmedbox.core.database.entity.DoseEventEntity
import com.aditya.smartmedbox.core.database.entity.DoseScheduleEntity
import com.aditya.smartmedbox.core.database.entity.MedicineEntity

@Database(
    entities = [
        MedicineEntity::class,
        DoseScheduleEntity::class,
        DoseEventEntity::class,
        DeviceStatusEntity::class
    ],
    version = DatabaseConstants.DATABASE_VERSION,
    exportSchema = true
)
@TypeConverters(SmartMedBoxTypeConverters::class)
abstract class SmartMedBoxDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun doseScheduleDao(): DoseScheduleDao
    abstract fun doseEventDao(): DoseEventDao
    abstract fun deviceStatusDao(): DeviceStatusDao
}
