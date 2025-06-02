package com.example.potholedetection_v1.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.potholedetection_v1.PotholeViewModel
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(viewModel: PotholeViewModel = viewModel()) {
    var sensitivity by remember { mutableStateOf(viewModel.getDetectionSensitivity()) }
    var backgroundDetection by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }
    var dynamicColor by remember { mutableStateOf(true) }
    var saveLocation by remember { mutableStateOf(true) }

    // Variable para controlar si los cambios han sido guardados
    var settingsSaved by remember { mutableStateOf(false) }

    // ESTADOS PARA USUARIO
    var userName by remember { mutableStateOf(viewModel.getUserName()) }
    var showUserDialog by remember { mutableStateOf(false) }

    // Efecto para mostrar mensaje de confirmación temporalmente
    LaunchedEffect(settingsSaved) {
        if (settingsSaved) {
            delay(2000) // Mostrar mensaje por 2 segundos
            settingsSaved = false
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // NUEVA SECCIÓN: Información del Usuario
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "User Information",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Display Name",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = userName,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(onClick = { showUserDialog = true }) {
                        Text("Change")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "User ID: ${viewModel.getUserId().take(12)}...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Device: ${viewModel.getDeviceInfo()}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Detection Sensitivity
        Text(
            text = "Detection Sensitivity",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Text(
            text = "Adjust how sensitive the pothole detection should be",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Slider(
            value = sensitivity,
            onValueChange = { sensitivity = it },
            valueRange = 0f..1f,
            steps = 10,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Low", fontSize = 12.sp)
            Text(text = "Medium", fontSize = 12.sp)
            Text(text = "High", fontSize = 12.sp)
        }

        // Mostrar el valor actual de sensibilidad
        Text(
            text = "Current value: ${String.format("%.2f", sensitivity)}",
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, start = 8.dp)
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Background Detection
        SettingsSwitchItem(
            title = "Background Detection",
            description = "Detect potholes even when app is in background",
            checked = backgroundDetection,
            onCheckedChange = { backgroundDetection = it }
        )

        // DynamicColor Mode
        SettingsSwitchItem(
            title = "Dynamic Color",
            description = "Use dynamic colors throughout the app",
            checked = dynamicColor,
            onCheckedChange = { dynamicColor = it }
        )

        // Dark Mode
        SettingsSwitchItem(
            title = "Dark Mode",
            description = "Use dark theme throughout the app",
            checked = darkMode,
            onCheckedChange = { darkMode = it }
        )

        // Save Location
        SettingsSwitchItem(
            title = "Save Location Data",
            description = "Store GPS coordinates with detections",
            checked = saveLocation,
            onCheckedChange = { saveLocation = it }
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        if (settingsSaved) {
            Text(
                text = "Settings saved successfully!",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Button to save settings
        Button(
            onClick = {
                viewModel.setDetectionSensitivity(sensitivity)
                settingsSaved = true
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "Save Settings")
        }
    }

// DIÁLOGO PARA CAMBIAR NOMBRE DE USUARIO
    if (showUserDialog) {
        var newUserName by remember { mutableStateOf(userName) }

        AlertDialog(
            onDismissRequest = { showUserDialog = false },
            title = { Text("Change Display Name") },
            text = {
                OutlinedTextField(
                    value = newUserName,
                    onValueChange = { newUserName = it },
                    label = { Text("Display Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newUserName.isNotBlank()) {
                            userName = newUserName
                            viewModel.setUserName(newUserName)
                            showUserDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUserDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}