package com.pulsehealth.app.ui.vitals

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pulsehealth.app.data.MeasurementContext
import com.pulsehealth.app.data.VitalType
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
class LogVitalBottomSheetComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun logVitalBottomSheet_rendersBloodPressureModeAndSteppers() {
        var dismissed = false

        composeTestRule.setContent {
            PulseTheme {
                LogVitalBottomSheet(
                    initialType = VitalType.BLOOD_PRESSURE,
                    onLogBloodPressure = { _, _, _, _, _ -> },
                    onLogHeartRate = { _, _, _ -> },
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Log Blood Pressure").assertIsDisplayed()
        composeTestRule.onNodeWithText("BLOOD PRESSURE (mmHg)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stage 1 Range (120 / 80 mmHg)").assertExists()
        composeTestRule.onNodeWithText("MEASUREMENT CONTEXT").assertExists()
        composeTestRule.onNodeWithText("Save Blood Pressure").assertExists()
    }

    @Test
    fun logVitalBottomSheet_switchTypeToHeartRate() {
        composeTestRule.setContent {
            PulseTheme {
                LogVitalBottomSheet(
                    initialType = VitalType.BLOOD_PRESSURE,
                    onLogBloodPressure = { _, _, _, _, _ -> },
                    onLogHeartRate = { _, _, _ -> },
                    onDismiss = {}
                )
            }
        }

        // Click Heart Rate tab
        composeTestRule.onNodeWithContentDescription("Select Heart Rate").performClick()

        composeTestRule.onNodeWithText("Log Heart Rate").assertIsDisplayed()
        composeTestRule.onNodeWithText("HEART RATE (BPM)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save Heart Rate").assertIsDisplayed()
    }

    @Test
    fun logVitalBottomSheet_saveBloodPressureTriggersCallback() {
        var savedSys: Int? = null
        var savedDia: Int? = null
        var savedPulse: Int? = null
        var savedCtx: MeasurementContext? = null
        var dismissed = false

        composeTestRule.setContent {
            PulseTheme {
                LogVitalBottomSheet(
                    initialType = VitalType.BLOOD_PRESSURE,
                    onLogBloodPressure = { sys, dia, pulse, ctx, _ ->
                        savedSys = sys
                        savedDia = dia
                        savedPulse = pulse
                        savedCtx = ctx
                    },
                    onLogHeartRate = { _, _, _ -> },
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Save Blood Pressure").performClick()

        assertEquals(120, savedSys)
        assertEquals(80, savedDia)
        assertEquals(72, savedPulse)
        assertEquals(MeasurementContext.RESTING, savedCtx)
        assertTrue(dismissed)
    }
}
