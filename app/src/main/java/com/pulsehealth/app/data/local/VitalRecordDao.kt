package com.pulsehealth.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: VitalRecordEntity)

    @Query("DELETE FROM vital_records WHERE id = :id")
    suspend fun deleteRecord(id: String)

    @Query("SELECT * FROM vital_records WHERE vitalType = :vitalType ORDER BY recordedAtEpochMillis DESC")
    fun observeRecordsForType(vitalType: String): Flow<List<VitalRecordEntity>>

    @Query("SELECT * FROM vital_records ORDER BY recordedAtEpochMillis DESC LIMIT :limit")
    fun observeRecentRecords(limit: Int = 50): Flow<List<VitalRecordEntity>>

    @Query("SELECT * FROM vital_records WHERE recordedAtEpochMillis >= :startEpochMillis AND recordedAtEpochMillis < :endEpochMillis ORDER BY recordedAtEpochMillis DESC")
    fun observeRecordsInRange(startEpochMillis: Long, endEpochMillis: Long): Flow<List<VitalRecordEntity>>

    @Query("SELECT * FROM vital_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: String): VitalRecordEntity?

    @Query("DELETE FROM vital_records")
    suspend fun clearAll()
}
