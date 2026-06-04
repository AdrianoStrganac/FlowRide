package com.example.flowride.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.flowride.screens.*
import com.example.flowride.components.AuthDialog
import com.example.flowride.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import com.example.flowride.data.UserRepository
import com.example.flowride.utils.ScannerUtils
import androidx.compose.ui.platform.LocalContext
import com.example.flowride.data.RentalRepository
import com.example.flowride.data.ActiveRental
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Rentals : Screen("rentals")
    object Locations : Screen("locations")
    object Booking : Screen("booking_tab")
    object Profile : Screen("profile")
    object Reservation : Screen("reservation/{bikeId}") {
        fun createRoute(bikeId: String) = "reservation/$bikeId"
    }
}

// Test-only hook to simulate scans
var simulateScanAction: ((String) -> Unit)? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowRideNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val currentUser = UserRepository.currentUser
    val isLoggedIn = currentUser != null
    val isAdmin = currentUser?.isAdmin ?: false
    
    val homeListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    var showAuthDialog by remember { mutableStateOf(false) }
    var authMode by remember { mutableStateOf("login") }
    var allowDismissOutside by remember { mutableStateOf(true) }
    var isFromRentalsTab by remember { mutableStateOf(false) }
    var scannedRental by remember { mutableStateOf<ActiveRental?>(null) }

    // Register test hook
    LaunchedEffect(Unit) {
        simulateScanAction = { result ->
            val rental = RentalRepository.allRentalsForAdmin.find { it.id == result }
            scannedRental = rental
        }
    }

    if (showAuthDialog) {
        AuthDialog(
            mode = authMode,
            allowDismissOutside = allowDismissOutside,
            onDismiss = { 
                showAuthDialog = false 
                if (isFromRentalsTab) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            },
            onSuccess = { showAuthDialog = false },
            onSwitchMode = { authMode = if (authMode == "login") "register" else "login" }
        )
    }

    if (scannedRental != null) {
        ScannedRentalDialog(
            rental = scannedRental!!,
            onDismiss = { scannedRental = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FlowRide",
                        style = MaterialTheme.typography.titleLarge,
                        color = Primary,
                        modifier = Modifier.clickable {
                            coroutineScope.launch {
                                homeListState.animateScrollToItem(0)
                            }
                        }
                    )
                },
                navigationIcon = {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = Primary,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌿", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = {
                            ScannerUtils.startScanner(context) { result ->
                        if (result != null) {
                            val rental = RentalRepository.allRentalsForAdmin.find { it.id == result }
                            scannedRental = rental
                        }
                    }
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.QrCodeScanner,
                                contentDescription = "Skeniraj",
                                tint = Primary
                            )
                        }
                    }
                    IconButton(onClick = {
                        if (isLoggedIn) {
                            navController.navigate(Screen.Profile.route)
                        } else {
                            authMode = "login"
                            allowDismissOutside = true
                            isFromRentalsTab = false
                            showAuthDialog = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profil",
                            tint = Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = Primary
                )
            )
        },
        bottomBar = {
            FlowRideBottomBar(
                navController = navController,
                isLoggedIn = isLoggedIn
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    isLoggedIn = isLoggedIn,
                    onLoginRequired = { 
                        authMode = "login"
                        allowDismissOutside = true
                        isFromRentalsTab = false
                        showAuthDialog = true 
                    },
                    onBikeSelected = { bikeId ->
                        navController.navigate(Screen.Reservation.createRoute(bikeId))
                    },
                    listState = homeListState
                )
            }
            composable(Screen.Rentals.route) {
                if (isLoggedIn) {
                    RentalsScreen()
                } else {
                    LaunchedEffect(Unit) {
                        authMode = "login"
                        allowDismissOutside = false
                        isFromRentalsTab = true
                        showAuthDialog = true
                    }
                }
            }
            composable(Screen.Locations.route) {
                LocationsScreen()
            }
            composable(Screen.Booking.route) {
                if (isLoggedIn) {
                    ReservationScreen(
                        bikeId = null,
                        onComplete = {
                            navController.popBackStack(Screen.Home.route, inclusive = false)
                        },
                        onBack = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        authMode = "login"
                        allowDismissOutside = false
                        isFromRentalsTab = true // reusing the same logic to force return to home on cancel
                        showAuthDialog = true
                    }
                }
            }
            composable(Screen.Profile.route) {
                if (isLoggedIn) {
                    ProfileScreen(
                        onLogout = {
                            UserRepository.logout()
                            navController.popBackStack(Screen.Home.route, inclusive = false)
                        },
                        onDeleteAccount = {
                            UserRepository.deleteAccount()
                            navController.popBackStack(Screen.Home.route, inclusive = false)
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                }
            }
            composable(Screen.Reservation.route) { backStackEntry ->
                val bikeId = backStackEntry.arguments?.getString("bikeId")
                ReservationScreen(
                    bikeId = bikeId,
                    onComplete = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun ScannedRentalDialog(rental: ActiveRental, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = PrimaryLight,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.CheckCircle, null, tint = Primary, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Podaci o rezervaciji", style = MaterialTheme.typography.headlineSmall)
                Text("Rezervacija je valjana i potvrđena", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                
                Spacer(Modifier.height(24.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScannedInfoRow("Vozilo", rental.vehicleType)
                    ScannedInfoRow("ID Najma", rental.id)
                    ScannedInfoRow("Korisnik", rental.userName)
                    ScannedInfoRow("Email", rental.userEmail)
                    ScannedInfoRow("Trajanje", rental.duration)
                    ScannedInfoRow("Status plaćanja", "Plaćeno (${rental.paymentMethod})")
                    ScannedInfoRow("Lokacija", rental.pickupLocation)
                }
                
                Spacer(Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        RentalRepository.confirmRental(rental.id)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Potvrdi najam")
                }
            }
        }
    }
}

@Composable
fun ScannedInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
