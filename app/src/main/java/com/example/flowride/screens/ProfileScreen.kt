package com.example.flowride.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.flowride.data.UserRepository
import com.example.flowride.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onLogout: () -> Unit, onDeleteAccount: () -> Unit) {
    val user = UserRepository.currentUser ?: return
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf("") }

    var editName by remember { mutableStateOf(user.name) }
    var editPhone by remember { mutableStateOf(user.phone) }
    var editAddress by remember { mutableStateOf(user.address) }

    // Ažuriraj edit polja kad se user promijeni
    LaunchedEffect(user) {
        editName = user.name
        editPhone = user.phone
        editAddress = user.address
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text("Brisanje računa") },
            text = {
                Text("Jeste li sigurni da želite trajno obrisati svoj račun? Ova radnja se ne može poništiti.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    onDeleteAccount()
                }) {
                    Text("Obriši", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Odustani")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = PrimaryLight,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        if (user.isAdmin) {
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = Primary
            ) {
                Text(
                    "Administrator",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryForeground
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Podaci / Edit forma
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            border = androidx.compose.foundation.BorderStroke(1.dp, Border),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Osobni podaci",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (!isEditing) {
                        TextButton(onClick = { isEditing = true }) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Uredi", color = Primary)
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Border
                )

                if (isEditing) {
                    // Edit mode
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Ime i prezime") },
                        leadingIcon = { Icon(Icons.Outlined.Person, null, tint = Primary) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Border
                        )
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Broj mobitela") },
                        leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = Primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Border
                        )
                    )
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Adresa stanovanja") },
                        leadingIcon = { Icon(Icons.Outlined.LocationOn, null, tint = Primary) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Border
                        )
                    )

                    if (saveError.isNotEmpty()) {
                        Text(
                            saveError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isEditing = false
                                saveError = ""
                                editName = user.name
                                editPhone = user.phone
                                editAddress = user.address
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                        ) {
                            Text("Odustani")
                        }
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isSaving = true
                                    saveError = ""
                                    val success = UserRepository.updateProfile(
                                        name = editName,
                                        phone = editPhone,
                                        address = editAddress
                                    )
                                    isSaving = false
                                    if (success) {
                                        isEditing = false
                                    } else {
                                        saveError = "Greška pri spremanju. Pokušaj ponovo."
                                    }
                                }
                            },
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = PrimaryForeground,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Spremi")
                            }
                        }
                    }
                } else {
                    // View mode
                    ProfileInfoRow(
                        label = "Ime i prezime",
                        value = user.name,
                        icon = Icons.Outlined.Person
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Border
                    )
                    ProfileInfoRow(
                        label = "Broj mobitela",
                        value = user.phone.ifEmpty { "Nije postavljeno" },
                        icon = Icons.Outlined.Phone
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Border
                    )
                    ProfileInfoRow(
                        label = "Adresa stanovanja",
                        value = user.address.ifEmpty { "Nije postavljeno" },
                        icon = Icons.Outlined.LocationOn
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Email kartica (nije editabilno)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            border = androidx.compose.foundation.BorderStroke(1.dp, Border),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Račun", style = MaterialTheme.typography.titleMedium)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Border)
                ProfileInfoRow(
                    label = "Email adresa",
                    value = user.email,
                    icon = Icons.Outlined.Email
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Outlined.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Odjava")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = { showDeleteConfirmation = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Obriši račun", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}