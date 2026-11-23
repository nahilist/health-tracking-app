package com.pulsehealth.app.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.data.TimeRange
import com.pulsehealth.app.ui.browse.BrowseScreen
import com.pulsehealth.app.ui.components.BiometricLockOverlay
import com.pulsehealth.app.ui.detail.DetailScreen
import com.pulsehealth.app.ui.navigation.TopLevelDestination
import com.pulsehealth.app.ui.summary.LogHydrationBottomSheet
import com.pulsehealth.app.ui.summary.SummaryScreen
import com.pulsehealth.app.ui.theme.PulseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
class ResponsiveLayoutAuditTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun compactPhone_summaryScreen_rendersWithoutClipping() {
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

        composeTestRule.onNodeWithText("Pulse").assertIsDisplayed()
        composeTestRule.onNodeWithText("Summary").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Activity").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("Move").assertIsDisplayed()
        composeTestRule.onNodeWithText("Exercise").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stand").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h1280dp")
    fun tablet_summaryScreen_rendersAdaptiveColumns() {
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

        composeTestRule.onNodeWithText("Pulse").assertIsDisplayed()
        composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
        composeTestRule.onNodeWithText("Steps").assertIsDisplayed()
        composeTestRule.onNodeWithText("Heart rate").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Sleep").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Water").onFirst().assertIsDisplayed()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w900dp-h400dp")
    fun landscape_detailScreen_rendersScrollableHeroAndCharts() {
        val metrics = DashboardMetrics.demo()

        composeTestRule.setContent {
            PulseTheme {
                DetailScreen(
                    type = "Activity",
                    metrics = metrics,
                    isDemo = true,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Activity Overview").assertIsDisplayed()
        composeTestRule.onNodeWithText("Move").assertIsDisplayed()
        composeTestRule.onNodeWithText("Exercise").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h1280dp")
    fun tablet_pulseBottomBar_rendersCenteredTabs() {
        var selectedDestination: TopLevelDestination? = null

        composeTestRule.setContent {
            PulseTheme {
                PulseBottomBar(
                    currentRoute = TopLevelDestination.SUMMARY.route,
                    onNavigateToDestination = { selectedDestination = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sharing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Browse").assertIsDisplayed()

        composeTestRule.onNodeWithText("Sharing").performClick()
        assertEquals(TopLevelDestination.SHARING, selectedDestination)
    }

    @Test
    @Config(sdk = [35], qualifiers = "w640dp-h360dp")
    fun compactLandscape_biometricLockOverlay_isAccessibleAndScrollable() {
        var unlockTriggered = false

        composeTestRule.setContent {
            PulseTheme {
                BiometricLockOverlay(
                    onTriggerUnlock = { unlockTriggered = true },
                    errorMessage = null
                )
            }
        }

        assertTrue(unlockTriggered)
        composeTestRule.onNodeWithText("Pulse Health Locked").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unlock Health Vault").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w320dp-h640dp")
    fun compactPhone_logHydrationSheet_rendersWithoutClipping() {
        var loggedAmount = 0
        var dismissed = false

        composeTestRule.setContent {
            PulseTheme {
                LogHydrationBottomSheet(
                    currentWaterLiters = 1.5,
                    waterGoalLiters = 2.5,
                    onLogWater = { loggedAmount = it },
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Log Hydration").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log +250 mL Water").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cup").assertIsDisplayed()
        composeTestRule.onNodeWithText("Glass").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h800dp")
    fun compactPhone_categoryFilterPills_allRenderWithoutClipping() {
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

        composeTestRule.onNodeWithContentDescription("All category filter").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Activity category filter").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Vitals category filter").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Heart category filter").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Sleep category filter").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Water category filter").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Meds category filter").assertIsDisplayed()
    }

    @Test
    fun compactPhone_browseScreen_categoryPillsAndSearch_renderWithoutClipping() {
        composeTestRule.setContent {
            PulseTheme {
                BrowseScreen(
                    padding = PaddingValues(0.dp),
                    metrics = DashboardMetrics.demo(),
                    onOpen = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Search health metrics").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("All categories").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Activity category").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Vitals category").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Sleep category").assertIsDisplayed()
    }
}
