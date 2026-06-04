package com.example.flowride.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.*
import com.example.flowride.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

data class RentalLocation(
    val name: String,
    val description: String,
    val count: Int,
    val latLng: LatLng,
    val allowedBikeCategoryIds: List<String> = listOf("classic", "ebike", "scooter")
)

val rentalLocations = listOf(
    // Zagreb City
    RentalLocation("Central Park Station", "Centar grada", 12, LatLng(45.8150, 15.9819)),
    RentalLocation("Downtown Terminal", "Poslovni kvart", 8, LatLng(45.8120, 15.9750)),
    RentalLocation("Harbor Point", "Uz obalu", 6, LatLng(45.8200, 15.9900)),
    RentalLocation("University Campus", "Sveučilišno područje", 15, LatLng(45.8250, 15.9600)),
    
    // Sljeme (Mountain/Nature) - No scooters or city cruisers
    RentalLocation(
        "Sljeme - Vrh", 
        "Planinarski dom", 
        5, 
        LatLng(45.9000, 15.9480),
        allowedBikeCategoryIds = listOf("classic") // Only Mountain/Gravel bikes here
    ),
    
    // Samobor (Nature/City)
    RentalLocation(
        "Samobor - Trg", 
        "Nature & History", 
        10, 
        LatLng(45.8000, 15.7100),
        allowedBikeCategoryIds = listOf("classic", "ebike")
    ),

    // Jarun (Lake/Park)
    RentalLocation("Jarun Lake", "Rekreacijska zona", 20, LatLng(45.7780, 15.9180)),
    
    // Varaždin (Another city)
    RentalLocation("Varaždin Centar", "Barokni grad", 14, LatLng(46.3080, 16.3380)),
)

@Composable
fun LocationsScreen() {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(45.8150, 15.9819), 10f)
    }
    var mapTouched by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<RentalLocation?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !mapTouched
    ) {
        item(span = { GridItemSpan(2) }) {
            Column {
                Text(
                    "Naše lokacije",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    "Pronađi najbližu stanicu za preuzimanje vozila",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(MaterialTheme.shapes.large)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    mapTouched = event.changes.any { it.pressed }
                                }
                            }
                        },
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        scrollGesturesEnabled = true,
                        zoomGesturesEnabled = true,
                    )
                ) {
                    rentalLocations.forEach { loc ->
                        Marker(
                            state = MarkerState(position = loc.latLng),
                            title = loc.name,
                            snippet = "${loc.count} vozila"
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        items(rentalLocations) { loc ->
            LocationCard(
                loc = loc,
                isSelected = selectedLocation?.name == loc.name,
                onClick = {
                    selectedLocation = loc
                    coroutineScope.launch {
                        // Scroll to the top to show the map
                        gridState.animateScrollToItem(0)
                        
                        // Zoom into the location on the map
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(loc.latLng, 14f)
                            ),
                            durationMs = 800
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun LocationCard(
    loc: RentalLocation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Primary else Border
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryLight else Surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (isSelected) Primary else PrimaryLight,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        null,
                        tint = if (isSelected) androidx.compose.ui.graphics.Color.White else Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(loc.name, style = MaterialTheme.typography.titleMedium)
            Text(loc.description, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            Spacer(Modifier.height(8.dp))
            
            // Show available categories badges
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (loc.allowedBikeCategoryIds.contains("classic")) Text("🚲", fontSize = 12.sp)
                if (loc.allowedBikeCategoryIds.contains("ebike")) Text("⚡", fontSize = 12.sp)
                if (loc.allowedBikeCategoryIds.contains("scooter")) Text("🛴", fontSize = 12.sp)
            }
            
            Spacer(Modifier.height(4.dp))
            Text("${loc.count} vozila", style = MaterialTheme.typography.headlineSmall, color = Primary)
        }
    }
}
