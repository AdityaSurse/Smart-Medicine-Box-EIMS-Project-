package com.aditya.smartmedbox.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aditya.smartmedbox.core.database.DatabaseConstants
import com.aditya.smartmedbox.core.database.SmartMedBoxDatabase
import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.core.database.dao.DoseEventDao
import com.aditya.smartmedbox.core.database.dao.DoseScheduleDao
import com.aditya.smartmedbox.core.database.dao.MedicineDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSmartMedBoxDatabase(
        @ApplicationContext context: Context
    ): SmartMedBoxDatabase {
        return Room.databaseBuilder(
            context,
            SmartMedBoxDatabase::class.java,
            DatabaseConstants.DATABASE_NAME
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    seedDatabase(db)
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }

    private fun seedDatabase(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        db.execSQL(
            """
            INSERT OR REPLACE INTO medicines (id, name, dosageDescription, strengthMg, formFactor, compartmentIndex, compartmentLabel, timings, repeatDays, currentStock, totalStock, lowStockThreshold, estimatedUnitWeightGrams, isActive, createdAt)
            VALUES 
            ('med_amox', 'Amoxicillin', '500 mg • Antibiotic capsule', 500, 'Antibiotic capsule', 1, 'Bay 01', '["08:00 AM","08:00 PM"]', '["M","T","W","T","F"]', 18, 30, 5, 0.55, 1, $now),
            ('med_metf', 'Metformin', '850 mg • Blood sugar regulation', 850, 'Blood sugar regulation', 2, 'Bay 02', '["12:30 PM"]', '["M","T","W","T","F"]', 28, 60, 10, 0.85, 1, $now),
            ('med_ator', 'Atorvastatin', '20 mg • Lipid management', 20, 'Coated Pill', 3, 'Bay 03', '["08:00 PM"]', '["M","T","W","T","F","S","S"]', 24, 30, 5, 0.45, 1, $now),
            ('med_para', 'Paracetamol', '650 mg • Analgesic tablet', 650, 'Tablet', 4, 'Bay 04', '["10:30 PM"]', '["M","T","W","T","F"]', 12, 20, 5, 0.65, 1, $now);
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT OR REPLACE INTO dose_schedules (id, medicineId, medicineName, times, daysOfWeek, gracePeriodMinutes, compartmentBay, isActive)
            VALUES
            ('sch_amox', 'med_amox', 'Amoxicillin', '["08:00 AM","08:00 PM"]', '[1,2,3,4,5,6,7]', 30, 'Bay 01', 1),
            ('sch_metf', 'med_metf', 'Metformin', '["12:30 PM"]', '[1,2,3,4,5]', 30, 'Bay 02', 1),
            ('sch_ator', 'med_ator', 'Atorvastatin', '["08:00 PM"]', '[1,2,3,4,5,6,7]', 45, 'Bay 03', 1);
            """.trimIndent()
        )

        val past5h = now - 5 * 3600 * 1000L
        val past2h = now - 2 * 3600 * 1000L
        val future45m = now + 45 * 60 * 1000L

        db.execSQL(
            """
            INSERT OR REPLACE INTO dose_events (id, scheduleId, medicineId, medicineName, dosage, scheduledTimeEpochMillis, status, loggedTimeEpochMillis, recordedWeightGrams, bayLabel)
            VALUES
            ('evt_01', 'sch_amox', 'med_amox', 'Amoxicillin', '500 mg • 1 capsule', $past5h, 'TAKEN', ${past5h + 120000L}, 0.55, 'Tray 01'),
            ('evt_02', 'sch_metf', 'med_metf', 'Metformin', '850 mg • 1 tablet', $past2h, 'TAKEN', ${past2h + 60000L}, 0.85, 'Tray 02'),
            ('evt_03', 'sch_ator', 'med_ator', 'Atorvastatin', '20 mg • 1 coated pill', $future45m, 'PENDING', NULL, NULL, 'Tray 03');
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT OR REPLACE INTO device_status (deviceId, temperatureCelsius, humidityPercent, weightGrams, lastSeenTimestamp, isBoxOpen, batteryPercent, isOnline)
            VALUES ('DG_BOX_4081', 21.4, 46.0, 14.8, $now, 0, 84, 1);
            """.trimIndent()
        )
    }

    @Provides
    fun provideMedicineDao(database: SmartMedBoxDatabase): MedicineDao = database.medicineDao()

    @Provides
    fun provideDoseScheduleDao(database: SmartMedBoxDatabase): DoseScheduleDao = database.doseScheduleDao()

    @Provides
    fun provideDoseEventDao(database: SmartMedBoxDatabase): DoseEventDao = database.doseEventDao()

    @Provides
    fun provideDeviceStatusDao(database: SmartMedBoxDatabase): DeviceStatusDao = database.deviceStatusDao()
}
