package com.example.flowride.pages

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule

class RentalsPage(private val composeTestRule: ComposeTestRule) {

    fun assertRentalExists(bikeName: String) {
        composeTestRule.onNodeWithText(bikeName).assertExists()
    }

    fun assertRentalDetails(bikeName: String, location: String) {
        // Find the card containing the bike name and check if it also has the location
        composeTestRule.onNode(
            hasText(bikeName) and hasAnyAncestor(hasAnyDescendant(hasText(location)))
        ).assertExists()
    }
}
