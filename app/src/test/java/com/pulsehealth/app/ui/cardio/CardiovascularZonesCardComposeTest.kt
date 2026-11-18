package com.pulsehealth.app.ui.cardio

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pulsehealth.app.ui.theme.PulseTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CardiovascularZonesCardComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cardioZonesCard_rendersHeaderAndMaxHr() {
        composeTestRule.setContent {
            PulseTheme {
                CardiovascularZonesCard(
                    currentRestingBpm = 64,
                    activeMinutesToday = 40,
                    recentHeartRateReadings = listOf(62, 64, 65, 63)
                )
            }
        }

        composeTestRule.onNodeWithText("Cardiovascular Zones & Pulse").assertIsDisplayed()
        composeTestRule.onNodeWithText("5-Tier HR Training & Sinus Cadence").assertIsDisplayed()
        composeTestRule.onNodeWithText("187 BPM Max").assertIsDisplayed()
        composeTestRule.onNodeWithText("SINUS CADENCE & RESTING PULSE WAVEFORM").assertExists()
        composeTestRule.onNodeWithText("Resting Stability: Optimal Stability").assertExists()
    }

    @Test
    fun clickingMaxHrPill_revealsTanakaAgeAdjuster() {
        composeTestRule.setContent {
            PulseTheme {
                CardiovascularZonesCard(
                    currentRestingBpm = 68,
                    activeMinutesToday = 50
                )
            }
        }

        // Click the Max HR pill
        composeTestRule.onNodeWithText("187 BPM Max").performClick()

        // Formula drawer appears
        composeTestRule.onNodeWithText("Tanaka Max HR Formula").assertIsDisplayed()
        composeTestRule.onNodeWithText("30 yrs").assertIsDisplayed()

        // Click increment age
        composeTestRule.onNodeWithContentDescription("Increase Age").performClick()
        composeTestRule.onNodeWithText("31 yrs").assertIsDisplayed()
    }

    @Test
    fun clickingZoneLegend_switchesSelectedZoneDetails() {
        composeTestRule.setContent {
            PulseTheme {
                CardiovascularZonesCard(
                    currentRestingBpm = 70,
                    activeMinutesToday = 60
                )
            }
        }

        // Default selection is Zone 2: Aerobic Base
        composeTestRule.onNodeWithText("Zone 2: Aerobic Base").assertIsDisplayed()

        // Switch to Zone 4
        composeTestRule.onNodeWithText("Z4").performClick()
        composeTestRule.onNodeWithText("Zone 4: Anaerobic Threshold").assertIsDisplayed()

        // Switch to Zone 5
        composeTestRule.onNodeWithText("Z5").performClick()
        composeTestRule.onNodeWithText("Zone 5: Peak Output").assertIsDisplayed()
    }
}
