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
import org.junit.Assert.assertThrows
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

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HealthDataRepository(
            hydrationDao = database.hydrationDao(),
            clock = Clock.fixed(Instant.parse("2026-11-13T08:00:00Z"), ZoneId.of("UTC")),
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
    }

    @Test
    fun invalidHydrationAmountIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            runTest { repository.addWater(0) }
        }
    }
}
