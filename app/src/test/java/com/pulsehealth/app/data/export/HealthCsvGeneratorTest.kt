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
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.time.ZoneId

class HealthCsvGeneratorTest {

    @Test
    fun generate_includesAllConfiguredSectionsAndHeaders() {
        val vitals = listOf(
            VitalRecordEntity(
                id = "v-1",
                vitalType = "BLOOD_PRESSURE",
                systolic = 125,
                diastolic = 82,
                heartRateBpm = 68,
                recordedAtEpochMillis = 1000L,
                measurementContext = "RESTING",
                notes = "Felt calm"
            )
        )

        val workouts = listOf(
            WorkoutSessionEntity(
                id = "w-1",
                workoutType = "CYCLING",
                title = "Afternoon Ride",
                startTimeEpochMillis = 2000L,
                endTimeEpochMillis = 2000L + 45 * 60_000L,
                durationMinutes = 45,
                activeCalories = 350,
                distanceMeters = 15000.0,
                source = "manual",
                notes = null,
                syncState = "synced"
            )
        )

        val session = SleepSessionEntity(
            id = "s-1",
            title = "Sleep",
            startTimeEpochMillis = 3000L,
            endTimeEpochMillis = 3000L + 480 * 60_000L,
            durationMinutes = 480,
            deepDurationMinutes = 120,
            lightDurationMinutes = 240,
            remDurationMinutes = 90,
            awakeDurationMinutes = 30,
            sleepEfficiencyPercentage = 92,
            source = "manual",
            notes = null,
            syncState = "synced"
        )
        val stages = listOf(
            SleepStageEntity("st-1", "s-1", "DEEP", 3000L, 3000L + 120 * 60_000L, 120)
        )

        val hydration = listOf(
            HydrationEntryEntity("h-1", 500, 4000L, "manual", "synced")
        )

        val summaries = listOf(
            DailyHealthSummaryEntity(
                epochDay = 105,
                steps = 9500,
                restingHeartRate = 60,
                sleepMinutes = 480,
                activeCalories = 420,
                exerciseMinutes = 45,
                standHours = 11,
                source = "local",
                updatedAtEpochMillis = 5000L
            )
        )

        val insights = listOf(
            HealthInsight(
                id = "ins-1",
                type = InsightType.HYDRATION_STREAK,
                severity = InsightSeverity.POSITIVE,
                category = InsightCategory.HYDRATION,
                title = "Hydration Streak",
                description = "Met water goal 3 days in a row.",
                recommendation = "Keep it up!",
                metricType = "Hydration",
                currentValueText = "3 days",
                baselineValueText = "2.0L"
            )
        )

        val data = HealthReportData(
            generatedAtEpochMillis = 1000L,
            timeRange = ReportTimeRange.LAST_7_DAYS,
            doctorNotes = "Testing notes with \"quotes\"",
            summaries = summaries,
            vitals = vitals,
            workouts = workouts,
            sleepSessions = listOf(SleepSessionWithStages(session, stages)),
            hydrationEntries = hydration,
            insights = insights
        )

        val out = ByteArrayOutputStream()
        HealthCsvGenerator.generate(data, out, ZoneId.of("UTC"))
        val csv = out.toString("UTF-8")

        assertTrue(csv.contains("# PULSE HEALTH CLINICAL & WELLNESS EXPORT"))
        assertTrue(csv.contains("Testing notes with \"\"quotes\"\""))
        assertTrue(csv.contains("# --- VITALS & BLOOD PRESSURE ---"))
        assertTrue(csv.contains("BLOOD_PRESSURE,125,82,68,\"RESTING\",\"Felt calm\""))
        assertTrue(csv.contains("# --- WORKOUT & EXERCISE SESSIONS ---"))
        assertTrue(csv.contains("CYCLING,45,350,15.00"))
        assertTrue(csv.contains("# --- SLEEP SESSIONS & EFFICIENCY ---"))
        assertTrue(csv.contains("480,120,240,90,30,92"))
        assertTrue(csv.contains("# --- HYDRATION ENTRIES ---"))
        assertTrue(csv.contains("500,0.50"))
        assertTrue(csv.contains("# --- DAILY HEALTH AGGREGATES ---"))
        assertTrue(csv.contains("105,9500,60,480,420,45,11"))
        assertTrue(csv.contains("# --- STATISTICAL ANOMALY & TREND INSIGHTS ---"))
        assertTrue(csv.contains("Hydration Streak"))
    }
}
