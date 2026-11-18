package com.pulsehealth.app.data

import com.pulsehealth.app.data.local.HydrationEntryEntity
import kotlinx.coroutines.flow.Flow

interface HydrationRepository {
    val todayWaterLiters: Flow<Double>
    val weeklyWaterLiters: Flow<Double>

    fun observeTodayEntries(): Flow<List<HydrationEntryEntity>>
    fun observeRecentEntries(limit: Int = 50, offset: Int = 0): Flow<List<HydrationEntryEntity>>
    fun observePastDaysEntries(days: Int = 14): Flow<List<HydrationEntryEntity>>

    suspend fun addWater(amountMilliliters: Int = 250, recordedAtEpochMillis: Long? = null)
    suspend fun updateWater(id: String, amountMilliliters: Int): Boolean
    suspend fun deleteWater(id: String): Boolean
    suspend fun getAllEntries(): List<HydrationEntryEntity>
    suspend fun clearAllData()
    suspend fun exportDataJson(): String
    suspend fun exportDataCsv(): String
    suspend fun exportClinicalSummary(): String
}

open class NoOpHydrationRepository : HydrationRepository {
    override val todayWaterLiters: Flow<Double> = kotlinx.coroutines.flow.flowOf(0.0)
    override val weeklyWaterLiters: Flow<Double> = kotlinx.coroutines.flow.flowOf(0.0)
    override fun observeTodayEntries(): Flow<List<HydrationEntryEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())
    override fun observeRecentEntries(limit: Int, offset: Int): Flow<List<HydrationEntryEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())
    override fun observePastDaysEntries(days: Int): Flow<List<HydrationEntryEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())
    override suspend fun addWater(amountMilliliters: Int, recordedAtEpochMillis: Long?) {}
    override suspend fun updateWater(id: String, amountMilliliters: Int): Boolean = false
    override suspend fun deleteWater(id: String): Boolean = false
    override suspend fun getAllEntries(): List<HydrationEntryEntity> = emptyList()
    override suspend fun clearAllData() {}
    override suspend fun exportDataJson(): String = "{}"
    override suspend fun exportDataCsv(): String = ""
    override suspend fun exportClinicalSummary(): String = ""
}
