package com.pulsehealth.app.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.pulsehealth.app.data.HydrationRepository
import com.pulsehealth.app.data.SleepRepository
import com.pulsehealth.app.data.VitalsRepository
import com.pulsehealth.app.data.WorkoutRepository
import com.pulsehealth.app.data.analytics.InsightsRepository
import com.pulsehealth.app.data.local.DailyHealthSummaryDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

interface HealthReportService {
    suspend fun generateReport(
        format: ReportFormat,
        range: ReportTimeRange,
        config: ReportSectionConfig = ReportSectionConfig(),
        doctorNotes: String? = null,
        patientName: String? = null
    ): Uri

    fun createShareIntent(uri: Uri, format: ReportFormat): Intent
}

class DefaultHealthReportService(
    private val context: Context,
    private val dailySummaryDao: DailyHealthSummaryDao,
    private val vitalsRepository: VitalsRepository,
    private val workoutRepository: WorkoutRepository,
    private val sleepRepository: SleepRepository,
    private val hydrationRepository: HydrationRepository,
    private val insightsRepository: InsightsRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : HealthReportService {

    override suspend fun generateReport(
        format: ReportFormat,
        range: ReportTimeRange,
        config: ReportSectionConfig,
        doctorNotes: String?,
        patientName: String?
    ): Uri = withContext(ioDispatcher) {
        val now = LocalDate.now(clock)
        val endEpochDay = now.toEpochDay()
        val startEpochDay = if (range == ReportTimeRange.ALL_TIME) {
            0L
        } else {
            now.minusDays(range.days.toLong()).toEpochDay()
        }

        val startEpochMillis = Instant.ofEpochMilli(clock.millis())
            .atZone(zoneId)
            .minusDays(range.days.toLong())
            .toInstant()
            .toEpochMilli()
        val endEpochMillis = clock.millis()

        // Gather relevant data across repositories
        val summaries = dailySummaryDao.getAllSummaries()
            .filter { it.epochDay in startEpochDay..endEpochDay }

        val vitals = if (config.includeVitals) {
            vitalsRepository.observeRecentVitals(100).first()
                .filter { range == ReportTimeRange.ALL_TIME || it.recordedAtEpochMillis >= startEpochMillis }
        } else emptyList()

        val workouts = if (config.includeWorkouts) {
            workoutRepository.observeAllWorkouts().first()
                .filter { range == ReportTimeRange.ALL_TIME || it.startTimeEpochMillis >= startEpochMillis }
        } else emptyList()

        val sleepSessions = if (config.includeSleep) {
            sleepRepository.observeAllSleepWithStages().first()
                .filter { range == ReportTimeRange.ALL_TIME || it.session.startTimeEpochMillis >= startEpochMillis }
        } else emptyList()

        val hydration = if (config.includeHydration) {
            hydrationRepository.getAllEntries()
                .filter { range == ReportTimeRange.ALL_TIME || it.recordedAtEpochMillis >= startEpochMillis }
        } else emptyList()

        val insights = if (config.includeInsights) {
            insightsRepository.observeInsights().first()
        } else emptyList()

        val reportData = HealthReportData(
            generatedAtEpochMillis = clock.millis(),
            timeRange = range,
            config = config,
            patientName = patientName,
            doctorNotes = doctorNotes,
            summaries = summaries,
            vitals = vitals,
            workouts = workouts,
            sleepSessions = sleepSessions,
            hydrationEntries = hydration,
            insights = insights
        )

        // Write to app cache/reports
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val timeStampStr = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            .format(Instant.ofEpochMilli(clock.millis()).atZone(zoneId))
        val fileName = "PulseHealth_Report_${range.name}_${timeStampStr}.${format.extension}"
        val reportFile = File(reportsDir, fileName)

        FileOutputStream(reportFile).use { out ->
            when (format) {
                ReportFormat.PDF -> ClinicalPdfGenerator.generate(reportData, out, zoneId)
                ReportFormat.CSV -> HealthCsvGenerator.generate(reportData, out, zoneId)
            }
        }

        try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                reportFile
            )
        } catch (_: IllegalArgumentException) {
            Uri.parse("content://${context.packageName}.fileprovider/health_reports/${reportFile.name}")
        }
    }

    override fun createShareIntent(uri: Uri, format: ReportFormat): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Pulse Health Clinical Wellness Report")
            putExtra(Intent.EXTRA_TEXT, "Attached is the confidential Pulse Health wellness report generated from on-device biometric records.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(sendIntent, "Share Health Report")
    }
}

class NoOpHealthReportService : HealthReportService {
    override suspend fun generateReport(
        format: ReportFormat,
        range: ReportTimeRange,
        config: ReportSectionConfig,
        doctorNotes: String?,
        patientName: String?
    ): Uri = Uri.EMPTY

    override fun createShareIntent(uri: Uri, format: ReportFormat): Intent = Intent()
}
