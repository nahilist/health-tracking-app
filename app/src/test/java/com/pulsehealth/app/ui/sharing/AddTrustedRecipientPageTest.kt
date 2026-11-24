package com.pulsehealth.app.ui.sharing

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.pulsehealth.app.data.ShareDuration
import com.pulsehealth.app.data.ShareRelationship
import com.pulsehealth.app.data.TrustedContact
import com.pulsehealth.app.ui.theme.PulseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h2200dp")
class AddTrustedRecipientPageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addTrustedRecipientContent_rendersAppleHeaderAndAllSections() {
        composeTestRule.setContent {
            PulseTheme {
                AddTrustedRecipientContent(
                    onDismiss = {},
                    onConfirm = { _, _, _, _, _ -> }
                )
            }
        }

        // Header & Hero
        composeTestRule.onNodeWithText("Share Health Data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Share with Someone").assertIsDisplayed()

        // Section 1: Recipient
        composeTestRule.onNodeWithText("RECIPIENT").assertIsDisplayed()
        composeTestRule.onNodeWithText("Recipient Name *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Healthcare provider").assertIsDisplayed()
        composeTestRule.onNodeWithText("Family member").assertIsDisplayed()

        // Section 2: Permissions
        composeTestRule.onAllNodesWithText("HEALTH DATA PERMISSIONS", substring = true).onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("Steps").assertIsDisplayed()
        composeTestRule.onNodeWithText("Heart Rate").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sleep & Rest").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hydration Intake").assertIsDisplayed()

        // Section 3: Duration
        composeTestRule.onNodeWithText("ACCESS DURATION").assertIsDisplayed()
        composeTestRule.onNodeWithText("30 Days").assertIsDisplayed()
        composeTestRule.onNodeWithText("RECOMMENDED").assertIsDisplayed()

        // Privacy card
        composeTestRule.onNodeWithText("Private & End-to-End Encrypted").assertIsDisplayed()

        // Sticky button (disabled initially since name is empty)
        composeTestRule.onNodeWithContentDescription("Authorize and save trusted recipient").assertIsNotEnabled()
    }

    @Test
    fun addTrustedRecipientContent_authorizesSuccessfullyWithSelectedInputs() {
        var confirmedName: String? = null
        var confirmedRelationship: ShareRelationship? = null
        var confirmedContactId: String? = null
        var confirmedMetrics: Set<String>? = null
        var confirmedDuration: ShareDuration? = null

        composeTestRule.setContent {
            PulseTheme {
                AddTrustedRecipientContent(
                    onDismiss = {},
                    onConfirm = { name, rel, contactId, metrics, duration ->
                        confirmedName = name
                        confirmedRelationship = rel
                        confirmedContactId = contactId
                        confirmedMetrics = metrics
                        confirmedDuration = duration
                    }
                )
            }
        }

        // Enter Name
        composeTestRule.onNodeWithText("e.g. Dr. Sarah Chen or Mom")
            .performTextInput("Dr. House")

        // Select Relationship: Family member
        composeTestRule.onNodeWithText("Family member").performClick()

        // Select Duration: Until revoked
        composeTestRule.onNodeWithText("Until revoked").performClick()

        // Authorize button should now be enabled
        val authorizeButton = composeTestRule.onNodeWithContentDescription("Authorize and save trusted recipient")
        authorizeButton.assertIsEnabled()
        authorizeButton.performClick()

        assertEquals("Dr. House", confirmedName)
        assertEquals(ShareRelationship.FAMILY, confirmedRelationship)
        assertNotNull(confirmedMetrics)
        val metrics = confirmedMetrics ?: emptySet()
        assertTrue(metrics.contains("Steps"))
        assertTrue(metrics.contains("Heart rate"))
        assertEquals(ShareDuration.INDEFINITE, confirmedDuration)
    }

    @Test
    fun addTrustedRecipientContent_quickActionButtons_modifySelection() {
        composeTestRule.setContent {
            PulseTheme {
                AddTrustedRecipientContent(
                    onDismiss = {},
                    onConfirm = { _, _, _, _, _ -> }
                )
            }
        }

        // Click "Clear"
        composeTestRule.onNodeWithText("Clear").performClick()
        composeTestRule.onAllNodesWithText("HEALTH DATA PERMISSIONS (0)").onFirst().assertIsDisplayed()

        // Click "All"
        composeTestRule.onNodeWithText("All").performClick()
        composeTestRule.onAllNodesWithText("HEALTH DATA PERMISSIONS (8)").onFirst().assertIsDisplayed()

        // Click "Essential"
        composeTestRule.onNodeWithText("Essential").performClick()
        composeTestRule.onAllNodesWithText("HEALTH DATA PERMISSIONS (4)").onFirst().assertIsDisplayed()
    }

    @Test
    fun editMetricsBottomSheet_updatesPermissionsAndSaves() {
        val contact = TrustedContact.create(
            name = "Dr. Lisa Cuddy",
            relationship = ShareRelationship.DOCTOR,
            contactIdentifier = "cuddy@princeton.org",
            sharedMetrics = setOf("Heart rate", "Steps"),
            duration = ShareDuration.ONE_MONTH
        )

        var savedMetrics: Set<String>? = null

        composeTestRule.setContent {
            PulseTheme {
                EditMetricsBottomSheet(
                    contact = contact,
                    onDismiss = {},
                    onSave = { savedMetrics = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Edit Permissions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dr. Lisa Cuddy").assertIsDisplayed()
        composeTestRule.onNodeWithText("AUTHORIZED METRICS (2)").assertIsDisplayed()

        // Click "All"
        composeTestRule.onNodeWithText("All").performClick()
        composeTestRule.onNodeWithText("AUTHORIZED METRICS (8)").assertIsDisplayed()

        // Save
        composeTestRule.onNodeWithText("Save Permissions").performClick()

        assertNotNull(savedMetrics)
        val updated = savedMetrics ?: emptySet()
        assertEquals(8, updated.size)
        assertTrue(updated.contains("Sleep"))
        assertTrue(updated.contains("Water"))
    }
}
