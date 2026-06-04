package com.example.flowride.pages

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule

class ProfilePage(private val composeTestRule: ComposeTestRule) {

    fun assertUserEmail(email: String) {
        composeTestRule.onNodeWithText(email).assertIsDisplayed()
    }

    fun clickLogout() {
        // Wait for screen to load
        composeTestRule.waitUntil(3000) {
            composeTestRule.onAllNodesWithText("Odjava").fetchSemanticsNodes().isNotEmpty()
        }
        // Try to click without scroll first, if fails, try scroll
        try {
            composeTestRule.onNodeWithText("Odjava").performClick()
        } catch (e: Exception) {
            composeTestRule.onNodeWithText("Odjava").performScrollTo().performClick()
        }
    }

    fun clickDeleteAccount() {
        try {
            composeTestRule.onNodeWithText("Obriši račun").performClick()
        } catch (e: Exception) {
            composeTestRule.onNodeWithText("Obriši račun").performScrollTo().performClick()
        }
    }

    fun confirmDelete() {
        composeTestRule.onNodeWithText("Obriši").performClick()
    }
}
