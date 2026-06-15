package com.example.flowride.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

@Composable
fun FlowRideBottomBar(
    navController: NavController,
    isLoggedIn: Boolean,
    isAdmin: Boolean = false,
    onHomeReselected: () -> Unit = {}
) {
    val baseItems = listOf(
        BottomNavItem(Screen.Home, "Početna", Icons.Outlined.Home),
        BottomNavItem(Screen.Locations, "Lokacije", Icons.Outlined.LocationOn),
        BottomNavItem(Screen.Booking, "Rezerviraj", Icons.Outlined.CalendarMonth),
        BottomNavItem(Screen.Rentals, "Moji najmi", Icons.Outlined.List),
    )

    val items = if (isAdmin) {
        baseItems + BottomNavItem(Screen.Admin, "Admin", Icons.Outlined.AdminPanelSettings)
    } else {
        baseItems
    }

    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = current == item.screen.route,
                onClick = {
                    if (current == item.screen.route) {
                        if (item.screen == Screen.Home) onHomeReselected()
                    } else {
                        navController.navigate(item.screen.route) { launchSingleTop = true }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}