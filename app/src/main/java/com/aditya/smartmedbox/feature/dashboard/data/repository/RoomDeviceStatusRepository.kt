package com.aditya.smartmedbox.feature.dashboard.data.repository

import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.feature.dashboard.data.mapper.toDomain
import com.aditya.smartmedbox.feature.dashboard.data.mapper.toEntity
import com.aditya.smartmedbox.feature.dashboard.domain.model.DeviceStatus
import com.aditya.smartmedbox.feature.dashboard.domain.repository.DeviceStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomDeviceStatusRepository @Inject constructor(
    private val deviceStatusDao: DeviceStatusDao
) : DeviceStatusRepository {

    override fun getDeviceStatus(): Flow<DeviceStatus> {
        return deviceStatusDao.getDeviceStatus("DG_BOX_4081").map { entity ->
            entity?.toDomain() ?: DeviceStatus()
        }
    }

    override suspend fun updateDeviceStatus(status: DeviceStatus): Result<Unit> {
        return try {
            deviceStatusDao.upsertDeviceStatus(status.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
