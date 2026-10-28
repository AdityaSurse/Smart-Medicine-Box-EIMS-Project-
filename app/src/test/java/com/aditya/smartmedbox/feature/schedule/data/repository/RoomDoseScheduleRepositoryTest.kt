package com.aditya.smartmedbox.feature.schedule.data.repository

import com.aditya.smartmedbox.core.database.dao.DoseScheduleDao
import com.aditya.smartmedbox.core.database.entity.DoseScheduleEntity
import com.aditya.smartmedbox.feature.schedule.domain.model.DoseSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoomDoseScheduleRepositoryTest {

    private lateinit var fakeDao: FakeTestDoseScheduleDao
    private lateinit var repository: RoomDoseScheduleRepository

    @Before
    fun setUp() {
        fakeDao = FakeTestDoseScheduleDao()
        repository = RoomDoseScheduleRepository(fakeDao)
    }

    @Test
    fun getSchedules_returnsMappedDomainSchedules() = runTest {
        val entity = DoseScheduleEntity(
            id = "sch_1",
            medicineId = "med_1",
            medicineName = "Amoxicillin",
            times = listOf("08:00 AM", "08:00 PM"),
            daysOfWeek = listOf(1, 2, 3),
            gracePeriodMinutes = 30,
            compartmentBay = "Bay 01",
            isActive = true
        )
        fakeDao.upsertSchedule(entity)

        val schedules = repository.getSchedules().first()
        assertEquals(1, schedules.size)
        assertEquals("Amoxicillin", schedules[0].medicineName)
        assertEquals(listOf(1, 2, 3), schedules[0].daysOfWeek)
    }

    @Test
    fun toggleScheduleActive_updatesDaoCorrectly() = runTest {
        val entity = DoseScheduleEntity(
            id = "sch_active",
            medicineId = "med_2",
            medicineName = "Metformin",
            times = listOf("12:00 PM"),
            daysOfWeek = listOf(1, 2, 3, 4, 5),
            gracePeriodMinutes = 30,
            compartmentBay = "Bay 02",
            isActive = true
        )
        fakeDao.upsertSchedule(entity)

        val toggleResult = repository.toggleScheduleActive("sch_active", false)
        assertTrue(toggleResult.isSuccess)

        val updated = repository.getScheduleById("sch_active").first()
        assertFalse(updated?.isActive ?: true)
    }

    @Test
    fun saveAndDeleteSchedule_persistsAndRemovesCorrectly() = runTest {
        val schedule = DoseSchedule(
            id = "sch_temp",
            medicineId = "med_temp",
            medicineName = "Vitamin C",
            times = listOf("09:00 AM"),
            daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
            gracePeriodMinutes = 45,
            compartmentBay = "Bay 03",
            isActive = true
        )

        repository.saveSchedule(schedule)
        val fetched = repository.getScheduleById("sch_temp").first()
        assertEquals("Vitamin C", fetched?.medicineName)

        repository.deleteSchedule("sch_temp")
        val deleted = repository.getScheduleById("sch_temp").first()
        assertEquals(null, deleted)
    }
}

private class FakeTestDoseScheduleDao : DoseScheduleDao {
    private val data = MutableStateFlow<Map<String, DoseScheduleEntity>>(emptyMap())

    override fun getAllSchedules(): Flow<List<DoseScheduleEntity>> {
        return data.map { it.values.toList() }
    }

    override fun getSchedulesForMedicine(medicineId: String): Flow<List<DoseScheduleEntity>> {
        return data.map { map -> map.values.filter { it.medicineId == medicineId } }
    }

    override fun getScheduleById(id: String): Flow<DoseScheduleEntity?> {
        return data.map { it[id] }
    }

    override suspend fun upsertSchedule(schedule: DoseScheduleEntity) {
        data.value = data.value + (schedule.id to schedule)
    }

    override suspend fun upsertSchedules(schedules: List<DoseScheduleEntity>) {
        data.value = data.value + schedules.associateBy { it.id }
    }

    override suspend fun toggleActive(id: String, isActive: Boolean) {
        val existing = data.value[id] ?: return
        data.value = data.value + (id to existing.copy(isActive = isActive))
    }

    override suspend fun deleteSchedule(id: String) {
        data.value = data.value - id
    }

    override suspend fun deleteAllSchedules() {
        data.value = emptyMap()
    }
}
