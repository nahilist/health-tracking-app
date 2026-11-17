package com.pulsehealth.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sessions",
    indices = [Index(value = ["startTimeEpochMillis"])]
)
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val workoutType: String,
    val title: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val durationMinutes: Long,
    val activeCalories: Int?,
    val distanceMeters: Double?,
    val source: String,
    val notes: String?,
    val syncState: String
)
