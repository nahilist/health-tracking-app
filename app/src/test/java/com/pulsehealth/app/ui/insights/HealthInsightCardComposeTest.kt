package com.pulsehealth.app.ui.insights

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pulsehealth.app.data.analytics.HealthInsight
import com.pulsehealth.app.data.analytics.InsightCategory
import com.pulsehealth.app.data.analytics.InsightSeverity
import com.pulsehealth.app.data.analytics.InsightType
import com.pulsehealth.app.ui.theme.PulseTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h900dp")
class HealthInsightCardComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun healthInsightCard_rendersTitleSeverityAndComparisonValues() {
        val insight = HealthInsight(
            id = "test-rhr-spike",
            type = InsightType.RESTING_HEART_RATE_SPIKE,
            severity = InsightSeverity.ATTENTION,
            category = InsightCategory.CARDIO,
            title = "Elevated Resting Heart Rate",
            description = "Your resting heart rate today (78 bpm) is 12 bpm above baseline.",
            recommendation = "Common non-clinical wellness factors include stress or dehydration. Prioritize rest.",
            metricType = "Resting heart rate",
            currentValueText = "78 bpm",
            baselineValueText = "66 bpm",
            deltaPercentage = 18
        )

        composeTestRule.setContent {
            PulseTheme {
                HealthInsightCard(insight = insight)
            }
        }

        composeTestRule.onNodeWithText("ATTENTION").assertIsDisplayed()
        composeTestRule.onNodeWithText("Elevated Resting Heart Rate").assertIsDisplayed()
        composeTestRule.onNodeWithText("78 bpm").assertIsDisplayed()
        composeTestRule.onNodeWithText("66 bpm").assertIsDisplayed()
        composeTestRule.onNodeWithText("+18%").assertIsDisplayed()
    }

    @Test
    fun healthInsightCard_expandsRecommendationOnClick() {
        val insight = HealthInsight(
            id = "test-sleep-recovery",
            type = InsightType.SLEEP_RESTORATIVE_HIGH,
            severity = InsightSeverity.POSITIVE,
            category = InsightCategory.RECOVERY,
            title = "High Restorative Sleep",
            description = "Deep sleep accounted for 28% of your night.",
            recommendation = "Optimal deep sleep enhances cellular repair.",
            metricType = "Sleep",
            currentValueText = "28% deep sleep",
            baselineValueText = "15-20% avg",
            deltaPercentage = 28
        )

        composeTestRule.setContent {
            PulseTheme {
                HealthInsightCard(insight = insight)
            }
        }

        composeTestRule.onNodeWithText("OPTIMAL").assertIsDisplayed()
        composeTestRule.onNodeWithText("High Restorative Sleep").assertIsDisplayed()

        // Tap card to expand recommendation
        composeTestRule.onNodeWithText("High Restorative Sleep").performClick()

        composeTestRule.onNodeWithText("Actionable Suggestion").assertIsDisplayed()
        composeTestRule.onNodeWithText("Optimal deep sleep enhances cellular repair.").assertIsDisplayed()
    }

    @Test
    fun insightsSection_rendersEmptyStateWhenNoInsights() {
        composeTestRule.setContent {
            PulseTheme {
                InsightsSection(insights = emptyList())
            }
        }

        composeTestRule.onNodeWithText("Health Insights & Trends").assertIsDisplayed()
        composeTestRule.onNodeWithText("All Metrics in Baseline Range").assertIsDisplayed()
    }

    @Test
    fun insightsSection_rendersCardsWithActiveCountBadge() {
        val insights = listOf(
            HealthInsight(
                id = "ins-1",
                type = InsightType.HYDRATION_STREAK,
                severity = InsightSeverity.POSITIVE,
                category = InsightCategory.HYDRATION,
                title = "3-Day Hydration Streak",
                description = "You've met your water goal for 3 days in a row.",
                recommendation = "Keep up the momentum!",
                metricType = "Hydration",
                currentValueText = "3 days",
                baselineValueText = "Target: 2.0L"
            )
        )

        composeTestRule.setContent {
            PulseTheme {
                InsightsSection(insights = insights)
            }
        }

        composeTestRule.onNodeWithText("1 Active").assertIsDisplayed()
        composeTestRule.onNodeWithText("3-Day Hydration Streak").assertIsDisplayed()
    }
}
