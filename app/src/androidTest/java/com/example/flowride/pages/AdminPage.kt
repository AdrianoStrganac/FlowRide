package com.example.flowride.pages

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule

class AdminPage(private val composeTestRule: ComposeTestRule) {

    fun assertRentalDialogVisible() {
        composeTestRule.onNodeWithText("Podaci o rezervaciji").assertIsDisplayed()
    }

    fun assertRentalUser(name: String) {
        composeTestRule.onNodeWithText(name).assertIsDisplayed()
    }

    fun confirmRental() {
        composeTestRule.onNodeWithText("Potvrdi najam").performClick()
    }
}
