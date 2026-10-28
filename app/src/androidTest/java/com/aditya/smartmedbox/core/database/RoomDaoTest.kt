package com.aditya.smartmedbox.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.core.database.dao.DoseEventDao
import com.aditya.smartmedbox.core.database.dao.DoseScheduleDao
import com.aditya.smartmedbox.core.database.dao.MedicineDao
import com.aditya.smartmedbox.core.database.entity.DeviceStatusEntity
import com.aditya.smartmedbox.core.database.entity.DoseEventEntity
import com.aditya.smartmedbox.core.database.entity.DoseScheduleEntity
import com.aditya.smartmedbox.core.database.entity.MedicineEntity
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RoomDaoTest {

    private lateinit var database: SmartMedBoxDatabase
    private lateinit var medicineDao: MedicineDao
    private lateinit var doseScheduleDao: DoseScheduleDao
    private lateinit var doseEventDao: DoseEventDao
    private lateinit var deviceStatusDao: DeviceStatusDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SmartMedBoxDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        medicineDao = database.medicineDao()
        doseScheduleDao = database.doseScheduleDao()
        doseEventDao = database.doseEventDao()
        deviceStatusDao = database.deviceStatusDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun testMedicineDao_insertAndQuery() = runTest {
        val med = MedicineEntity(
            id = "med_test_1",
            name = "Lisinopril",
            dosageDescription = "10 mg tablet",
            strengthMg = 10,
            compartmentIndex = 1,
            compartmentLabel = "Bay 01",
            currentStock = 28
        )
        medicineDao.upsertMedicine(med)

        medicineDao.getMedicineById("med_test_1").test {
            val item = awaitItem()
            assertNotNull(item)
            assertEquals("Lisinopril", item?.name)
            assertEquals(28, item?.currentStock)
        }
    }

    @Test
    fun testMedicineDao_updateStock() = runTest {
        val med = MedicineEntity(
            id = "med_test_2",
            name = "Metformin",
            dosageDescription = "850 mg",
            currentStock = 30
        )
        medicineDao.upsertMedicine(med)
        medicineDao.updateStock("med_test_2", 29)

        medicineDao.getMedicineById("med_test_2").test {
            val item = awaitItem()
            assertEquals(29, item?.currentStock)
        }
    }

    @Test
    fun testDoseScheduleDao_insertAndQuery() = runTest {
        val schedule = DoseScheduleEntity(
            id = "sch_test_1",
            medicineId = "med_test_1",
            medicineName = "Lisinopril",
            times = listOf("08:00 AM"),
            daysOfWeek = listOf(1, 2, 3, 4, 5),
            gracePeriodMinutes = 30,
            compartmentBay = "Bay 01"
        )
        doseScheduleDao.upsertSchedule(schedule)

        doseScheduleDao.getScheduleById("sch_test_1").test {
            val item = awaitItem()
            assertNotNull(item)
            assertEquals("Bay 01", item?.compartmentBay)
            assertEquals(listOf("08:00 AM"), item?.times)
        }
    }

    @Test
    fun testDoseEventDao_insertAndLog() = runTest {
        val event = DoseEventEntity(
            id = "evt_test_1",
            scheduleId = "sch_test_1",
            medicineId = "med_test_1",
            medicineName = "Lisinopril",
            dosage = "10 mg",
            scheduledTimeEpochMillis = 1700000000L,
            status = DoseStatus.PENDING
        )
        doseEventDao.upsertEvent(event)

        doseEventDao.logDose("evt_test_1", DoseStatus.TAKEN.name, 1700000050L, 0.5f)

        doseEventDao.getEventById("evt_test_1").test {
            val item = awaitItem()
            assertNotNull(item)
            assertEquals(DoseStatus.TAKEN, item?.status)
            assertEquals(1700000050L, item?.loggedTimeEpochMillis)
        }
    }

    @Test
    fun testDeviceStatusDao_upsertAndQuery() = runTest {
        val status = DeviceStatusEntity(
            deviceId = "DG_BOX_4081",
            temperatureCelsius = 21.8f,
            humidityPercent = 45.0f,
            batteryPercent = 88
        )
        deviceStatusDao.upsertDeviceStatus(status)

        deviceStatusDao.getDeviceStatus("DG_BOX_4081").test {
            val item = awaitItem()
            assertNotNull(item)
            assertEquals(21.8f, item?.temperatureCelsius)
            assertEquals(88, item?.batteryPercent)
        }
    }
}
