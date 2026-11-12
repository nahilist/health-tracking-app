package com.pulsehealth.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_health_summaries")
data class DailyHealthSummaryEntity(
    @PrimaryKey val epochDay: Long,
    val steps: Long,
    val restingHeartRate: Long?,
    val sleepMinutes: Long,
    val activeCalories: Long,
    val exerciseMinutes: Long,
    val standHours: Long,
    val source: String,
    val updatedAtEpochMillis: Long
)
