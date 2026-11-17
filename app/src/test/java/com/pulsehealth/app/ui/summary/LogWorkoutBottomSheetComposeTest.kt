package com.pulsehealth.app.ui.summary

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pulsehealth.app.data.WorkoutType
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
class LogWorkoutBottomSheetComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun logWorkoutBottomSheet_rendersTypesAndPresets() {
        var dismissed = false

        composeTestRule.setContent {
            PulseTheme {
                LogWorkoutBottomSheet(
                    onLogWorkout = { _, _, _, _, _, _ -> },
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Log Workout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Running").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cycling").assertIsDisplayed()
        composeTestRule.onNodeWithText("30 min").assertIsDisplayed()
        composeTestRule.onNodeWithText("45m").assertIsDisplayed()
    }

    @Test
    fun logWorkoutBottomSheet_changingPresetAndTypeTriggersCallback() {
        var loggedType: WorkoutType? = null
        var loggedDuration: Long? = null
        var loggedTitle: String? = null

        composeTestRule.setContent {
            PulseTheme {
                LogWorkoutBottomSheet(
                    onLogWorkout = { type, title, duration, _, _, _ ->
                        loggedType = type
                        loggedTitle = title
                        loggedDuration = duration
                    },
                    onDismiss = {}
                )
            }
        }

        // 1. Select Cycling
        composeTestRule.onNodeWithText("Cycling").performClick()

        // 2. Select 45m preset
        composeTestRule.onNodeWithText("45m").performClick()

        // 3. Click Save Workout button
        composeTestRule.onNodeWithContentDescription("Confirm and log 45 minute Cycling workout").performClick()

        assertEquals(WorkoutType.CYCLING, loggedType)
        assertEquals(45L, loggedDuration)
        assertEquals(WorkoutType.CYCLING.defaultTitle, loggedTitle)
    }

    @Test
    fun logWorkoutBottomSheet_closeButtonInvokesDismiss() {
        var dismissed = false

        composeTestRule.setContent {
            PulseTheme {
                LogWorkoutBottomSheet(
                    onLogWorkout = { _, _, _, _, _, _ -> },
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Close log workout dialog").performClick()
        assertTrue(dismissed)
    }
}
