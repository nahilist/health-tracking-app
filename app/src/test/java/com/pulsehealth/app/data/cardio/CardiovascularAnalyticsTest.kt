package com.pulsehealth.app.data.cardio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CardiovascularAnalyticsTest {

    @Test
    fun tanakaFormula_calculatesExpectedMaxHeartRate() {
        // Tanaka Formula: 208 - (0.7 * Age)
        // Age 30: 208 - 21.0 = 187
        assertEquals(187, HeartRateAnalyticsCalculator.calculateMaxHeartRate(30))

        // Age 20: 208 - 14.0 = 194
        assertEquals(194, HeartRateAnalyticsCalculator.calculateMaxHeartRate(20))

        // Age 50: 208 - 35.0 = 173
        assertEquals(173, HeartRateAnalyticsCalculator.calculateMaxHeartRate(50))

        // Age out of normal bounds defaults safely to 30
        assertEquals(187, HeartRateAnalyticsCalculator.calculateMaxHeartRate(-5))
        assertEquals(187, HeartRateAnalyticsCalculator.calculateMaxHeartRate(150))
    }

    @Test
    fun classifyZone_mapsBpmAccuratelyAgainstMaxHr() {
        val maxHr = 200 // Makes percentages easy to verify

        // < 60% (< 120 BPM) -> ZONE 1
        assertEquals(HeartRateZone.ZONE_1_RECOVERY, HeartRateAnalyticsCalculator.classifyZone(110, maxHr))

        // 60%..69% (120..139 BPM) -> ZONE 2
        assertEquals(HeartRateZone.ZONE_2_FAT_BURN, HeartRateAnalyticsCalculator.classifyZone(125, maxHr))

        // 70%..79% (140..159 BPM) -> ZONE 3
        assertEquals(HeartRateZone.ZONE_3_AEROBIC, HeartRateAnalyticsCalculator.classifyZone(150, maxHr))

        // 80%..89% (160..179 BPM) -> ZONE 4
        assertEquals(HeartRateZone.ZONE_4_ANAEROBIC, HeartRateAnalyticsCalculator.classifyZone(170, maxHr))

        // >= 90% (>= 180 BPM) -> ZONE 5
        assertEquals(HeartRateZone.ZONE_5_MAXIMUM, HeartRateAnalyticsCalculator.classifyZone(185, maxHr))
    }

    @Test
    fun calculateZoneBpmRanges_providesContiguousBpmWindows() {
        val maxHr = 187
        val ranges = HeartRateAnalyticsCalculator.calculateZoneBpmRanges(maxHr)

        assertEquals(5, ranges.size)

        val z1 = ranges[HeartRateZone.ZONE_1_RECOVERY]!!
        val z2 = ranges[HeartRateZone.ZONE_2_FAT_BURN]!!
        val z3 = ranges[HeartRateZone.ZONE_3_AEROBIC]!!
        val z4 = ranges[HeartRateZone.ZONE_4_ANAEROBIC]!!
        val z5 = ranges[HeartRateZone.ZONE_5_MAXIMUM]!!

        assertTrue(z1.first > 0)
        assertTrue(z1.last <= z2.first + 1)
        assertTrue(z2.last <= z3.first + 1)
        assertTrue(z3.last <= z4.first + 1)
        assertTrue(z4.last <= z5.first + 1)
        assertEquals(maxHr, z5.last)
    }

    @Test
    fun computeZoneBreakdowns_sumsToTotalDuration() {
        val totalMinutes = 60
        val avgBpm = 142
        val maxHr = 187

        val breakdowns = HeartRateAnalyticsCalculator.computeZoneBreakdowns(
            totalDurationMinutes = totalMinutes,
            averageBpm = avgBpm,
            maxHr = maxHr,
            activeCaloriesBurned = 450
        )

        assertEquals(5, breakdowns.size)
        val sumMinutes = breakdowns.sumOf { it.durationMinutes }
        // Rounding may differ by at most 1-2 minutes from total
        assertTrue(sumMinutes in (totalMinutes - 2)..(totalMinutes + 2))

        val sumCalories = breakdowns.sumOf { it.estimatedCaloriesBurned }
        assertTrue(sumCalories > 0)
    }

    @Test
    fun cardioRecoveryStatus_classifiesBpmDropAccurately() {
        assertEquals(CardioRecoveryStatus.EXCELLENT, CardioRecoveryStatus.fromBpmDrop(35))
        assertEquals(CardioRecoveryStatus.EXCELLENT, CardioRecoveryStatus.fromBpmDrop(30))
        assertEquals(CardioRecoveryStatus.GOOD, CardioRecoveryStatus.fromBpmDrop(25))
        assertEquals(CardioRecoveryStatus.GOOD, CardioRecoveryStatus.fromBpmDrop(20))
        assertEquals(CardioRecoveryStatus.FAIR, CardioRecoveryStatus.fromBpmDrop(15))
        assertEquals(CardioRecoveryStatus.FAIR, CardioRecoveryStatus.fromBpmDrop(12))
        assertEquals(CardioRecoveryStatus.DELAYED, CardioRecoveryStatus.fromBpmDrop(8))
    }

    @Test
    fun computeRhrStability_measuresCadenceAndVariance() {
        // High stability sample (minimal variance around 65)
        val steadySamples = listOf(64, 65, 66, 65, 64, 65, 66)
        val steadyMetrics = HeartRateAnalyticsCalculator.computeRhrStability(steadySamples)
        assertEquals(65, steadyMetrics.averageBpm)
        assertEquals("Optimal Stability", steadyMetrics.statusLabel)
        assertTrue(steadyMetrics.stabilityScore >= 90)

        // Fluctuating sample (wide variance)
        val fluctuatingSamples = listOf(55, 75, 95, 60, 85, 100)
        val fluctuatingMetrics = HeartRateAnalyticsCalculator.computeRhrStability(fluctuatingSamples)
        assertEquals("High Fluctuation", fluctuatingMetrics.statusLabel)
        assertTrue(fluctuatingMetrics.stabilityScore < 70)

        // Empty sample handling
        val emptyMetrics = HeartRateAnalyticsCalculator.computeRhrStability(emptyList())
        assertEquals("No Readings", emptyMetrics.statusLabel)
        assertEquals(0, emptyMetrics.averageBpm)
    }
}
