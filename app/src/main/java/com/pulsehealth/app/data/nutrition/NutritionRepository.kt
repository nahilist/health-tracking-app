package com.pulsehealth.app.data.nutrition

import com.pulsehealth.app.data.local.MealDao
import com.pulsehealth.app.data.local.MealLogEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

interface NutritionRepository {
    fun observeMealsForDate(date: LocalDate): Flow<List<MealLog>>
    fun observeTodaySummary(activeCaloriesBurned: Int = 0, calorieGoal: Int = 2200): Flow<DailyNutritionSummary>
    suspend fun getMealsForDate(date: LocalDate): List<MealLog>
    suspend fun logMeal(
        mealType: MealType,
        name: String,
        calories: Int,
        proteinGrams: Double,
        carbsGrams: Double,
        fatGrams: Double,
        notes: String? = null,
        epochMillis: Long? = null
    ): MealLog
    suspend fun deleteMeal(id: String)
    suspend fun deleteAllMeals()
}

class DefaultNutritionRepository(
    private val mealDao: MealDao,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : NutritionRepository {

    override fun observeMealsForDate(date: LocalDate): Flow<List<MealLog>> {
        val startEpoch = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endEpoch = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        return mealDao.observeMealsBetween(startEpoch, endEpoch).map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override fun observeTodaySummary(activeCaloriesBurned: Int, calorieGoal: Int): Flow<DailyNutritionSummary> {
        val today = LocalDate.now(clock)
        return observeMealsForDate(today).map { meals ->
            calculateSummary(meals, activeCaloriesBurned, calorieGoal)
        }.flowOn(ioDispatcher)
    }

    override suspend fun getMealsForDate(date: LocalDate): List<MealLog> = withContext(ioDispatcher) {
        val startEpoch = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endEpoch = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        mealDao.getMealsBetween(startEpoch, endEpoch).map { it.toDomain() }
    }

    override suspend fun logMeal(
        mealType: MealType,
        name: String,
        calories: Int,
        proteinGrams: Double,
        carbsGrams: Double,
        fatGrams: Double,
        notes: String?,
        epochMillis: Long?
    ): MealLog = withContext(ioDispatcher) {
        val actualEpoch = epochMillis ?: clock.millis()
        val id = UUID.randomUUID().toString()
        val entity = MealLogEntity(
            id = id,
            mealType = mealType.name,
            name = name.trim().ifEmpty { "${mealType.label} Meal" },
            calories = calories.coerceAtLeast(0),
            proteinGrams = proteinGrams.coerceAtLeast(0.0),
            carbsGrams = carbsGrams.coerceAtLeast(0.0),
            fatGrams = fatGrams.coerceAtLeast(0.0),
            loggedAtEpochMillis = actualEpoch,
            notes = notes?.trim()
        )
        mealDao.upsertMeal(entity)
        entity.toDomain()
    }

    override suspend fun deleteMeal(id: String) = withContext(ioDispatcher) {
        mealDao.deleteMeal(id)
    }

    override suspend fun deleteAllMeals() = withContext(ioDispatcher) {
        mealDao.deleteAllMeals()
    }

    companion object {
        fun calculateSummary(
            meals: List<MealLog>,
            activeCaloriesBurned: Int = 0,
            calorieGoal: Int = 2200,
            proteinGoalGrams: Double = 140.0,
            carbsGoalGrams: Double = 220.0,
            fatGoalGrams: Double = 65.0
        ): DailyNutritionSummary {
            val totalCalories = meals.sumOf { it.calories }
            val totalProtein = meals.sumOf { it.proteinGrams }
            val totalCarbs = meals.sumOf { it.carbsGrams }
            val totalFat = meals.sumOf { it.fatGrams }
            val net = (totalCalories - activeCaloriesBurned).coerceAtLeast(0)
            val byType = meals.groupBy { it.mealType }

            return DailyNutritionSummary(
                totalCalories = totalCalories,
                calorieGoal = calorieGoal,
                activeBurnedCalories = activeCaloriesBurned,
                netCalories = net,
                totalProteinGrams = totalProtein,
                proteinGoalGrams = proteinGoalGrams,
                totalCarbsGrams = totalCarbs,
                carbsGoalGrams = carbsGoalGrams,
                totalFatGrams = totalFat,
                fatGoalGrams = fatGoalGrams,
                meals = meals,
                mealsByType = byType
            )
        }
    }
}

private fun MealLogEntity.toDomain(): MealLog = MealLog(
    id = id,
    mealType = MealType.fromString(mealType),
    name = name,
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    loggedAtEpochMillis = loggedAtEpochMillis,
    notes = notes
)

class NoOpNutritionRepository : NutritionRepository {
    override fun observeMealsForDate(date: LocalDate): Flow<List<MealLog>> = kotlinx.coroutines.flow.flowOf(emptyList())
    override fun observeTodaySummary(activeCaloriesBurned: Int, calorieGoal: Int): Flow<DailyNutritionSummary> =
        kotlinx.coroutines.flow.flowOf(DailyNutritionSummary(calorieGoal = calorieGoal, activeBurnedCalories = activeCaloriesBurned))
    override suspend fun getMealsForDate(date: LocalDate): List<MealLog> = emptyList()
    override suspend fun logMeal(
        mealType: MealType,
        name: String,
        calories: Int,
        proteinGrams: Double,
        carbsGrams: Double,
        fatGrams: Double,
        notes: String?,
        epochMillis: Long?
    ): MealLog = MealLog(
        UUID.randomUUID().toString(),
        mealType,
        name,
        calories,
        proteinGrams,
        carbsGrams,
        fatGrams,
        epochMillis ?: System.currentTimeMillis(),
        notes
    )
    override suspend fun deleteMeal(id: String) {}
    override suspend fun deleteAllMeals() {}
}

