package com.example.flowride.pages

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule

class ReservationPage(private val composeTestRule: ComposeTestRule) {

    fun assertTitleVisible() {
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Rezervacija").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Rezervacija").assertIsDisplayed()
    }

    fun selectDate(date: String = "15. 08. 2026.") {
        // Clear and enter the new date
        composeTestRule.onNode(hasTestTag("date_input_field")).performTextReplacement(date)
    }

    fun setHours(targetHours: Int) {
        // Current default is 2. Let's use the Add button until we reach target.
        // Or just click the Add button 2 times to get to 4.
        repeat(targetHours - 2) {
            composeTestRule.onNodeWithContentDescription("Add").performClick()
        }
    }

    fun selectLocation(locationName: String) {
        // The dropdown shows the current location. Click it to open.
        // We use onFirst() in case multiple nodes match
        composeTestRule.onAllNodes(hasText("Central Park Station") and hasClickAction()).onFirst().performClick()
        // Wait for and select item
        composeTestRule.onNodeWithText(locationName).performClick()
    }

    fun selectPaymentMethod(methodLabel: String) {
        composeTestRule.onNodeWithText(methodLabel).performClick()
    }

    fun confirmReservation() {
        composeTestRule.onNodeWithText("Potvrdi rezervaciju").performScrollTo().performClick()
    }

    fun clickSuccessOk() {
        // If the success dialog doesn't appear, check if there's a validation error
        try {
            composeTestRule.waitUntil(5000) {
                composeTestRule.onAllNodes(hasTestTag("success_ok_button")).fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: Exception) {
            // Check for date error message
            if (composeTestRule.onAllNodesWithText("Molimo odaberite datum danas ili u budućnosti.").fetchSemanticsNodes().isNotEmpty()) {
                throw AssertionError("Reservation failed: Date validation error displayed.")
            }
            throw e
        }
        composeTestRule.onNode(hasTestTag("success_ok_button")).performClick()
    }
}
