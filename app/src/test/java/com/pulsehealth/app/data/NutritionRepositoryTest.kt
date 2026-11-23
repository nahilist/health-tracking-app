package com.pulsehealth.app.data

import com.pulsehealth.app.data.local.MealDao
import com.pulsehealth.app.data.local.MealLogEntity
import com.pulsehealth.app.data.nutrition.DefaultNutritionRepository
import com.pulsehealth.app.data.nutrition.MealLog
import com.pulsehealth.app.data.nutrition.MealType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class NutritionRepositoryTest {

    private val fixedClock = Clock.fixed(Instant.parse("2026-11-23T12:00:00Z"), ZoneId.of("UTC"))
    private val zoneId = ZoneId.of("UTC")

    private class FakeMealDao : MealDao {
        val meals = mutableListOf<MealLogEntity>()

        override fun observeMealsBetween(startEpochMillis: Long, endEpochMillis: Long): Flow<List<MealLogEntity>> {
            return flowOf(meals.filter { it.loggedAtEpochMillis in startEpochMillis..endEpochMillis })
        }

        override suspend fun getMealsBetween(startEpochMillis: Long, endEpochMillis: Long): List<MealLogEntity> {
            return meals.filter { it.loggedAtEpochMillis in startEpochMillis..endEpochMillis }
        }

        override suspend fun getMealById(id: String): MealLogEntity? = meals.find { it.id == id }

        override suspend fun upsertMeal(meal: MealLogEntity) {
            meals.removeAll { it.id == meal.id }
            meals.add(meal)
        }

        override suspend fun deleteMeal(id: String) {
            meals.removeAll { it.id == id }
        }

        override suspend fun deleteAllMeals() {
            meals.clear()
        }
    }

    @Test
    fun logMealAndObserveSummaryComputesTotalsAndNetCalories() = runBlocking {
        val fakeDao = FakeMealDao()
        val repo = DefaultNutritionRepository(fakeDao, fixedClock, zoneId)

        val epoch = fixedClock.instant().toEpochMilli()
        repo.logMeal(MealType.BREAKFAST, "Eggs & Toast", 400, 24.0, 30.0, 14.0, null, epoch)
        repo.logMeal(MealType.LUNCH, "Chicken Rice Bowl", 650, 48.0, 65.0, 16.0, null, epoch + 3600_000)

        val meals = repo.getMealsForDate(LocalDate.now(fixedClock))
        assertEquals(2, meals.size)

        val summary = DefaultNutritionRepository.calculateSummary(
            meals = meals,
            activeCaloriesBurned = 350,
            calorieGoal = 2000,
            proteinGoalGrams = 150.0,
            carbsGoalGrams = 200.0,
            fatGoalGrams = 65.0
        )

        assertEquals(1050, summary.totalCalories)
        assertEquals(700, summary.netCalories)
        assertEquals(950, summary.remainingCalories)
        assertEquals(72.0, summary.totalProteinGrams, 0.01)
        assertEquals(95.0, summary.totalCarbsGrams, 0.01)
        assertEquals(30.0, summary.totalFatGrams, 0.01)
        assertEquals(1, summary.mealsByType[MealType.BREAKFAST]?.size)
        assertEquals(1, summary.mealsByType[MealType.LUNCH]?.size)
    }

    @Test
    fun deleteMealRemovesItemFromSummary() = runBlocking {
        val fakeDao = FakeMealDao()
        val repo = DefaultNutritionRepository(fakeDao, fixedClock, zoneId)

        val logged = repo.logMeal(MealType.SNACK, "Protein Bar", 220, 20.0, 22.0, 7.0, null)
        assertEquals(1, repo.getMealsForDate(LocalDate.now(fixedClock)).size)

        repo.deleteMeal(logged.id)
        assertTrue(repo.getMealsForDate(LocalDate.now(fixedClock)).isEmpty())
    }
}
