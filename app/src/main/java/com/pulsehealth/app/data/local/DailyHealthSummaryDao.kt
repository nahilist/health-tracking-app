package com.pulsehealth.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyHealthSummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(summary: DailyHealthSummaryEntity)

    @Query("SELECT * FROM daily_health_summaries WHERE epochDay = :epochDay")
    fun observeByDay(epochDay: Long): Flow<DailyHealthSummaryEntity?>

    @Query(
        """
        SELECT * FROM daily_health_summaries
        WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY epochDay ASC
        """
    )
    fun observeRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DailyHealthSummaryEntity>>

    @Query("SELECT * FROM daily_health_summaries ORDER BY epochDay DESC")
    suspend fun getAllSummaries(): List<DailyHealthSummaryEntity>

    @Query("DELETE FROM daily_health_summaries")
    suspend fun deleteAll(): Int
}
