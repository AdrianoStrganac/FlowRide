package com.example.flowride.tests

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.flowride.MainActivity
import com.example.flowride.pages.AuthPage
import com.example.flowride.pages.HomePage
import com.example.flowride.pages.ReservationPage
import com.example.flowride.pages.*
import com.example.flowride.data.UserRepository
import com.example.flowride.navigation.simulateScanAction
import com.example.flowride.data.RentalRepository
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * Automatski testovi za FlowRide aplikaciju.
 * Koristi Page Object Model (POM) i OOP principe.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class FlowRideE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val homePage = HomePage(composeTestRule)
    private val authPage = AuthPage(composeTestRule)
    private val reservationPage = ReservationPage(composeTestRule)
    private val profilePage = ProfilePage(composeTestRule)
    private val adminPage = AdminPage(composeTestRule)
    private val rentalsPage = RentalsPage(composeTestRule)

    private val testUser = "tester@flowride.com"
    private val testPass = "password123"

    private fun slowDown(millis: Long = 1000) {
        composeTestRule.waitForIdle()
        Thread.sleep(millis)
    }

    @Before
    fun setUp() {
        // No global setup that alters state between tests.
    }

    @Test
    fun test01_RegisterNewUser() {
        // Ensure starting clean for the first test run
        UserRepository.logout()
        
        homePage.clickProfileIcon()
        slowDown()
        
        // Wait for Auth Dialog
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Email").fetchSemanticsNodes().isNotEmpty()
        }
        
        authPage.switchToRegister()
        slowDown()
        authPage.register("Test Runner", testUser, "091234567", "Testing St 123")
        slowDown()
        
        // After registration, we are logged in.
        // Let's logout so test02 can test Login.
        homePage.clickProfileIcon()
        slowDown()
        profilePage.clickLogout()
        slowDown()
    }

    @Test
    fun test02_LoginFlow() {
        // Make sure we are at Home and logged out
        homePage.clickProfileIcon()
        slowDown()

        authPage.enterEmail(testUser)
        slowDown()
        authPage.enterPassword(testPass)
        slowDown()
        authPage.clickLogin()
        slowDown()
    }

    @Test
    fun test03_NavigateTabs() {
        homePage.navigateToTab("Lokacije")
        slowDown()
        homePage.navigateToTab("Moji najmi")
        slowDown()
        homePage.navigateToTab("Početna")
        slowDown()
    }

    @Test
    fun test04_ReservationProcess() {
        // Ensure logged in (from test02)
        homePage.navigateToTab("Rezerviraj")
        slowDown()
        
        // Choose a bike
        homePage.selectBike("Mountain Bike")
        slowDown()
        
        reservationPage.assertTitleVisible()
        slowDown()
        
        // Insert a date
        reservationPage.selectDate("15. 08. 2026.")
        slowDown()
        
        // Set at least 4 hours (default is 2, so click Add twice)
        reservationPage.setHours(4)
        slowDown()
        
        // Pick a pickup location
        reservationPage.selectLocation("Samobor - Trg")
        slowDown()
        
        // Choose payment method
        reservationPage.selectPaymentMethod("PayPal")
        slowDown()
        
        // Click rent bike (Potvrdi rezervaciju)
        reservationPage.confirmReservation()
        slowDown()
        
        // Success message should appear, click Great (Odlično!)
        reservationPage.clickSuccessOk()
        slowDown()
        
        // Go to reservations tab (Moji najmi)
        homePage.navigateToTab("Moji najmi")
        slowDown()
        
        // Look at that exact reservation confirming it is actually booked
        rentalsPage.assertRentalDetails("Mountain Bike", "Samobor - Trg")
        slowDown()
    }

    @Test
    fun test05_AdminScannerAccess() {
        // Logout user first
        homePage.clickProfileIcon()
        slowDown()
        profilePage.clickLogout()
        slowDown()

        homePage.clickProfileIcon()
        slowDown()
        authPage.enterEmail("admin@flowride.com")
        slowDown()
        authPage.enterPassword("admin123")
        slowDown()
        authPage.clickLogin()
        slowDown()
        
        // Admin should see scanner
        composeTestRule.onNodeWithContentDescription("Skeniraj").assertIsDisplayed()
        slowDown()
    }

    @Test
    fun test06_AdminConfirmRental() {
        // Ensure admin is logged in (from test05)
        // Simulate a scan of the first rental (use allRentalsForAdmin for scanning)
        val rentals = RentalRepository.allRentalsForAdmin
        if (rentals.isEmpty()) return // Should have at least the mock one
        
        val rentalId = rentals.first().id
        composeTestRule.runOnUiThread {
            simulateScanAction?.invoke(rentalId)
        }
        slowDown()

        // Verify dialog and confirm
        adminPage.assertRentalDialogVisible()
        slowDown()
        adminPage.confirmRental()
        slowDown()
        
        // Verify rental status updated
        assert(RentalRepository.allRentalsForAdmin.first { it.id == rentalId }.status == "completed")
    }

    @Test
    fun test07_LocationFiltering() {
        // Switch back to normal user
        homePage.clickProfileIcon()
        slowDown()
        profilePage.clickLogout()
        slowDown()

        homePage.clickProfileIcon()
        slowDown()
        authPage.enterEmail(testUser)
        slowDown()
        authPage.enterPassword(testPass)
        slowDown()
        authPage.clickLogin()
        slowDown()

        // Sljeme only allows 'classic'
        homePage.expandCategory("Klasični bicikli")
        slowDown()
        homePage.selectBike("Mountain Bike")
        slowDown()
        
        // Open location dropdown
        composeTestRule.onNode(hasText("Central Park Station") and hasClickAction()).performClick()
        slowDown()
        // Sljeme should be visible
        composeTestRule.onNodeWithText("Sljeme - Vrh").assertExists()
        slowDown()
        
        // Cancel and go back
        composeTestRule.onNodeWithText("Odustani").performClick()
        slowDown()
        homePage.navigateToTab("Početna")
        slowDown()

        // E-Bicikli - Sljeme should NOT be visible
        homePage.expandCategory("E-Bicikli")
        slowDown()
        homePage.selectBike("E-City Cruiser")
        slowDown()
        
        composeTestRule.onNode(hasText("Central Park Station") and hasClickAction()).performClick()
        slowDown()
        composeTestRule.onNodeWithText("Sljeme - Vrh").assertDoesNotExist()
        slowDown()
    }

    @Test
    fun test08_DeleteAccount() {
        // Navigate to Profile
        homePage.clickProfileIcon()
        slowDown()
        profilePage.assertUserEmail(testUser)
        slowDown()

        // Delete account
        profilePage.clickDeleteAccount()
        slowDown()
        profilePage.confirmDelete()
        slowDown()

        // Should be logged out and back to Home
        homePage.assertHeroVisible()
        assert(UserRepository.currentUser == null)
    }
}
