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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<HydrationEntryEntity>)

    @Query("SELECT * FROM hydration_entries WHERE syncState = :syncState")
    suspend fun getEntriesBySyncState(syncState: String): List<HydrationEntryEntity>

    @Query("UPDATE hydration_entries SET syncState = :syncState WHERE id = :id")
    suspend fun updateSyncState(id: String, syncState: String): Int

    @Delete
    suspend fun delete(entry: HydrationEntryEntity)

    @Query("DELETE FROM hydration_entries WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("UPDATE hydration_entries SET amountMilliliters = :amountMilliliters WHERE id = :id")
    suspend fun updateAmount(id: String, amountMilliliters: Int): Int

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

    @Query(
        """
        SELECT * FROM hydration_entries
        ORDER BY recordedAtEpochMillis DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observeRecentEntries(limit: Int = 50, offset: Int = 0): Flow<List<HydrationEntryEntity>>

    @Query("SELECT * FROM hydration_entries WHERE id = :id")
    suspend fun getEntryById(id: String): HydrationEntryEntity?

    @Query("SELECT * FROM hydration_entries ORDER BY recordedAtEpochMillis DESC")
    suspend fun getAllEntries(): List<HydrationEntryEntity>

    @Query("DELETE FROM hydration_entries")
    suspend fun deleteAll(): Int

    @Query("DELETE FROM hydration_entries WHERE recordedAtEpochMillis < :cutoffEpochMillis")
    suspend fun deleteOlderThan(cutoffEpochMillis: Long): Int
}
