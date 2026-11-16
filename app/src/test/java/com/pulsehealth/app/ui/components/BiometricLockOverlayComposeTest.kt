package com.pulsehealth.app.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pulsehealth.app.ui.theme.PulseTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BiometricLockOverlayComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun lockOverlay_rendersTitleDescriptionAndTriggersAutoUnlock() {
        var autoTriggerCount = 0

        composeTestRule.setContent {
            PulseTheme {
                BiometricLockOverlay(
                    onTriggerUnlock = { autoTriggerCount++ }
                )
            }
        }

        composeTestRule.onNodeWithText("Pulse Health Locked").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Biometric or device screen lock is required to access your private health metrics and daily records."
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("Unlock Health Vault").assertIsDisplayed()

        // LaunchedEffect triggers auto unlock on mount
        assertTrue(autoTriggerCount >= 1)
    }

    @Test
    fun clickingUnlockHealthVaultButton_invokesUnlockCallback() {
        var unlockClickedCount = 0

        composeTestRule.setContent {
            PulseTheme {
                BiometricLockOverlay(
                    onTriggerUnlock = { unlockClickedCount++ }
                )
            }
        }

        val initialCount = unlockClickedCount
        composeTestRule.onNodeWithText("Unlock Health Vault").performClick()
        assertTrue(unlockClickedCount > initialCount)
    }
}
