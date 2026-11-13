package com.pulsehealth.app.data

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class TimeRange(val label: String, val days: Long) {
    TODAY("Today", 1), WEEK("Week", 7), MONTH("Month", 30)
}

enum class DataSource(val label: String) {
    NONE("No data"),
    LOCAL("Local records"),
    HEALTH_CONNECT("Health Connect"),
    DEMO("Demo sample")
}

data class DashboardMetrics(
    val steps: Long? = null,
    val stepGoal: Long = 10_000,
    val heartRate: Long? = null,
    val sleepMinutes: Long? = null,
    val waterLiters: Double = 0.0,
    val waterGoalLiters: Double = 2.0,
    val activeCalories: Long? = null,
    val calorieGoal: Long = 500,
    val exerciseMinutes: Long? = null,
    val exerciseGoalMinutes: Long = 30,
    val standHours: Long? = null,
    val standGoalHours: Long = 12,
    val source: DataSource = DataSource.NONE,
    val lastUpdatedEpochMillis: Long? = null
) {
    val hasAnyData: Boolean
        get() = steps != null || heartRate != null || sleepMinutes != null ||
            waterLiters > 0.0 || activeCalories != null || exerciseMinutes != null ||
            standHours != null

    companion object {
        fun demo(clockMillis: Long = System.currentTimeMillis()): DashboardMetrics = DashboardMetrics(
            steps = 8_432,
            stepGoal = 10_000,
            heartRate = 62,
            sleepMinutes = 468,
            waterLiters = 1.5,
            waterGoalLiters = 2.0,
            activeCalories = 360,
            calorieGoal = 500,
            exerciseMinutes = 24,
            exerciseGoalMinutes = 30,
            standHours = 8,
            standGoalHours = 12,
            source = DataSource.DEMO,
            lastUpdatedEpochMillis = clockMillis
        )
    }
}

data class DashboardUiState(
    val metrics: DashboardMetrics = DashboardMetrics(),
    val selectedRange: TimeRange = TimeRange.TODAY,
    val healthConnectAvailable: Boolean = false,
    val hasHealthPermissions: Boolean = false,
    val isSyncing: Boolean = false,
    val hiddenCards: Set<String> = emptySet(),
    val message: String? = null,
    val isDemoMode: Boolean = false,
    val lastSyncedEpochMillis: Long? = null
) {
    val freshnessLabel: String?
        get() {
            val millis = lastSyncedEpochMillis ?: metrics.lastUpdatedEpochMillis ?: return null
            return formatFreshness(millis)
        }

    companion object {
        fun formatFreshness(epochMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
            val diffMillis = (nowMillis - epochMillis).coerceAtLeast(0)
            val minutes = diffMillis / (1000 * 60)
            val hours = minutes / 60
            return when {
                minutes < 1 -> "Updated just now"
                minutes < 60 -> "Updated ${minutes}m ago"
                hours < 24 -> "Updated ${hours}h ago"
                else -> {
                    val date = Instant.ofEpochMilli(epochMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    "Updated ${date.format(DateTimeFormatter.ofPattern("MMM d"))}"
                }
            }
        }
    }
}
