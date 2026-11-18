package com.pulsehealth.app.data.export

import com.pulsehealth.app.data.analytics.HealthInsight
import com.pulsehealth.app.data.local.DailyHealthSummaryEntity
import com.pulsehealth.app.data.local.HydrationEntryEntity
import com.pulsehealth.app.data.local.SleepSessionWithStages
import com.pulsehealth.app.data.local.VitalRecordEntity
import com.pulsehealth.app.data.local.WorkoutSessionEntity

enum class ReportTimeRange(val days: Int, val displayLabel: String) {
    LAST_7_DAYS(7, "Last 7 Days"),
    LAST_30_DAYS(30, "Last 30 Days"),
    LAST_90_DAYS(90, "Last 90 Days"),
    ALL_TIME(365, "All Available Records")
}

enum class ReportFormat(val displayLabel: String, val extension: String, val mimeType: String) {
    PDF("Clinical PDF Document (.pdf)", "pdf", "application/pdf"),
    CSV("Spreadsheet Bundle (.csv)", "csv", "text/csv")
}

data class ReportSectionConfig(
    val includeVitals: Boolean = true,
    val includeWorkouts: Boolean = true,
    val includeSleep: Boolean = true,
    val includeHydration: Boolean = true,
    val includeInsights: Boolean = true
)

data class HealthReportData(
    val generatedAtEpochMillis: Long = System.currentTimeMillis(),
    val timeRange: ReportTimeRange = ReportTimeRange.LAST_30_DAYS,
    val config: ReportSectionConfig = ReportSectionConfig(),
    val patientName: String? = null,
    val doctorNotes: String? = null,
    val summaries: List<DailyHealthSummaryEntity> = emptyList(),
    val vitals: List<VitalRecordEntity> = emptyList(),
    val workouts: List<WorkoutSessionEntity> = emptyList(),
    val sleepSessions: List<SleepSessionWithStages> = emptyList(),
    val hydrationEntries: List<HydrationEntryEntity> = emptyList(),
    val insights: List<HealthInsight> = emptyList()
)
