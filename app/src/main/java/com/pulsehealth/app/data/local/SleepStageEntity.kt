package com.pulsehealth.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sleep_stages",
    foreignKeys = [
        ForeignKey(
            entity = SleepSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["startTimeEpochMillis"])
    ]
)
data class SleepStageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val stage: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val durationMinutes: Long
)
