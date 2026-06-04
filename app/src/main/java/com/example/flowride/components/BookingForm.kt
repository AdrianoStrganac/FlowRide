package com.example.flowride.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.testTag
import com.example.flowride.data.ActiveRental
import com.example.flowride.data.RentalRepository
import com.example.flowride.screens.BikeModel
import com.example.flowride.screens.rentalLocations
import com.example.flowride.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class PaymentMethod(val id: String, val label: String, val icon: ImageVector)

val paymentMethods = listOf(
    PaymentMethod("card", "Kreditna kartica", Icons.Outlined.CreditCard),
    PaymentMethod("paypal", "PayPal", Icons.Outlined.AccountBalanceWallet),
    PaymentMethod("cash", "Gotovina", Icons.Outlined.Payments),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingForm(bike: BikeModel, onClose: () -> Unit, onSuccess: () -> Unit) {
    // Filter locations based on bike category
    val availableLocations = remember(bike.categoryId) {
        rentalLocations.filter { it.allowedBikeCategoryIds.contains(bike.categoryId) }
    }

    var hours by remember { mutableIntStateOf(2) }
    var selectedLocation by remember { mutableStateOf(availableLocations.firstOrNull()?.name ?: "No locations available") }
    var selectedPayment by remember { mutableStateOf("card") }
    var locationExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }

    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = today
    )

    // Manual date text state for testing and alternative input
    val sdf = remember { SimpleDateFormat("dd. MM. yyyy.", Locale.getDefault()) }
    var dateText by remember { 
        mutableStateOf(sdf.format(Date(today))) 
    }

    // Update text when date is picked from calendar or changed manually
    LaunchedEffect(datePickerState.selectedDateMillis) {
        val selected = datePickerState.selectedDateMillis
        if (selected != null) {
            val formatted = sdf.format(Date(selected))
            if (dateText != formatted) {
                dateText = formatted
            }
        }
        dateError = false
    }

    fun onDateTextChange(newText: String) {
        dateText = newText
        try {
            val parsedDate = sdf.parse(newText)
            if (parsedDate != null && parsedDate.time != datePickerState.selectedDateMillis) {
                // Ensure we don't cause a loop if it's already the same date
                datePickerState.selectedDateMillis = parsedDate.time
            }
        } catch (e: Exception) {
            // Invalid format while typing, wait for valid input
        }
    }

    val selectedDateFormatted = datePickerState.selectedDateMillis?.let {
        SimpleDateFormat("dd. MM. yyyy.", Locale.getDefault()).format(Date(it))
    } ?: "Odaberi datum"

    val total = bike.pricePerHour * hours

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge,
            icon = {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = PrimaryLight,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.CheckCircle, null,
                            tint = Primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    "Rezervacija potvrđena!",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Tvoja rezervacija za ${bike.name} je uspješno kreirana.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = PrimaryLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row {
                                Text("Datum: ", style = MaterialTheme.typography.labelMedium)
                                Text(selectedDateFormatted, style = MaterialTheme.typography.bodySmall)
                            }
                            Row {
                                Text("Trajanje: ", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    "$hours ${if (hours == 1) "sat" else "sata"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row {
                                Text("Lokacija: ", style = MaterialTheme.typography.labelMedium)
                                Text(selectedLocation, style = MaterialTheme.typography.bodySmall)
                            }
                            Row {
                                Text("Ukupno: ", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    "$$total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false; onSuccess() },
                    modifier = Modifier.fillMaxWidth().testTag("success_ok_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Odlično!")
                }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Potvrdi", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Odustani", color = TextMuted)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Primary,
                    todayDateBorderColor = Primary,
                    selectedDayContentColor = PrimaryForeground,
                )
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Naslov
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Rezervacija", style = MaterialTheme.typography.titleLarge)
                    Text(bike.name, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Outlined.Close, contentDescription = "Zatvori")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Border)

            // Datum
            Text(
                "Datum preuzimanja",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = dateText,
                onValueChange = { onDateTextChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (dateError) 6.dp else 16.dp)
                    .testTag("date_input_field"),
                label = { Text("Odaberi datum (dan. mj. god.)") },
                shape = MaterialTheme.shapes.medium,
                leadingIcon = {
                    Icon(Icons.Outlined.CalendarToday, null, tint = Primary)
                },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }, modifier = Modifier.testTag("open_calendar_button")) {
                        Icon(Icons.Outlined.CalendarMonth, "Otvori kalendar", tint = Primary)
                    }
                },
                isError = dateError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Border
                )
            )

            if (dateError) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Icon(
                        Icons.Outlined.Error, null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Molimo odaberite datum danas ili u budućnosti.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Trajanje
            Text(
                "Trajanje najma",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                IconButton(
                    onClick = { if (hours > 1) hours-- },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Outlined.Remove, "Remove", tint = Primary)
                }
                Text(
                    "$hours ${if (hours == 1) "sat" else "sata"}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                IconButton(
                    onClick = { if (hours < 24) hours++ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Outlined.Add, "Add", tint = Primary)
                }
            }
            Slider(
                value = hours.toFloat(),
                onValueChange = { hours = it.toInt() },
                valueRange = 1f..24f,
                steps = 22,
                colors = SliderDefaults.colors(
                    thumbColor = Primary,
                    activeTrackColor = Primary,
                    inactiveTrackColor = PrimaryLight
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Lokacija
            Text(
                "Lokacija preuzimanja",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ExposedDropdownMenuBox(
                expanded = locationExpanded,
                onExpandedChange = { locationExpanded = it },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                OutlinedTextField(
                    value = selectedLocation,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Outlined.LocationOn, null, tint = Primary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(locationExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Border
                    )
                )
                ExposedDropdownMenu(
                    expanded = locationExpanded,
                    onDismissRequest = { locationExpanded = false }
                ) {
                    availableLocations.forEach { loc ->
                        DropdownMenuItem(
                            text = { Text(loc.name) },
                            onClick = { selectedLocation = loc.name; locationExpanded = false },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.LocationOn, null,
                                    tint = Primary, modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Način plaćanja
            Text(
                "Način plaćanja",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                paymentMethods.forEach { method ->
                    val isSelected = selectedPayment == method.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = { selectedPayment = method.id }
                            ),
                        shape = MaterialTheme.shapes.medium,
                        color = if (isSelected) PrimaryLight else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Primary else Border
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                method.icon, null,
                                tint = if (isSelected) Primary else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                method.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) Primary else TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Outlined.CheckCircle, null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Ukupno
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = PrimaryLight
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ukupno", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(
                            "$hours ${if (hours == 1) "sat" else "sata"} × $${bike.pricePerHour}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted
                        )
                    }
                    Text(
                        "$$total",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Primary
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis == null || selectedMillis < today) {
                        dateError = true
                        return@Button
                    }
                    dateError = false

                    val sdf = SimpleDateFormat("dd. MM. yyyy.", Locale.getDefault())
                    val startDate = sdf.format(Date(selectedMillis))
                    val endMillis = selectedMillis + (hours * 3600 * 1000L)
                    val endDate = sdf.format(Date(endMillis))

                    val user = com.example.flowride.data.UserRepository.currentUser
                    val rental = ActiveRental(
                        id = "R${System.currentTimeMillis().toString().takeLast(6)}",
                        vehicleType = bike.name,
                        startDate = startDate,
                        endDate = endDate,
                        pickupLocation = selectedLocation,
                        duration = "$hours ${if (hours == 1) "sat" else "sata"}",
                        price = total,
                        paymentMethod = selectedPayment,
                        userName = user?.name ?: "Gost",
                        userEmail = user?.email ?: ""
                    )
                    RentalRepository.addRental(rental)
                    showSuccessDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Outlined.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Potvrdi rezervaciju")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Text("Odustani")
            }
        }
    }
}
