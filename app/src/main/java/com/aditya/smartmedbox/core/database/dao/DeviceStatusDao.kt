package com.aditya.smartmedbox.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aditya.smartmedbox.core.database.entity.DeviceStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceStatusDao {
    @Query("SELECT * FROM device_status WHERE deviceId = :deviceId LIMIT 1")
    fun getDeviceStatus(deviceId: String = "DG_BOX_4081"): Flow<DeviceStatusEntity?>

    fun getDeviceStatusFlow(deviceId: String = "DG_BOX_4081"): Flow<DeviceStatusEntity?> = getDeviceStatus(deviceId)

    @Query("UPDATE device_status SET isOnline = :isOnline, lastSeenTimestamp = :timestamp WHERE deviceId = :deviceId")
    suspend fun updateOnlineStatus(deviceId: String, isOnline: Boolean, timestamp: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDeviceStatus(status: DeviceStatusEntity)
}
