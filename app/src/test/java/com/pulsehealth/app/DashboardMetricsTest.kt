package com.pulsehealth.app

import com.pulsehealth.app.data.DashboardMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardMetricsTest {
    @Test
    fun defaultGoalsAreValid() {
        val metrics = DashboardMetrics()
        assertTrue(metrics.stepGoal > 0)
        assertTrue(metrics.waterGoalLiters > 0)
        assertEquals(468, metrics.sleepMinutes)
    }
}
