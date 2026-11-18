package com.pulsehealth.app.data.analytics

import com.pulsehealth.app.data.DailyHydrationProgress
import com.pulsehealth.app.data.local.SleepSessionWithStages
import com.pulsehealth.app.data.SleepStageType
import com.pulsehealth.app.data.local.DailyHealthSummaryEntity
import com.pulsehealth.app.data.local.SleepSessionEntity
import com.pulsehealth.app.data.local.SleepStageEntity
import com.pulsehealth.app.data.local.VitalRecordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HealthAnomalyDetectorTest {

    @Test
    fun detect_returnsEmptyList_whenNoDataProvided() {
        val insights = HealthAnomalyDetector.detect(
            summaries = emptyList(),
            vitalRecords = emptyList(),
            hydrationProgress = emptyList(),
            sleepSessions = emptyList()
        )
        assertTrue(insights.isEmpty())
    }

    @Test
    fun detect_identifiesRestingHeartRateSpike_whenLatestIsSignificantlyElevated() {
        // Baseline 6 days at ~60 bpm, today jumps to 78 bpm (+18 bpm spike)
        val summaries = listOf(
            createSummary(epochDay = 100, rhr = 60),
            createSummary(epochDay = 101, rhr = 61),
            createSummary(epochDay = 102, rhr = 59),
            createSummary(epochDay = 103, rhr = 60),
            createSummary(epochDay = 104, rhr = 62),
            createSummary(epochDay = 105, rhr = 60),
            createSummary(epochDay = 106, rhr = 78) // Today's spike
        )

        val insights = HealthAnomalyDetector.detect(summaries = summaries)

        val rhrSpike = insights.find { it.type == InsightType.RESTING_HEART_RATE_SPIKE }
        assertNotNull("Expected RESTING_HEART_RATE_SPIKE insight", rhrSpike)
        assertEquals(InsightSeverity.ATTENTION, rhrSpike?.severity)
        assertEquals(InsightCategory.CARDIO, rhrSpike?.category)
        assertEquals("78 bpm", rhrSpike?.currentValueText)
        assertEquals("60 bpm", rhrSpike?.baselineValueText)
        assertTrue(rhrSpike?.description?.contains("18 bpm above") == true)
    }

    @Test
    fun detect_identifiesOptimalRestingRecovery_whenHeartRateIsHealthyAndLow() {
        val summaries = listOf(
            createSummary(epochDay = 100, rhr = 62),
            createSummary(epochDay = 101, rhr = 60),
            createSummary(epochDay = 102, rhr = 58),
            createSummary(epochDay = 103, rhr = 56) // Optimal recovery zone
        )

        val insights = HealthAnomalyDetector.detect(summaries = summaries)

        val optimalRhr = insights.find { it.type == InsightType.RESTING_HEART_RATE_OPTIMAL }
        assertNotNull("Expected RESTING_HEART_RATE_OPTIMAL insight", optimalRhr)
        assertEquals(InsightSeverity.POSITIVE, optimalRhr?.severity)
        assertEquals(InsightCategory.RECOVERY, optimalRhr?.category)
    }

    @Test
    fun detect_identifiesBloodPressureElevatedPattern() {
        val vitals = listOf(
            VitalRecordEntity(
                id = "bp-1",
                vitalType = "BLOOD_PRESSURE",
                systolic = 135,
                diastolic = 85,
                recordedAtEpochMillis = 1000L,
                measurementContext = "RESTING"
            ),
            VitalRecordEntity(
                id = "bp-2",
                vitalType = "BLOOD_PRESSURE",
                systolic = 130,
                diastolic = 84,
                recordedAtEpochMillis = 2000L,
                measurementContext = "RESTING"
            )
        )

        val insights = HealthAnomalyDetector.detect(
            summaries = emptyList(),
            vitalRecords = vitals
        )

        val bpInsight = insights.find { it.type == InsightType.BLOOD_PRESSURE_ELEVATED_TREND }
        assertNotNull("Expected BLOOD_PRESSURE_ELEVATED_TREND insight", bpInsight)
        assertEquals(InsightSeverity.ATTENTION, bpInsight?.severity)
        assertTrue(bpInsight?.description?.contains("133 / 85 mmHg") == true || bpInsight?.description?.contains("133") == true)
    }

    @Test
    fun detect_identifiesNormalBloodPressurePattern() {
        val vitals = listOf(
            VitalRecordEntity(
                id = "bp-1",
                vitalType = "BLOOD_PRESSURE",
                systolic = 118,
                diastolic = 76,
                recordedAtEpochMillis = 1000L,
                measurementContext = "RESTING"
            ),
            VitalRecordEntity(
                id = "bp-2",
                vitalType = "BLOOD_PRESSURE",
                systolic = 115,
                diastolic = 74,
                recordedAtEpochMillis = 2000L,
                measurementContext = "RESTING"
            )
        )

        val insights = HealthAnomalyDetector.detect(
            summaries = emptyList(),
            vitalRecords = vitals
        )

        val bpInsight = insights.find { it.type == InsightType.BLOOD_PRESSURE_OPTIMAL }
        assertNotNull("Expected BLOOD_PRESSURE_OPTIMAL insight", bpInsight)
        assertEquals(InsightSeverity.POSITIVE, bpInsight?.severity)
    }

    @Test
    fun detect_identifiesSleepDeficit() {
        val summaries = listOf(
            createSummary(epochDay = 100, sleepMinutes = 480),
            createSummary(epochDay = 101, sleepMinutes = 490),
            createSummary(epochDay = 102, sleepMinutes = 470),
            createSummary(epochDay = 103, sleepMinutes = 240) // 4 hours sleep (deficit)
        )

        val insights = HealthAnomalyDetector.detect(
            summaries = summaries,
            sleepGoalMinutes = 480L
        )

        val sleepDeficit = insights.find { it.type == InsightType.SLEEP_DEFICIT }
        assertNotNull("Expected SLEEP_DEFICIT insight", sleepDeficit)
        assertEquals(InsightSeverity.ATTENTION, sleepDeficit?.severity)
        assertEquals("4h 0m", sleepDeficit?.currentValueText)
    }

    @Test
    fun detect_identifiesHighRestorativeDeepSleep() {
        // Total sleep: 8 hours (480 min), Deep: 2.5 hours (150 min, >31%)
        val session = SleepSessionEntity(
            id = "sleep-sess-1",
            title = "Night Sleep",
            startTimeEpochMillis = 0L,
            endTimeEpochMillis = 480 * 60_000L,
            durationMinutes = 480,
            deepDurationMinutes = 150,
            lightDurationMinutes = 330,
            remDurationMinutes = 0,
            awakeDurationMinutes = 0,
            sleepEfficiencyPercentage = 95,
            source = "manual",
            notes = null,
            syncState = "synced"
        )
        val stages = listOf(
            SleepStageEntity(
                id = "st-1",
                sessionId = "sleep-sess-1",
                stage = SleepStageType.DEEP.name,
                startTimeEpochMillis = 0L,
                endTimeEpochMillis = 150 * 60_000L,
                durationMinutes = 150
            ),
            SleepStageEntity(
                id = "st-2",
                sessionId = "sleep-sess-1",
                stage = SleepStageType.LIGHT.name,
                startTimeEpochMillis = 150 * 60_000L,
                endTimeEpochMillis = 480 * 60_000L,
                durationMinutes = 330
            )
        )

        val insights = HealthAnomalyDetector.detect(
            summaries = emptyList(),
            sleepSessions = listOf(SleepSessionWithStages(session, stages))
        )

        val deepSleep = insights.find { it.type == InsightType.SLEEP_RESTORATIVE_HIGH }
        assertNotNull("Expected SLEEP_RESTORATIVE_HIGH insight", deepSleep)
        assertEquals(InsightSeverity.POSITIVE, deepSleep?.severity)
        assertEquals("31% deep sleep", deepSleep?.currentValueText)
    }

    @Test
    fun detect_identifiesActivitySurge() {
        val summaries = listOf(
            createSummary(epochDay = 100, steps = 10_000),
            createSummary(epochDay = 101, steps = 16_500) // 165% of 10,000 goal
        )

        val insights = HealthAnomalyDetector.detect(
            summaries = summaries,
            stepGoal = 10_000L
        )

        val surge = insights.find { it.type == InsightType.ACTIVITY_SURGE }
        assertNotNull("Expected ACTIVITY_SURGE insight", surge)
        assertEquals(InsightSeverity.POSITIVE, surge?.severity)
        assertTrue(surge?.title?.contains("Activity Surge") == true)
    }

    @Test
    fun detect_identifiesHydrationStreak() {
        val hydration = listOf(
            DailyHydrationProgress(LocalDate.of(2026, 11, 15), "Sun", 2.2, 2.0, false),
            DailyHydrationProgress(LocalDate.of(2026, 11, 16), "Mon", 2.5, 2.0, false),
            DailyHydrationProgress(LocalDate.of(2026, 11, 17), "Tue", 2.1, 2.0, false),
            DailyHydrationProgress(LocalDate.of(2026, 11, 18), "Today", 2.0, 2.0, true)
        )

        val insights = HealthAnomalyDetector.detect(
            summaries = emptyList(),
            hydrationProgress = hydration,
            waterGoalLiters = 2.0
        )

        val streak = insights.find { it.type == InsightType.HYDRATION_STREAK }
        assertNotNull("Expected HYDRATION_STREAK insight", streak)
        assertEquals(InsightSeverity.POSITIVE, streak?.severity)
        assertTrue(streak?.title?.contains("4-Day Hydration Streak") == true)
    }

    private fun createSummary(
        epochDay: Long,
        steps: Long = 8000,
        rhr: Long? = 65,
        sleepMinutes: Long = 450
    ): DailyHealthSummaryEntity {
        return DailyHealthSummaryEntity(
            epochDay = epochDay,
            steps = steps,
            restingHeartRate = rhr,
            sleepMinutes = sleepMinutes,
            activeCalories = 400,
            exerciseMinutes = 30,
            standHours = 10,
            source = "local",
            updatedAtEpochMillis = 1000L
        )
    }
}
