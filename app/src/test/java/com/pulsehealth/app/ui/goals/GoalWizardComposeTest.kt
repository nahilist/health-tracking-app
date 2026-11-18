package com.pulsehealth.app.ui.goals

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.ui.theme.PulseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h900dp")
class GoalWizardComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun goalWizard_rendersInitialStepAndPresets() {
        composeTestRule.setContent {
            PulseTheme {
                GoalWizardBottomSheet(
                    initialWaterGoalLiters = 2.0,
                    initialStepGoal = 10_000L,
                    initialCalorieGoal = 500L,
                    initialSleepGoalMinutes = 480L,
                    onSaveGoals = { _, _, _, _ -> },
                    onDismiss = {}
                )
            }
        }

        // Header & step indicator
        composeTestRule.onNodeWithText("Customize Targets").assertIsDisplayed()
        composeTestRule.onNodeWithText("Step 1 of 5").assertIsDisplayed()

        // Step 1 title & numbers
        composeTestRule.onNodeWithText("Daily Step Target").assertIsDisplayed()
        composeTestRule.onNodeWithText("steps per day").assertIsDisplayed()

        // Preset chips
        composeTestRule.onNodeWithText("Light").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active").assertIsDisplayed()
        composeTestRule.onNodeWithText("Conditioning").assertIsDisplayed()
        composeTestRule.onNodeWithText("Athlete").assertIsDisplayed()

        // Bottom CTA
        composeTestRule.onNodeWithText("Next Step").assertIsDisplayed()
    }

    @Test
    fun goalWizard_navigatesThroughAllFiveSteps() {
        composeTestRule.setContent {
            PulseTheme {
                GoalWizardBottomSheet(
                    initialWaterGoalLiters = 2.0,
                    initialStepGoal = 10_000L,
                    initialCalorieGoal = 500L,
                    initialSleepGoalMinutes = 480L,
                    onSaveGoals = { _, _, _, _ -> },
                    onDismiss = {}
                )
            }
        }

        // Step 1: Steps -> Click Next
        composeTestRule.onNodeWithText("Daily Step Target").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next Step").performClick()

        // Step 2: Calories -> Click Next
        composeTestRule.onNodeWithText("Step 2 of 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active Calorie Burn").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next Step").performClick()

        // Step 3: Water -> Click Next
        composeTestRule.onNodeWithText("Step 3 of 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Daily Hydration Target").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next Step").performClick()

        // Step 4: Sleep -> Click Next
        composeTestRule.onNodeWithText("Step 4 of 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nightly Sleep Duration").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next Step").performClick()

        // Step 5: Review Summary
        composeTestRule.onNodeWithText("Step 5 of 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Review Daily Targets").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save & Apply Targets").assertIsDisplayed()
    }

    @Test
    fun goalWizard_presetSelectionUpdatesValueAndSaves() {
        var savedWater = 0.0
        var savedSteps = 0L
        var savedCalories = 0L
        var savedSleep = 0L

        composeTestRule.setContent {
            PulseTheme {
                GoalWizardBottomSheet(
                    initialWaterGoalLiters = 2.0,
                    initialStepGoal = 10_000L,
                    initialCalorieGoal = 500L,
                    initialSleepGoalMinutes = 480L,
                    onSaveGoals = { w, s, c, sl ->
                        savedWater = w
                        savedSteps = s
                        savedCalories = c
                        savedSleep = sl
                    },
                    onDismiss = {}
                )
            }
        }

        // Step 1: Select "Athlete" (18,000 steps)
        composeTestRule.onNodeWithText("Athlete").performClick()
        composeTestRule.onNodeWithText("Next Step").performClick()

        // Step 2: Select "Peak" (1,000 kcal)
        composeTestRule.onNodeWithText("Peak").performClick()
        composeTestRule.onNodeWithText("Next Step").performClick()

        // Step 3: Select "Hydrated" (2.5L)
        composeTestRule.onNodeWithText("Hydrated").performClick()
        composeTestRule.onNodeWithText("Next Step").performClick()

        // Step 4: Select "Extended" (540m / 9h)
        composeTestRule.onNodeWithText("Extended").performClick()
        composeTestRule.onNodeWithText("Next Step").performClick()

        // Step 5: Save & Apply
        composeTestRule.onNodeWithText("Save & Apply Targets").performClick()

        assertEquals(18_000L, savedSteps)
        assertEquals(1_000L, savedCalories)
        assertEquals(2.5, savedWater, 0.05)
        assertEquals(540L, savedSleep)
    }

    @Test
    fun goalProgressSummaryCard_rendersPillMetricsAndClickOpensWizard() {
        var wizardOpened = false

        composeTestRule.setContent {
            PulseTheme {
                GoalProgressSummaryCard(
                    metrics = DashboardMetrics(
                        steps = 8_200L,
                        stepGoal = 10_000L,
                        activeCalories = 420L,
                        calorieGoal = 500L,
                        waterLiters = 1.75,
                        waterGoalLiters = 2.0,
                        sleepMinutes = 450L
                    ),
                    onOpenWizard = { wizardOpened = true }
                )
            }
        }

        // Card header & pill labels
        composeTestRule.onNodeWithText("Daily Wellness Targets").assertIsDisplayed()
        composeTestRule.onNodeWithText("Steps").assertIsDisplayed()
        composeTestRule.onNodeWithText("Burn").assertIsDisplayed()
        composeTestRule.onNodeWithText("Water").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sleep").assertIsDisplayed()

        // Values
        composeTestRule.onNodeWithText("8,200").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.8L").assertIsDisplayed()

        // Adjust CTA click
        composeTestRule.onNodeWithText("Adjust").performClick()
        assertTrue(wizardOpened)
    }
}
