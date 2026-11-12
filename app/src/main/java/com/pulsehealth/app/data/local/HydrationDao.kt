package com.pulsehealth.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: HydrationEntryEntity)

    @Delete
    suspend fun delete(entry: HydrationEntryEntity)

    @Query(
        """
        SELECT COALESCE(SUM(amountMilliliters), 0)
        FROM hydration_entries
        WHERE recordedAtEpochMillis >= :startInclusive
          AND recordedAtEpochMillis < :endExclusive
        """
    )
    fun observeTotalMilliliters(startInclusive: Long, endExclusive: Long): Flow<Long>

    @Query(
        """
        SELECT * FROM hydration_entries
        WHERE recordedAtEpochMillis >= :startInclusive
          AND recordedAtEpochMillis < :endExclusive
        ORDER BY recordedAtEpochMillis DESC
        """
    )
    fun observeEntries(startInclusive: Long, endExclusive: Long): Flow<List<HydrationEntryEntity>>

    @Query("DELETE FROM hydration_entries WHERE recordedAtEpochMillis < :cutoffEpochMillis")
    suspend fun deleteOlderThan(cutoffEpochMillis: Long): Int
}
