package com.aditya.smartmedbox.feature.schedule.data.mapper

import com.aditya.smartmedbox.core.database.entity.DoseScheduleEntity
import com.aditya.smartmedbox.feature.schedule.domain.model.DoseSchedule

fun DoseScheduleEntity.toDomain(): DoseSchedule {
    return DoseSchedule(
        id = id,
        medicineId = medicineId,
        medicineName = medicineName,
        times = times,
        daysOfWeek = daysOfWeek,
        gracePeriodMinutes = gracePeriodMinutes,
        compartmentBay = compartmentBay,
        isActive = isActive
    )
}

fun DoseSchedule.toEntity(): DoseScheduleEntity {
    return DoseScheduleEntity(
        id = id,
        medicineId = medicineId,
        medicineName = medicineName,
        times = times,
        daysOfWeek = daysOfWeek,
        gracePeriodMinutes = gracePeriodMinutes,
        compartmentBay = compartmentBay,
        isActive = isActive
    )
}
