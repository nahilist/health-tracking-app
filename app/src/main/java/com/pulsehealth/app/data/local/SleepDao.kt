package com.pulsehealth.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_sessions ORDER BY startTimeEpochMillis DESC")
    fun observeAllSleepSessions(): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE startTimeEpochMillis >= :startEpochMillis AND startTimeEpochMillis < :endEpochMillis ORDER BY startTimeEpochMillis DESC")
    fun observeSleepSessionsInRange(startEpochMillis: Long, endEpochMillis: Long): Flow<List<SleepSessionEntity>>

    @Transaction
    @Query("SELECT * FROM sleep_sessions ORDER BY startTimeEpochMillis DESC")
    fun observeAllSleepWithStages(): Flow<List<SleepSessionWithStages>>

    @Transaction
    @Query("SELECT * FROM sleep_sessions WHERE startTimeEpochMillis >= :startEpochMillis AND startTimeEpochMillis < :endEpochMillis ORDER BY startTimeEpochMillis DESC")
    fun observeSleepWithStagesInRange(startEpochMillis: Long, endEpochMillis: Long): Flow<List<SleepSessionWithStages>>

    @Transaction
    @Query("SELECT * FROM sleep_sessions ORDER BY startTimeEpochMillis DESC LIMIT 1")
    fun observeLatestSleepWithStages(): Flow<SleepSessionWithStages?>

    @Query("SELECT * FROM sleep_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): SleepSessionEntity?

    @Query("SELECT * FROM sleep_stages WHERE sessionId = :sessionId ORDER BY startTimeEpochMillis ASC")
    suspend fun getStagesForSession(sessionId: String): List<SleepStageEntity>

    @Query("SELECT * FROM sleep_sessions ORDER BY startTimeEpochMillis DESC")
    suspend fun getAllSessions(): List<SleepSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: SleepSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessions(sessions: List<SleepSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStages(stages: List<SleepStageEntity>)

    @Transaction
    suspend fun upsertSessionWithStages(session: SleepSessionEntity, stages: List<SleepStageEntity>) {
        upsertSession(session)
        if (stages.isNotEmpty()) {
            upsertStages(stages)
        }
    }

    @Query("DELETE FROM sleep_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String): Int

    @Query("DELETE FROM sleep_sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM sleep_stages")
    suspend fun deleteAllStages()
}
