package com.pulsehealth.app.data.export

import com.pulsehealth.app.data.analytics.HealthInsight
import com.pulsehealth.app.data.analytics.InsightCategory
import com.pulsehealth.app.data.analytics.InsightSeverity
import com.pulsehealth.app.data.analytics.InsightType
import com.pulsehealth.app.data.local.DailyHealthSummaryEntity
import com.pulsehealth.app.data.local.HydrationEntryEntity
import com.pulsehealth.app.data.local.SleepSessionEntity
import com.pulsehealth.app.data.local.SleepSessionWithStages
import com.pulsehealth.app.data.local.SleepStageEntity
import com.pulsehealth.app.data.local.VitalRecordEntity
import com.pulsehealth.app.data.local.WorkoutSessionEntity
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ClinicalPdfGeneratorTest {

    @Test
    fun generate_producesValidNonEmptyPdfDocument_withMinimalData() {
        val data = HealthReportData(
            generatedAtEpochMillis = 1000L,
            timeRange = ReportTimeRange.LAST_7_DAYS
        )

        val out = ByteArrayOutputStream()
        ClinicalPdfGenerator.generate(data, out, ZoneId.of("UTC"))

        val bytes = out.toByteArray()
        assertTrue("PDF should have bytes", bytes.isNotEmpty())
        // PDF header magic bytes: %PDF-
        val header = String(bytes.take(5).toByteArray())
        assertTrue("Expected %PDF- magic bytes but got: $header", header.startsWith("%PDF"))
    }

    @Test
    fun generate_handlesCompleteMultiSectionData_withPagination() {
        val vitals = (1..15).map { i ->
            VitalRecordEntity(
                id = "vital-$i",
                vitalType = "BLOOD_PRESSURE",
                systolic = 120 + i,
                diastolic = 80 + (i % 5),
                recordedAtEpochMillis = 1000L + (i * 3600_000L),
                measurementContext = "RESTING"
            )
        }

        val workouts = (1..6).map { i ->
            WorkoutSessionEntity(
                id = "workout-$i",
                workoutType = "RUNNING",
                title = "Morning Run",
                startTimeEpochMillis = 1000L + (i * 3600_000L),
                endTimeEpochMillis = 1000L + (i * 3600_000L) + (30 + i * 5) * 60_000L,
                durationMinutes = (30 + i * 5).toLong(),
                activeCalories = 300 + i * 20,
                distanceMeters = 5000.0,
                source = "manual",
                notes = null,
                syncState = "synced"
            )
        }

        val session = SleepSessionEntity(
            id = "sess-1",
            title = "Night Sleep",
            startTimeEpochMillis = 0L,
            endTimeEpochMillis = 480 * 60_000L,
            durationMinutes = 480,
            deepDurationMinutes = 120,
            lightDurationMinutes = 240,
            remDurationMinutes = 90,
            awakeDurationMinutes = 30,
            sleepEfficiencyPercentage = 94,
            source = "manual",
            notes = null,
            syncState = "synced"
        )
        val stages = listOf(
            SleepStageEntity("st-1", "sess-1", "DEEP", 0L, 120 * 60_000L, 120),
            SleepStageEntity("st-2", "sess-1", "LIGHT", 120 * 60_000L, 360 * 60_000L, 240)
        )

        val hydration = listOf(
            HydrationEntryEntity("hyd-1", 500, 1000L, "manual", "synced"),
            HydrationEntryEntity("hyd-2", 750, 2000L, "manual", "synced")
        )

        val summaries = listOf(
            DailyHealthSummaryEntity(
                epochDay = 100,
                steps = 11000,
                restingHeartRate = 62,
                sleepMinutes = 480,
                activeCalories = 500,
                exerciseMinutes = 45,
                standHours = 12,
                source = "local",
                updatedAtEpochMillis = 1000L
            )
        )

        val insights = listOf(
            HealthInsight(
                id = "ins-1",
                type = InsightType.RESTING_HEART_RATE_OPTIMAL,
                severity = InsightSeverity.POSITIVE,
                category = InsightCategory.CARDIO,
                title = "Optimal Resting Recovery",
                description = "Resting heart rate in optimal range.",
                recommendation = "Keep up consistent sleep habits.",
                metricType = "Resting heart rate",
                currentValueText = "62 bpm",
                baselineValueText = "60 bpm"
            )
        )

        val data = HealthReportData(
            generatedAtEpochMillis = 1000L,
            timeRange = ReportTimeRange.LAST_30_DAYS,
            doctorNotes = "Reviewing blood pressure trends prior to scheduled annual exam.",
            patientName = "Alex",
            summaries = summaries,
            vitals = vitals,
            workouts = workouts,
            sleepSessions = listOf(SleepSessionWithStages(session, stages)),
            hydrationEntries = hydration,
            insights = insights
        )

        val bytes = ClinicalPdfGenerator.generateByteArray(data, ZoneId.of("UTC"))
        assertNotNull(bytes)
        assertTrue(bytes.size > 1000)
    }
}
