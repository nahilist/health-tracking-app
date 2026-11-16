package com.pulsehealth.app.ui.components

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
class NetworkStatusPillComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysOfflineWarning_whenDisconnected() {
        composeTestRule.setContent {
            PulseTheme {
                NetworkStatusPill(
                    isOnline = false,
                    isSyncing = false
                )
            }
        }

        composeTestRule.onNodeWithText("Offline • Local records safe").assertIsDisplayed()
    }

    @Test
    fun displaysSyncingPill_whenActiveCloudSync() {
        composeTestRule.setContent {
            PulseTheme {
                NetworkStatusPill(
                    isOnline = true,
                    isSyncing = true
                )
            }
        }

        composeTestRule.onNodeWithText("Syncing with cloud...").assertIsDisplayed()
    }
}
