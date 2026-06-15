package com.example.flowride.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.flowride.components.BikeCardFirestore
import com.example.flowride.components.BookingForm
import com.example.flowride.data.BikeModelFirestore
import com.example.flowride.data.VehicleRepository
import com.example.flowride.ui.theme.Primary

@Composable
fun ReservationScreen(bikeId: String?, onComplete: () -> Unit, onBack: () -> Unit) {
    var selectedBikeId by remember { mutableStateOf(bikeId) }
    val scrollState = rememberScrollState()

    val categories = VehicleRepository.categories
    val allVehicles = VehicleRepository.vehicles
    val bike: BikeModelFirestore? = allVehicles.find { it.id == selectedBikeId }

    LaunchedEffect(selectedBikeId) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(8.dp)
    ) {
        if (bike != null) {
            BookingForm(
                bike = bike,
                onClose = {
                    if (bikeId == null) {
                        selectedBikeId = null
                    } else {
                        onBack()
                    }
                },
                onSuccess = onComplete
            )
        } else {
            Text(
                "Odaberi vozilo za rezervaciju",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                categories.forEach { category ->
                    // Dohvati samo dostupna vozila za običnog korisnika
                    val models = VehicleRepository.getVehiclesForCategory(category.id, onlyAvailable = true)
                    
                    if (models.isNotEmpty()) {
                        Text(
                            category.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        models.forEach { model ->
                            BikeCardFirestore(
                                bike = model,
                                onClick = { selectedBikeId = model.id }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Povratak")
            }
        }
    }
}