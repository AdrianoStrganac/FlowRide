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
import com.example.flowride.components.BikeCardFirestore
import com.example.flowride.data.BikeCategoryFirestore
import com.example.flowride.data.VehicleRepository
import com.example.flowride.ui.theme.*
import kotlinx.coroutines.launch

// Zadržavamo stare klase zbog kompatibilnosti s ostalim dijelovima koda
data class BikeModel(
    val id: String,
    val name: String,
    val emoji: String,
    val pricePerHour: Int,
    val description: String,
    val imageRes: Any,
    val features: List<String>,
    val categoryId: String
)

data class BikeCategory(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val imageRes: Any,
    val models: List<BikeModel>
)

// Zadržavamo bikes val zbog ReservationScreen koji ga koristi
val bikes: List<BikeModel> = emptyList()

@Composable
fun HomeScreen(
    isLoggedIn: Boolean,
    onLoginRequired: () -> Unit,
    onBikeSelected: (String) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    var expandedCategoryId by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val categories = VehicleRepository.categories
    val isLoading = categories.isEmpty()

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

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Učitavanje vozila...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            itemsIndexed(categories) { index, category ->
                val isExpanded = expandedCategoryId == category.id
                val models = VehicleRepository.getVehiclesForCategory(category.id)

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CategoryCardFirestore(
                        category = category,
                        isExpanded = isExpanded,
                        onClick = {
                            val wasExpanded = isExpanded
                            expandedCategoryId = if (wasExpanded) null else category.id
                            if (!wasExpanded) {
                                coroutineScope.launch {
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
                            models.forEach { model ->
                                BikeCardFirestore(
                                    bike = model,
                                    onClick = {
                                        if (!isLoggedIn) {
                                            onLoginRequired()
                                            return@BikeCardFirestore
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
fun CategoryCardFirestore(
    category: BikeCategoryFirestore,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(
            if (isExpanded) 2.dp else 1.dp,
            if (isExpanded) Primary else Border
        ),
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
                Text(
                    category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
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
                    Icon(
                        Icons.Outlined.LocationOn, null,
                        tint = Color.White, modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "50+ lokacija", color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AccessTime, null,
                        tint = Color.White, modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Dostupno 24/7", color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
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
                            Icon(
                                icon, null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
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