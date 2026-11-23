package com.pulsehealth.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_logs",
    indices = [
        Index("loggedAtEpochMillis"),
        Index("mealType")
    ]
)
data class MealLogEntity(
    @PrimaryKey val id: String,
    val mealType: String,
    val name: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val loggedAtEpochMillis: Long,
    val notes: String? = null,
    val syncState: String = "synced"
)
