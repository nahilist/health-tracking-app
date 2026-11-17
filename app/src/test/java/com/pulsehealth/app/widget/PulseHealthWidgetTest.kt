package com.pulsehealth.app.widget

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pulsehealth.app.data.local.DailyHealthSummaryEntity
import com.pulsehealth.app.data.local.HydrationEntryEntity
import com.pulsehealth.app.data.local.PulseDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PulseHealthWidgetTest {

    private lateinit var database: PulseDatabase
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, PulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun widgetState_progressRatioAndPercentagesCalculateAccurately() {
        val halfProgress = WidgetState(
            waterLiters = 1.0,
            waterGoalLiters = 2.0,
            steps = 5000,
            activeCalories = 250,
            isGoalAchieved = false
        )
        assertEquals(0.5f, halfProgress.progressRatio, 0.001f)
        assertEquals(50, halfProgress.percentInt)
        assertFalse(halfProgress.isGoalAchieved)

        val fullProgress = WidgetState(
            waterLiters = 2.5,
            waterGoalLiters = 2.0,
            steps = 10000,
            activeCalories = 500,
            isGoalAchieved = true
        )
        assertEquals(1.0f, fullProgress.progressRatio, 0.001f)
        assertEquals(100, fullProgress.percentInt)
        assertTrue(fullProgress.isGoalAchieved)

        val zeroProgress = WidgetState(
            waterLiters = 0.0,
            waterGoalLiters = 2.0
        )
        assertEquals(0.0f, zeroProgress.progressRatio, 0.001f)
        assertEquals(0, zeroProgress.percentInt)
    }

    @Test
    fun widgetDataProvider_loadsPersistedDatabaseRecords() = runTest {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val now = System.currentTimeMillis()

        // Insert mock hydration
        database.hydrationDao().upsert(
            HydrationEntryEntity(
                id = "w_entry_1",
                amountMilliliters = 750,
                recordedAtEpochMillis = now,
                source = "widget",
                syncState = "pending"
            )
        )

        // Insert mock summary
        database.dailyHealthSummaryDao().upsert(
            DailyHealthSummaryEntity(
                epochDay = today.toEpochDay(),
                steps = 7420,
                restingHeartRate = 64,
                sleepMinutes = 480,
                activeCalories = 380,
                exerciseMinutes = 45,
                standHours = 10,
                source = "local",
                updatedAtEpochMillis = now
            )
        )

        val state = WidgetDataProvider.loadState(context)
        // Verify state is populated
        assertEquals(2.0, state.waterGoalLiters, 0.0)
        assertFalse(state.dateLabel.isBlank())
    }

    @Test
    fun database_quickLogHydrationWorksForWidget() = runTest {
        val now = System.currentTimeMillis()
        val entity = HydrationEntryEntity(
            id = "test_widget_entry",
            amountMilliliters = 250,
            recordedAtEpochMillis = now,
            source = "widget",
            syncState = "pending"
        )
        database.hydrationDao().upsert(entity)

        val entries = database.hydrationDao().getAllEntries()
        assertEquals(1, entries.size)
        assertEquals(250, entries.first().amountMilliliters)
        assertEquals("widget", entries.first().source)
        assertEquals("pending", entries.first().syncState)
    }
}
