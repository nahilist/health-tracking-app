package com.pulsehealth.app.data.nutrition

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector

enum class MealType(
    val label: String,
    val icon: ImageVector,
    val defaultTargetCalories: Int
) {
    BREAKFAST("Breakfast", Icons.Default.BreakfastDining, 500),
    LUNCH("Lunch", Icons.Default.LunchDining, 650),
    DINNER("Dinner", Icons.Default.DinnerDining, 750),
    SNACK("Snacks", Icons.Default.Restaurant, 300);

    companion object {
        fun fromString(value: String): MealType = entries.find { it.name.equals(value, ignoreCase = true) } ?: SNACK
    }
}

data class MealLog(
    val id: String,
    val mealType: MealType,
    val name: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val loggedAtEpochMillis: Long,
    val notes: String? = null
)

data class DailyNutritionSummary(
    val totalCalories: Int = 0,
    val calorieGoal: Int = 2200,
    val activeBurnedCalories: Int = 0,
    val netCalories: Int = 0,
    val totalProteinGrams: Double = 0.0,
    val proteinGoalGrams: Double = 140.0,
    val totalCarbsGrams: Double = 0.0,
    val carbsGoalGrams: Double = 220.0,
    val totalFatGrams: Double = 0.0,
    val fatGoalGrams: Double = 65.0,
    val meals: List<MealLog> = emptyList(),
    val mealsByType: Map<MealType, List<MealLog>> = emptyMap()
) {
    val remainingCalories: Int
        get() = (calorieGoal - totalCalories).coerceAtLeast(0)

    val calorieProgress: Float
        get() = (totalCalories.toFloat() / calorieGoal.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    val proteinProgress: Float
        get() = (totalProteinGrams.toFloat() / proteinGoalGrams.coerceAtLeast(1.0).toFloat()).coerceIn(0f, 1f)

    val carbsProgress: Float
        get() = (totalCarbsGrams.toFloat() / carbsGoalGrams.coerceAtLeast(1.0).toFloat()).coerceIn(0f, 1f)

    val fatProgress: Float
        get() = (totalFatGrams.toFloat() / fatGoalGrams.coerceAtLeast(1.0).toFloat()).coerceIn(0f, 1f)
}
