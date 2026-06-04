package com.example.flowride.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import com.example.flowride.data.ActiveRental
import com.example.flowride.data.RentalRepository
import com.example.flowride.ui.theme.*
import com.example.flowride.utils.QrGenerator

@Composable
fun RentalsScreen() {
    var activeTab by remember { mutableIntStateOf(0) }
    val allRentals = RentalRepository.rentals
    val tabs = listOf(
        "Trenutni (${allRentals.count { it.status == "active" }})",
        "Prošli (${allRentals.count { it.status == "completed" }})"
    )
    val filtered = if (activeTab == 0)
        allRentals.filter { it.status == "active" }
    else
        allRentals.filter { it.status == "completed" }

    var selectedRentalForQr by remember { mutableStateOf<ActiveRental?>(null) }

    if (selectedRentalForQr != null) {
        QrCodeDialog(
            rental = selectedRentalForQr!!,
            onDismiss = { selectedRentalForQr = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
            Text("Moji najmi", style = MaterialTheme.typography.headlineMedium)
            Text("Pregledaj i upravljaj rezervacijama",
                style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        }
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Primary
        ) {
            tabs.forEachIndexed { i, title ->
                Tab(selected = activeTab == i, onClick = { activeTab = i },
                    text = { Text(title) })
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = PrimaryLight,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.DirectionsBike, null,
                                tint = Primary, modifier = Modifier.size(30.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Nema ${if (activeTab == 0) "trenutnih" else "prošlih"} najma",
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (activeTab == 0) "Rezerviraj vozilo na početnoj stranici"
                        else "Tvoja povijest najma će se prikazati ovdje",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { rental -> 
                    RentalCard(
                        rental = rental,
                        onShowQr = { selectedRentalForQr = rental }
                    ) 
                }
            }
        }
    }
}

@Composable
fun RentalCard(rental: ActiveRental, onShowQr: () -> Unit) {
    val isActive = rental.status == "active"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp, if (isActive) Primary.copy(alpha = 0.4f) else Border),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = if (isActive) Primary else SurfaceMuted,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isActive) Icons.Outlined.ElectricBolt
                                else Icons.Outlined.DirectionsBike,
                                null,
                                tint = if (isActive) androidx.compose.ui.graphics.Color.White
                                else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(rental.vehicleType, style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("ID: ${rental.id}",
                                style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            if (isActive) {
                                Surface(shape = MaterialTheme.shapes.extraLarge,
                                    color = PrimaryLight) {
                                    Text("Aktivan",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Primary)
                                }
                            }
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$${rental.price}",
                        style = MaterialTheme.typography.headlineSmall, color = Primary)
                    Text(rental.duration,
                        style = MaterialTheme.typography.labelMedium, color = TextMuted)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Border)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RentalDetail(Icons.Outlined.CalendarToday, "Početak", rental.startDate)
                RentalDetail(Icons.Outlined.AccessTime, "Kraj", rental.endDate)
                RentalDetail(Icons.Outlined.LocationOn, "Lokacija", rental.pickupLocation)
                RentalDetail(Icons.Outlined.Payment, "Plaćanje",
                    when (rental.paymentMethod) {
                        "card" -> "Kreditna kartica"
                        "paypal" -> "PayPal"
                        else -> "Gotovina"
                    }
                )
            }

            if (isActive) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onShowQr,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight, contentColor = Primary),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Outlined.QrCode, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Prikaži QR kod za preuzimanje")
                }
            }
        }
    }
}

@Composable
fun QrCodeDialog(rental: ActiveRental, onDismiss: () -> Unit) {
    val qrBitmap = remember(rental.id) {
        QrGenerator.generateQrCode(rental.id)
    }

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
                Text(
                    "QR kod za preuzimanje",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    "Pokaži ovaj kod zaposleniku na stanici",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(Modifier.height(24.dp))
                
                Surface(
                    modifier = Modifier
                        .size(240.dp)
                        .padding(8.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = androidx.compose.ui.graphics.Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.fillMaxSize(),
                            filterQuality = androidx.compose.ui.graphics.FilterQuality.None
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Greška pri generiranju QR koda", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryLight, MaterialTheme.shapes.medium)
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(rental.vehicleType, style = MaterialTheme.typography.titleMedium)
                    Text("ID Rezervacije: ${rental.id}", style = MaterialTheme.typography.bodySmall)
                }
                
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Zatvori")
                }
            }
        }
    }
}

@Composable
fun RentalDetail(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icon, null, tint = Primary,
            modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Spacer(Modifier.width(6.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Text(value, style = MaterialTheme.typography.bodySmall,
                softWrap = true,
                overflow = androidx.compose.ui.text.style.TextOverflow.Visible)
        }
    }
}
