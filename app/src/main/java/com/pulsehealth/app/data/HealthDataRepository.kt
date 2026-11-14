package com.pulsehealth.app.data

import com.pulsehealth.app.data.local.DailyHealthSummaryDao
import com.pulsehealth.app.data.local.HydrationDao
import com.pulsehealth.app.data.local.HydrationEntryEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class HealthDataRepository(
    private val hydrationDao: HydrationDao,
    private val dailyHealthSummaryDao: DailyHealthSummaryDao? = null,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : HydrationRepository {

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

typealias DefaultHydrationRepository = HealthDataRepository
