package com.pulsehealth.app

import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.data.DataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardMetricsTest {
    @Test
    fun defaultMetricsHaveUnknownValuesAndValidGoals() {
        val metrics = DashboardMetrics()
        assertTrue(metrics.stepGoal > 0)
        assertTrue(metrics.waterGoalLiters > 0)
        assertTrue(metrics.calorieGoal > 0)
        assertTrue(metrics.exerciseGoalMinutes > 0)
        assertTrue(metrics.standGoalHours > 0)

        assertNull(metrics.steps)
        assertNull(metrics.heartRate)
        assertNull(metrics.sleepMinutes)
        assertNull(metrics.activeCalories)
        assertNull(metrics.exerciseMinutes)
        assertNull(metrics.standHours)
        assertEquals(0.0, metrics.waterLiters, 0.0)
        assertEquals(DataSource.NONE, metrics.source)
        assertNull(metrics.lastUpdatedEpochMillis)
        assertFalse(metrics.hasAnyData)
    }

    @Test
    fun demoMetricsContainExplicitLabeledSampleValues() {
        val clockMillis = 1_700_000_000_000L
        val demo = DashboardMetrics.demo(clockMillis)

        assertEquals(DataSource.DEMO, demo.source)
        assertEquals(8_432L, demo.steps)
        assertEquals(62L, demo.heartRate)
        assertEquals(468L, demo.sleepMinutes)
        assertEquals(1.5, demo.waterLiters, 0.0)
        assertEquals(360L, demo.activeCalories)
        assertEquals(24L, demo.exerciseMinutes)
        assertEquals(8L, demo.standHours)
        assertEquals(clockMillis, demo.lastUpdatedEpochMillis)
        assertTrue(demo.hasAnyData)
    }

    @Test
    fun hasAnyDataReflectsRecordPresence() {
        val empty = DashboardMetrics()
        assertFalse(empty.hasAnyData)

        val withSteps = empty.copy(steps = 1_000)
        assertTrue(withSteps.hasAnyData)

        val withWater = empty.copy(waterLiters = 0.25)
        assertTrue(withWater.hasAnyData)

        val withHeartRate = empty.copy(heartRate = 70)
        assertTrue(withHeartRate.hasAnyData)
    }

    @Test
    fun freshnessFormattingReturnsExpectedLabels() {
        val now = 1_000_000L

        // Just now (< 60s)
        assertEquals("Updated just now", DashboardUiState.formatFreshness(now - 30_000L, now))

        // 5 minutes ago
        assertEquals("Updated 5m ago", DashboardUiState.formatFreshness(now - (5 * 60 * 1000L), now))

        // 2 hours ago
        assertEquals("Updated 2h ago", DashboardUiState.formatFreshness(now - (2 * 60 * 60 * 1000L), now))
    }

    @Test
    fun uiStateFreshnessLabelPicksAvailableTimestamps() {
        val stateWithoutTime = DashboardUiState()
        assertNull(stateWithoutTime.freshnessLabel)

        val stateWithMetricTime = DashboardUiState(
            metrics = DashboardMetrics(lastUpdatedEpochMillis = System.currentTimeMillis() - 10_000)
        )
        assertEquals("Updated just now", stateWithMetricTime.freshnessLabel)

        val stateWithSyncTime = DashboardUiState(
            lastSyncedEpochMillis = System.currentTimeMillis() - 10_000
        )
        assertEquals("Updated just now", stateWithSyncTime.freshnessLabel)
    }
}
