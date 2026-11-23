package com.pulsehealth.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_logs WHERE loggedAtEpochMillis BETWEEN :startEpochMillis AND :endEpochMillis ORDER BY loggedAtEpochMillis ASC")
    fun observeMealsBetween(startEpochMillis: Long, endEpochMillis: Long): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs WHERE loggedAtEpochMillis BETWEEN :startEpochMillis AND :endEpochMillis ORDER BY loggedAtEpochMillis ASC")
    suspend fun getMealsBetween(startEpochMillis: Long, endEpochMillis: Long): List<MealLogEntity>

    @Query("SELECT * FROM meal_logs WHERE id = :id")
    suspend fun getMealById(id: String): MealLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeal(meal: MealLogEntity)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMeal(id: String)

    @Query("DELETE FROM meal_logs")
    suspend fun deleteAllMeals()
}
