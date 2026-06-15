package com.example.flowride

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.flowride.data.RentalRepository
import com.example.flowride.data.UserRepository
import com.example.flowride.data.VehicleRepository
import com.example.flowride.navigation.FlowRideNavGraph
import com.example.flowride.ui.theme.FlowRideTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Zatraži dozvolu za notifikacije (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }

        lifecycleScope.launch {
            UserRepository.init()
            VehicleRepository.loadCategories()
            VehicleRepository.loadVehicles()
            VehicleRepository.startVehiclesListener()
            RentalRepository.loadRentals()
            RentalRepository.loadScannedRentals()
            isReady = true
        }


        enableEdgeToEdge()
        setContent {
            FlowRideTheme {
                FlowRideNavGraph()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        VehicleRepository.stopVehiclesListener()
    }
}