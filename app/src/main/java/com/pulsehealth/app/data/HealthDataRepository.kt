package com.pulsehealth.app.data

import com.pulsehealth.app.data.local.DailyHealthSummaryDao
import com.pulsehealth.app.data.local.DailyHealthSummaryEntity
import com.pulsehealth.app.data.local.HydrationDao
import com.pulsehealth.app.data.local.HydrationEntryEntity
import com.pulsehealth.app.data.local.WorkoutDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class HealthDataRepository(
    private val hydrationDao: HydrationDao,
    private val dailyHealthSummaryDao: DailyHealthSummaryDao? = null,
    private val workoutDao: WorkoutDao? = null,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : HydrationRepository, DailySummaryRepository {

    override val todayWaterLiters: Flow<Double>
        get() {
            val (start, end) = todayBounds()
            return hydrationDao.observeTotalMilliliters(start, end).map { it / 1_000.0 }
        }

    override val weeklyWaterLiters: Flow<Double>
        get() {
            val (start, end) = weekBounds()
            return hydrationDao.observeTotalMilliliters(start, end).map { it / 1_000.0 }
        }

    override fun observeTodayEntries(): Flow<List<HydrationEntryEntity>> {
        val (start, end) = todayBounds()
        return hydrationDao.observeEntries(start, end)
    }

    override fun observeRecentEntries(limit: Int, offset: Int): Flow<List<HydrationEntryEntity>> {
        require(limit > 0) { "Limit must be greater than 0" }
        require(offset >= 0) { "Offset must be greater than or equal to 0" }
        return hydrationDao.observeRecentEntries(limit, offset)
    }

    override fun observePastDaysEntries(days: Int): Flow<List<HydrationEntryEntity>> {
        require(days in 1..365) { "Days must be between 1 and 365" }
        val today = Instant.ofEpochMilli(clock.millis()).atZone(zoneId).toLocalDate()
        val start = today.minusDays((days - 1).toLong()).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return hydrationDao.observeEntries(start, end)
    }

    override suspend fun addWater(
        amountMilliliters: Int,
        recordedAtEpochMillis: Long?
    ) = withContext(ioDispatcher) {
        require(amountMilliliters in 1..5_000) { "Hydration amount must be between 1 and 5000 mL" }
        hydrationDao.upsert(
            HydrationEntryEntity(
                id = UUID.randomUUID().toString(),
                amountMilliliters = amountMilliliters,
                recordedAtEpochMillis = recordedAtEpochMillis ?: clock.millis(),
                source = "manual",
                syncState = "pending"
            )
        )
    }

    override suspend fun updateWater(
        id: String,
        amountMilliliters: Int
    ): Boolean = withContext(ioDispatcher) {
        require(amountMilliliters in 1..5_000) { "Hydration amount must be between 1 and 5000 mL" }
        hydrationDao.updateAmount(id, amountMilliliters) > 0
    }

    override suspend fun deleteWater(id: String): Boolean = withContext(ioDispatcher) {
        hydrationDao.deleteById(id) > 0
    }

    override suspend fun getAllEntries(): List<HydrationEntryEntity> = withContext(ioDispatcher) {
        hydrationDao.getAllEntries()
    }

    override suspend fun clearAllData() = withContext(ioDispatcher) {
        hydrationDao.deleteAll()
        dailyHealthSummaryDao?.deleteAll()
        workoutDao?.deleteAll()
        Unit
    }

    override suspend fun exportDataJson(): String = withContext(ioDispatcher) {
        val entries = hydrationDao.getAllEntries()
        val summaries = dailyHealthSummaryDao?.getAllSummaries() ?: emptyList()
        buildString {
            append("{\n")
            append("  \"app\": \"Pulse Health\",\n")
            append("  \"version\": \"1.0.0\",\n")
            append("  \"exportEpochMillis\": ${clock.millis()},\n")
            append("  \"hydrationEntriesCount\": ${entries.size},\n")
            append("  \"hydrationEntries\": [\n")
            entries.forEachIndexed { index, entry ->
                append("    {\n")
                append("      \"id\": \"${entry.id}\",\n")
                append("      \"amountMilliliters\": ${entry.amountMilliliters},\n")
                append("      \"recordedAtEpochMillis\": ${entry.recordedAtEpochMillis},\n")
                append("      \"source\": \"${entry.source}\",\n")
                append("      \"syncState\": \"${entry.syncState}\"\n")
                append("    }")
                if (index < entries.size - 1) append(",")
                append("\n")
            }
            append("  ],\n")
            append("  \"dailySummariesCount\": ${summaries.size},\n")
            append("  \"dailySummaries\": [\n")
            summaries.forEachIndexed { index, s ->
                append("    {\n")
                append("      \"epochDay\": ${s.epochDay},\n")
                append("      \"steps\": ${s.steps},\n")
                append("      \"restingHeartRate\": ${s.restingHeartRate},\n")
                append("      \"sleepMinutes\": ${s.sleepMinutes},\n")
                append("      \"activeCalories\": ${s.activeCalories},\n")
                append("      \"exerciseMinutes\": ${s.exerciseMinutes},\n")
                append("      \"standHours\": ${s.standHours},\n")
                append("      \"source\": \"${s.source}\",\n")
                append("      \"updatedAtEpochMillis\": ${s.updatedAtEpochMillis}\n")
                append("    }")
                if (index < summaries.size - 1) append(",")
                append("\n")
            }
            append("  ]\n")
            append("}")
        }
    }

    override suspend fun exportDataCsv(): String = withContext(ioDispatcher) {
        val entries = hydrationDao.getAllEntries()
        val summaries = dailyHealthSummaryDao?.getAllSummaries() ?: emptyList()
        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

        buildString {
            append("# Pulse Health Export (CSV)\n")
            append("# Generated: ${Instant.ofEpochMilli(clock.millis()).atZone(zoneId)}\n\n")

            append("# --- Daily Summaries ---\n")
            append("Date,EpochDay,Steps,RestingHeartRate_Bpm,Sleep_Minutes,ActiveCalories_kcal,Exercise_Minutes,Stand_Hours,Source\n")
            summaries.forEach { s ->
                val dateStr = LocalDate.ofEpochDay(s.epochDay).format(dateFormatter)
                append("$dateStr,${s.epochDay},${s.steps},${s.restingHeartRate ?: ""},${s.sleepMinutes},${s.activeCalories},${s.exerciseMinutes},${s.standHours},\"${s.source}\"\n")
            }

            append("\n# --- Hydration Entries ---\n")
            append("ID,Date,Time,Amount_mL,Amount_L,Source,SyncState\n")
            entries.forEach { e ->
                val zdt = Instant.ofEpochMilli(e.recordedAtEpochMillis).atZone(zoneId)
                val dateStr = zdt.toLocalDate().format(dateFormatter)
                val timeStr = zdt.toLocalTime().format(timeFormatter)
                val litersStr = String.format(Locale.US, "%.2f", e.amountMilliliters / 1000.0)
                append("${e.id},$dateStr,$timeStr,${e.amountMilliliters},$litersStr,\"${e.source}\",\"${e.syncState}\"\n")
            }
        }
    }

    override suspend fun exportClinicalSummary(): String = withContext(ioDispatcher) {
        val entries = hydrationDao.getAllEntries()
        val summaries = dailyHealthSummaryDao?.getAllSummaries() ?: emptyList()
        val now = Instant.ofEpochMilli(clock.millis()).atZone(zoneId)

        val totalEntries = entries.size
        val totalWaterLitres = entries.sumOf { it.amountMilliliters } / 1000.0
        val avgSteps = if (summaries.isNotEmpty()) summaries.map { it.steps }.average().toLong() else 0L
        val maxSteps = if (summaries.isNotEmpty()) summaries.maxOf { it.steps } else 0L
        val hrList = summaries.mapNotNull { it.restingHeartRate }.filter { it > 0 }
        val avgHr = if (hrList.isNotEmpty()) hrList.average().toInt() else null
        val minHr = if (hrList.isNotEmpty()) hrList.minOrNull() else null
        val maxHr = if (hrList.isNotEmpty()) hrList.maxOrNull() else null
        val avgSleepMin = if (summaries.isNotEmpty()) summaries.map { it.sleepMinutes }.average().toLong() else 0L
        val avgCalories = if (summaries.isNotEmpty()) summaries.map { it.activeCalories }.average().toLong() else 0L
        val avgExerciseMin = if (summaries.isNotEmpty()) summaries.map { it.exerciseMinutes }.average().toLong() else 0L

        buildString {
            append("====================================================\n")
            append("              PULSE HEALTH WELLNESS REPORT          \n")
            append("====================================================\n")
            append("Generated on: ${now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))} (${zoneId.id})\n")
            append("Application: Pulse Health v1.0.0 (Local-First Android)\n\n")
            append("CLINICAL & CONSULTATION DISCLAIMER:\n")
            append("This document contains self-tracked wellness data collected via\n")
            append("on-device sensors and manual entries. It is intended solely for\n")
            append("personal tracking and consultation with qualified medical professionals.\n")
            append("It does NOT constitute medical diagnosis, treatment, or clinical advice.\n\n")

            append("1. ACTIVITY & CARDIOVASCULAR SUMMARY\n")
            append("----------------------------------------------------\n")
            append("Days Recorded:               ${summaries.size} day(s)\n")
            append("Average Daily Steps:         ${String.format(Locale.US, "%,d", avgSteps)} steps\n")
            append("Peak Daily Steps:            ${String.format(Locale.US, "%,d", maxSteps)} steps\n")
            if (avgHr != null) {
                append("Resting Heart Rate Average:  $avgHr bpm (Range: $minHr - $maxHr bpm)\n")
            } else {
                append("Resting Heart Rate Average:  No readings recorded\n")
            }
            append("Average Active Calories:     $avgCalories kcal/day\n")
            append("Average Exercise Duration:   $avgExerciseMin minutes/day\n\n")

            append("2. SLEEP REST PROFILE\n")
            append("----------------------------------------------------\n")
            val sleepHours = avgSleepMin / 60
            val sleepRemMinutes = avgSleepMin % 60
            append("Average Nightly Sleep:       $sleepHours hr $sleepRemMinutes min (${avgSleepMin} min)\n\n")

            append("3. HYDRATION PROFILE\n")
            append("----------------------------------------------------\n")
            append("Total Logged Volume:         ${String.format(Locale.US, "%.2f", totalWaterLitres)} Liters\n")
            append("Total Logged Entries:        $totalEntries record(s)\n\n")

            append("====================================================\n")
            append("End of Wellness Report - Pulse Health\n")
            append("====================================================\n")
        }
    }

    override fun observeDailySummary(epochDay: Long): Flow<DailyHealthSummaryEntity?> =
        dailyHealthSummaryDao?.observeByDay(epochDay) ?: flowOf(null)

    override fun observeSummaryRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DailyHealthSummaryEntity>> =
        dailyHealthSummaryDao?.observeRange(startEpochDay, endEpochDay) ?: flowOf(emptyList())

    override suspend fun getSummary(epochDay: Long): DailyHealthSummaryEntity? = withContext(ioDispatcher) {
        dailyHealthSummaryDao?.getByDay(epochDay)
    }

    override suspend fun saveDailySummary(summary: DailyHealthSummaryEntity) = withContext(ioDispatcher) {
        dailyHealthSummaryDao?.upsert(summary) ?: Unit
    }

    override suspend fun saveDailySummaries(summaries: List<DailyHealthSummaryEntity>) = withContext(ioDispatcher) {
        dailyHealthSummaryDao?.upsertAll(summaries) ?: Unit
    }

    override suspend fun getAllSummaries(): List<DailyHealthSummaryEntity> = withContext(ioDispatcher) {
        dailyHealthSummaryDao?.getAllSummaries() ?: emptyList()
    }

    override suspend fun deleteAllSummaries(): Int = withContext(ioDispatcher) {
        dailyHealthSummaryDao?.deleteAll() ?: 0
    }

    private fun todayBounds(): Pair<Long, Long> {
        val today = Instant.ofEpochMilli(clock.millis()).atZone(zoneId).toLocalDate()
        val start = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return start to end
    }

    private fun weekBounds(): Pair<Long, Long> {
        val today = Instant.ofEpochMilli(clock.millis()).atZone(zoneId).toLocalDate()
        val start = today.minusDays(6).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return start to end
    }
}

fun DailyHealthSummaryEntity.toDashboardMetrics(
    waterLiters: Double = 0.0,
    waterGoalLiters: Double = 2.0
): DashboardMetrics = DashboardMetrics(
    steps = steps,
    heartRate = restingHeartRate,
    sleepMinutes = sleepMinutes,
    waterLiters = waterLiters,
    waterGoalLiters = waterGoalLiters,
    activeCalories = activeCalories,
    exerciseMinutes = exerciseMinutes,
    standHours = standHours,
    source = DataSource.entries.firstOrNull { it.label == source } ?: DataSource.HEALTH_CONNECT,
    lastUpdatedEpochMillis = updatedAtEpochMillis
)

fun DashboardMetrics.toDailyHealthSummary(
    epochDay: Long,
    sourceLabel: String = source.label
): DailyHealthSummaryEntity = DailyHealthSummaryEntity(
    epochDay = epochDay,
    steps = steps ?: 0L,
    restingHeartRate = heartRate,
    sleepMinutes = sleepMinutes ?: 0L,
    activeCalories = activeCalories ?: 0L,
    exerciseMinutes = exerciseMinutes ?: 0L,
    standHours = standHours ?: 0L,
    source = sourceLabel,
    updatedAtEpochMillis = lastUpdatedEpochMillis ?: System.currentTimeMillis()
)

typealias DefaultHydrationRepository = HealthDataRepository
