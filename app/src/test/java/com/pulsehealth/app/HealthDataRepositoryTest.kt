package com.pulsehealth.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pulsehealth.app.data.HealthDataRepository
import com.pulsehealth.app.data.local.PulseDatabase
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
}
