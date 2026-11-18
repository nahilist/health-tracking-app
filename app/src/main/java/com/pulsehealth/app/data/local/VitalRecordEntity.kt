package com.pulsehealth.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vital_records",
    indices = [
        Index("recordedAtEpochMillis"),
        Index("vitalType")
    ]
)
data class VitalRecordEntity(
    @PrimaryKey
    val id: String,
    val vitalType: String,
    val systolic: Int? = null,
    val diastolic: Int? = null,
    val heartRateBpm: Int? = null,
    val recordedAtEpochMillis: Long,
    val measurementContext: String,
    val notes: String? = null,
    val source: String = "manual",
    val syncState: String = "pending"
)
