package com.example.potholedetection_v1.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.potholedetection_v1.PotholeViewModel
import com.example.potholedetection_v1.data.SensorData
import com.example.potholedetection_v1.navigation.AppRoutes

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: PotholeViewModel = viewModel()
) {
    val context = LocalContext.current
    var isDetecting by remember { mutableStateOf(false) }

    // Collect sensor data
    val sensorData by viewModel.sensorData.collectAsState(initial = null)

    // Collect detection count
    val detectionCount by viewModel.detectionCount.collectAsState()

    // Remember if background mode is enabled
    var backgroundModeEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Status card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDetecting) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isDetecting) "Detection Active" else "Detection Inactive",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDetecting) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isDetecting) {
                        if (backgroundModeEnabled) "Running in background mode" else "Currently monitoring road conditions"
                    } else "Tap Start to begin pothole detection",
                    textAlign = TextAlign.Center,
                    color = if (isDetecting) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isDetecting && detectionCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Potholes detected: $detectionCount",
                        fontWeight = FontWeight.Bold,
                        color = if (isDetecting) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Data display (only shown when detecting and not in background mode)
        if (isDetecting && sensorData != null) {
            LiveSensorDataDisplay(sensorData)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Background mode switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Background Detection",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = backgroundModeEnabled,
                onCheckedChange = { enabled ->
                    backgroundModeEnabled = enabled
                }
            )
        }

        // Control buttons
        Button(
            onClick = {
                isDetecting = !isDetecting
                if (isDetecting) {
                    viewModel.startDetection()
                } else {
                    viewModel.stopDetection()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDetecting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (isDetecting) Icons.Filled.Close else Icons.Filled.PlayArrow,
                contentDescription = if (isDetecting) "Stop Detection" else "Start Detection",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = if (isDetecting) "Stop Detection" else "Start Detection",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Map button
        OutlinedButton(
            onClick = { navController.navigate(AppRoutes.MAP) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = "View Map",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = "View Pothole Map")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun LiveSensorDataDisplay(sensorData: SensorData?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Live Sensor Data",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Divider()

            Spacer(modifier = Modifier.height(8.dp))

            if (sensorData != null) {
                // Accelerometer data
                Text(text = "Accelerometer X: ${String.format("%.2f", sensorData.accelerometerX)} m/s²")
                Text(text = "Accelerometer Y: ${String.format("%.2f", sensorData.accelerometerY)} m/s²")
                Text(text = "Accelerometer Z: ${String.format("%.2f", sensorData.accelerometerZ)} m/s²")

                Spacer(modifier = Modifier.height(8.dp))

                // Gyroscope data
                Text(text = "Gyroscope X: ${String.format("%.2f", sensorData.gyroscopeX)} rad/s")
                Text(text = "Gyroscope Y: ${String.format("%.2f", sensorData.gyroscopeY)} rad/s")
                Text(text = "Gyroscope Z: ${String.format("%.2f", sensorData.gyroscopeZ)} rad/s")

                Spacer(modifier = Modifier.height(8.dp))

                // GPS data
                if (sensorData.latitude != 0.0 || sensorData.longitude != 0.0) {
                    Text(text = "Location: ${String.format("%.6f", sensorData.latitude)}, ${String.format("%.6f", sensorData.longitude)}")
                } else {
                    Text(text = "Location: Waiting for GPS...")
                }

                // Speed data
                Text(text = "Speed: ${String.format("%.1f", sensorData.speed * 3.6f)} km/h")
            } else {
                Text(text = "Waiting for sensor data...")
            }
        }
    }
}