package com.pulsehealth.app

import com.pulsehealth.app.data.DailyHydrationProgress
import com.pulsehealth.app.data.WeeklyDigestCalculator
import com.pulsehealth.app.data.local.DailyHealthSummaryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeeklyWellnessDigestTest {

    @Test
    fun calculateReturnsNullWhenNoDataAvailable() {
        val digest = WeeklyDigestCalculator.calculate(
            summaries = emptyList(),
            weeklyHydration = emptyList()
        )
        assertNull(digest)
    }

    @Test
    fun calculateAggregatesWeeklyMetricsCorrectly() {
        val summaries = listOf(
            DailyHealthSummaryEntity(epochDay = 101, steps = 8_000, restingHeartRate = 60, sleepMinutes = 450, activeCalories = 400, exerciseMinutes = 30, standHours = 8, source = "Health Connect", updatedAtEpochMillis = 1),
            DailyHealthSummaryEntity(epochDay = 102, steps = 11_000, restingHeartRate = 62, sleepMinutes = 480, activeCalories = 550, exerciseMinutes = 45, standHours = 10, source = "Health Connect", updatedAtEpochMillis = 2),
            DailyHealthSummaryEntity(epochDay = 103, steps = 10_500, restingHeartRate = 64, sleepMinutes = 420, activeCalories = 500, exerciseMinutes = 35, standHours = 9, source = "Health Connect", updatedAtEpochMillis = 3)
        )
        val hydration = listOf(
            DailyHydrationProgress(LocalDate.now().minusDays(2), "Wed", 2.0, 2.0, true),
            DailyHydrationProgress(LocalDate.now().minusDays(1), "Thu", 1.5, 2.0, false),
            DailyHydrationProgress(LocalDate.now(), "Fri", 2.5, 2.0, true)
        )

        val digest = WeeklyDigestCalculator.calculate(
            summaries = summaries,
            weeklyHydration = hydration,
            stepGoal = 10_000L,
            waterGoalLiters = 2.0
        )

        assertNotNull(digest)
        assertEquals(29_500L, digest!!.totalSteps)
        assertEquals(9_833L, digest.averageDailySteps)
        assertEquals(2, digest.stepGoalHitDays)
        assertEquals(1_450L, digest.totalActiveCalories)
        assertEquals(450L, digest.averageNightlySleepMinutes)
        assertEquals(62L, digest.averageRestingHeartRate)
        assertEquals("Optimal & Rested", digest.restingHeartRateStatus)
        assertEquals(6.0, digest.totalWaterLiters, 0.001)
        assertEquals(2, digest.hydrationGoalHitDays)
        assertEquals(3, digest.recordedDaysCount)
        assertTrue(digest.consistencyScore in 50..100)
    }

    @Test
    fun calculateDemoProvidesPopulatedDigest() {
        val demo = WeeklyDigestCalculator.calculateDemo()
        assertTrue(demo.totalSteps > 50_000L)
        assertEquals(7, demo.recordedDaysCount)
        assertTrue(demo.consistencyScore >= 80)
        assertTrue(demo.milestoneMessage.contains("Outstanding weekly momentum"))
    }

    @Test
    fun restingHeartRateBandsReflectExpectedStatus() {
        val lowHrSummary = listOf(
            DailyHealthSummaryEntity(epochDay = 1, steps = 5000, restingHeartRate = 60, sleepMinutes = 400, activeCalories = 300, exerciseMinutes = 20, standHours = 6, source = "HC", updatedAtEpochMillis = 1)
        )
        val lowHrDigest = WeeklyDigestCalculator.calculate(lowHrSummary, emptyList())
        assertEquals("Optimal & Rested", lowHrDigest?.restingHeartRateStatus)

        val moderateHrSummary = listOf(
            DailyHealthSummaryEntity(epochDay = 1, steps = 5000, restingHeartRate = 80, sleepMinutes = 400, activeCalories = 300, exerciseMinutes = 20, standHours = 6, source = "HC", updatedAtEpochMillis = 1)
        )
        val modHrDigest = WeeklyDigestCalculator.calculate(moderateHrSummary, emptyList())
        assertEquals("Typical Baseline", modHrDigest?.restingHeartRateStatus)

        val elevatedHrSummary = listOf(
            DailyHealthSummaryEntity(epochDay = 1, steps = 5000, restingHeartRate = 92, sleepMinutes = 400, activeCalories = 300, exerciseMinutes = 20, standHours = 6, source = "HC", updatedAtEpochMillis = 1)
        )
        val elevatedHrDigest = WeeklyDigestCalculator.calculate(elevatedHrSummary, emptyList())
        assertEquals("Elevated Baseline", elevatedHrDigest?.restingHeartRateStatus)
    }
}
