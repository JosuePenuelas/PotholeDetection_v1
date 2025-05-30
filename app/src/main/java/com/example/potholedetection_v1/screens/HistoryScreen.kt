package com.example.potholedetection_v1.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.potholedetection_v1.PotholeViewModel
import com.example.potholedetection_v1.data.PotholeDetection
import com.example.potholedetection_v1.data.Severity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: PotholeViewModel = viewModel()
) {
    // Recolectar detecciones desde Firebase
    val detections by viewModel.detections.collectAsState(initial = emptyList())

    // Estados para filtros
    var showFilterDialog by remember { mutableStateOf(false) }
    var filterSeverity by remember { mutableStateOf<Severity?>(null) }
    var filterDays by remember { mutableStateOf(30) } // Mostrar últimos 30 días por defecto

    // Filtrar detecciones
    val filteredDetections = remember(detections, filterSeverity, filterDays) {
        val cutoffTime = System.currentTimeMillis() - (filterDays * 24 * 60 * 60 * 1000L)
        detections.filter { detection ->
            val matchesSeverity = filterSeverity?.let { it == detection.severity } ?: true
            val matchesDate = detection.timestamp.time >= cutoffTime
            matchesSeverity && matchesDate
        }.sortedByDescending { it.timestamp } // Más recientes primero
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header con título y botón de filtro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Detection History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { showFilterDialog = true }) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Filter"
                )
            }
        }

        // Información de resumen
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                val highCount = filteredDetections.count { it.severity == Severity.HIGH }
                val mediumCount = filteredDetections.count { it.severity == Severity.MEDIUM }
                val lowCount = filteredDetections.count { it.severity == Severity.LOW }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SeverityChip("High", highCount, Color.Red)
                    SeverityChip("Medium", mediumCount, Color.Yellow)
                    SeverityChip("Low", lowCount, Color.Green)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Total: ${filteredDetections.size} detections",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Lista de detecciones
        if (filteredDetections.isEmpty()) {
            // Estado vacío
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "No detections",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No potholes detected yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Start detection to begin monitoring road conditions",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredDetections) { detection ->
                    HistoryItem(
                        detection = detection,
                        onDeleteClick = {
                            // Opcional: implementar eliminación
                            // viewModel.deletePotholeDetection(detection.id)
                        }
                    )
                }
            }
        }
    }

    // Diálogo de filtros
    if (showFilterDialog) {
        FilterDialog(
            currentSeverity = filterSeverity,
            currentDays = filterDays,
            onSeverityChange = { filterSeverity = it },
            onDaysChange = { filterDays = it },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
fun HistoryItem(
    detection: PotholeDetection,
    onDeleteClick: () -> Unit = {}
) {
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de severidad (código existente)
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        when (detection.severity) {
                            Severity.HIGH -> Color.Red.copy(alpha = 0.8f)
                            Severity.MEDIUM -> Color.Yellow.copy(alpha = 0.8f)
                            Severity.LOW -> Color.Green.copy(alpha = 0.8f)
                        },
                        shape = MaterialTheme.shapes.small
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                // Ubicación
                Text(
                    text = "${String.format("%.6f", detection.latitude)}, ${String.format("%.6f", detection.longitude)}",
                    fontWeight = FontWeight.Bold
                )

                // Fecha y hora
                Text(
                    text = dateFormatter.format(detection.timestamp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // NUEVA LÍNEA: Información del usuario
                Text(
                    text = "Detected by: ${detection.userName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Información adicional (código existente)
                Row(
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Speed: ${String.format("%.1f", detection.speed * 3.6f)} km/h",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Confidence: ${String.format("%.1f%%", detection.confidence * 100)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // NUEVA LÍNEA: Información del dispositivo
                Text(
                    text = "Device: ${detection.deviceModel}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Columna de severidad (código existente)
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Severity",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = detection.severity.name,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SeverityChip(label: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FilterDialog(
    currentSeverity: Severity?,
    currentDays: Int,
    onSeverityChange: (Severity?) -> Unit,
    onDaysChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Detections") },
        text = {
            Column {
                // Filtro por severidad
                Text(
                    text = "Filter by Severity:",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentSeverity == null,
                        onClick = { onSeverityChange(null) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = currentSeverity == Severity.HIGH,
                        onClick = { onSeverityChange(Severity.HIGH) },
                        label = { Text("High") }
                    )
                    FilterChip(
                        selected = currentSeverity == Severity.MEDIUM,
                        onClick = { onSeverityChange(Severity.MEDIUM) },
                        label = { Text("Medium") }
                    )
                    FilterChip(
                        selected = currentSeverity == Severity.LOW,
                        onClick = { onSeverityChange(Severity.LOW) },
                        label = { Text("Low") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filtro por tiempo
                Text(
                    text = "Show detections from last:",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentDays == 1,
                        onClick = { onDaysChange(1) },
                        label = { Text("1 Day") }
                    )
                    FilterChip(
                        selected = currentDays == 7,
                        onClick = { onDaysChange(7) },
                        label = { Text("7 Days") }
                    )
                    FilterChip(
                        selected = currentDays == 30,
                        onClick = { onDaysChange(30) },
                        label = { Text("30 Days") }
                    )
                    FilterChip(
                        selected = currentDays == 365,
                        onClick = { onDaysChange(365) },
                        label = { Text("All") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}