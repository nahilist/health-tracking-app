package com.pulsehealth.app.ui.nutrition

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pulsehealth.app.data.nutrition.DailyNutritionSummary
import com.pulsehealth.app.data.nutrition.MealLog
import com.pulsehealth.app.data.nutrition.MealType
import com.pulsehealth.app.ui.theme.PulseTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
class DailyNutritionCardComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dailyNutritionCard_rendersCaloriesMacrosAndSlots() {
        var clickedType: MealType? = null
        val summary = DailyNutritionSummary(
            totalCalories = 1450,
            calorieGoal = 2200,
            activeBurnedCalories = 300,
            netCalories = 1150,
            totalProteinGrams = 110.0,
            proteinGoalGrams = 140.0,
            totalCarbsGrams = 150.0,
            carbsGoalGrams = 220.0,
            totalFatGrams = 42.0,
            fatGoalGrams = 65.0,
            meals = listOf(
                MealLog("m1", MealType.BREAKFAST, "Oats & Whey", 450, 32.0, 55.0, 8.0, 1000L)
            ),
            mealsByType = mapOf(
                MealType.BREAKFAST to listOf(MealLog("m1", MealType.BREAKFAST, "Oats & Whey", 450, 32.0, 55.0, 8.0, 1000L))
            )
        )

        composeTestRule.setContent {
            PulseTheme {
                DailyNutritionCard(
                    summary = summary,
                    onAddMealClick = { clickedType = it },
                    onDeleteMealClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Nutrition").assertIsDisplayed()
        composeTestRule.onNodeWithText("Daily fuel & macronutrients").assertIsDisplayed()
        composeTestRule.onNodeWithText("Protein").assertIsDisplayed()
        composeTestRule.onNodeWithText("Carbs").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Breakfast").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lunch").assertIsDisplayed()

        // Click Add Lunch
        composeTestRule.onNodeWithContentDescription("Add Lunch").performClick()
        assertEquals(MealType.LUNCH, clickedType)
    }
}
