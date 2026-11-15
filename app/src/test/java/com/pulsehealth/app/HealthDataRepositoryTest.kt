package com.pulsehealth.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.DataSource
import com.pulsehealth.app.data.HealthDataRepository
import com.pulsehealth.app.data.local.DailyHealthSummaryEntity
import com.pulsehealth.app.data.local.PulseDatabase
import com.pulsehealth.app.data.toDailyHealthSummary
import com.pulsehealth.app.data.toDashboardMetrics
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
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
class HealthDataRepositoryTest {
    private lateinit var database: PulseDatabase
    private lateinit var repository: HealthDataRepository
    private val fixedInstant = Instant.parse("2026-11-13T08:00:00Z")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HealthDataRepository(
            hydrationDao = database.hydrationDao(),
            dailyHealthSummaryDao = database.dailyHealthSummaryDao(),
            clock = Clock.fixed(fixedInstant, ZoneId.of("UTC")),
            zoneId = ZoneId.of("UTC")
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun addingWaterPersistsAndUpdatesDailyTotal() = runTest {
        repository.addWater(250)
        repository.addWater(500)

        assertEquals(0.75, repository.todayWaterLiters.first(), 0.0)
        val entries = repository.observeTodayEntries().first()
        assertEquals(2, entries.size)
        assertEquals(500, entries[0].amountMilliliters)
        assertEquals(250, entries[1].amountMilliliters)
    }

    @Test
    fun editingWaterEntryUpdatesAmountAndTotal() = runTest {
        repository.addWater(250)
        val entries = repository.observeTodayEntries().first()
        val entryId = entries.first().id

        val updated = repository.updateWater(entryId, 600)
        assertTrue(updated)

        assertEquals(0.60, repository.todayWaterLiters.first(), 0.0)
    }

    @Test
    fun deletingWaterEntryRemovesItFromTotal() = runTest {
        repository.addWater(250)
        repository.addWater(500)
        val entries = repository.observeTodayEntries().first()
        assertEquals(2, entries.size)

        val deleted = repository.deleteWater(entries.first().id)
        assertTrue(deleted)

        val remaining = repository.observeTodayEntries().first()
        assertEquals(1, remaining.size)
        assertEquals(0.25, repository.todayWaterLiters.first(), 0.0)
    }

    @Test
    fun weeklyTotalAggregatesAcrossSevenDays() = runTest {
        // Today
        repository.addWater(500, fixedInstant.toEpochMilli())
        // 3 days ago (within 7 days)
        repository.addWater(500, fixedInstant.minusSeconds(3 * 86400).toEpochMilli())
        // 10 days ago (outside 7-day window)
        repository.addWater(500, fixedInstant.minusSeconds(10 * 86400).toEpochMilli())

        assertEquals(0.5, repository.todayWaterLiters.first(), 0.0)
        assertEquals(1.0, repository.weeklyWaterLiters.first(), 0.0)
    }

    @Test
    fun observeRecentEntriesRespectsLimitAndOffset() = runTest {
        for (i in 1..10) {
            repository.addWater(100 * i, fixedInstant.minusSeconds(i.toLong() * 60).toEpochMilli())
        }

        val firstPage = repository.observeRecentEntries(limit = 4, offset = 0).first()
        assertEquals(4, firstPage.size)

        val secondPage = repository.observeRecentEntries(limit = 4, offset = 4).first()
        assertEquals(4, secondPage.size)

        // Ensure distinct items across pages
        val firstPageIds = firstPage.map { it.id }.toSet()
        val secondPageIds = secondPage.map { it.id }.toSet()
        assertTrue(firstPageIds.intersect(secondPageIds).isEmpty())
    }

    @Test
    fun invalidHydrationAmountIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            runTest { repository.addWater(0) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runTest { repository.addWater(5001) }
        }
    }

    @Test
    fun clearAllDataRemovesAllHydrationEntries() = runTest {
        repository.addWater(250)
        repository.addWater(500)
        assertEquals(2, repository.getAllEntries().size)

        repository.clearAllData()

        assertEquals(0, repository.getAllEntries().size)
        assertEquals(0.0, repository.todayWaterLiters.first(), 0.0)
    }

    @Test
    fun exportDataJsonProducesValidStructuredJson() = runTest {
        repository.addWater(250)
        repository.addWater(500)

        val json = repository.exportDataJson()

        assertTrue(json.contains("\"app\": \"Pulse Health\""))
        assertTrue(json.contains("\"hydrationEntriesCount\": 2"))
        assertTrue(json.contains("\"amountMilliliters\": 250"))
        assertTrue(json.contains("\"amountMilliliters\": 500"))
    }

    @Test
    fun dailySummaryPersistsAndCanBeRetrievedByEpochDay() = runTest {
        val epochDay = 20770L
        val summary = DailyHealthSummaryEntity(
            epochDay = epochDay,
            steps = 8450L,
            restingHeartRate = 64,
            sleepMinutes = 450L,
            activeCalories = 420L,
            exerciseMinutes = 35L,
            standHours = 9L,
            source = "Health Connect",
            updatedAtEpochMillis = 1731500000000L
        )

        repository.saveDailySummary(summary)

        val retrieved = repository.getSummary(epochDay)
        assertTrue(retrieved != null)
        assertEquals(8450L, retrieved?.steps)
        assertEquals(64L, retrieved?.restingHeartRate)
        assertEquals(450L, retrieved?.sleepMinutes)

        val observed = repository.observeDailySummary(epochDay).first()
        assertEquals(8450L, observed?.steps)
    }

    @Test
    fun observeSummaryRangeReturnsRecordsWithinBounds() = runTest {
        val s1 = DailyHealthSummaryEntity(
            epochDay = 100L,
            steps = 5000L,
            restingHeartRate = 60,
            sleepMinutes = 400L,
            activeCalories = 300L,
            exerciseMinutes = 20L,
            standHours = 6L,
            source = "Health Connect",
            updatedAtEpochMillis = 1000L
        )
        val s2 = DailyHealthSummaryEntity(
            epochDay = 101L,
            steps = 7500L,
            restingHeartRate = 62,
            sleepMinutes = 420L,
            activeCalories = 350L,
            exerciseMinutes = 25L,
            standHours = 7L,
            source = "Health Connect",
            updatedAtEpochMillis = 2000L
        )
        val s3 = DailyHealthSummaryEntity(
            epochDay = 105L,
            steps = 10000L,
            restingHeartRate = 58,
            sleepMinutes = 480L,
            activeCalories = 500L,
            exerciseMinutes = 45L,
            standHours = 10L,
            source = "Health Connect",
            updatedAtEpochMillis = 3000L
        )

        repository.saveDailySummaries(listOf(s1, s2, s3))

        val inRange = repository.observeSummaryRange(100L, 102L).first()
        assertEquals(2, inRange.size)
        assertEquals(100L, inRange[0].epochDay)
        assertEquals(101L, inRange[1].epochDay)

        val all = repository.getAllSummaries()
        assertEquals(3, all.size)

        val deletedCount = repository.deleteAllSummaries()
        assertEquals(3, deletedCount)
        assertEquals(0, repository.getAllSummaries().size)
    }

    @Test
    fun toDashboardMetricsAndToDailyHealthSummaryConversion() {
        val epochDay = 20000L
        val originalMetrics = DashboardMetrics(
            steps = 9500L,
            heartRate = 65,
            sleepMinutes = 460L,
            activeCalories = 480L,
            exerciseMinutes = 40L,
            standHours = 8L,
            source = DataSource.HEALTH_CONNECT,
            lastUpdatedEpochMillis = 123456789L
        )

        val summary = originalMetrics.toDailyHealthSummary(epochDay)
        assertEquals(epochDay, summary.epochDay)
        assertEquals(9500L, summary.steps)
        assertEquals(65L, summary.restingHeartRate)
        assertEquals(460L, summary.sleepMinutes)
        assertEquals("Health Connect", summary.source)

        val convertedBack = summary.toDashboardMetrics(waterLiters = 1.75, waterGoalLiters = 2.5)
        assertEquals(9500L, convertedBack.steps)
        assertEquals(65L, convertedBack.heartRate)
        assertEquals(460L, convertedBack.sleepMinutes)
        assertEquals(1.75, convertedBack.waterLiters, 0.001)
        assertEquals(2.5, convertedBack.waterGoalLiters, 0.001)
        assertEquals(DataSource.HEALTH_CONNECT, convertedBack.source)
    }
}
