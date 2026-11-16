package com.pulsehealth.app.data

import com.pulsehealth.app.data.local.DailyHealthSummaryEntity

data class WeeklyWellnessDigest(
    val totalSteps: Long,
    val averageDailySteps: Long,
    val stepGoalHitDays: Int,
    val totalActiveCalories: Long,
    val averageNightlySleepMinutes: Long,
    val averageRestingHeartRate: Long?,
    val restingHeartRateStatus: String,
    val totalWaterLiters: Double,
    val hydrationGoalHitDays: Int,
    val recordedDaysCount: Int,
    val consistencyScore: Int,
    val milestoneMessage: String
)

object WeeklyDigestCalculator {

    fun calculate(
        summaries: List<DailyHealthSummaryEntity>,
        weeklyHydration: List<DailyHydrationProgress>,
        stepGoal: Long = 10_000L,
        calorieGoal: Long = 500L,
        sleepGoalMinutes: Long = 480L,
        waterGoalLiters: Double = 2.0
    ): WeeklyWellnessDigest? {
        val recentSummaries = summaries.takeLast(7)
        if (recentSummaries.isEmpty() && weeklyHydration.isEmpty()) return null

        val recordedDays = maxOf(recentSummaries.size, weeklyHydration.size).coerceAtLeast(1)

        val totalSteps = recentSummaries.sumOf { it.steps }
        val avgSteps = if (recentSummaries.isNotEmpty()) totalSteps / recentSummaries.size else 0L
        val stepHitDays = recentSummaries.count { it.steps >= stepGoal }

        val totalCalories = recentSummaries.sumOf { it.activeCalories }

        val sleepList = recentSummaries.map { it.sleepMinutes }.filter { it > 0 }
        val avgSleep = if (sleepList.isNotEmpty()) sleepList.average().toLong() else 0L

        val hrList = recentSummaries.mapNotNull { it.restingHeartRate }.filter { it > 0 }
        val avgHr = if (hrList.isNotEmpty()) hrList.average().toLong() else null
        val hrStatus = when {
            avgHr == null -> "No data"
            avgHr in 55..75 -> "Optimal & Rested"
            avgHr in 76..85 -> "Typical Baseline"
            else -> "Elevated Baseline"
        }

        val totalWater = weeklyHydration.sumOf { it.waterLiters }
        val waterHitDays = weeklyHydration.count { it.isGoalAchieved }

        val stepScore = if (recordedDays > 0) (stepHitDays.toFloat() / recordedDays * 40f).toInt() else 0
        val waterScore = if (recordedDays > 0) (waterHitDays.toFloat() / recordedDays * 35f).toInt() else 0
        val sleepScore = if (avgSleep >= (sleepGoalMinutes - 60)) 25 else (avgSleep.toFloat() / sleepGoalMinutes * 25f).toInt().coerceIn(0, 25)
        val consistencyScore = (stepScore + waterScore + sleepScore).coerceIn(0, 100)

        val milestone = when {
            consistencyScore >= 85 -> "Exceptional week! You maintained outstanding consistency across activity, hydration, and recovery."
            consistencyScore >= 65 -> "Great momentum this week! Your habits are trending consistently toward your wellness goals."
            consistencyScore >= 40 -> "Steady progress. Small, regular increases in daily steps and hydration will build strong momentum."
            else -> "A new week is beginning. Set comfortable daily targets to establish a healthy rhythm."
        }

        return WeeklyWellnessDigest(
            totalSteps = totalSteps,
            averageDailySteps = avgSteps,
            stepGoalHitDays = stepHitDays,
            totalActiveCalories = totalCalories,
            averageNightlySleepMinutes = avgSleep,
            averageRestingHeartRate = avgHr,
            restingHeartRateStatus = hrStatus,
            totalWaterLiters = totalWater,
            hydrationGoalHitDays = waterHitDays,
            recordedDaysCount = recordedDays,
            consistencyScore = consistencyScore,
            milestoneMessage = milestone
        )
    }

    fun calculateDemo(
        stepGoal: Long = 10_000L,
        waterGoalLiters: Double = 2.0
    ): WeeklyWellnessDigest = WeeklyWellnessDigest(
        totalSteps = 58_420L,
        averageDailySteps = 8_345L,
        stepGoalHitDays = 5,
        totalActiveCalories = 2_520L,
        averageNightlySleepMinutes = 468L,
        averageRestingHeartRate = 62L,
        restingHeartRateStatus = "Optimal & Rested",
        totalWaterLiters = 13.5,
        hydrationGoalHitDays = 6,
        recordedDaysCount = 7,
        consistencyScore = 88,
        milestoneMessage = "Outstanding weekly momentum! Your activity, hydration, and sleep consistency are well balanced."
    )
}
