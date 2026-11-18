package com.pulsehealth.app.data.analytics

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pulsehealth.app.data.DefaultSleepRepository
import com.pulsehealth.app.data.DefaultVitalsRepository
import com.pulsehealth.app.data.HydrationRepository
import com.pulsehealth.app.data.MeasurementContext
import com.pulsehealth.app.data.VitalType
import com.pulsehealth.app.data.NoOpHydrationRepository
import com.pulsehealth.app.data.PreferencesRepository
import com.pulsehealth.app.data.SleepRepository
import com.pulsehealth.app.data.VitalsRepository
import com.pulsehealth.app.data.local.DailyHealthSummaryEntity
import com.pulsehealth.app.data.local.HydrationEntryEntity
import com.pulsehealth.app.data.local.PulseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class InsightsRepositoryTest {

    private lateinit var database: PulseDatabase
    private lateinit var vitalsRepository: VitalsRepository
    private lateinit var sleepRepository: SleepRepository
    private lateinit var insightsRepository: DefaultInsightsRepository

    private val fixedClock = Clock.fixed(Instant.parse("2026-11-18T10:00:00Z"), ZoneId.of("UTC"))
    private val zoneId = ZoneId.of("UTC")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        vitalsRepository = DefaultVitalsRepository(
            vitalRecordDao = database.vitalRecordDao(),
            clock = fixedClock
        )

        sleepRepository = DefaultSleepRepository(
            sleepDao = database.sleepDao(),
            clock = fixedClock,
            zoneId = zoneId
        )

        val fakeHydrationRepo = object : NoOpHydrationRepository() {
            override fun observePastDaysEntries(days: Int): Flow<List<HydrationEntryEntity>> {
                return flowOf(emptyList())
            }
        }

        val fakePreferencesRepo = object : FakePreferencesRepository() {
            override val stepGoal: Flow<Long> = flowOf(10_000L)
            override val sleepGoalMinutes: Flow<Long> = flowOf(480L)
            override val waterGoalLiters: Flow<Double> = flowOf(2.0)
        }

        insightsRepository = DefaultInsightsRepository(
            dailySummaryDao = database.dailyHealthSummaryDao(),
            vitalsRepository = vitalsRepository,
            hydrationRepository = fakeHydrationRepo,
            sleepRepository = sleepRepository,
            preferencesRepository = fakePreferencesRepo,
            clock = fixedClock,
            zoneId = zoneId
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeInsights_emitsEmptyList_whenDatabaseIsEmpty() = runTest {
        val insights = insightsRepository.observeInsights().first()
        assertTrue(insights.isEmpty())
    }

    @Test
    fun observeInsights_emitsRestingHeartRateSpike_whenBaselineAndSpikeInserted() = runTest {
        val todayEpochDay = LocalDate.now(fixedClock).toEpochDay()
        val dao = database.dailyHealthSummaryDao()

        // 5 baseline days around 60 bpm
        for (i in 5 downTo 1) {
            dao.upsert(
                DailyHealthSummaryEntity(
                    epochDay = todayEpochDay - i,
                    steps = 8000,
                    restingHeartRate = 60,
                    sleepMinutes = 480,
                    activeCalories = 400,
                    exerciseMinutes = 30,
                    standHours = 10,
                    source = "local",
                    updatedAtEpochMillis = 1000L
                )
            )
        }

        // Today's spike to 82 bpm (+22 bpm)
        dao.upsert(
            DailyHealthSummaryEntity(
                epochDay = todayEpochDay,
                steps = 8000,
                restingHeartRate = 82,
                sleepMinutes = 480,
                activeCalories = 400,
                exerciseMinutes = 30,
                standHours = 10,
                source = "local",
                updatedAtEpochMillis = 1000L
            )
        )

        val insights = insightsRepository.observeInsights().first()
        val rhrInsight = insights.find { it.type == InsightType.RESTING_HEART_RATE_SPIKE }

        assertNotNull(rhrInsight)
        assertEquals(InsightSeverity.ATTENTION, rhrInsight?.severity)
        assertEquals("82 bpm", rhrInsight?.currentValueText)
    }

    @Test
    fun observeInsightsForMetric_filtersByMetricType() = runTest {
        // Add two blood pressure entries in elevated range
        vitalsRepository.logVital(
            type = VitalType.BLOOD_PRESSURE,
            systolic = 135,
            diastolic = 88,
            heartRateBpm = 72,
            context = MeasurementContext.RESTING
        )
        vitalsRepository.logVital(
            type = VitalType.BLOOD_PRESSURE,
            systolic = 132,
            diastolic = 85,
            heartRateBpm = 70,
            context = MeasurementContext.RESTING
        )

        val bpInsights = insightsRepository.observeInsightsForMetric("Blood pressure").first()
        assertEquals(1, bpInsights.size)
        assertEquals(InsightType.BLOOD_PRESSURE_ELEVATED_TREND, bpInsights.first().type)

        val sleepInsights = insightsRepository.observeInsightsForMetric("Sleep").first()
        assertTrue(sleepInsights.isEmpty())
    }
}

private open class FakePreferencesRepository : PreferencesRepository {
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
