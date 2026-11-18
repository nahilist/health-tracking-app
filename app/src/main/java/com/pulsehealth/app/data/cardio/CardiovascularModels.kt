package com.pulsehealth.app.data.cardio

import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Standard 5-tier cardiovascular exercise heart rate zones based on percentage of Maximum Heart Rate (HRmax).
 */
enum class HeartRateZone(
    val zoneNumber: Int,
    val zoneName: String,
    val minPercent: Int,
    val maxPercent: Int,
    val colorHex: Long,
    val shortDescription: String,
    val physiologicalBenefit: String
) {
    ZONE_1_RECOVERY(
        zoneNumber = 1,
        zoneName = "Active Recovery",
        minPercent = 50,
        maxPercent = 60,
        colorHex = 0xFF5C9E9A, // Soft Emerald
        shortDescription = "Light, gentle movement with effortless conversational breathing.",
        physiologicalBenefit = "Accelerates metabolic waste clearance, promotes blood circulation, and eases post-workout muscle soreness."
    ),
    ZONE_2_FAT_BURN(
        zoneNumber = 2,
        zoneName = "Aerobic Base",
        minPercent = 60,
        maxPercent = 70,
        colorHex = 0xFF0D5C58, // Deep Forest Emerald
        shortDescription = "Sustainable, continuous pace; ideal for building long-term stamina.",
        physiologicalBenefit = "Maximizes mitochondrial density and cellular fat oxidation; the cornerstone of aerobic cardiovascular fitness."
    ),
    ZONE_3_AEROBIC(
        zoneNumber = 3,
        zoneName = "Aerobic Fitness",
        minPercent = 70,
        maxPercent = 80,
        colorHex = 0xFF00897B, // Teal / Energetic Green
        shortDescription = "Moderate-to-vigorous tempo; speaking requires focused breathing.",
        physiologicalBenefit = "Enhances cardiac stroke volume, expands lung capacity, and strengthens blood transport efficiency."
    ),
    ZONE_4_ANAEROBIC(
        zoneNumber = 4,
        zoneName = "Anaerobic Threshold",
        minPercent = 80,
        maxPercent = 90,
        colorHex = 0xFFE65100, // Warm Accent Amber / Orange
        shortDescription = "Hard effort pushing lactic threshold; sustainable only in short intervals.",
        physiologicalBenefit = "Raises lactate tolerance threshold, recruits fast-twitch muscle fibers, and boosts speed endurance."
    ),
    ZONE_5_MAXIMUM(
        zoneNumber = 5,
        zoneName = "Peak Output",
        minPercent = 90,
        maxPercent = 100,
        colorHex = 0xFFD81B60, // Vivid Pink / Peak Red
        shortDescription = "Maximum sprint power; all-out exertion sustainable for under 2 minutes.",
        physiologicalBenefit = "Maximizes VO2 peak output, neuromuscular motor unit recruitment, and supreme sprint capacity."
    );

    val label: String get() = "Zone $zoneNumber: $zoneName"
    val percentRangeString: String get() = "$minPercent%–$maxPercent% Max HR"
}

/**
 * Calculated BPM boundaries and aggregated statistics for a specific Heart Rate Zone.
 */
data class CardiovascularZoneBreakdown(
    val zone: HeartRateZone,
    val minBpm: Int,
    val maxBpm: Int,
    val durationMinutes: Int,
    val percentageOfTotal: Float,
    val estimatedCaloriesBurned: Int
)

/**
 * Cardio recovery status assessing parasympathetic nervous system reactivation 1-minute post exercise.
 */
enum class CardioRecoveryStatus(
    val title: String,
    val description: String,
    val minimumBpmDrop: Int,
    val colorHex: Long
) {
    EXCELLENT(
        title = "Excellent Cardio Recovery",
        description = "Rapid heart rate deceleration indicating elite parasympathetic reactivation and high cardiovascular fitness.",
        minimumBpmDrop = 30,
        colorHex = 0xFF0D5C58
    ),
    GOOD(
        title = "Healthy Cardio Recovery",
        description = "Steady, robust heart rate drop within healthy physiological standards.",
        minimumBpmDrop = 20,
        colorHex = 0xFF00897B
    ),
    FAIR(
        title = "Moderate Recovery",
        description = "Average heart rate recovery rate; consistent cool downs and hydration will improve efficiency.",
        minimumBpmDrop = 12,
        colorHex = 0xFFE65100
    ),
    DELAYED(
        title = "Delayed Recovery Notice",
        description = "Subtle pulse deceleration; ensure proper hydration, extended cool-down intervals, and adequate sleep.",
        minimumBpmDrop = 0,
        colorHex = 0xFFD81B60
    );

    companion object {
        fun fromBpmDrop(bpmDrop: Int): CardioRecoveryStatus {
            return when {
                bpmDrop >= 30 -> EXCELLENT
                bpmDrop >= 20 -> GOOD
                bpmDrop >= 12 -> FAIR
                else -> DELAYED
            }
        }
    }
}

/**
 * Mathematical and physiological utilities for calculating cardiovascular zones,
 * maximum heart rates, and pulse stability metrics.
 */
object HeartRateAnalyticsCalculator {

    /**
     * Calculates estimated maximum heart rate using the validated Tanaka formula:
     * HRmax = 208 - (0.7 * Age)
     */
    fun calculateMaxHeartRate(ageYears: Int = 30): Int {
        val safeAge = if (ageYears in 10..110) ageYears else 30
        return (208.0 - (0.7 * safeAge)).roundToInt()
    }

    /**
     * Returns the min and max BPM for each of the 5 heart rate zones.
     */
    fun calculateZoneBpmRanges(maxHr: Int): Map<HeartRateZone, IntRange> {
        val safeMax = if (maxHr in 120..240) maxHr else 187
        return HeartRateZone.entries.associateWith { zone ->
            val minBpm = (safeMax * (zone.minPercent / 100.0)).roundToInt()
            val maxBpm = (safeMax * (zone.maxPercent / 100.0)).roundToInt()
            minBpm..maxBpm
        }
    }

    /**
     * Classifies a real-time or recorded BPM into its respective Heart Rate Zone.
     */
    fun classifyZone(bpm: Int, maxHr: Int = 187): HeartRateZone {
        val safeMax = if (maxHr in 120..240) maxHr else 187
        val percentage = (bpm.toDouble() / safeMax.toDouble()) * 100.0
        return when {
            percentage >= 90.0 -> HeartRateZone.ZONE_5_MAXIMUM
            percentage >= 80.0 -> HeartRateZone.ZONE_4_ANAEROBIC
            percentage >= 70.0 -> HeartRateZone.ZONE_3_AEROBIC
            percentage >= 60.0 -> HeartRateZone.ZONE_2_FAT_BURN
            else -> HeartRateZone.ZONE_1_RECOVERY
        }
    }

    /**
     * Computes a realistic multi-zone distribution from total active duration and average BPM.
     */
    fun computeZoneBreakdowns(
        totalDurationMinutes: Int,
        averageBpm: Int,
        maxHr: Int = 187,
        activeCaloriesBurned: Int = 0
    ): List<CardiovascularZoneBreakdown> {
        val safeDuration = totalDurationMinutes.coerceAtLeast(0)
        val ranges = calculateZoneBpmRanges(maxHr)
        val primaryZone = classifyZone(averageBpm, maxHr)

        // Realistic bell curve distribution centered around primaryZone
        val weights = HeartRateZone.entries.map { zone ->
            val distance = kotlin.math.abs(zone.zoneNumber - primaryZone.zoneNumber)
            when (distance) {
                0 -> 0.45f
                1 -> 0.22f
                2 -> 0.05f
                else -> 0.005f
            }
        }
        val sumWeights = weights.sum()
        val normalizedWeights = weights.map { it / sumWeights }

        return HeartRateZone.entries.mapIndexed { index, zone ->
            val weight = normalizedWeights[index]
            val durationInZone = (safeDuration * weight).roundToInt()
            val caloriesInZone = if (activeCaloriesBurned > 0) {
                (activeCaloriesBurned * weight).roundToInt()
            } else {
                // Calorie burn estimate based on zone intensity multiplier
                val intensityMultiplier = 4 + (zone.zoneNumber * 2) // 6 to 14 kcal/min
                durationInZone * intensityMultiplier
            }
            val range = ranges[zone] ?: (100..120)

            CardiovascularZoneBreakdown(
                zone = zone,
                minBpm = range.first,
                maxBpm = range.last,
                durationMinutes = durationInZone,
                percentageOfTotal = weight,
                estimatedCaloriesBurned = caloriesInZone
            )
        }
    }

    /**
     * Computes statistical Resting Heart Rate (RHR) stability across a series of historical readings.
     */
    fun computeRhrStability(readings: List<Int>): RhrStabilityMetrics {
        if (readings.isEmpty()) {
            return RhrStabilityMetrics(
                averageBpm = 0,
                minBpm = 0,
                maxBpm = 0,
                standardDeviation = 0.0,
                stabilityScore = 100,
                statusLabel = "No Readings"
            )
        }

        val avg = readings.average()
        val min = readings.minOrNull() ?: 0
        val max = readings.maxOrNull() ?: 0

        val variance = readings.map { (it - avg) * (it - avg) }.average()
        val stdDev = sqrt(variance)

        // Standard deviation < 3 BPM indicates very stable resting heart rate
        val score = (100.0 - (stdDev * 10.0)).coerceIn(0.0, 100.0).roundToInt()

        val label = when {
            stdDev <= 3.0 -> "Optimal Stability"
            stdDev <= 6.0 -> "Normal Cadence"
            stdDev <= 9.0 -> "Moderate Variability"
            else -> "High Fluctuation"
        }

        return RhrStabilityMetrics(
            averageBpm = avg.roundToInt(),
            minBpm = min,
            maxBpm = max,
            standardDeviation = (stdDev * 10.0).roundToInt() / 10.0,
            stabilityScore = score,
            statusLabel = label
        )
    }
}

/**
 * Statistical summary of Resting Heart Rate stability over a rolling period.
 */
data class RhrStabilityMetrics(
    val averageBpm: Int,
    val minBpm: Int,
    val maxBpm: Int,
    val standardDeviation: Double,
    val stabilityScore: Int,
    val statusLabel: String
)
