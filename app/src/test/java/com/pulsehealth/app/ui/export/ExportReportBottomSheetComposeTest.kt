package com.pulsehealth.app.ui.export

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pulsehealth.app.data.export.ReportFormat
import com.pulsehealth.app.data.export.ReportSectionConfig
import com.pulsehealth.app.data.export.ReportTimeRange
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
class ExportReportBottomSheetComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun exportReportBottomSheet_rendersFormatAndOptions() {
        composeTestRule.setContent {
            PulseTheme {
                ExportReportBottomSheet(
                    onDismiss = {},
                    onGenerateAndShare = { _, _, _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Export Health Report").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clinical PDF").assertIsDisplayed()
        composeTestRule.onNodeWithText("CSV Bundle").assertIsDisplayed()
        composeTestRule.onNodeWithText("30 Days").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vitals & Blood Pressure (AHA Ranges)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Generate & Share PDF Report").assertIsDisplayed()
    }

    @Test
    fun exportReportBottomSheet_switchesFormatAndInvokesCallback() {
        var generatedFormat: ReportFormat? = null
        var generatedRange: ReportTimeRange? = null
        var generatedConfig: ReportSectionConfig? = null

        composeTestRule.setContent {
            PulseTheme {
                ExportReportBottomSheet(
                    onDismiss = {},
                    onGenerateAndShare = { format, range, config, _ ->
                        generatedFormat = format
                        generatedRange = range
                        generatedConfig = config
                    }
                )
            }
        }

        // Switch format to CSV Bundle
        composeTestRule.onNodeWithText("CSV Bundle").performClick()
        composeTestRule.onNodeWithText("Generate & Share CSV Bundle").assertIsDisplayed()

        // Switch range to 7 Days
        composeTestRule.onNodeWithText("7 Days").performClick()

        // Tap Generate & Share CTA
        composeTestRule.onNodeWithText("Generate & Share CSV Bundle").performClick()

        assertEquals(ReportFormat.CSV, generatedFormat)
        assertEquals(ReportTimeRange.LAST_7_DAYS, generatedRange)
        assertTrue(generatedConfig?.includeVitals == true)
        assertTrue(generatedConfig?.includeSleep == true)
    }
}
