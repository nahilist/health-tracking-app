package com.pulsehealth.app.data

enum class TimeRange(val label: String, val days: Long) {
    TODAY("Today", 1), WEEK("Week", 7), MONTH("Month", 30)
}

data class DashboardMetrics(
    val steps: Long = 8_432,
    val stepGoal: Long = 10_000,
    val heartRate: Long = 62,
    val sleepMinutes: Long = 468,
    val waterLiters: Double = 1.5,
    val waterGoalLiters: Double = 2.0,
    val activeCalories: Long = 360,
    val calorieGoal: Long = 500,
    val exerciseMinutes: Long = 24,
    val exerciseGoalMinutes: Long = 30,
    val standHours: Long = 8,
    val standGoalHours: Long = 12,
    val source: DataSource = DataSource.DEMO
)

enum class DataSource { DEMO, HEALTH_CONNECT }

data class DashboardUiState(
    val metrics: DashboardMetrics = DashboardMetrics(),
    val selectedRange: TimeRange = TimeRange.TODAY,
    val healthConnectAvailable: Boolean = false,
    val hasHealthPermissions: Boolean = false,
    val isSyncing: Boolean = false,
    val hiddenCards: Set<String> = emptySet(),
    val message: String? = null
)
