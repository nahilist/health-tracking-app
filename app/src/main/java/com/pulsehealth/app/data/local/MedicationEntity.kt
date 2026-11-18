package com.pulsehealth.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dosage: Double,
    val unit: String,
    val frequency: String,
    val scheduledTimes: String,
    val instructions: String? = null,
    val colorHex: String = "#0D5C58",
    val isActive: Boolean = true,
    val createdAtEpochMillis: Long,
    val notes: String? = null
)
