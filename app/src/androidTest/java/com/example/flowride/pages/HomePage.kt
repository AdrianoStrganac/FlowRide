package com.example.flowride.pages

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule

class HomePage(private val composeTestRule: ComposeTestRule) {

    fun expandCategory(categoryName: String) {
        composeTestRule.onNodeWithText(categoryName).performClick()
        // Wait for models to be visible
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Odaberi").fetchSemanticsNodes().isNotEmpty()
        }
    }

    fun selectBike(bikeName: String) {
        // Find the specific bike ID based on the name
        val bikeId = when(bikeName) {
            "Mountain Bike" -> "classic_mtb"
            "Gradski Road Bike" -> "classic_road"
            "Gravel Bike" -> "classic_gravel"
            "E-City Cruiser" -> "ebike_city"
            "E-Cargo Bike" -> "ebike_cargo"
            "Scooter Pro" -> "scooter_pro"
            "Scooter Lite" -> "scooter_lite"
            else -> ""
        }
        
        if (bikeId.isNotEmpty()) {
            composeTestRule.onNode(hasTestTag("select_button_$bikeId")).performClick()
        } else {
            // Fallback to finding by ancestor if ID is unknown
            composeTestRule.onAllNodesWithText("Odaberi").onFirst().performClick()
        }
    }

    fun clickProfileIcon() {
        composeTestRule.onNodeWithContentDescription("Profil").performClick()
    }
    
    fun navigateToTab(tabName: String) {
        composeTestRule.onNodeWithText(tabName).performClick()
    }

    fun assertHeroVisible() {
        // Use wait to allow navigation to complete
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Istraži prirodu na dva kotača").fetchSemanticsNodes().isNotEmpty()
        }
        // Just assert it exists, as it might be partially obscured if scrolled
        composeTestRule.onNodeWithText("Istraži prirodu na dva kotača").assertExists()
    }
}
