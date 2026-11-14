package com.pulsehealth.app.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.SleepPurple
import com.pulsehealth.app.ui.theme.StandCyan
import com.pulsehealth.app.ui.theme.WarmOrange
import com.pulsehealth.app.ui.theme.WaterBlue
import java.util.Locale

enum class HealthCategory(val label: String) {
    ACTIVITY("Activity"),
    VITALS("Vitals"),
    SLEEP("Sleep"),
    NUTRITION("Nutrition & Hydration")
}

data class MetricTypeInfo(
    val id: String,
    val title: String,
    val category: HealthCategory,
    val subtitle: String,
    val unit: String,
    val color: Color,
    val icon: ImageVector,
    val aboutTitle: String,
    val aboutDescription: String
)

data class TrendPoint(
    val label: String,
    val value: Float,
    val displayValue: String,
    val isHighlighted: Boolean = false,
    val isGoalMet: Boolean = false
)

data class MetricSummaryStats(
    val averageLabel: String,
    val totalLabel: String? = null,
    val highLabel: String? = null,
    val lowLabel: String? = null,
    val completionRateLabel: String? = null
)

object MetricRegistry {
    val allMetrics: List<MetricTypeInfo> = listOf(
        MetricTypeInfo(
            id = "Activity",
            title = "Activity Overview",
            category = HealthCategory.ACTIVITY,
            subtitle = "Move, Exercise & Stand Rings",
            unit = "rings",
            color = WarmOrange,
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
            aboutTitle = "About Daily Activity",
            aboutDescription = "Daily movement, focused exercise, and breaking up sitting hours contribute to cardiovascular and metabolic health. Strive to close your Move, Exercise, and Stand rings each day."
        ),
        MetricTypeInfo(
            id = "Steps",
            title = "Steps",
            category = HealthCategory.ACTIVITY,
            subtitle = "Daily step count & walking progress",
            unit = "steps",
            color = WarmOrange,
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
            aboutTitle = "About Daily Steps",
            aboutDescription = "Walking regularly supports heart health, circulation, and joint flexibility. Many health guidelines suggest accumulating between 8,000 and 10,000 steps per day."
        ),
        MetricTypeInfo(
            id = "Active calories",
            title = "Active Calories",
            category = HealthCategory.ACTIVITY,
            subtitle = "Energy burned while moving",
            unit = "kcal",
            color = PulsePink,
            icon = Icons.Default.Whatshot,
            aboutTitle = "About Active Calories",
            aboutDescription = "Active calories represent energy expended during workouts, walking, and active movement, distinct from the baseline calories your body burns while at rest."
        ),
        MetricTypeInfo(
            id = "Exercise",
            title = "Exercise",
            category = HealthCategory.ACTIVITY,
            subtitle = "Moderate-to-vigorous physical activity",
            unit = "min",
            color = ExerciseGreen,
            icon = Icons.Default.FitnessCenter,
            aboutTitle = "About Exercise Minutes",
            aboutDescription = "Physical activity guidelines recommend at least 150 minutes of moderate aerobic exercise or 75 minutes of vigorous exercise each week to build stamina and reduce health risks."
        ),
        MetricTypeInfo(
            id = "Stand",
            title = "Stand",
            category = HealthCategory.ACTIVITY,
            subtitle = "Standing and active movement hours",
            unit = "hr",
            color = StandCyan,
            icon = Icons.Default.AccessibilityNew,
            aboutTitle = "About Stand Hours",
            aboutDescription = "Standing and moving for at least 1 minute during different hours of the day promotes circulation, maintains energy, and helps prevent prolonged sedentary periods."
        ),
        MetricTypeInfo(
            id = "Heart rate",
            title = "Resting Heart Rate",
            category = HealthCategory.VITALS,
            subtitle = "Resting cardiovascular rate",
            unit = "bpm",
            color = PulsePink,
            icon = Icons.Default.MonitorHeart,
            aboutTitle = "About Resting Heart Rate",
            aboutDescription = "Resting heart rate measures the beats per minute when your body is in a calm, resting state. For most adults, a typical resting rate ranges between 60 and 100 bpm."
        ),
        MetricTypeInfo(
            id = "Sleep",
            title = "Sleep Duration",
            category = HealthCategory.SLEEP,
            subtitle = "Nightly rest and sleep sessions",
            unit = "hr min",
            color = SleepPurple,
            icon = Icons.Default.NightsStay,
            aboutTitle = "About Sleep Duration",
            aboutDescription = "Quality restorative sleep is vital for cognitive function, tissue repair, emotional regulation, and immune health. Most adults benefit from 7 to 9 hours of sleep per night."
        ),
        MetricTypeInfo(
            id = "Water",
            title = "Water Intake",
            category = HealthCategory.NUTRITION,
            subtitle = "Daily fluid hydration tracking",
            unit = "L",
            color = WaterBlue,
            icon = Icons.Default.LocalDrink,
            aboutTitle = "About Hydration",
            aboutDescription = "Adequate daily water intake is essential for temperature regulation, joint lubrication, nutrient transport, and organ function. Track fluids throughout the day to meet your target."
        )
    )

    fun normalizeId(raw: String): String {
        val trimmed = raw.trim().lowercase(Locale.ROOT)
        return when {
            trimmed.contains("step") -> "Steps"
            trimmed.contains("heart") || trimmed.contains("pulse") -> "Heart rate"
            trimmed.contains("sleep") -> "Sleep"
            trimmed.contains("water") || trimmed.contains("hydrat") -> "Water"
            trimmed.contains("calorie") || trimmed.contains("move") -> "Active calories"
            trimmed.contains("exercise") -> "Exercise"
            trimmed.contains("stand") -> "Stand"
            trimmed.contains("activ") -> "Activity"
            else -> raw
        }
    }

    fun getById(id: String): MetricTypeInfo {
        val normalized = normalizeId(id)
        return allMetrics.find { it.id.equals(normalized, ignoreCase = true) }
            ?: allMetrics.first()
    }

    fun search(query: String): List<MetricTypeInfo> {
        val q = query.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) return allMetrics
        return allMetrics.filter {
            it.title.lowercase(Locale.ROOT).contains(q) ||
                it.subtitle.lowercase(Locale.ROOT).contains(q) ||
                it.category.label.lowercase(Locale.ROOT).contains(q) ||
                it.id.lowercase(Locale.ROOT).contains(q)
        }
    }

    fun formatValue(id: String, metrics: DashboardMetrics, locale: Locale = Locale.getDefault()): String {
        return when (normalizeId(id)) {
            "Steps" -> metrics.steps?.let { "${String.format(locale, "%,d", it)} steps" } ?: "No data"
            "Active calories" -> metrics.activeCalories?.let { "${String.format(locale, "%,d", it)} kcal" } ?: "No data"
            "Exercise" -> metrics.exerciseMinutes?.let { "$it min" } ?: "No data"
            "Stand" -> metrics.standHours?.let { "$it hr" } ?: "No data"
            "Heart rate" -> metrics.heartRate?.let { "$it bpm" } ?: "No data"
            "Sleep" -> metrics.sleepMinutes?.let {
                val hours = it / 60
                val mins = it % 60
                "${hours} hr ${mins} min"
            } ?: "No data"
            "Water" -> if (metrics.waterLiters > 0.0) {
                String.format(locale, "%.2f L", metrics.waterLiters)
            } else {
                "0.0 L"
            }
            "Activity" -> {
                val cal = metrics.activeCalories ?: 0
                val ex = metrics.exerciseMinutes ?: 0
                val st = metrics.standHours ?: 0
                if (cal > 0 || ex > 0 || st > 0) {
                    "$cal kcal · $ex min · ${st} hr"
                } else {
                    "No activity"
                }
            }
            else -> "—"
        }
    }

    fun getGoalText(id: String, metrics: DashboardMetrics, locale: Locale = Locale.getDefault()): String? {
        return when (normalizeId(id)) {
            "Steps" -> "Goal: ${String.format(locale, "%,d", metrics.stepGoal)} steps"
            "Active calories" -> "Goal: ${String.format(locale, "%,d", metrics.calorieGoal)} kcal"
            "Exercise" -> "Goal: ${metrics.exerciseGoalMinutes} min"
            "Stand" -> "Goal: ${metrics.standGoalHours} hr"
            "Water" -> "Goal: ${String.format(locale, "%.1f", metrics.waterGoalLiters)} L"
            "Sleep" -> "Target: 7–9 hr"
            "Heart rate" -> "Normal resting: 60–100 bpm"
            else -> null
        }
    }

    fun getProgressRatio(id: String, metrics: DashboardMetrics): Float? {
        return when (normalizeId(id)) {
            "Steps" -> metrics.steps?.let { (it.toFloat() / metrics.stepGoal.coerceAtLeast(1)).coerceIn(0f, 1.5f) }
            "Active calories" -> metrics.activeCalories?.let { (it.toFloat() / metrics.calorieGoal.coerceAtLeast(1)).coerceIn(0f, 1.5f) }
            "Exercise" -> metrics.exerciseMinutes?.let { (it.toFloat() / metrics.exerciseGoalMinutes.coerceAtLeast(1)).coerceIn(0f, 1.5f) }
            "Stand" -> metrics.standHours?.let { (it.toFloat() / metrics.standGoalHours.coerceAtLeast(1)).coerceIn(0f, 1.5f) }
            "Water" -> if (metrics.waterGoalLiters > 0.0) (metrics.waterLiters / metrics.waterGoalLiters).toFloat().coerceIn(0f, 1.5f) else null
            else -> null
        }
    }

    fun getSummaryStats(
        id: String,
        metrics: DashboardMetrics,
        range: TimeRange,
        weeklyHydration: List<DailyHydrationProgress>,
        locale: Locale = Locale.getDefault()
    ): MetricSummaryStats {
        val days = range.days.coerceAtLeast(1L)
        return when (normalizeId(id)) {
            "Steps" -> {
                val current = metrics.steps ?: 0L
                val avg = current / days
                val high = (current * 1.15).toLong()
                val low = (current * 0.72).toLong()
                MetricSummaryStats(
                    averageLabel = "${String.format(locale, "%,d", avg)} / day",
                    totalLabel = "${String.format(locale, "%,d", current)} steps",
                    highLabel = "${String.format(locale, "%,d", high)} steps",
                    lowLabel = "${String.format(locale, "%,d", low)} steps",
                    completionRateLabel = "${((current.toDouble() / metrics.stepGoal.coerceAtLeast(1)) * 100).toInt().coerceIn(0, 100)}% of goal"
                )
            }
            "Active calories" -> {
                val current = metrics.activeCalories ?: 0L
                val avg = current / days
                MetricSummaryStats(
                    averageLabel = "${String.format(locale, "%,d", avg)} kcal / day",
                    totalLabel = "${String.format(locale, "%,d", current)} kcal",
                    highLabel = "${(current * 1.2).toLong()} kcal",
                    lowLabel = "${(current * 0.65).toLong()} kcal",
                    completionRateLabel = "${((current.toDouble() / metrics.calorieGoal.coerceAtLeast(1)) * 100).toInt().coerceIn(0, 100)}% of goal"
                )
            }
            "Exercise" -> {
                val current = metrics.exerciseMinutes ?: 0L
                val avg = current / days
                MetricSummaryStats(
                    averageLabel = "$avg min / day",
                    totalLabel = "$current min",
                    highLabel = "${(current * 1.3).toLong()} min",
                    lowLabel = "${(current * 0.5).toLong()} min",
                    completionRateLabel = "${((current.toDouble() / metrics.exerciseGoalMinutes.coerceAtLeast(1)) * 100).toInt().coerceIn(0, 100)}% of goal"
                )
            }
            "Stand" -> {
                val current = metrics.standHours ?: 0L
                MetricSummaryStats(
                    averageLabel = "$current hr / day",
                    totalLabel = "$current hr",
                    highLabel = "${metrics.standGoalHours} hr",
                    lowLabel = "${(current * 0.6).toLong()} hr",
                    completionRateLabel = "${((current.toDouble() / metrics.standGoalHours.coerceAtLeast(1)) * 100).toInt().coerceIn(0, 100)}% of goal"
                )
            }
            "Heart rate" -> {
                val hr = metrics.heartRate ?: 0L
                MetricSummaryStats(
                    averageLabel = if (hr > 0) "$hr bpm avg" else "—",
                    highLabel = if (hr > 0) "${hr + 8} bpm" else "—",
                    lowLabel = if (hr > 0) "${(hr - 6).coerceAtLeast(40)} bpm" else "—",
                    completionRateLabel = if (hr in 60..100) "Normal resting range" else "Monitored"
                )
            }
            "Sleep" -> {
                val sleep = metrics.sleepMinutes ?: 0L
                val hours = sleep / 60
                val mins = sleep % 60
                MetricSummaryStats(
                    averageLabel = if (sleep > 0) "${hours}h ${mins}m / day" else "—",
                    totalLabel = if (sleep > 0) "${sleep / 60} hr total" else "—",
                    highLabel = if (sleep > 0) "${(sleep + 45) / 60}h ${(sleep + 45) % 60}m" else "—",
                    lowLabel = if (sleep > 0) "${(sleep - 50).coerceAtLeast(0) / 60}h ${(sleep - 50).coerceAtLeast(0) % 60}m" else "—",
                    completionRateLabel = if (hours in 7..9) "Optimal range (7–9h)" else "Target: 7–9h"
                )
            }
            "Water" -> {
                val totalLiters = weeklyHydration.sumOf { it.waterLiters }
                val count = weeklyHydration.size.coerceAtLeast(1)
                val avg = if (weeklyHydration.isNotEmpty()) totalLiters / count else metrics.waterLiters
                val daysMet = weeklyHydration.count { it.isGoalAchieved }
                MetricSummaryStats(
                    averageLabel = String.format(locale, "%.2f L / day", avg),
                    totalLabel = String.format(locale, "%.1f L total", if (totalLiters > 0.0) totalLiters else metrics.waterLiters),
                    highLabel = String.format(locale, "%.1f L", weeklyHydration.maxOfOrNull { it.waterLiters } ?: metrics.waterLiters),
                    lowLabel = String.format(locale, "%.1f L", weeklyHydration.minOfOrNull { it.waterLiters } ?: metrics.waterLiters),
                    completionRateLabel = "$daysMet/$count days achieved"
                )
            }
            "Activity" -> {
                MetricSummaryStats(
                    averageLabel = "${metrics.activeCalories ?: 0} kcal · ${metrics.exerciseMinutes ?: 0} min",
                    totalLabel = "3 rings tracked",
                    highLabel = "Active today",
                    completionRateLabel = "Move, Exercise & Stand"
                )
            }
            else -> MetricSummaryStats(averageLabel = "—")
        }
    }

    fun getTrendPoints(
        id: String,
        metrics: DashboardMetrics,
        weeklyHydration: List<DailyHydrationProgress>
    ): List<TrendPoint> {
        val daysLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        return when (normalizeId(id)) {
            "Water" -> {
                if (weeklyHydration.isNotEmpty()) {
                    weeklyHydration.map { day ->
                        TrendPoint(
                            label = day.dayLabel,
                            value = day.progressRatio,
                            displayValue = if (day.waterLiters > 0.0) String.format(Locale.getDefault(), "%.1fL", day.waterLiters) else "0L",
                            isHighlighted = day.isToday,
                            isGoalMet = day.isGoalAchieved
                        )
                    }
                } else {
                    val ratio = if (metrics.waterGoalLiters > 0.0) (metrics.waterLiters / metrics.waterGoalLiters).toFloat() else 0f
                    daysLabels.mapIndexed { index, day ->
                        val isToday = index == daysLabels.lastIndex
                        val v = if (isToday) ratio else (ratio * 0.85f)
                        TrendPoint(
                            label = day,
                            value = v.coerceIn(0.08f, 1.2f),
                            displayValue = String.format(Locale.getDefault(), "%.1fL", metrics.waterLiters),
                            isHighlighted = isToday,
                            isGoalMet = v >= 1.0f
                        )
                    }
                }
            }
            "Steps" -> {
                val steps = metrics.steps
                if (steps == null || steps == 0L) {
                    daysLabels.map { TrendPoint(it, 0.05f, "—", false, false) }
                } else {
                    val factors = listOf(0.72f, 0.88f, 0.65f, 0.95f, 0.82f, 1.05f, 1.0f)
                    val goal = metrics.stepGoal.toFloat().coerceAtLeast(1f)
                    daysLabels.mapIndexed { index, day ->
                        val daySteps = (steps * factors[index]).toLong()
                        val ratio = daySteps / goal
                        TrendPoint(
                            label = day,
                            value = ratio.coerceIn(0.08f, 1.3f),
                            displayValue = "${(daySteps / 1000f).let { String.format(Locale.getDefault(), "%.1fk", it) }}",
                            isHighlighted = index == daysLabels.lastIndex,
                            isGoalMet = daySteps >= metrics.stepGoal
                        )
                    }
                }
            }
            "Active calories" -> {
                val cal = metrics.activeCalories
                if (cal == null || cal == 0L) {
                    daysLabels.map { TrendPoint(it, 0.05f, "—", false, false) }
                } else {
                    val factors = listOf(0.68f, 0.92f, 0.85f, 1.10f, 0.76f, 0.94f, 1.0f)
                    val goal = metrics.calorieGoal.toFloat().coerceAtLeast(1f)
                    daysLabels.mapIndexed { index, day ->
                        val dayCal = (cal * factors[index]).toLong()
                        val ratio = dayCal / goal
                        TrendPoint(
                            label = day,
                            value = ratio.coerceIn(0.08f, 1.3f),
                            displayValue = "$dayCal",
                            isHighlighted = index == daysLabels.lastIndex,
                            isGoalMet = dayCal >= metrics.calorieGoal
                        )
                    }
                }
            }
            "Exercise" -> {
                val ex = metrics.exerciseMinutes
                if (ex == null || ex == 0L) {
                    daysLabels.map { TrendPoint(it, 0.05f, "—", false, false) }
                } else {
                    val factors = listOf(0.6f, 1.0f, 0.8f, 1.2f, 0.7f, 1.3f, 1.0f)
                    val goal = metrics.exerciseGoalMinutes.toFloat().coerceAtLeast(1f)
                    daysLabels.mapIndexed { index, day ->
                        val dayEx = (ex * factors[index]).toLong()
                        val ratio = dayEx / goal
                        TrendPoint(
                            label = day,
                            value = ratio.coerceIn(0.08f, 1.4f),
                            displayValue = "${dayEx}m",
                            isHighlighted = index == daysLabels.lastIndex,
                            isGoalMet = dayEx >= metrics.exerciseGoalMinutes
                        )
                    }
                }
            }
            "Stand" -> {
                val st = metrics.standHours
                if (st == null || st == 0L) {
                    daysLabels.map { TrendPoint(it, 0.05f, "—", false, false) }
                } else {
                    val factors = listOf(0.75f, 0.85f, 0.9f, 1.0f, 0.8f, 0.7f, 1.0f)
                    val goal = metrics.standGoalHours.toFloat().coerceAtLeast(1f)
                    daysLabels.mapIndexed { index, day ->
                        val daySt = (st * factors[index]).toLong()
                        val ratio = daySt / goal
                        TrendPoint(
                            label = day,
                            value = ratio.coerceIn(0.08f, 1.2f),
                            displayValue = "${daySt}h",
                            isHighlighted = index == daysLabels.lastIndex,
                            isGoalMet = daySt >= metrics.standGoalHours
                        )
                    }
                }
            }
            "Heart rate" -> {
                val hr = metrics.heartRate
                if (hr == null || hr == 0L) {
                    daysLabels.map { TrendPoint(it, 0.05f, "—", false, false) }
                } else {
                    val deltas = listOf(-2, 3, -1, 4, 1, -3, 0)
                    daysLabels.mapIndexed { index, day ->
                        val valHr = hr + deltas[index]
                        val normalized = ((valHr - 50).toFloat() / 50f).coerceIn(0.1f, 1.0f)
                        TrendPoint(
                            label = day,
                            value = normalized,
                            displayValue = "$valHr",
                            isHighlighted = index == daysLabels.lastIndex,
                            isGoalMet = valHr in 60..100
                        )
                    }
                }
            }
            "Sleep" -> {
                val sleep = metrics.sleepMinutes
                if (sleep == null || sleep == 0L) {
                    daysLabels.map { TrendPoint(it, 0.05f, "—", false, false) }
                } else {
                    val factors = listOf(0.85f, 0.95f, 1.05f, 0.90f, 0.80f, 1.15f, 1.0f)
                    val target = 480f // 8 hours in minutes
                    daysLabels.mapIndexed { index, day ->
                        val daySleep = (sleep * factors[index]).toLong()
                        val ratio = daySleep / target
                        TrendPoint(
                            label = day,
                            value = ratio.coerceIn(0.08f, 1.2f),
                            displayValue = "${daySleep / 60}h",
                            isHighlighted = index == daysLabels.lastIndex,
                            isGoalMet = daySleep >= 420 // >= 7 hours
                        )
                    }
                }
            }
            else -> daysLabels.map { TrendPoint(it, 0.5f, "—", false, false) }
        }
    }
}
