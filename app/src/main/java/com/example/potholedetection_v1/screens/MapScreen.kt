package com.example.potholedetection_v1.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.potholedetection_v1.PotholeViewModel
import com.example.potholedetection_v1.data.Severity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: PotholeViewModel = viewModel(),
    targetLat: Double? = null,
    targetLng: Double? = null
) {
    // Recolectar datos de detecciones
    val detections by viewModel.detections.collectAsState(initial = emptyList())

    // Estado para el mapa
    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = true,
                mapType = MapType.NORMAL
            )
        )
    }

    // Estado de la cámara
    var cameraPositionState = rememberCameraPositionState {
        // Si hay coordenadas específicas, centrar ahí
        if (targetLat != null && targetLng != null) {
            position = CameraPosition.fromLatLngZoom(
                LatLng(targetLat, targetLng),
                16f // Zoom más cercano para mostrar la ubicación específica
            )
        } else if (detections.isNotEmpty()) {
            // Si no, usar la primera detección
            position = CameraPosition.fromLatLngZoom(
                LatLng(detections[0].latitude, detections[0].longitude),
                14f
            )
        } else {
            // Posición predeterminada (centro de Mexicali)
            position = CameraPosition.fromLatLngZoom(
                LatLng(32.6278, -115.4545),
                13f
            )
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Si hay una ubicación específica, centrar ahí
                    if (targetLat != null && targetLng != null) {
                        val newPosition = CameraPosition.Builder()
                            .target(LatLng(targetLat, targetLng))
                            .zoom(16f)
                            .build()
                        cameraPositionState.move(CameraUpdateFactory.newCameraPosition(newPosition))
                    } else if (detections.isNotEmpty()) {
                        // Si no, centrar en la última detección
                        val lastDetection = detections[0]
                        val newPosition = CameraPosition.Builder()
                            .target(LatLng(lastDetection.latitude, lastDetection.longitude))
                            .zoom(15f)
                            .build()
                        cameraPositionState.move(CameraUpdateFactory.newCameraPosition(newPosition))
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Center Map"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Si estamos mostrando una ubicación específica, mostrar información especial
                    if (targetLat != null && targetLng != null) {
                        Text(
                            text = "Viewing Specific Location",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Lat: ${String.format("%.6f", targetLat)}, Lng: ${String.format("%.6f", targetLng)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = "Potholes Detected: ${detections.size}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Contador por severidad
                    val highCount = detections.count { it.severity == Severity.HIGH }
                    val mediumCount = detections.count { it.severity == Severity.MEDIUM }
                    val lowCount = detections.count { it.severity == Severity.LOW }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SeverityTag("High", highCount, Color.Red)
                        SeverityTag("Medium", mediumCount, Color.Yellow)
                        SeverityTag("Low", lowCount, Color.Green)
                    }
                }
            }

            // Mapa
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = true
                ),
                onMapLoaded = {
                    // Si hay coordenadas específicas, centrar automáticamente
                    if (targetLat != null && targetLng != null) {
                        val targetPosition = CameraPosition.Builder()
                            .target(LatLng(targetLat, targetLng))
                            .zoom(16f)
                            .build()
                        cameraPositionState.move(CameraUpdateFactory.newCameraPosition(targetPosition))
                    }
                }
            ) {
                // Añadir marcadores para cada bache
                detections.forEach { detection ->
                    val position = LatLng(detection.latitude, detection.longitude)

                    // Determinar color según severidad
                    val markerColor = when (detection.severity) {
                        Severity.HIGH -> BitmapDescriptorFactory.HUE_RED
                        Severity.MEDIUM -> BitmapDescriptorFactory.HUE_YELLOW
                        Severity.LOW -> BitmapDescriptorFactory.HUE_GREEN
                    }

                    Marker(
                        state = MarkerState(position = position),
                        title = "Pothole",
                        snippet = "Detected: ${detection.timestamp}, Severity: ${detection.severity}",
                        icon = BitmapDescriptorFactory.defaultMarker(markerColor)
                    )
                }

                // Si estamos mostrando una ubicación específica, añadir un marcador especial
                if (targetLat != null && targetLng != null) {
                    Marker(
                        state = MarkerState(position = LatLng(targetLat, targetLng)),
                        title = "Selected Pothole",
                        snippet = "Location from history",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }
            }
        }
    }
}

@Composable
fun SeverityTag(label: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.bodySmall
        )
    }
}