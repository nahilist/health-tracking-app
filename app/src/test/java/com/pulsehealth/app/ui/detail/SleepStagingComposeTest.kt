package com.pulsehealth.app.ui.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.pulsehealth.app.ui.theme.PulseTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SleepStagingComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun sleepHypnogramCard_rendersDemoSessionCorrectly() {
        composeTestRule.setContent {
            PulseTheme {
                SleepHypnogramCard(
                    sessionWithStages = null,
                    isDemo = true
                )
            }
        }

        composeTestRule.onNodeWithText("SLEEP HYPNOGRAM").assertIsDisplayed()
        composeTestRule.onNodeWithText("Deep").assertIsDisplayed()
        composeTestRule.onNodeWithText("Light").assertIsDisplayed()
        composeTestRule.onNodeWithText("REM").assertIsDisplayed()
        composeTestRule.onNodeWithText("Awake").assertIsDisplayed()
    }

    @Test
    fun sleepStageBreakdownCard_rendersAllStagesAndDurations() {
        val demoSession = createDemoSleepSession()

        composeTestRule.setContent {
            PulseTheme {
                SleepStageBreakdownCard(
                    sessionWithStages = demoSession,
                    isDemo = false
                )
            }
        }

        composeTestRule.onNodeWithText("STAGE BREAKDOWN").assertIsDisplayed()
        composeTestRule.onNodeWithText("Deep Sleep").assertIsDisplayed()
        composeTestRule.onNodeWithText("Light Sleep").assertIsDisplayed()
        composeTestRule.onNodeWithText("REM Sleep").assertIsDisplayed()
        composeTestRule.onNodeWithText("Awake").assertIsDisplayed()
    }

    @Test
    fun sleepEfficiencyCard_rendersEfficiencyAndDurations() {
        val demoSession = createDemoSleepSession()

        composeTestRule.setContent {
            PulseTheme {
                SleepEfficiencyCard(
                    sessionWithStages = demoSession,
                    isDemo = false
                )
            }
        }

        composeTestRule.onNodeWithText("RESTFULNESS & EFFICIENCY").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sleep Efficiency").assertIsDisplayed()
        composeTestRule.onNodeWithText("Time Asleep").assertIsDisplayed()
        composeTestRule.onNodeWithText("Time in Bed").assertIsDisplayed()
    }

    @Test
    fun sleepEmptyStateCard_rendersInformativeGuidance() {
        composeTestRule.setContent {
            PulseTheme {
                SleepHypnogramCard(
                    sessionWithStages = null,
                    isDemo = false
                )
            }
        }

        composeTestRule.onNodeWithText("No Sleep Staging Recorded").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wear your connected wearable to track sleep stages").assertIsDisplayed()
    }
}
