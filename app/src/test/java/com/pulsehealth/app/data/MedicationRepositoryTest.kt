package com.pulsehealth.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pulsehealth.app.data.local.PulseDatabase
import com.pulsehealth.app.data.medication.DoseStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MedicationRepositoryTest {

    private lateinit var database: PulseDatabase
    private lateinit var repository: DefaultMedicationRepository
    private val fixedClock = Clock.fixed(Instant.parse("2026-11-18T10:00:00Z"), ZoneId.of("UTC"))
    private val zoneId = ZoneId.of("UTC")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = DefaultMedicationRepository(
            medicationDao = database.medicationDao(),
            clock = fixedClock,
            zoneId = zoneId
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveMedication_insertsActiveMedicationAndEmitsInActiveList() = runTest {
        val id = repository.saveMedication(
            name = "Vitamin D3",
            dosage = 2000.0,
            unit = "IU",
            frequency = "DAILY",
            scheduledTimes = listOf("08:00"),
            instructions = "With meal",
            colorHex = "#0D5C58",
            notes = "Winter supplement"
        )

        assertNotNull(id)
        val activeMeds = repository.observeActiveMedications().first()
        assertEquals(1, activeMeds.size)
        assertEquals("Vitamin D3", activeMeds[0].name)
        assertEquals(2000.0, activeMeds[0].dosage, 0.01)
        assertEquals("08:00", activeMeds[0].scheduledTimes)
        assertTrue(activeMeds[0].isActive)
    }

    @Test
    fun observeDailySchedule_emitsPendingDosesForDailyMedication() = runTest {
        val medId = repository.saveMedication(
            name = "Lisinopril",
            dosage = 10.0,
            unit = "mg",
            frequency = "DAILY",
            scheduledTimes = listOf("08:00")
        )

        val today = LocalDate.now(fixedClock)
        val schedule = repository.observeDailySchedule(today).first()

        assertEquals(1, schedule.size)
        val dose = schedule[0]
        assertEquals(medId, dose.medication.id)
        assertEquals(DoseStatus.PENDING, dose.status)
        assertEquals("08:00 AM", dose.timeLabel)
    }

    @Test
    fun logDose_marksDoseAsTaken_updatesScheduleStatus() = runTest {
        val medId = repository.saveMedication(
            name = "Omega-3",
            dosage = 1000.0,
            unit = "mg",
            frequency = "DAILY",
            scheduledTimes = listOf("08:00")
        )

        val today = LocalDate.now(fixedClock)
        val initialSchedule = repository.observeDailySchedule(today).first()
        val dose = initialSchedule[0]

        // Mark dose as TAKEN
        repository.logDose(
            medicationId = medId,
            scheduledTimeEpochMillis = dose.scheduledTimeEpochMillis,
            status = DoseStatus.TAKEN,
            notes = "Taken on time"
        )

        val updatedSchedule = repository.observeDailySchedule(today).first()
        assertEquals(1, updatedSchedule.size)
        assertEquals(DoseStatus.TAKEN, updatedSchedule[0].status)
        assertNotNull(updatedSchedule[0].logId)
    }

    @Test
    fun removeDoseLog_revertsDoseToPending() = runTest {
        val medId = repository.saveMedication(
            name = "Iron",
            dosage = 65.0,
            unit = "mg",
            frequency = "DAILY",
            scheduledTimes = listOf("09:00")
        )

        val today = LocalDate.now(fixedClock)
        val schedule = repository.observeDailySchedule(today).first()
        val dose = schedule[0]

        repository.logDose(medId, dose.scheduledTimeEpochMillis, DoseStatus.TAKEN)
        assertEquals(DoseStatus.TAKEN, repository.observeDailySchedule(today).first()[0].status)

        // Undo dose log
        repository.removeDoseLog(medId, dose.scheduledTimeEpochMillis)
        val reverted = repository.observeDailySchedule(today).first()[0]
        assertEquals(DoseStatus.PENDING, reverted.status)
    }

    @Test
    fun observeAdherenceSummary_computesCorrectAdherenceAndStreak() = runTest {
        val medId = repository.saveMedication(
            name = "Daily Multi",
            dosage = 1.0,
            unit = "tablet",
            frequency = "DAILY",
            scheduledTimes = listOf("08:00")
        )

        val today = LocalDate.now(fixedClock)
        val schedule = repository.observeDailySchedule(today).first()
        val dose = schedule[0]

        // Mark taken today
        repository.logDose(medId, dose.scheduledTimeEpochMillis, DoseStatus.TAKEN)

        val adherence = repository.observeAdherenceSummary(7).first()
        assertTrue(adherence.takenDoses >= 1)
        assertTrue(adherence.adherencePercentage > 0)
        assertEquals(1, adherence.currentStreakDays)
    }

    @Test
    fun deleteMedication_removesMedicationFromDatabase() = runTest {
        val medId = repository.saveMedication(
            name = "Temporary Antacid",
            dosage = 20.0,
            unit = "mg",
            frequency = "DAILY",
            scheduledTimes = listOf("12:00")
        )

        assertEquals(1, repository.observeAllMedications().first().size)
        repository.deleteMedication(medId)
        assertEquals(0, repository.observeAllMedications().first().size)
    }
}
