package com.pulsehealth.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_sessions ORDER BY startTimeEpochMillis DESC")
    fun observeAllWorkouts(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE startTimeEpochMillis >= :startEpochMillis AND startTimeEpochMillis < :endEpochMillis ORDER BY startTimeEpochMillis DESC")
    fun observeWorkoutsInRange(startEpochMillis: Long, endEpochMillis: Long): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions ORDER BY startTimeEpochMillis DESC")
    suspend fun getAllWorkouts(): List<WorkoutSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(workout: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(workouts: List<WorkoutSessionEntity>)

    @Delete
    suspend fun delete(workout: WorkoutSessionEntity)

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAll()
}
