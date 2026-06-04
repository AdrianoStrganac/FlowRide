package com.example.flowride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.flowride.navigation.FlowRideNavGraph
import com.example.flowride.ui.theme.FlowRideTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.flowride.data.UserRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        UserRepository.init(this)
        com.example.flowride.data.RentalRepository.init(this)

        enableEdgeToEdge()
        setContent {
            FlowRideTheme {
                FlowRideNavGraph()
            }
        }
    }
}