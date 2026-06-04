package com.example.flowride.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import com.example.flowride.components.BikeCard
import com.example.flowride.ui.theme.*
import kotlinx.coroutines.launch

data class BikeModel(
    val id: String,
    val name: String,
    val emoji: String,
    val pricePerHour: Int,
    val description: String,
    val imageRes: Any,
    val features: List<String>,
    val categoryId: String // Added to link model to its parent category
)

data class BikeCategory(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val imageRes: Any,
    val models: List<BikeModel>
)

val bikeCategories = listOf(
    BikeCategory(
        "classic", "Klasični bicikli", "🚲",
        "Savršen za opuštene gradske vožnje",
        "https://images.unsplash.com/photo-1768347443976-3eda4373bc1a?w=600",
        listOf(
            BikeModel("classic_mtb", "Mountain Bike", "⛰️", 6,
                "Spreman za sve terene i avanture",
                imageRes = com.example.flowride.R.drawable.mtb_bike,
                features = listOf("Disk kočnice", "12 brzina", "Prednja suspenzija"),
                categoryId = "classic"),
            BikeModel("classic_road", "Gradski Road Bike", "🏙️", 5,
                "Lagani bicikl za brzu gradsku vožnju",
                imageRes = com.example.flowride.R.drawable.road_bike,
                features = listOf("Lagani okvir", "Tanke gume", "Sportski sic"),
                categoryId = "classic"),
            BikeModel("classic_gravel", "Gravel Bike", "🌲", 8,
                "Sposoban bicikl za veliku udaljenosti kroz urbane puteve",
                imageRes = com.example.flowride.R.drawable.gravel_bike,
                features = listOf("Lagani okvir s dodatnom opremom", "Izdržljive gume", "Sportski sic"),
                categoryId = "classic")
        )
    ),
    BikeCategory(
        "ebike", "E-Bicikli", "⚡",
        "Električna vožnja za dulje relacije",
        "https://images.unsplash.com/photo-1760588774830-2a9f3bf4965d?w=600",
        listOf(
            BikeModel("ebike_city", "E-City Cruiser", "🚲", 8,
                "Najudobnija električna vožnja",
                imageRes = com.example.flowride.R.drawable.e_city_cruiser,
                features = listOf("Doseg 60km", "Udobno sjedalo", "Pedalna asistencija"),
                categoryId = "ebike"),
            BikeModel("ebike_cargo", "E-Cargo Bike", "📦", 10,
                "Prevezi teret s lakoćom",
                imageRes = com.example.flowride.R.drawable.e_cargo_bike,
                features = listOf("Veliki prtljažnik", "Snažan motor", "Doseg 40km"),
                categoryId = "ebike")
        )
    ),
    BikeCategory(
        "scooter", "E-Romobili", "🛴",
        "Brz i okretan gradski prijevoz",
        "https://images.unsplash.com/photo-1558981403-c5f91cbba527?w=600",
        listOf(
            BikeModel("scooter_pro", "Scooter Pro", "💨", 8,
                "Maksimalna brzina i stabilnost",
                imageRes = com.example.flowride.R.drawable.e_scooter_pro,
                features = listOf("Doseg 45km", "Apsorpcija udaraca", "Digitalni zaslon"),
                categoryId = "scooter"),
            BikeModel("scooter_lite", "Scooter Lite", "☁️", 6,
                "Lagani i lako sklopivi romobil",
                imageRes = com.example.flowride.R.drawable.e_scooter_lite,
                features = listOf("Težina 12kg", "Sklopiv", "Doseg 20km"),
                categoryId = "scooter")
        )
    ),
)

// Helper list for screens that need to find a specific bike by ID
val bikes = bikeCategories.flatMap { it.models }

@Composable
fun HomeScreen(
    isLoggedIn: Boolean,
    onLoginRequired: () -> Unit,
    onBikeSelected: (String) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    var expandedCategoryId by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        item { HeroSection() }
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Odaberi svoju kategoriju",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    "Odaberi kategoriju vozila za prikaz dostupnih modela",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        itemsIndexed(bikeCategories) { index, category ->
            val isExpanded = expandedCategoryId == category.id
            
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                CategoryCard(
                    category = category,
                    isExpanded = isExpanded,
                        onClick = {
                            val wasExpanded = isExpanded
                            expandedCategoryId = if (wasExpanded) null else category.id
                            
                            if (!wasExpanded) {
                                coroutineScope.launch {
                                    // Small delay to allow the state change to trigger the animation
                                    // and ensure the scroll targets the correct layout position
                                    kotlinx.coroutines.delay(50)
                                    listState.animateScrollToItem(index + 2)
                                }
                            }
                        }
                )

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp)) {
                        category.models.forEach { model ->
                            BikeCard(
                                bike = model,
                                isSelected = false,
                                onClick = {
                                    if (!isLoggedIn) {
                                        onLoginRequired()
                                        return@BikeCard
                                    }
                                    onBikeSelected(model.id)
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(Modifier.height(12.dp))
                FeaturesSection()
            }
        }
    }
}

@Composable
fun CategoryCard(category: BikeCategory, isExpanded: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(if (isExpanded) 2.dp else 1.dp, if (isExpanded) Primary else Border),
        colors = CardDefaults.cardColors(containerColor = Surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = PrimaryLight,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(category.emoji, fontSize = 24.sp)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, style = MaterialTheme.typography.titleMedium)
                Text(category.description, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Icon(
                imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = Primary
            )
        }
    }
}


@Composable
fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1778574171631-ed9578da400b?w=1200",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0x660F3D2C), Color(0x992D7A5F))
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Istraži prirodu na dva kotača",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Iznajmi bicikl, e-bicikl ili romobil za dobru avanturu",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null,
                        tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("50+ lokacija", color = Color.White,
                        style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AccessTime, null,
                        tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Dostupno 24/7", color = Color.White,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun FeaturesSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = PrimaryLight
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Zašto odabrati FlowRide",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                textAlign = TextAlign.Center
            )
            listOf(
                Triple(Icons.Outlined.Eco, "Nula emisija",
                    "Sva vozila su 100% električna ili na pedale"),
                Triple(Icons.Outlined.LocationOn, "Praktične lokacije",
                    "Preuzmi i vrati na bilo kojoj od naših stanica"),
                Triple(Icons.Outlined.AccessTime, "Fleksibilni najam",
                    "Satne, dnevne i tjedne opcije dostupne"),
            ).forEach { (icon, title, desc) ->
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = Primary,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
        }
    }
}
