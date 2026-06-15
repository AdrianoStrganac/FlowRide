package com.example.flowride.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.flowride.data.ActiveRental
import com.example.flowride.data.BikeModelFirestore
import com.example.flowride.data.RentalRepository
import com.example.flowride.data.VehicleRepository
import com.example.flowride.ui.theme.*
import com.example.flowride.utils.ScannerUtils
import kotlinx.coroutines.launch

@Composable
fun AdminScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Rezervacije", "Vozila")

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text("Admin Panel", style = MaterialTheme.typography.headlineMedium)
            Text("Upravljanje sustavom", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        }
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Primary
        ) {
            tabs.forEachIndexed { i, title ->
                Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(title) })
            }
        }
        when (selectedTab) {
            0 -> AdminRentalsTab()
            1 -> AdminVehiclesTab()
        }
    }
}

@Composable
fun AdminRentalsTab() {
    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity ?: return
    val coroutineScope = rememberCoroutineScope()

    // Lokalna lista skeniranih najama - samo ono što admin skenira
    var scannedRentals by remember { mutableStateOf<List<ActiveRental>>(emptyList()) }
    var scannedRental by remember { mutableStateOf<ActiveRental?>(null) }
    var activeTab by remember { mutableIntStateOf(0) }

    // Prati promjene statusa iz repozitorija za već skenirane najme
    val allRentals = RentalRepository.allRentalsForAdmin
    val updatedScannedRentals = scannedRentals.map { scanned ->
        allRentals.find { it.id == scanned.id } ?: scanned
    }

    val active = updatedScannedRentals.filter { it.status == "active" || it.status == "in_progress" }
    val completed = updatedScannedRentals.filter { it.status == "completed" }
    val displayed = if (activeTab == 0) active else completed

    // Dijalog za potvrdu aktivacije
    var confirmActivateRental by remember { mutableStateOf<ActiveRental?>(null) }
    var confirmCompleteRental by remember { mutableStateOf<ActiveRental?>(null) }

    if (confirmActivateRental != null) {
        AlertDialog(
            onDismissRequest = { confirmActivateRental = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text("Aktivacija najma") },
            text = { Text("Jesi li siguran da želiš aktivirati najam za ${confirmActivateRental!!.vehicleType} (${confirmActivateRental!!.userName})?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            RentalRepository.startRental(confirmActivateRental!!.id)
                            RentalRepository.loadRentals() // ← dodaj ovo
                            confirmActivateRental = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = MaterialTheme.shapes.medium
                ) { Text("Aktiviraj") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { confirmActivateRental = null },
                    shape = MaterialTheme.shapes.medium
                ) { Text("Odustani") }
            }
        )
    }

    if (confirmCompleteRental != null) {
        AlertDialog(
            onDismissRequest = { confirmCompleteRental = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text("Završavanje najma") },
            text = { Text("Jesi li siguran da želiš završiti najam za ${confirmCompleteRental!!.vehicleType} (${confirmCompleteRental!!.userName})?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            RentalRepository.confirmRental(confirmCompleteRental!!.id)
                            RentalRepository.loadRentals() // ← dodaj ovo
                            confirmCompleteRental = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = MaterialTheme.shapes.medium
                ) { Text("Završi") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { confirmCompleteRental = null },
                    shape = MaterialTheme.shapes.medium
                ) { Text("Odustani") }
            }
        )
    }

    if (scannedRental != null) {
        AdminScanDialog(
            rental = allRentals.find { it.id == scannedRental!!.id } ?: scannedRental!!,
            onDismiss = { scannedRental = null },
            onActivate = {
                val rental = allRentals.find { it.id == scannedRental!!.id } ?: scannedRental!!
                scannedRental = null
                confirmActivateRental = rental
            },
            onConfirm = {
                val rental = allRentals.find { it.id == scannedRental!!.id } ?: scannedRental!!
                scannedRental = null
                confirmCompleteRental = rental
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = {
                ScannerUtils.startScanner(activity) { result ->
                    if (result != null) {
                        coroutineScope.launch {
                            val rental = RentalRepository.findRentalById(result)
                            if (rental != null) {
                                // Dodaj u listu ako već nije
                                if (scannedRentals.none { it.id == rental.id }) {
                                    scannedRentals = scannedRentals + rental
                                }
                                scannedRental = rental
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Outlined.QrCodeScanner, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Skeniraj QR kod rezervacije")
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                label = { Text("Aktivne (${active.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryLight,
                    selectedLabelColor = Primary
                )
            )
            FilterChip(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                label = { Text("Završene (${completed.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryLight,
                    selectedLabelColor = Primary
                )
            )
        }

        if (displayed.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = PrimaryLight,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.QrCodeScanner, null,
                                tint = Primary, modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Nema skeniranih najama", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Skeniraj QR kod korisnikovog najma",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayed) { rental ->
                    AdminRentalCard(
                        rental = rental,
                        onConfirm = if (rental.status == "active") {
                            { confirmActivateRental = rental }
                        } else null,
                        onComplete = if (rental.status == "in_progress") {
                            { confirmCompleteRental = rental }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
fun AdminRentalCard(
    rental: ActiveRental,
    onConfirm: (() -> Unit)?,
    onComplete: (() -> Unit)? = null
) {
    val isActive = rental.status == "active"
    val isInProgress = rental.status == "in_progress"

    val borderColor = when {
        isInProgress -> Primary
        isActive -> Primary.copy(alpha = 0.4f)
        else -> Border
    }

    val statusLabel = when (rental.status) {
        "active" -> "Rezervirano"
        "in_progress" -> "U tijeku"
        "completed" -> "Završeno"
        else -> rental.status
    }

    val statusColor = when {
        isInProgress -> Primary
        isActive -> PrimaryLight
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val statusTextColor = when {
        isInProgress -> androidx.compose.ui.graphics.Color.White
        isActive -> Primary
        else -> TextMuted
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(rental.vehicleType, style = MaterialTheme.typography.titleMedium)
                    Text("ID: ${rental.id}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = statusColor
                ) {
                    Text(
                        statusLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = statusTextColor
                    )
                }
            }

            // Timer za in_progress najme
            if (isInProgress && rental.startTimeMillis != null) {
                Spacer(Modifier.height(12.dp))
                AdminRentalTimer(
                    startTimeMillis = rental.startTimeMillis,
                    durationMinutes = rental.durationMinutes,
                    onExpired = { onComplete?.invoke() }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Border)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AdminInfoRow(Icons.Outlined.Person, "Korisnik", rental.userName)
                AdminInfoRow(Icons.Outlined.Email, "Email", rental.userEmail)
                AdminInfoRow(Icons.Outlined.LocationOn, "Lokacija", rental.pickupLocation)
                AdminInfoRow(Icons.Outlined.CalendarToday, "Datum", "${rental.startDate} → ${rental.endDate}")
                AdminInfoRow(Icons.Outlined.AccessTime, "Trajanje", rental.duration)
                AdminInfoRow(
                    Icons.Outlined.Payment, "Plaćanje",
                    when (rental.paymentMethod) {
                        "card" -> "Kreditna kartica"
                        "paypal" -> "PayPal"
                        else -> "Gotovina"
                    }
                )
                AdminInfoRow(Icons.Outlined.AttachMoney, "Ukupno", "$${rental.price}")
            }

            // Aktiviraj dugme za rezervirane
            if (onConfirm != null) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Aktiviraj najam")
                }
            }

            // Završi ručno dugme za in_progress
            if (onComplete != null) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Završi najam")
                }
            }
        }
    }
}

@Composable
fun AdminRentalTimer(
    startTimeMillis: Long,
    durationMinutes: Int,
    onExpired: () -> Unit
) {
    var timeLeftMillis by remember {
        mutableLongStateOf(
            (startTimeMillis + durationMinutes * 60 * 1000L) - System.currentTimeMillis()
        )
    }
    var expired by remember { mutableStateOf(false) }

    LaunchedEffect(startTimeMillis) {
        while (timeLeftMillis > 0) {
            kotlinx.coroutines.delay(1000)
            timeLeftMillis = (startTimeMillis + durationMinutes * 60 * 1000L) - System.currentTimeMillis()
        }
        if (!expired) {
            expired = true
            onExpired()
        }
    }

    val totalSeconds = (timeLeftMillis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val progress = (timeLeftMillis.toFloat() / (durationMinutes * 60 * 1000L)).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryLight.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Outlined.Timer, null, tint = Primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (hours > 0)
                    String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                else
                    String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.titleLarge,
                color = Primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = Primary,
            trackColor = Border,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Text(
            "Preostalo vrijeme najma",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun AdminScanDialog(
    rental: ActiveRental,
    onDismiss: () -> Unit,
    onActivate: () -> Unit,
    onConfirm: () -> Unit
) {
    val isInProgress = rental.status == "in_progress"
    val isActive = rental.status == "active"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        icon = {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = PrimaryLight,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.QrCodeScanner, null, tint = Primary, modifier = Modifier.size(32.dp))
                }
            }
        },
        title = {
            Text(
                "Skenirana rezervacija",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Status badge
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = if (isInProgress) Primary else PrimaryLight,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        when (rental.status) {
                            "active" -> "Rezervirano"
                            "in_progress" -> "U tijeku"
                            "completed" -> "Završeno"
                            else -> rental.status
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isInProgress) androidx.compose.ui.graphics.Color.White else Primary
                    )
                }
                Spacer(Modifier.height(4.dp))
                AdminInfoRow(Icons.Outlined.DirectionsBike, "Vozilo", rental.vehicleType)
                AdminInfoRow(Icons.Outlined.Person, "Korisnik", rental.userName)
                AdminInfoRow(Icons.Outlined.Email, "Email", rental.userEmail)
                AdminInfoRow(Icons.Outlined.LocationOn, "Lokacija", rental.pickupLocation)
                AdminInfoRow(Icons.Outlined.AccessTime, "Trajanje", rental.duration)
                AdminInfoRow(Icons.Outlined.AttachMoney, "Ukupno", "$${rental.price}")
            }
        },
        confirmButton = {
            when {
                isActive -> Button(
                    onClick = onActivate,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Aktiviraj najam")
                }
                isInProgress -> Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Završi najam")
                }
                else -> {}
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Zatvori")
            }
        }
    )
}

@Composable
fun AdminInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(15.dp).padding(top = 2.dp))
        Spacer(Modifier.width(6.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Text(value, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun AdminScanDialog(rental: ActiveRental, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        icon = {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = PrimaryLight,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.QrCodeScanner, null, tint = Primary, modifier = Modifier.size(32.dp))
                }
            }
        },
        title = {
            Text(
                "Skenirana rezervacija",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminInfoRow(Icons.Outlined.DirectionsBike, "Vozilo", rental.vehicleType)
                AdminInfoRow(Icons.Outlined.Person, "Korisnik", rental.userName)
                AdminInfoRow(Icons.Outlined.Email, "Email", rental.userEmail)
                AdminInfoRow(Icons.Outlined.LocationOn, "Lokacija", rental.pickupLocation)
                AdminInfoRow(Icons.Outlined.AccessTime, "Trajanje", rental.duration)
                AdminInfoRow(Icons.Outlined.AttachMoney, "Ukupno", "$${rental.price}")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Potvrdi najam")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Zatvori")
            }
        }
    )
}

@Composable
fun AdminVehiclesTab() {
    val coroutineScope = rememberCoroutineScope()
    val vehicles = VehicleRepository.vehicles
    var showAddDialog by remember { mutableStateOf(false) }
    var editingVehicle by remember { mutableStateOf<BikeModelFirestore?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<BikeModelFirestore?>(null) }

    if (showAddDialog || editingVehicle != null) {
        VehicleEditDialog(
            vehicle = editingVehicle,
            onDismiss = { showAddDialog = false; editingVehicle = null },
            onSave = { vehicle ->
                coroutineScope.launch {
                    VehicleRepository.saveVehicle(vehicle)
                    showAddDialog = false
                    editingVehicle = null
                }
            }
        )
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text("Brisanje vozila") },
            text = { Text("Jeste li sigurni da želite obrisati ${showDeleteConfirm!!.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        VehicleRepository.deleteVehicle(showDeleteConfirm!!.id)
                        showDeleteConfirm = null
                    }
                }) {
                    Text("Obriši", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Odustani") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Dodaj novo vozilo")
        }

        if (vehicles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nema vozila", color = TextMuted)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(vehicles) { vehicle ->
                    AdminVehicleCard(
                        vehicle = vehicle,
                        onEdit = { editingVehicle = vehicle },
                        onDelete = { showDeleteConfirm = vehicle }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminVehicleCard(
    vehicle: BikeModelFirestore,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        colors = CardDefaults.cardColors(
            containerColor = if (vehicle.isAvailable) MaterialTheme.colorScheme.surface else SurfaceMuted
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(vehicle.emoji, fontSize = 24.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            vehicle.name, 
                            style = MaterialTheme.typography.titleMedium,
                            color = if (vehicle.isAvailable) MaterialTheme.colorScheme.onSurface else TextMuted
                        )
                        Text(
                            "${vehicle.categoryId} • $${vehicle.pricePerHour}/sat",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Switch za dostupnost
                    Switch(
                        checked = vehicle.isAvailable,
                        onCheckedChange = { 
                            coroutineScope.launch {
                                VehicleRepository.toggleAvailability(vehicle.id, vehicle.isAvailable)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Primary,
                            checkedTrackColor = PrimaryLight
                        ),
                        modifier = Modifier.scale(0.7f)
                    )
                    
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, null, tint = Primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            if (vehicle.features.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    vehicle.features.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            if (!vehicle.isAvailable) {
                Text(
                    "TRENUTNO NEDOSTUPNO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleEditDialog(
    vehicle: BikeModelFirestore?,
    onDismiss: () -> Unit,
    onSave: (BikeModelFirestore) -> Unit
) {
    val isNew = vehicle == null
    var id by remember { mutableStateOf(vehicle?.id ?: "") }
    var name by remember { mutableStateOf(vehicle?.name ?: "") }
    var emoji by remember { mutableStateOf(vehicle?.emoji ?: "") }
    var price by remember { mutableStateOf(vehicle?.pricePerHour?.toString() ?: "") }
    var description by remember { mutableStateOf(vehicle?.description ?: "") }
    var imageUrl by remember { mutableStateOf(vehicle?.imageUrl ?: "") }
    var categoryId by remember { mutableStateOf(vehicle?.categoryId ?: "classic") }
    var isAvailable by remember { mutableStateOf(vehicle?.isAvailable ?: true) }
    var featuresText by remember { mutableStateOf(vehicle?.features?.joinToString(", ") ?: "") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categories = listOf("classic", "ebike", "scooter")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text(if (isNew) "Novo vozilo" else "Uredi vozilo") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                if (isNew) {
                    OutlinedTextField(
                        value = id, onValueChange = { id = it },
                        label = { Text("ID (npr. classic_mtb)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                    )
                }
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Naziv") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                )
                
                // Red za dostupnost u dijalogu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dostupno za najam", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Primary)
                    )
                }

                OutlinedTextField(
                    value = emoji, onValueChange = { emoji = it },
                    label = { Text("Emoji") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                )
                OutlinedTextField(
                    value = price, onValueChange = { price = it },
                    label = { Text("Cijena po satu ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Opis") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                )
                OutlinedTextField(
                    value = imageUrl, onValueChange = { imageUrl = it },
                    label = { Text("URL slike") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                )
                OutlinedTextField(
                    value = featuresText, onValueChange = { featuresText = it },
                    label = { Text("Značajke (odvojene zarezom)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = categoryId,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategorija") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = { categoryId = cat; categoryExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newVehicle = BikeModelFirestore(
                        id = if (isNew) id else vehicle!!.id,
                        name = name,
                        emoji = emoji,
                        pricePerHour = price.toIntOrNull() ?: 0,
                        description = description,
                        imageUrl = imageUrl,
                        features = featuresText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        categoryId = categoryId,
                        isAvailable = isAvailable
                    )
                    onSave(newVehicle)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Spremi")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = MaterialTheme.shapes.medium) {
                Text("Odustani")
            }
        }
    )
}
