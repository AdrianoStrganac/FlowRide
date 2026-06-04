package com.example.flowride.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import com.example.flowride.data.UserRepository
import com.example.flowride.ui.theme.Primary
import com.example.flowride.ui.theme.PrimaryLight
import com.example.flowride.ui.theme.TextMuted

@Composable
fun AuthDialog(
    mode: String,
    showDismissButton: Boolean = true,
    allowDismissOutside: Boolean = true,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onSwitchMode: () -> Unit
) {
    val isLogin = mode == "login"
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (allowDismissOutside) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = PrimaryLight,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Person, null,
                            tint = Primary, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(if (isLogin) "Dobrodošli natrag" else "Kreiraj račun",
                    style = MaterialTheme.typography.headlineSmall)
                Text(if (isLogin) "Prijavite se za eko-putovanje"
                else "Registriraj se za iznajmljivanje",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isLogin) {
                    OutlinedTextField(value = name, onValueChange = { name = it },
                        label = { Text("Puno ime") },
                        leadingIcon = { Icon(Icons.Outlined.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium)
                    
                    OutlinedTextField(value = phone, onValueChange = { phone = it },
                        label = { Text("Broj mobitela") },
                        leadingIcon = { Icon(Icons.Outlined.Phone, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium)
                    
                    OutlinedTextField(value = address, onValueChange = { address = it },
                        label = { Text("Adresa stanovanja") },
                        leadingIcon = { Icon(Icons.Outlined.LocationOn, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium)
                }
                
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Outlined.Email, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium)
                
                OutlinedTextField(value = password, onValueChange = { password = it },
                    label = { Text("Lozinka") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium)

                TextButton(onClick = onSwitchMode, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(if (isLogin) "Nemaš račun? Registriraj se"
                    else "Već imaš račun? Prijavi se",
                        color = Primary)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (isLogin) {
                    UserRepository.login(email)
                } else {
                    UserRepository.register(name, email, phone, address)
                }
                onSuccess()
            },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text(if (isLogin) "Prijava" else "Kreiraj račun")
            }
        },
        dismissButton = {
            if (showDismissButton) {
                TextButton(onClick = onDismiss) {
                    Text("Odustani", color = TextMuted)
                }
            }
        }
    )
}
