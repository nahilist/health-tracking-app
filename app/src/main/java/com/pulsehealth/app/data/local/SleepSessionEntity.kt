package com.pulsehealth.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sleep_sessions",
    indices = [Index(value = ["startTimeEpochMillis"])]
)
data class SleepSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val durationMinutes: Long,
    val deepDurationMinutes: Long,
    val lightDurationMinutes: Long,
    val remDurationMinutes: Long,
    val awakeDurationMinutes: Long,
    val sleepEfficiencyPercentage: Int,
    val source: String,
    val notes: String?,
    val syncState: String
)
