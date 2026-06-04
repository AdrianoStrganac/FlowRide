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
fun FlowRideBottomBar(navController: NavController, isLoggedIn: Boolean) {
    val items = listOf(
        BottomNavItem(Screen.Home, "Početna", Icons.Outlined.Home),
        BottomNavItem(Screen.Booking, "Rezerviraj", Icons.Outlined.AddCircleOutline),
        BottomNavItem(Screen.Locations, "Lokacije", Icons.Outlined.LocationOn),
        BottomNavItem(Screen.Rentals, "Moji najmi", Icons.Outlined.List),
    )
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = current == item.screen.route,
                onClick = { navController.navigate(item.screen.route) { launchSingleTop = true } },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}