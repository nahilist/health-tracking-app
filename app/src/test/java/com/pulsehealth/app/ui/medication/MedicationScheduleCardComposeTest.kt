package com.pulsehealth.app.ui.medication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pulsehealth.app.data.local.MedicationEntity
import com.pulsehealth.app.data.medication.DoseStatus
import com.pulsehealth.app.data.medication.MedicationAdherenceSummary
import com.pulsehealth.app.data.medication.ScheduledDoseItem
import com.pulsehealth.app.ui.theme.PulseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MedicationScheduleCardComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_rendersFriendlyMessageAndAddButton() {
        var addClicked = false

        composeTestRule.setContent {
            PulseTheme {
                MedicationScheduleCard(
                    schedule = emptyList(),
                    adherence = MedicationAdherenceSummary(0, 0, 0, 100, 0),
                    onTakeDose = { _, _ -> },
                    onSkipDose = { _, _ -> },
                    onUndoDose = { _, _ -> },
                    onAddMedicationClick = { addClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("medication_schedule_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Daily Medications").assertIsDisplayed()
        composeTestRule.onNodeWithText("No medications scheduled for today").assertIsDisplayed()

        composeTestRule.onNodeWithTag("add_medication_button").performClick()
        assertTrue("Add medication callback should be invoked", addClicked)
    }

    @Test
    fun scheduledDoses_rendersMedicationNameDosageAndTime() {
        val testMed = MedicationEntity(
            id = "med_1",
            name = "Vitamin D3",
            dosage = 2000.0,
            unit = "IU",
            frequency = "DAILY",
            scheduledTimes = "08:00",
            instructions = "With breakfast",
            colorHex = "#0D5C58",
            isActive = true,
            createdAtEpochMillis = 1000L
        )

        val item = ScheduledDoseItem(
            medication = testMed,
            scheduledTimeEpochMillis = 1731600000000L,
            timeLabel = "08:00 AM",
            status = DoseStatus.PENDING
        )

        var takenMedId = ""

        composeTestRule.setContent {
            PulseTheme {
                MedicationScheduleCard(
                    schedule = listOf(item),
                    adherence = MedicationAdherenceSummary(1, 0, 0, 85, 3),
                    onTakeDose = { id, _ -> takenMedId = id },
                    onSkipDose = { _, _ -> },
                    onUndoDose = { _, _ -> },
                    onAddMedicationClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Daily Medications").assertIsDisplayed()
        composeTestRule.onNodeWithText("85% adherence · 3d streak 🔥").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vitamin D3").assertIsDisplayed()
        composeTestRule.onNodeWithText("2000 IU · With breakfast").assertIsDisplayed()
        composeTestRule.onNodeWithText("08:00 AM").assertIsDisplayed()

        // Perform click on Take
        composeTestRule.onNodeWithTag("take_dose_med_1").performClick()
        assertEquals("med_1", takenMedId)
    }

    @Test
    fun takenBadge_rendersWithUndoOption() {
        val testMed = MedicationEntity(
            id = "med_2",
            name = "Magnesium",
            dosage = 250.0,
            unit = "mg",
            frequency = "DAILY",
            scheduledTimes = "21:00",
            colorHex = "#7C3AED",
            isActive = true,
            createdAtEpochMillis = 1000L
        )

        val item = ScheduledDoseItem(
            medication = testMed,
            scheduledTimeEpochMillis = 1731600000000L,
            timeLabel = "09:00 PM",
            status = DoseStatus.TAKEN
        )

        var undoMedId = ""

        composeTestRule.setContent {
            PulseTheme {
                MedicationScheduleCard(
                    schedule = listOf(item),
                    adherence = MedicationAdherenceSummary(1, 1, 0, 100, 5),
                    onTakeDose = { _, _ -> },
                    onSkipDose = { _, _ -> },
                    onUndoDose = { id, _ -> undoMedId = id },
                    onAddMedicationClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("taken_badge_med_2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("taken_badge_med_2").performClick()
        assertEquals("med_2", undoMedId)
    }
}
