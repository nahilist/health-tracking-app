package com.pulsehealth.app.data.export

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object HealthCsvGenerator {

    fun generate(data: HealthReportData, outputStream: OutputStream, zoneId: ZoneId = ZoneId.systemDefault()) {
        val writer = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)

        writer.write("# PULSE HEALTH CLINICAL & WELLNESS EXPORT\n")
        writer.write("# GeneratedAt,${Instant.ofEpochMilli(data.generatedAtEpochMillis).atZone(zoneId)}\n")
        writer.write("# Scope,${data.timeRange.displayLabel}\n")
        if (!data.doctorNotes.isNullOrBlank()) {
            writer.write("# Notes,\"${escapeCsv(data.doctorNotes)}\"\n")
        }
        writer.write("\n")

        // 1. Vitals Section
        if (data.config.includeVitals && data.vitals.isNotEmpty()) {
            writer.write("# --- VITALS & BLOOD PRESSURE ---\n")
            writer.write("ID,Date,Time,VitalType,Systolic_mmHg,Diastolic_mmHg,HeartRate_bpm,Context,Notes,Source\n")
            data.vitals.sortedByDescending { it.recordedAtEpochMillis }.forEach { v ->
                val zdt = Instant.ofEpochMilli(v.recordedAtEpochMillis).atZone(zoneId)
                writer.write("${v.id},${zdt.toLocalDate().format(dateFormatter)},${zdt.toLocalTime().format(timeFormatter)},${v.vitalType},${v.systolic ?: ""},${v.diastolic ?: ""},${v.heartRateBpm ?: ""},\"${escapeCsv(v.measurementContext)}\",\"${escapeCsv(v.notes ?: "")}\",\"${v.source}\"\n")
            }
            writer.write("\n")
        }

        // 2. Workouts Section
        if (data.config.includeWorkouts && data.workouts.isNotEmpty()) {
            writer.write("# --- WORKOUT & EXERCISE SESSIONS ---\n")
            writer.write("ID,Date,Time,WorkoutType,Duration_min,ActiveCalories_kcal,Distance_km,Source,Notes\n")
            data.workouts.sortedByDescending { it.startTimeEpochMillis }.forEach { w ->
                val zdt = Instant.ofEpochMilli(w.startTimeEpochMillis).atZone(zoneId)
                writer.write("${w.id},${zdt.toLocalDate().format(dateFormatter)},${zdt.toLocalTime().format(timeFormatter)},${w.workoutType},${w.durationMinutes},${w.activeCalories ?: ""},${w.distanceMeters?.let { String.format(Locale.US, "%.2f", it / 1000.0) } ?: ""},\"${w.source}\",\"${escapeCsv(w.notes ?: "")}\"\n")
            }
            writer.write("\n")
        }

        // 3. Sleep Sessions
        if (data.config.includeSleep && data.sleepSessions.isNotEmpty()) {
            writer.write("# --- SLEEP SESSIONS & EFFICIENCY ---\n")
            writer.write("ID,StartDate,StartTime,EndDate,EndTime,Duration_min,Deep_min,Light_min,REM_min,Awake_min,Efficiency_pct,Source\n")
            data.sleepSessions.sortedByDescending { it.session.startTimeEpochMillis }.forEach { s ->
                val startZdt = Instant.ofEpochMilli(s.session.startTimeEpochMillis).atZone(zoneId)
                val endZdt = Instant.ofEpochMilli(s.session.endTimeEpochMillis).atZone(zoneId)
                writer.write("${s.session.id},${startZdt.toLocalDate().format(dateFormatter)},${startZdt.toLocalTime().format(timeFormatter)},${endZdt.toLocalDate().format(dateFormatter)},${endZdt.toLocalTime().format(timeFormatter)},${s.session.durationMinutes},${s.session.deepDurationMinutes},${s.session.lightDurationMinutes},${s.session.remDurationMinutes},${s.session.awakeDurationMinutes},${s.session.sleepEfficiencyPercentage},\"${s.session.source}\"\n")
            }
            writer.write("\n")
        }

        // 4. Hydration Entries
        if (data.config.includeHydration && data.hydrationEntries.isNotEmpty()) {
            writer.write("# --- HYDRATION ENTRIES ---\n")
            writer.write("ID,Date,Time,Amount_mL,Amount_L,Source,SyncState\n")
            data.hydrationEntries.sortedByDescending { it.recordedAtEpochMillis }.forEach { h ->
                val zdt = Instant.ofEpochMilli(h.recordedAtEpochMillis).atZone(zoneId)
                writer.write("${h.id},${zdt.toLocalDate().format(dateFormatter)},${zdt.toLocalTime().format(timeFormatter)},${h.amountMilliliters},${String.format(Locale.US, "%.2f", h.amountMilliliters / 1000.0)},\"${h.source}\",\"${h.syncState}\"\n")
            }
            writer.write("\n")
        }

        // 5. Daily Summaries
        if (data.summaries.isNotEmpty()) {
            writer.write("# --- DAILY HEALTH AGGREGATES ---\n")
            writer.write("EpochDay,Steps,RestingHeartRate_bpm,Sleep_min,ActiveCalories_kcal,Exercise_min,Stand_hours,Source\n")
            data.summaries.sortedByDescending { it.epochDay }.forEach { ds ->
                writer.write("${ds.epochDay},${ds.steps},${ds.restingHeartRate ?: ""},${ds.sleepMinutes},${ds.activeCalories},${ds.exerciseMinutes},${ds.standHours},\"${ds.source}\"\n")
            }
            writer.write("\n")
        }

        // 6. Insights
        if (data.config.includeInsights && data.insights.isNotEmpty()) {
            writer.write("# --- STATISTICAL ANOMALY & TREND INSIGHTS ---\n")
            writer.write("ID,Category,Severity,Title,Description,Recommendation,CurrentValue,BaselineValue\n")
            data.insights.forEach { ins ->
                writer.write("${ins.id},${ins.category.name},${ins.severity.name},\"${escapeCsv(ins.title)}\",\"${escapeCsv(ins.description)}\",\"${escapeCsv(ins.recommendation)}\",\"${escapeCsv(ins.currentValueText)}\",\"${escapeCsv(ins.baselineValueText)}\"\n")
            }
        }

        writer.flush()
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }
}
