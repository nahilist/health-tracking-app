package com.pulsehealth.app.data.export

import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pulsehealth.app.data.DefaultSleepRepository
import com.pulsehealth.app.data.DefaultVitalsRepository
import com.pulsehealth.app.data.DefaultWorkoutRepository
import com.pulsehealth.app.data.MeasurementContext
import com.pulsehealth.app.data.NoOpHydrationRepository
import com.pulsehealth.app.data.VitalType
import com.pulsehealth.app.data.WorkoutType
import com.pulsehealth.app.data.analytics.DefaultInsightsRepository
import com.pulsehealth.app.data.local.DailyHealthSummaryEntity
import com.pulsehealth.app.data.local.HydrationEntryEntity
import com.pulsehealth.app.data.local.PulseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HealthReportServiceTest {

    private lateinit var context: Context
    private lateinit var database: PulseDatabase
    private lateinit var service: DefaultHealthReportService
    private val fixedClock = Clock.fixed(Instant.parse("2026-11-18T10:00:00Z"), ZoneId.of("UTC"))
    private val zoneId = ZoneId.of("UTC")

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val vitalsRepo = DefaultVitalsRepository(
            vitalRecordDao = database.vitalRecordDao(),
            clock = fixedClock
        )

        val workoutRepo = DefaultWorkoutRepository(
            workoutDao = database.workoutDao(),
            clock = fixedClock
        )

        val sleepRepo = DefaultSleepRepository(
            sleepDao = database.sleepDao(),
            clock = fixedClock,
            zoneId = zoneId
        )

        val hydrationRepo = object : NoOpHydrationRepository() {
            override suspend fun getAllEntries(): List<HydrationEntryEntity> {
                return listOf(HydrationEntryEntity("h-1", 500, fixedClock.millis(), "manual", "synced"))
            }
        }

        val insightsRepo = DefaultInsightsRepository(
            dailySummaryDao = database.dailyHealthSummaryDao(),
            vitalsRepository = vitalsRepo,
            hydrationRepository = hydrationRepo,
            sleepRepository = sleepRepo,
            preferencesRepository = FakePreferencesRepo(),
            clock = fixedClock,
            zoneId = zoneId
        )

        service = DefaultHealthReportService(
            context = context,
            dailySummaryDao = database.dailyHealthSummaryDao(),
            vitalsRepository = vitalsRepo,
            workoutRepository = workoutRepo,
            sleepRepository = sleepRepo,
            hydrationRepository = hydrationRepo,
            insightsRepository = insightsRepo,
            clock = fixedClock,
            zoneId = zoneId
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun generateReport_pdf_createsReportFileAndReturnsContentUri() = runTest {
        // Seed some data
        database.dailyHealthSummaryDao().upsert(
            DailyHealthSummaryEntity(
                epochDay = 100,
                steps = 8500,
                restingHeartRate = 65,
                sleepMinutes = 450,
                activeCalories = 400,
                exerciseMinutes = 30,
                standHours = 10,
                source = "local",
                updatedAtEpochMillis = fixedClock.millis()
            )
        )

        val uri = service.generateReport(
            format = ReportFormat.PDF,
            range = ReportTimeRange.LAST_7_DAYS,
            doctorNotes = "Testing clinical notes"
        )

        assertNotNull(uri)
        assertTrue("URI should be a content URI", uri.toString().startsWith("content://"))
        assertTrue("URI should reference a PDF", uri.toString().endsWith(".pdf"))
    }

    @Test
    fun generateReport_csv_createsReportFileAndReturnsContentUri() = runTest {
        val uri = service.generateReport(
            format = ReportFormat.CSV,
            range = ReportTimeRange.LAST_30_DAYS
        )

        assertNotNull(uri)
        assertTrue(uri.toString().startsWith("content://"))
        assertTrue(uri.toString().endsWith(".csv"))
    }

    @Test
    fun createShareIntent_configuresActionSendWithUriAndPermissions() = runTest {
        val uri = service.generateReport(ReportFormat.PDF, ReportTimeRange.LAST_7_DAYS)
        val chooserIntent = service.createShareIntent(uri, ReportFormat.PDF)

        assertNotNull(chooserIntent)
        assertEquals(Intent.ACTION_CHOOSER, chooserIntent.action)

        val innerIntent = chooserIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertNotNull(innerIntent)
        assertEquals(Intent.ACTION_SEND, innerIntent?.action)
        assertEquals(ReportFormat.PDF.mimeType, innerIntent?.type)
        assertTrue(innerIntent?.flags?.and(Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0)
    }
}

private class FakePreferencesRepo : com.pulsehealth.app.data.PreferencesRepository {
    override val hiddenCards: Flow<Set<String>> = flowOf(emptySet())
    override val waterGoalLiters: Flow<Double> = flowOf(2.0)
    override val stepGoal: Flow<Long> = flowOf(10_000L)
    override val calorieGoal: Flow<Long> = flowOf(500L)
    override val sleepGoalMinutes: Flow<Long> = flowOf(480L)
    override val syncWaterToHealthConnect: Flow<Boolean> = flowOf(false)
    override val onboardingCompleted: Flow<Boolean> = flowOf(true)
    override val remindersEnabled: Flow<Boolean> = flowOf(false)
    override val reminderIntervalHours: Flow<Int> = flowOf(2)
    override val quietHoursStartHour: Flow<Int> = flowOf(22)
    override val quietHoursEndHour: Flow<Int> = flowOf(8)
    override val biometricLockEnabled: Flow<Boolean> = flowOf(false)
    override val biometricLockTimeoutSeconds: Flow<Long> = flowOf(0L)

    override suspend fun toggleCard(id: String) {}
    override suspend fun setWaterGoalLiters(goalLiters: Double) {}
    override suspend fun setStepGoal(steps: Long) {}
    override suspend fun setCalorieGoal(calories: Long) {}
    override suspend fun setSleepGoalMinutes(minutes: Long) {}
    override suspend fun setSyncWaterToHealthConnect(enabled: Boolean) {}
    override suspend fun setOnboardingCompleted(completed: Boolean) {}
    override suspend fun setRemindersEnabled(enabled: Boolean) {}
    override suspend fun setReminderIntervalHours(hours: Int) {}
    override suspend fun setQuietHours(startHour: Int, endHour: Int) {}
    override suspend fun setBiometricLockEnabled(enabled: Boolean) {}
    override suspend fun setBiometricLockTimeoutSeconds(seconds: Long) {}
    override suspend fun clearAllPreferences() {}
}
