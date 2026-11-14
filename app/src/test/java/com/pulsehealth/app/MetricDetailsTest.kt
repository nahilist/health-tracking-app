package com.pulsehealth.app

import com.pulsehealth.app.data.DailyHydrationProgress
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.HealthCategory
import com.pulsehealth.app.data.MetricRegistry
import com.pulsehealth.app.data.TimeRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class MetricDetailsTest {

    @Test
    fun `allMetrics contains eight distinct health metrics with proper categories`() {
        val metrics = MetricRegistry.allMetrics
        assertEquals(8, metrics.size)

        val activityMetrics = metrics.filter { it.category == HealthCategory.ACTIVITY }
        assertEquals(5, activityMetrics.size)
        assertTrue(activityMetrics.any { it.id == "Activity" })
        assertTrue(activityMetrics.any { it.id == "Steps" })
        assertTrue(activityMetrics.any { it.id == "Active calories" })
        assertTrue(activityMetrics.any { it.id == "Exercise" })
        assertTrue(activityMetrics.any { it.id == "Stand" })

        val vitalsMetrics = metrics.filter { it.category == HealthCategory.VITALS }
        assertEquals(1, vitalsMetrics.size)
        assertEquals("Heart rate", vitalsMetrics.first().id)

        val sleepMetrics = metrics.filter { it.category == HealthCategory.SLEEP }
        assertEquals(1, sleepMetrics.size)
        assertEquals("Sleep", sleepMetrics.first().id)

        val nutritionMetrics = metrics.filter { it.category == HealthCategory.NUTRITION }
        assertEquals(1, nutritionMetrics.size)
        assertEquals("Water", nutritionMetrics.first().id)
    }

    @Test
    fun `normalizeId normalizes common variations and case`() {
        assertEquals("Steps", MetricRegistry.normalizeId("steps"))
        assertEquals("Steps", MetricRegistry.normalizeId("DAILY STEPS"))
        assertEquals("Heart rate", MetricRegistry.normalizeId("heart rate"))
        assertEquals("Heart rate", MetricRegistry.normalizeId("resting heart rate"))
        assertEquals("Heart rate", MetricRegistry.normalizeId("pulse"))
        assertEquals("Sleep", MetricRegistry.normalizeId("sleep"))
        assertEquals("Water", MetricRegistry.normalizeId("water"))
        assertEquals("Water", MetricRegistry.normalizeId("hydration"))
        assertEquals("Active calories", MetricRegistry.normalizeId("active calories"))
        assertEquals("Active calories", MetricRegistry.normalizeId("move"))
        assertEquals("Exercise", MetricRegistry.normalizeId("exercise"))
        assertEquals("Stand", MetricRegistry.normalizeId("stand"))
        assertEquals("Activity", MetricRegistry.normalizeId("activity"))
    }

    @Test
    fun `search filters metrics by keyword or category`() {
        assertEquals(8, MetricRegistry.search("").size)

        val stepResults = MetricRegistry.search("step")
        assertEquals(1, stepResults.size)
        assertEquals("Steps", stepResults.first().id)

        val vitalsResults = MetricRegistry.search("vitals")
        assertEquals(1, vitalsResults.size)
        assertEquals("Heart rate", vitalsResults.first().id)

        val emptyResults = MetricRegistry.search("nonexistentxyz")
        assertTrue(emptyResults.isEmpty())
    }

    @Test
    fun `formatValue formats correctly with and without data`() {
        val sample = DashboardMetrics.demo()
        val empty = DashboardMetrics()

        assertEquals("8,432 steps", MetricRegistry.formatValue("Steps", sample, Locale.US))
        assertEquals("No data", MetricRegistry.formatValue("Steps", empty, Locale.US))

        assertEquals("62 bpm", MetricRegistry.formatValue("Heart rate", sample, Locale.US))
        assertEquals("No data", MetricRegistry.formatValue("Heart rate", empty, Locale.US))

        assertEquals("7 hr 48 min", MetricRegistry.formatValue("Sleep", sample, Locale.US))
        assertEquals("No data", MetricRegistry.formatValue("Sleep", empty, Locale.US))

        assertEquals("1.50 L", MetricRegistry.formatValue("Water", sample, Locale.US))
        assertEquals("0.0 L", MetricRegistry.formatValue("Water", empty, Locale.US))

        assertEquals("360 kcal", MetricRegistry.formatValue("Active calories", sample, Locale.US))
        assertEquals("24 min", MetricRegistry.formatValue("Exercise", sample, Locale.US))
        assertEquals("8 hr", MetricRegistry.formatValue("Stand", sample, Locale.US))
    }

    @Test
    fun `getGoalText and getProgressRatio calculate accurately`() {
        val sample = DashboardMetrics(
            steps = 8_000,
            stepGoal = 10_000,
            waterLiters = 1.5,
            waterGoalLiters = 2.0
        )

        assertEquals("Goal: 10,000 steps", MetricRegistry.getGoalText("Steps", sample, Locale.US))
        assertEquals(0.8f, MetricRegistry.getProgressRatio("Steps", sample)!!, 0.01f)

        assertEquals("Goal: 2.0 L", MetricRegistry.getGoalText("Water", sample, Locale.US))
        assertEquals(0.75f, MetricRegistry.getProgressRatio("Water", sample)!!, 0.01f)
    }

    @Test
    fun `getSummaryStats returns appropriate metrics breakdown`() {
        val sample = DashboardMetrics.demo()
        val stats = MetricRegistry.getSummaryStats(
            id = "Steps",
            metrics = sample,
            range = TimeRange.TODAY,
            weeklyHydration = emptyList(),
            locale = Locale.US
        )

        assertNotNull(stats.averageLabel)
        assertTrue(stats.averageLabel.contains("8,432"))
        assertNotNull(stats.totalLabel)
        assertNotNull(stats.highLabel)
        assertNotNull(stats.lowLabel)
        assertNotNull(stats.completionRateLabel)
    }

    @Test
    fun `getTrendPoints generates seven day points with correct today highlight`() {
        val sample = DashboardMetrics.demo()
        val points = MetricRegistry.getTrendPoints("Steps", sample, emptyList())

        assertEquals(7, points.size)
        assertTrue(points.last().isHighlighted)
        assertFalse(points.first().isHighlighted)

        val sampleWithGoalMet = DashboardMetrics(steps = 11_000, stepGoal = 10_000)
        val pointsWithGoal = MetricRegistry.getTrendPoints("Steps", sampleWithGoalMet, emptyList())
        assertTrue(pointsWithGoal.any { it.isGoalMet })

        val weeklyHydration = listOf(
            DailyHydrationProgress(LocalDate.now().minusDays(1), "Sun", 2.2, 2.0, false),
            DailyHydrationProgress(LocalDate.now(), "Mon", 2.0, 2.0, true)
        )
        val waterPoints = MetricRegistry.getTrendPoints("Water", sample, weeklyHydration)
        assertEquals(2, waterPoints.size)
        assertTrue(waterPoints.last().isHighlighted)
        assertTrue(waterPoints.all { it.isGoalMet })
    }
}
