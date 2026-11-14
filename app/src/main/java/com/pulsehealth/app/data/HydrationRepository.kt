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
}
