package com.pulsehealth.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pulsehealth.app.data.DefaultSleepRepository
import com.pulsehealth.app.data.SleepBreakdownSummary
import com.pulsehealth.app.data.SleepStageType
import com.pulsehealth.app.data.local.PulseDatabase
import com.pulsehealth.app.data.local.SleepSessionEntity
import com.pulsehealth.app.data.local.SleepSessionWithStages
import com.pulsehealth.app.data.local.SleepStageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SleepRepositoryTest {

    private lateinit var database: PulseDatabase
    private lateinit var repository: DefaultSleepRepository
    private val fixedZone = ZoneId.of("UTC")
    private val fixedClock = Clock.fixed(Instant.parse("2026-11-17T12:00:00Z"), fixedZone)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DefaultSleepRepository(
            sleepDao = database.sleepDao(),
            clock = fixedClock,
            zoneId = fixedZone
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun logManualSleepCreatesSessionAndStagesWithEfficiency() = runTest {
        val start = Instant.parse("2026-11-16T22:00:00Z").toEpochMilli()
        val end = Instant.parse("2026-11-17T06:00:00Z").toEpochMilli() // 8 hours = 480 min

        val sessionWithStages = repository.logManualSleep(
            startTimeEpochMillis = start,
            endTimeEpochMillis = end,
            deepMinutes = 120, // 2h
            lightMinutes = 240, // 4h
            remMinutes = 90, // 1.5h
            awakeMinutes = 30, // 0.5h
            notes = "Felt well-rested"
        )

        assertNotNull(sessionWithStages)
        val session = sessionWithStages.session
        assertEquals(480L, session.durationMinutes)
        assertEquals(120L, session.deepDurationMinutes)
        assertEquals(240L, session.lightDurationMinutes)
        assertEquals(90L, session.remDurationMinutes)
        assertEquals(30L, session.awakeDurationMinutes)
        // (120 + 240 + 90) / 480 = 450 / 480 = 93.75% -> 94%
        assertEquals(94, session.sleepEfficiencyPercentage)
        assertEquals("Felt well-rested", session.notes)

        val stages = sessionWithStages.stages
        assertEquals(4, stages.size)
        assertTrue(stages.any { it.stage == SleepStageType.DEEP.name })
        assertTrue(stages.any { it.stage == SleepStageType.LIGHT.name })
        assertTrue(stages.any { it.stage == SleepStageType.REM.name })
        assertTrue(stages.any { it.stage == SleepStageType.AWAKE.name })
    }

    @Test(expected = IllegalArgumentException::class)
    fun logManualSleepRejectsInvalidTimes() = runTest {
        val start = Instant.parse("2026-11-17T06:00:00Z").toEpochMilli()
        val end = Instant.parse("2026-11-16T22:00:00Z").toEpochMilli() // end before start
        repository.logManualSleep(start, end)
    }

    @Test
    fun observeSleepWithStagesReturnsPersistedData() = runTest {
        val start = Instant.parse("2026-11-16T23:00:00Z").toEpochMilli()
        val end = Instant.parse("2026-11-17T07:00:00Z").toEpochMilli()

        repository.logManualSleep(start, end)

        val allSleep = repository.observeAllSleepWithStages().first()
        assertEquals(1, allSleep.size)
        assertEquals(480L, allSleep.first().session.durationMinutes)

        val todaySleep = repository.observeTodaySleep().first()
        assertNotNull(todaySleep)
        assertEquals(allSleep.first().session.id, todaySleep?.session?.id)
    }

    @Test
    fun deleteSleepSessionRemovesSessionAndStages() = runTest {
        val start = Instant.parse("2026-11-16T23:00:00Z").toEpochMilli()
        val end = Instant.parse("2026-11-17T07:00:00Z").toEpochMilli()

        val logged = repository.logManualSleep(start, end)
        val deleted = repository.deleteSleepSession(logged.session.id)
        assertTrue(deleted)

        val all = repository.observeAllSleepWithStages().first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun sleepBreakdownSummaryComputesPercentagesAccurately() {
        val stages = listOf(
            SleepStageEntity("s1", "session1", SleepStageType.DEEP.name, 1000, 2000, 100),
            SleepStageEntity("s2", "session1", SleepStageType.LIGHT.name, 2000, 3000, 200),
            SleepStageEntity("s3", "session1", SleepStageType.REM.name, 3000, 4000, 100),
            SleepStageEntity("s4", "session1", SleepStageType.AWAKE.name, 4000, 5000, 100)
        )
        // total inBed = 500 min, totalSleep = 400 min
        val summary = SleepBreakdownSummary.fromStages(stages, 500)

        assertEquals(400L, summary.totalSleepMinutes)
        assertEquals(500L, summary.inBedMinutes)
        assertEquals(100L, summary.deepMinutes)
        assertEquals(200L, summary.lightMinutes)
        assertEquals(100L, summary.remMinutes)
        assertEquals(100L, summary.awakeMinutes)

        assertEquals(20, summary.deepPercentage) // 100 / 500 = 20%
        assertEquals(40, summary.lightPercentage) // 200 / 500 = 40%
        assertEquals(20, summary.remPercentage) // 100 / 500 = 20%
        assertEquals(20, summary.awakePercentage) // 100 / 500 = 20%
        assertEquals(80, summary.sleepEfficiencyPercentage) // 400 / 500 = 80%
        assertEquals("Good", summary.efficiencyRating)
    }

    @Test
    fun importHealthConnectSleepInsertsMultipleSessions() = runTest {
        val session1 = SleepSessionEntity("hc_1", "HC Sleep 1", 1000L, 2000L, 400L, 80L, 200L, 80L, 40L, 90, "health_connect", null, "synced")
        val stage1 = SleepStageEntity("stg_1", "hc_1", SleepStageType.DEEP.name, 1000L, 1500L, 80L)

        repository.importHealthConnectSleep(listOf(SleepSessionWithStages(session1, listOf(stage1))))

        val all = repository.observeAllSleepWithStages().first()
        assertEquals(1, all.size)
        assertEquals("HC Sleep 1", all.first().session.title)
        assertEquals(1, all.first().stages.size)
    }
}
