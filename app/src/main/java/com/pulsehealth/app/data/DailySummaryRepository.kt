package com.pulsehealth.app.data

import com.pulsehealth.app.data.local.DailyHealthSummaryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface DailySummaryRepository {
    fun observeDailySummary(epochDay: Long): Flow<DailyHealthSummaryEntity?>
    fun observeSummaryRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DailyHealthSummaryEntity>>
    suspend fun getSummary(epochDay: Long): DailyHealthSummaryEntity?
    suspend fun saveDailySummary(summary: DailyHealthSummaryEntity)
    suspend fun saveDailySummaries(summaries: List<DailyHealthSummaryEntity>)
    suspend fun getAllSummaries(): List<DailyHealthSummaryEntity>
    suspend fun deleteAllSummaries(): Int
}

class NoOpDailySummaryRepository : DailySummaryRepository {
    override fun observeDailySummary(epochDay: Long): Flow<DailyHealthSummaryEntity?> = flowOf(null)
    override fun observeSummaryRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DailyHealthSummaryEntity>> = flowOf(emptyList())
    override suspend fun getSummary(epochDay: Long): DailyHealthSummaryEntity? = null
    override suspend fun saveDailySummary(summary: DailyHealthSummaryEntity) = Unit
    override suspend fun saveDailySummaries(summaries: List<DailyHealthSummaryEntity>) = Unit
    override suspend fun getAllSummaries(): List<DailyHealthSummaryEntity> = emptyList()
    override suspend fun deleteAllSummaries(): Int = 0
}
