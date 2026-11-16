package com.pulsehealth.app.ui.summary

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.data.DataSource
import com.pulsehealth.app.data.TimeRange
import com.pulsehealth.app.data.WeeklyDigestCalculator
import com.pulsehealth.app.data.WeeklyWellnessDigest
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
class SummaryScreenComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun summaryScreen_rendersHeaderTimeRangeTabsAndProfileButton() {
        var profileClicked = false
        var selectedRange: TimeRange? = null

        val state = DashboardUiState(
            metrics = DashboardMetrics.demo(),
            selectedRange = TimeRange.TODAY,
            isOnline = true
        )

        composeTestRule.setContent {
            PulseTheme {
                SummaryScreen(
                    state = state,
                    contentPadding = PaddingValues(0.dp),
                    onRange = { selectedRange = it },
                    onEdit = {},
                    onProfile = { profileClicked = true },
                    onDetail = {},
                    onExitDemo = {},
                    onRetry = {},
                    onDismissError = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today").assertIsDisplayed()
        composeTestRule.onNodeWithText("Week").assertIsDisplayed()
        composeTestRule.onNodeWithText("Month").assertIsDisplayed()

        // Tap Week time range pill
        composeTestRule.onNodeWithText("Week").performClick()
        assertEquals(TimeRange.WEEK, selectedRange)

        // Tap Profile button
        composeTestRule.onNodeWithContentDescription("Open profile and settings").performClick()
        assertTrue(profileClicked)
    }

    @Test
    fun summaryScreen_displaysOfflinePill_whenDeviceIsOffline() {
        val offlineState = DashboardUiState(
            metrics = DashboardMetrics(source = DataSource.LOCAL),
            isOnline = false,
            isCloudSyncing = false
        )

        composeTestRule.setContent {
            PulseTheme {
                SummaryScreen(
                    state = offlineState,
                    contentPadding = PaddingValues(0.dp),
                    onRange = {},
                    onEdit = {},
                    onProfile = {},
                    onDetail = {},
                    onExitDemo = {},
                    onRetry = {},
                    onDismissError = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Offline • Local records safe").assertIsDisplayed()
    }

    @Test
    fun weeklyDigestCard_rendersConsistencyScoreAndMilestone() {
        val digest = WeeklyDigestCalculator.calculateDemo()

        composeTestRule.setContent {
            PulseTheme {
                WeeklyDigestCard(digest = digest)
            }
        }

        composeTestRule.onNodeWithText("Weekly Wellness Digest").assertIsDisplayed()
        composeTestRule.onNodeWithText("${digest.consistencyScore}% Score").assertIsDisplayed()
        composeTestRule.onNodeWithText(digest.milestoneMessage).assertIsDisplayed()
    }
}
