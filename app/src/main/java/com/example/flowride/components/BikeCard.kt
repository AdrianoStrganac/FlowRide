package com.example.flowride.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import androidx.compose.ui.platform.testTag
import com.example.flowride.ui.theme.Border
import com.example.flowride.ui.theme.Primary
import com.example.flowride.ui.theme.PrimaryLight
import com.example.flowride.ui.theme.Surface
import com.example.flowride.ui.theme.TextMuted
import com.example.flowride.data.BikeModelFirestore
import com.example.flowride.screens.BikeModel

@Composable
fun BikeCard(bike: BikeModel, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isSelected) 1.02f else 1f, label = "scale")
    val borderColor = if (isSelected) Primary else Border

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .testTag("bike_card_${bike.id}"),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 0.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                AsyncImage(
                    model = bike.imageRes,
                    contentDescription = bike.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Text(bike.emoji, modifier = Modifier.padding(8.dp), fontSize = 18.sp)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(bike.name, style = MaterialTheme.typography.titleMedium)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "$${bike.pricePerHour}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Primary
                        )
                        Text(
                            "po satu",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    bike.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                bike.features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(MaterialTheme.shapes.extraLarge)
                                .background(Primary)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(feature, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().testTag("select_button_${bike.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Primary else PrimaryLight,
                        contentColor = if (isSelected) Color.White else Primary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(if (isSelected) "Odabrano" else "Odaberi")
                }
            }
        }
    }
}

@Composable
fun BikeCardFirestore(bike: BikeModelFirestore, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bike_card_${bike.id}"),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, Border),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                AsyncImage(
                    model = bike.imageUrl,
                    contentDescription = bike.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Text(bike.emoji, modifier = Modifier.padding(8.dp), fontSize = 18.sp)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(bike.name, style = MaterialTheme.typography.titleMedium)
                    Column(horizontalAlignment = Alignment.End) {
                        Text("$${bike.pricePerHour}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Primary)
                        Text("po satu",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(bike.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 10.dp))

                bike.features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(modifier = Modifier
                            .size(6.dp)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(Primary))
                        Spacer(Modifier.width(8.dp))
                        Text(feature, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().testTag("select_button_${bike.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryLight,
                        contentColor = Primary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Odaberi")
                }
            }
        }
    }
}