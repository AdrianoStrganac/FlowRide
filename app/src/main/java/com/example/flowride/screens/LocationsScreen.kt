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
    val city: String = "Zagreb",
    val allowedBikeCategoryIds: List<String> = listOf("classic", "ebike", "scooter")
)

val rentalLocations = listOf(
    // ─── Zagreb ───────────────────────────────────────────────
    RentalLocation("Trg bana Jelačića", "Glavni gradski trg", 18, LatLng(45.8131, 15.9775), "Zagreb"),
    RentalLocation("Glavni kolodvor", "Željeznički kolodvor", 14, LatLng(45.8048, 15.9786), "Zagreb"),
    RentalLocation("Jarun", "Jezero i rekreacija", 20, LatLng(45.7780, 15.9180), "Zagreb"),
    RentalLocation("Bundek", "Park uz Savu", 12, LatLng(45.7920, 15.9890), "Zagreb"),
    RentalLocation("Maksimir", "Park i ZOO", 10, LatLng(45.8280, 16.0190), "Zagreb"),
    RentalLocation("Trešnjevka", "Stambeni kvart", 9, LatLng(45.8030, 15.9460), "Zagreb"),
    RentalLocation(
        "Sljeme - Vrh", "Planinarski dom", 5, LatLng(45.8990, 15.9480), "Zagreb",
        allowedBikeCategoryIds = listOf("classic")
    ),

    // ─── Zagreb okolica ───────────────────────────────────────
    RentalLocation(
        "Samobor - Trg", "Povijesna jezgra", 8, LatLng(45.8000, 15.7100), "Zagreb okolica",
        allowedBikeCategoryIds = listOf("classic", "ebike")
    ),
    RentalLocation("Velika Gorica", "Centar grada", 7, LatLng(45.7130, 16.0760), "Zagreb okolica"),
    RentalLocation("Zaprešić", "Stanica zapad", 6, LatLng(45.8570, 15.8080), "Zagreb okolica"),

    // ─── Split ────────────────────────────────────────────────
    RentalLocation("Riva Split", "Gradska luka", 16, LatLng(43.5081, 16.4402), "Split"),
    RentalLocation("Bačvice", "Plaža i šetnica", 13, LatLng(43.5030, 16.4520), "Split"),
    RentalLocation(
        "Marjan", "Park-šuma", 8, LatLng(43.5090, 16.4180), "Split",
        allowedBikeCategoryIds = listOf("classic", "ebike")
    ),
    RentalLocation("Žnjan", "Obalna zona", 11, LatLng(43.5140, 16.4790), "Split"),

    // ─── Rijeka ───────────────────────────────────────────────
    RentalLocation("Korzo Rijeka", "Glavna pješačka zona", 15, LatLng(45.3270, 14.4420), "Rijeka"),
    RentalLocation("Molo Longo", "Lukobran", 9, LatLng(45.3160, 14.4350), "Rijeka"),
    RentalLocation("Trsat", "Gradina i vidikovac", 6, LatLng(45.3290, 14.4640), "Rijeka"),
    RentalLocation("Kantrida", "Uz more", 8, LatLng(45.3390, 14.3850), "Rijeka"),

    // ─── Osijek ───────────────────────────────────────────────
    RentalLocation("Tvrđa", "Barokna jezgra", 12, LatLng(45.5610, 18.6960), "Osijek"),
    RentalLocation("Promenada", "Šetnica uz Dravu", 14, LatLng(45.5580, 18.6820), "Osijek"),
    RentalLocation("Gradski vrt", "Sportski centar", 10, LatLng(45.5530, 18.7050), "Osijek"),
    RentalLocation("Copacabana", "Gradska plaža", 9, LatLng(45.5650, 18.6710), "Osijek"),
)

val cityOrder = listOf("Zagreb", "Zagreb okolica", "Split", "Rijeka", "Osijek")

val cityCenter = mapOf(
    "Zagreb" to LatLng(45.8131, 15.9775),
    "Zagreb okolica" to LatLng(45.8000, 15.8000),
    "Split" to LatLng(43.5081, 16.4402),
    "Rijeka" to LatLng(45.3270, 14.4420),
    "Osijek" to LatLng(45.5580, 18.6820),
)

@Composable
fun LocationsScreen() {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(45.8131, 15.9775), 10f)
    }
    var mapTouched by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<RentalLocation?>(null) }
    var selectedCity by remember { mutableStateOf("Zagreb") }
    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    val filteredLocations = rentalLocations.filter { it.city == selectedCity }
    val groupedLocations = rentalLocations.groupBy { it.city }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !mapTouched
    ) {
        // ─── Naslov ───────────────────────────────────────────
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

                // ─── Karta ────────────────────────────────────
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
                    // Prikaži samo markere odabranog grada
                    filteredLocations.forEach { loc ->
                        Marker(
                            state = MarkerState(position = loc.latLng),
                            title = loc.name,
                            snippet = "${loc.count} vozila"
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ─── Filter chipovi po gradu ──────────────────
                ScrollableTabRow(
                    selectedTabIndex = cityOrder.indexOf(selectedCity),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Primary,
                    edgePadding = 0.dp,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    cityOrder.forEach { city ->
                        val count = groupedLocations[city]?.size ?: 0
                        Tab(
                            selected = selectedCity == city,
                            onClick = {
                                selectedCity = city
                                selectedLocation = null
                                coroutineScope.launch {
                                    gridState.animateScrollToItem(0)
                                    cityCenter[city]?.let { center ->
                                        cameraPositionState.animate(
                                            update = CameraUpdateFactory.newCameraPosition(
                                                CameraPosition.fromLatLngZoom(center, 12f)
                                            ),
                                            durationMs = 600
                                        )
                                    }
                                }
                            },
                            text = {
                                Text("$city ($count)")
                            }
                        )
                    }
                }
            }
        }

        // ─── Kartice lokacija za odabrani grad ────────────────
        items(filteredLocations) { loc ->
            LocationCard(
                loc = loc,
                isSelected = selectedLocation?.name == loc.name,
                onClick = {
                    selectedLocation = loc
                    coroutineScope.launch {
                        gridState.animateScrollToItem(0)
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(loc.latLng, 15f)
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
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (loc.allowedBikeCategoryIds.contains("classic")) Text("🚲", fontSize = 12.sp)
                if (loc.allowedBikeCategoryIds.contains("ebike")) Text("⚡", fontSize = 12.sp)
                if (loc.allowedBikeCategoryIds.contains("scooter")) Text("🛴", fontSize = 12.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${loc.count} vozila",
                style = MaterialTheme.typography.headlineSmall,
                color = Primary
            )
        }
    }
}