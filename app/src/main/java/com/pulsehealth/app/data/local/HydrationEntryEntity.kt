package com.pulsehealth.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hydration_entries",
    indices = [Index(value = ["recordedAtEpochMillis"])]
)
data class HydrationEntryEntity(
    @PrimaryKey val id: String,
    val amountMilliliters: Int,
    val recordedAtEpochMillis: Long,
    val source: String,
    val syncState: String
)
