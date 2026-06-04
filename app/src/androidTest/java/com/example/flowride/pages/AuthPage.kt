package com.example.flowride.pages

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule

class AuthPage(private val composeTestRule: ComposeTestRule) {

    fun assertIsVisible() {
        composeTestRule.onNodeWithText("Dobrodošli natrag").assertIsDisplayed()
    }

    fun enterEmail(email: String) {
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Email") and hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasText("Email") and hasSetTextAction()).performTextInput(email)
    }

    fun enterPassword(password: String) {
        composeTestRule.onNode(hasText("Lozinka") and hasSetTextAction()).performTextInput(password)
    }

    fun clickLogin() {
        composeTestRule.onNode(hasText("Prijava") and hasClickAction()).performClick()
        // Wait for the dialog to close
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Dobrodošli natrag").fetchSemanticsNodes().isEmpty()
        }
    }

    fun switchToRegister() {
        composeTestRule.onNodeWithText("Nemaš račun? Registriraj se").performClick()
    }

    fun register(name: String, email: String, phone: String, address: String) {
        composeTestRule.onNode(hasText("Puno ime") and hasSetTextAction()).performTextInput(name)
        composeTestRule.onNode(hasText("Email") and hasSetTextAction()).performTextInput(email)
        composeTestRule.onNode(hasText("Broj mobitela") and hasSetTextAction()).performTextInput(phone)
        composeTestRule.onNode(hasText("Adresa stanovanja") and hasSetTextAction()).performTextInput(address)
        composeTestRule.onNode(hasText("Lozinka") and hasSetTextAction()).performTextInput("password123")
        
        // Use hasClickAction to distinguish button from dialog title
        composeTestRule.onNode(hasText("Kreiraj račun") and hasClickAction()).performClick()
    }
}
