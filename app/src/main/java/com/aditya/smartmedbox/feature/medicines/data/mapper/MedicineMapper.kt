package com.aditya.smartmedbox.feature.medicines.data.mapper

import com.aditya.smartmedbox.core.database.entity.MedicineEntity
import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine

fun MedicineEntity.toDomain(): Medicine {
    return Medicine(
        id = id,
        name = name,
        dosageDescription = dosageDescription,
        strengthMg = strengthMg,
        formFactor = formFactor,
        compartmentIndex = compartmentIndex,
        compartmentLabel = compartmentLabel,
        timings = timings,
        repeatDays = repeatDays,
        currentStock = currentStock,
        totalStock = totalStock,
        lowStockThreshold = lowStockThreshold,
        estimatedUnitWeightGrams = estimatedUnitWeightGrams,
        isActive = isActive,
        createdAt = createdAt
    )
}

fun Medicine.toEntity(): MedicineEntity {
    return MedicineEntity(
        id = id,
        name = name,
        dosageDescription = dosageDescription,
        strengthMg = strengthMg,
        formFactor = formFactor,
        compartmentIndex = compartmentIndex,
        compartmentLabel = compartmentLabel,
        timings = timings,
        repeatDays = repeatDays,
        currentStock = currentStock,
        totalStock = totalStock,
        lowStockThreshold = lowStockThreshold,
        estimatedUnitWeightGrams = estimatedUnitWeightGrams,
        isActive = isActive,
        createdAt = createdAt
    )
}

