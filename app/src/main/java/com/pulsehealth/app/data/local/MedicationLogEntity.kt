package com.pulsehealth.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("medicationId"),
        Index("scheduledTimeEpochMillis")
    ]
)
data class MedicationLogEntity(
    @PrimaryKey val id: String,
    val medicationId: String,
    val scheduledTimeEpochMillis: Long,
    val loggedAtEpochMillis: Long,
    val status: String, // "TAKEN", "SKIPPED"
    val notes: String? = null
)
