package com.pulsehealth.app.data.analytics

enum class InsightSeverity {
    POSITIVE,
    INFO,
    ATTENTION
}

enum class InsightCategory {
    CARDIO,
    SLEEP,
    ACTIVITY,
    HYDRATION,
    RECOVERY
}

enum class InsightType {
    RESTING_HEART_RATE_SPIKE,
    RESTING_HEART_RATE_OPTIMAL,
    BLOOD_PRESSURE_ELEVATED_TREND,
    BLOOD_PRESSURE_OPTIMAL,
    SLEEP_DEFICIT,
    SLEEP_RESTORATIVE_HIGH,
    ACTIVITY_SURGE,
    ACTIVITY_DROP,
    HYDRATION_STREAK,
    HYDRATION_DEFICIT
}

data class HealthInsight(
    val id: String,
    val type: InsightType,
    val severity: InsightSeverity,
    val category: InsightCategory,
    val title: String,
    val description: String,
    val recommendation: String,
    val metricType: String,
    val currentValueText: String,
    val baselineValueText: String,
    val deltaPercentage: Int? = null,
    val detectedAtEpochMillis: Long = System.currentTimeMillis()
)
