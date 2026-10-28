package com.aditya.smartmedbox.feature.dashboard.data.mapper

import com.aditya.smartmedbox.core.database.entity.DeviceStatusEntity
import com.aditya.smartmedbox.feature.dashboard.domain.model.DeviceStatus

fun DeviceStatusEntity.toDomain(): DeviceStatus {
    return DeviceStatus(
        deviceId = deviceId,
        temperatureCelsius = temperatureCelsius,
        humidityPercent = humidityPercent,
        weightGrams = weightGrams,
        lastSeenTimestamp = lastSeenTimestamp,
        isBoxOpen = isBoxOpen,
        batteryPercent = batteryPercent,
        isOnline = isOnline
    )
}

fun DeviceStatus.toEntity(): DeviceStatusEntity {
    return DeviceStatusEntity(
        deviceId = deviceId,
        temperatureCelsius = temperatureCelsius,
        humidityPercent = humidityPercent,
        weightGrams = weightGrams,
        lastSeenTimestamp = lastSeenTimestamp,
        isBoxOpen = isBoxOpen,
        batteryPercent = batteryPercent,
        isOnline = isOnline
    )
}

