package com.pulsehealth.app.data.analytics

import com.pulsehealth.app.data.DailyHydrationProgress
import com.pulsehealth.app.data.local.SleepSessionWithStages
import com.pulsehealth.app.data.SleepStageType
import com.pulsehealth.app.data.local.DailyHealthSummaryEntity
import com.pulsehealth.app.data.local.VitalRecordEntity
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.sqrt

object HealthAnomalyDetector {

    fun detect(
        summaries: List<DailyHealthSummaryEntity>,
        vitalRecords: List<VitalRecordEntity> = emptyList(),
        hydrationProgress: List<DailyHydrationProgress> = emptyList(),
        sleepSessions: List<SleepSessionWithStages> = emptyList(),
        stepGoal: Long = 10_000L,
        sleepGoalMinutes: Long = 480L,
        waterGoalLiters: Double = 2.0
    ): List<HealthInsight> {
        val insights = mutableListOf<HealthInsight>()

        // 1. Resting Heart Rate Analysis (Baseline & Anomaly)
        detectRestingHeartRateInsights(summaries, insights)

        // 2. Blood Pressure Trends
        detectBloodPressureInsights(vitalRecords, insights)

        // 3. Sleep Deficit & Restorative Stages
        detectSleepInsights(summaries, sleepSessions, sleepGoalMinutes, insights)

        // 4. Activity Milestones & Rest Patterns
        detectActivityInsights(summaries, stepGoal, insights)

        // 5. Hydration Consistency & Deficits
        detectHydrationInsights(hydrationProgress, waterGoalLiters, insights)

        return insights
    }

    private fun detectRestingHeartRateInsights(
        summaries: List<DailyHealthSummaryEntity>,
        insights: MutableList<HealthInsight>
    ) {
        val validHrSummaries = summaries.filter { it.restingHeartRate != null && it.restingHeartRate > 0 }
        if (validHrSummaries.size < 2) return

        val recentHrList = validHrSummaries.takeLast(7)
        val todayHrSummary = recentHrList.last()
        val todayHr = todayHrSummary.restingHeartRate ?: return

        val baselineHrList = recentHrList.dropLast(1).mapNotNull { it.restingHeartRate }
        if (baselineHrList.isEmpty()) return

        val baselineMean = baselineHrList.average()
        val variance = baselineHrList.map { (it - baselineMean) * (it - baselineMean) }.average()
        val stdDev = sqrt(variance)

        val deltaBpm = todayHr - baselineMean.roundToInt()
        val percentDelta = ((todayHr - baselineMean) / baselineMean * 100).roundToInt()

        // Significant spike: > 1.75 standard deviations AND at least 8 bpm above baseline
        if (deltaBpm >= 8 && (stdDev == 0.0 || deltaBpm >= 1.75 * stdDev)) {
            insights.add(
                HealthInsight(
                    id = "insight-rhr-spike-${todayHrSummary.epochDay}",
                    type = InsightType.RESTING_HEART_RATE_SPIKE,
                    severity = InsightSeverity.ATTENTION,
                    category = InsightCategory.CARDIO,
                    title = "Elevated Resting Heart Rate",
                    description = "Your resting heart rate today ($todayHr bpm) is $deltaBpm bpm above your 7-day baseline (${baselineMean.roundToInt()} bpm).",
                    recommendation = "Common non-clinical wellness factors include intense workouts, dehydration, poor sleep, or stress. Prioritize hydration and rest.",
                    metricType = "Resting heart rate",
                    currentValueText = "$todayHr bpm",
                    baselineValueText = "${baselineMean.roundToInt()} bpm",
                    deltaPercentage = percentDelta
                )
            )
        } else if (todayHr in 50..65 && deltaBpm <= 0) {
            insights.add(
                HealthInsight(
                    id = "insight-rhr-optimal-${todayHrSummary.epochDay}",
                    type = InsightType.RESTING_HEART_RATE_OPTIMAL,
                    severity = InsightSeverity.POSITIVE,
                    category = InsightCategory.RECOVERY,
                    title = "Optimal Resting Recovery",
                    description = "Your resting heart rate ($todayHr bpm) is within a relaxed, well-rested recovery zone.",
                    recommendation = "Consistent cardiovascular fitness and quality rest help maintain an efficient resting baseline.",
                    metricType = "Resting heart rate",
                    currentValueText = "$todayHr bpm",
                    baselineValueText = "${baselineMean.roundToInt()} bpm",
                    deltaPercentage = percentDelta
                )
            )
        }
    }

    private fun detectBloodPressureInsights(
        vitalRecords: List<VitalRecordEntity>,
        insights: MutableList<HealthInsight>
    ) {
        val bpRecords = vitalRecords.filter { it.vitalType == "BLOOD_PRESSURE" && it.systolic != null && it.diastolic != null }
            .sortedBy { it.recordedAtEpochMillis }

        if (bpRecords.size < 2) return

        val recentBp = bpRecords.takeLast(4)
        val elevatedCount = recentBp.count { (it.systolic ?: 0) >= 120 || (it.diastolic ?: 0) >= 80 }

        if (elevatedCount >= 2 && recentBp.size >= 2) {
            val avgSys = recentBp.mapNotNull { it.systolic }.average().roundToInt()
            val avgDia = recentBp.mapNotNull { it.diastolic }.average().roundToInt()

            insights.add(
                HealthInsight(
                    id = "insight-bp-elevated-${recentBp.last().id}",
                    type = InsightType.BLOOD_PRESSURE_ELEVATED_TREND,
                    severity = InsightSeverity.ATTENTION,
                    category = InsightCategory.CARDIO,
                    title = "Elevated Blood Pressure Pattern",
                    description = "Your recent blood pressure readings averaged $avgSys / $avgDia mmHg across ${recentBp.size} measurements.",
                    recommendation = "Measure under resting conditions at the same time each day. Lifestyle factors like sodium, caffeine, and stress can influence readings.",
                    metricType = "Blood pressure",
                    currentValueText = "$avgSys / $avgDia mmHg",
                    baselineValueText = "< 120 / 80 mmHg",
                    deltaPercentage = null
                )
            )
        } else if (recentBp.all { (it.systolic ?: 0) < 120 && (it.diastolic ?: 0) < 80 }) {
            val latest = recentBp.last()
            insights.add(
                HealthInsight(
                    id = "insight-bp-optimal-${latest.id}",
                    type = InsightType.BLOOD_PRESSURE_OPTIMAL,
                    severity = InsightSeverity.POSITIVE,
                    category = InsightCategory.CARDIO,
                    title = "Normal Blood Pressure Range",
                    description = "Your recent readings (${latest.systolic} / ${latest.diastolic} mmHg) align with standard wellness guidelines.",
                    recommendation = "Regular aerobic activity, balanced nutrition, and sound sleep support healthy blood pressure.",
                    metricType = "Blood pressure",
                    currentValueText = "${latest.systolic} / ${latest.diastolic} mmHg",
                    baselineValueText = "< 120 / 80 mmHg",
                    deltaPercentage = null
                )
            )
        }
    }

    private fun detectSleepInsights(
        summaries: List<DailyHealthSummaryEntity>,
        sleepSessions: List<SleepSessionWithStages>,
        sleepGoalMinutes: Long,
        insights: MutableList<HealthInsight>
    ) {
        val validSleepSummaries = summaries.filter { it.sleepMinutes > 0 }
        if (validSleepSummaries.isNotEmpty()) {
            val latest = validSleepSummaries.last()
            val baselineSleep = if (validSleepSummaries.size > 1) {
                validSleepSummaries.dropLast(1).takeLast(7).map { it.sleepMinutes }.average()
            } else {
                sleepGoalMinutes.toDouble()
            }

            val deficitMins = (baselineSleep - latest.sleepMinutes).roundToInt()
            // Sleep deficit: < 5 hours OR > 90 minutes below baseline
            if (latest.sleepMinutes < 300 || (deficitMins >= 90 && latest.sleepMinutes < sleepGoalMinutes)) {
                val hours = latest.sleepMinutes / 60
                val mins = latest.sleepMinutes % 60
                val baseHours = baselineSleep.roundToInt() / 60
                val baseMins = baselineSleep.roundToInt() % 60

                insights.add(
                    HealthInsight(
                        id = "insight-sleep-deficit-${latest.epochDay}",
                        type = InsightType.SLEEP_DEFICIT,
                        severity = InsightSeverity.ATTENTION,
                        category = InsightCategory.SLEEP,
                        title = "Significant Sleep Deficit",
                        description = "You logged ${hours}h ${mins}m of sleep, which is ${deficitMins}m below your baseline (${baseHours}h ${baseMins}m).",
                        recommendation = "Sleep debt impacts cognitive clarity and physical recovery. Consider an earlier bedtime tonight.",
                        metricType = "Sleep",
                        currentValueText = "${hours}h ${mins}m",
                        baselineValueText = "${baseHours}h ${baseMins}m",
                        deltaPercentage = -((deficitMins.toDouble() / baselineSleep) * 100).roundToInt()
                    )
                )
            }
        }

        // Deep sleep stage percentage check
        if (sleepSessions.isNotEmpty()) {
            val latestSession = sleepSessions.maxByOrNull { it.session.startTimeEpochMillis }
            if (latestSession != null && latestSession.stages.isNotEmpty()) {
                val deepMinutes = latestSession.stages
                    .filter { it.stage == SleepStageType.DEEP.name }
                    .sumOf { (it.endTimeEpochMillis - it.startTimeEpochMillis) / 60000L }

                val totalSleepMinutes = latestSession.stages
                    .filter { it.stage != SleepStageType.AWAKE.name && it.stage != SleepStageType.UNKNOWN.name }
                    .sumOf { (it.endTimeEpochMillis - it.startTimeEpochMillis) / 60000L }

                if (totalSleepMinutes > 0) {
                    val deepRatio = deepMinutes.toDouble() / totalSleepMinutes
                    val deepPercent = (deepRatio * 100).roundToInt()

                    if (deepRatio >= 0.25 && deepMinutes >= 60) {
                        insights.add(
                            HealthInsight(
                                id = "insight-deep-sleep-${latestSession.session.id}",
                                type = InsightType.SLEEP_RESTORATIVE_HIGH,
                                severity = InsightSeverity.POSITIVE,
                                category = InsightCategory.RECOVERY,
                                title = "High Restorative Sleep",
                                description = "Deep sleep accounted for $deepPercent% (${deepMinutes / 60}h ${deepMinutes % 60}m) of your night, boosting cellular repair.",
                                recommendation = "Optimal deep sleep enhances physical muscle restoration and immune health.",
                                metricType = "Sleep",
                                currentValueText = "$deepPercent% deep sleep",
                                baselineValueText = "15-20% avg",
                                deltaPercentage = deepPercent
                            )
                        )
                    }
                }
            }
        }
    }

    private fun detectActivityInsights(
        summaries: List<DailyHealthSummaryEntity>,
        stepGoal: Long,
        insights: MutableList<HealthInsight>
    ) {
        if (summaries.isEmpty()) return

        val latest = summaries.last()
        val pastSummaries = if (summaries.size > 1) summaries.dropLast(1).takeLast(7) else emptyList()
        val avgSteps = if (pastSummaries.isNotEmpty()) pastSummaries.map { it.steps }.average() else stepGoal.toDouble()

        if (latest.steps >= (stepGoal * 1.4).toLong() && latest.steps >= 12_000L) {
            val pctAbove = (((latest.steps - stepGoal).toDouble() / stepGoal) * 100).roundToInt()
            insights.add(
                HealthInsight(
                    id = "insight-activity-surge-${latest.epochDay}",
                    type = InsightType.ACTIVITY_SURGE,
                    severity = InsightSeverity.POSITIVE,
                    category = InsightCategory.ACTIVITY,
                    title = "Activity Surge",
                    description = "You've exceeded your daily step target by $pctAbove% (${String.format(Locale.getDefault(), "%,d", latest.steps)} steps today)!",
                    recommendation = "Great momentum! Ensure you rehydrate and incorporate light stretching after high-volume movement.",
                    metricType = "Steps",
                    currentValueText = String.format(Locale.getDefault(), "%,d steps", latest.steps),
                    baselineValueText = String.format(Locale.getDefault(), "%,d goal", stepGoal),
                    deltaPercentage = pctAbove
                )
            )
        } else if (pastSummaries.size >= 3 && latest.steps < (avgSteps * 0.35).toLong() && latest.steps > 0) {
            insights.add(
                HealthInsight(
                    id = "insight-activity-drop-${latest.epochDay}",
                    type = InsightType.ACTIVITY_DROP,
                    severity = InsightSeverity.INFO,
                    category = InsightCategory.ACTIVITY,
                    title = "Light Movement Day",
                    description = "Your steps today (${String.format(Locale.getDefault(), "%,d", latest.steps)}) are lower than your 7-day average (${String.format(Locale.getDefault(), "%,d", avgSteps.roundToInt())}).",
                    recommendation = "Rest days are essential for muscle repair. An easy evening stroll or stretching can support circulation.",
                    metricType = "Steps",
                    currentValueText = String.format(Locale.getDefault(), "%,d steps", latest.steps),
                    baselineValueText = String.format(Locale.getDefault(), "%,d avg", avgSteps.roundToInt()),
                    deltaPercentage = -(((avgSteps - latest.steps) / avgSteps) * 100).roundToInt()
                )
            )
        }
    }

    private fun detectHydrationInsights(
        hydrationProgress: List<DailyHydrationProgress>,
        waterGoalLiters: Double,
        insights: MutableList<HealthInsight>
    ) {
        if (hydrationProgress.isEmpty() || hydrationProgress.none { it.waterLiters > 0.0 }) return

        // Calculate consecutive goal hit streak
        var streak = 0
        for (item in hydrationProgress.reversed()) {
            if (item.isGoalAchieved) {
                streak++
            } else {
                break
            }
        }

        if (streak >= 3) {
            insights.add(
                HealthInsight(
                    id = "insight-hydration-streak-$streak",
                    type = InsightType.HYDRATION_STREAK,
                    severity = InsightSeverity.POSITIVE,
                    category = InsightCategory.HYDRATION,
                    title = "$streak-Day Hydration Streak",
                    description = "You've reached your daily hydration goal of ${String.format(Locale.getDefault(), "%.1f", waterGoalLiters)}L for $streak consecutive days!",
                    recommendation = "Consistent daily hydration supports cognitive stamina, digestion, and cardiovascular efficiency.",
                    metricType = "Hydration",
                    currentValueText = "$streak days",
                    baselineValueText = "Target: ${String.format(Locale.getDefault(), "%.1f", waterGoalLiters)}L",
                    deltaPercentage = null
                )
            )
        } else if (hydrationProgress.size >= 2) {
            val lastTwo = hydrationProgress.takeLast(2)
            if (lastTwo.all { it.waterLiters < (waterGoalLiters * 0.5) }) {
                val latest = lastTwo.last()
                insights.add(
                    HealthInsight(
                        id = "insight-hydration-deficit-${latest.date.toEpochDay()}",
                        type = InsightType.HYDRATION_DEFICIT,
                        severity = InsightSeverity.ATTENTION,
                        category = InsightCategory.HYDRATION,
                        title = "Hydration Below Target",
                        description = "Water intake has been below 50% of your daily goal (${String.format(Locale.getDefault(), "%.1f", waterGoalLiters)}L) over the past 2 days.",
                        recommendation = "Even mild dehydration elevates resting heart rate and reduces energy. Keep a water bottle nearby.",
                        metricType = "Hydration",
                        currentValueText = "${String.format(Locale.getDefault(), "%.1f", latest.waterLiters)}L",
                        baselineValueText = "${String.format(Locale.getDefault(), "%.1f", waterGoalLiters)}L goal",
                        deltaPercentage = -50
                    )
                )
            }
        }
    }
}
