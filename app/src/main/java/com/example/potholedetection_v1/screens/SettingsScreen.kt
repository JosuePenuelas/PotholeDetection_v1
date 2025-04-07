package com.example.potholedetection_v1.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen() {
    var sensitivity by remember { mutableStateOf(0.7f) }
    var backgroundDetection by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }
    var saveLocation by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Background Detection
        SettingsSwitchItem(
            title = "Background Detection",
            description = "Detect potholes even when app is in background",
            checked = backgroundDetection,
            onCheckedChange = { backgroundDetection = it }
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

        // Button to save settings
        Button(
            onClick = { /* Save settings */ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "Save Settings")
        }
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