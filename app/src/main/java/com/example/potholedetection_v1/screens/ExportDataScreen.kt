package com.example.potholedetection_v1.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.potholedetection_v1.PotholeViewModel
import com.example.potholedetection_v1.data.PotholeDetection
import com.example.potholedetection_v1.data.Severity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExportDataScreen(
    viewModel: PotholeViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Recolectar detecciones desde Firebase
    val detections by viewModel.detections.collectAsState(initial = emptyList())

    // Estados para mostrar progreso de exportación
    var isExporting by remember { mutableStateOf(false) }
    var exportSuccess by remember { mutableStateOf<String?>(null) }
    var exportError by remember { mutableStateOf<String?>(null) }

    // Calcular estadísticas
    val highCount = detections.count { it.severity == Severity.HIGH }
    val mediumCount = detections.count { it.severity == Severity.MEDIUM }
    val lowCount = detections.count { it.severity == Severity.LOW }

    // Calcular tamaño aproximado de datos
    val dataSize = detections.size * 0.5 // Aproximadamente 0.5 KB por detección
    val dataSizeText = if (dataSize < 1024) {
        "${String.format("%.1f", dataSize)} KB"
    } else {
        "${String.format("%.1f", dataSize / 1024)} MB"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Export Data",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Data Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "All time",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Total detections: ${detections.size}")
                Text(text = "High severity: $highCount")
                Text(text = "Medium severity: $mediumCount")
                Text(text = "Low severity: $lowCount")

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Data size: $dataSizeText")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Export Options",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Mostrar mensaje de estado
        exportSuccess?.let { message ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        exportError?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Export as CSV
        ExportOptionItem(
            icon = Icons.Default.Create,
            title = "Export as CSV",
            description = "Spreadsheet format for data analysis",
            isLoading = isExporting,
            onClick = {
                scope.launch {
                    exportSuccess = null
                    exportError = null
                    isExporting = true

                    try {
                        val result = exportToCSV(context, detections)
                        exportSuccess = result
                    } catch (e: Exception) {
                        exportError = "Error exporting CSV: ${e.message}"
                    } finally {
                        isExporting = false
                    }
                }
            }
        )

        // Export as KML
        ExportOptionItem(
            icon = Icons.Default.Place,
            title = "Export as KML",
            description = "For Google Earth and other mapping applications",
            onClick = {
                scope.launch {
                    exportSuccess = null
                    exportError = null

                    try {
                        val result = exportToKML(context, detections)
                        exportSuccess = result
                    } catch (e: Exception) {
                        exportError = "Error exporting KML: ${e.message}"
                    }
                }
            }
        )

        // Share with authorities
        ExportOptionItem(
            icon = Icons.Default.Share,
            title = "Share with Authorities",
            description = "Send data directly to city maintenance department",
            onClick = {
                scope.launch {
                    try {
                        shareWithAuthorities(context, detections)
                    } catch (e: Exception) {
                        exportError = "Error sharing data: ${e.message}"
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = if (isLoading) { {} } else onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
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

            if (!isLoading) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Export",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Función para exportar a CSV
private suspend fun exportToCSV(context: Context, detections: List<PotholeDetection>): String {
    return withContext(Dispatchers.IO) {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fileName = "potholes_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"

        val file = File(context.getExternalFilesDir(null), fileName)

        FileWriter(file).use { writer ->
            // ENCABEZADOS ACTUALIZADOS
            writer.append("ID,Timestamp,Latitude,Longitude,Severity,Confidence,Speed_kmh,AccelX,AccelY,AccelZ,GyroX,GyroY,GyroZ,UserID,UserName,DeviceModel,DeviceID,IsSynced\n")

            // Escribir datos
            detections.forEach { detection ->
                writer.append("${detection.id},")
                writer.append("${dateFormatter.format(detection.timestamp)},")
                writer.append("${detection.latitude},")
                writer.append("${detection.longitude},")
                writer.append("${detection.severity},")
                writer.append("${detection.confidence},")
                writer.append("${detection.speed * 3.6f},")
                writer.append("${detection.accelerationX},")
                writer.append("${detection.accelerationY},")
                writer.append("${detection.accelerationZ},")
                writer.append("${detection.gyroscopeX},")
                writer.append("${detection.gyroscopeY},")
                writer.append("${detection.gyroscopeZ},")
                // NUEVAS COLUMNAS
                writer.append("${detection.userId},")
                writer.append("${detection.userName},")
                writer.append("${detection.deviceModel},")
                writer.append("${detection.deviceId},")
                writer.append("${detection.isSynced}\n")
            }
        }

        shareFile(context, file, "text/csv")
        "CSV exported successfully: $fileName"
    }
}

// Función para exportar a KML
private suspend fun exportToKML(context: Context, detections: List<PotholeDetection>): String {
    return withContext(Dispatchers.IO) {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fileName = "potholes_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.kml"

        val file = File(context.getExternalFilesDir(null), fileName)

        FileWriter(file).use { writer ->
            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            writer.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
            writer.append("<Document>\n")
            writer.append("<name>Pothole Detections</name>\n")
            writer.append("<description>Detected potholes from mobile app</description>\n")

            // Estilos para diferentes severidades
            writer.append("<Style id=\"highSeverity\">\n")
            writer.append("<IconStyle><Icon><href>http://maps.google.com/mapfiles/kml/paddle/red-circle.png</href></Icon></IconStyle>\n")
            writer.append("</Style>\n")

            writer.append("<Style id=\"mediumSeverity\">\n")
            writer.append("<IconStyle><Icon><href>http://maps.google.com/mapfiles/kml/paddle/ylw-circle.png</href></Icon></IconStyle>\n")
            writer.append("</Style>\n")

            writer.append("<Style id=\"lowSeverity\">\n")
            writer.append("<IconStyle><Icon><href>http://maps.google.com/mapfiles/kml/paddle/grn-circle.png</href></Icon></IconStyle>\n")
            writer.append("</Style>\n")

            // Escribir placemarks
            detections.forEach { detection ->
                val styleUrl = when (detection.severity) {
                    Severity.HIGH -> "#highSeverity"
                    Severity.MEDIUM -> "#mediumSeverity"
                    Severity.LOW -> "#lowSeverity"
                }

                writer.append("<Placemark>\n")
                writer.append("<name>Pothole - ${detection.severity}</name>\n")
                writer.append("<description>")
                writer.append("Detected: ${dateFormatter.format(detection.timestamp)}&lt;br/&gt;")
                writer.append("Severity: ${detection.severity}&lt;br/&gt;")
                writer.append("Confidence: ${String.format("%.1f%%", detection.confidence * 100)}&lt;br/&gt;")
                writer.append("Speed: ${String.format("%.1f", detection.speed * 3.6f)} km/h")
                writer.append("</description>\n")
                writer.append("<styleUrl>$styleUrl</styleUrl>\n")
                writer.append("<Point>\n")
                writer.append("<coordinates>${detection.longitude},${detection.latitude},0</coordinates>\n")
                writer.append("</Point>\n")
                writer.append("</Placemark>\n")
            }

            writer.append("</Document>\n")
            writer.append("</kml>\n")
        }

        // Compartir el archivo
        shareFile(context, file, "application/vnd.google-earth.kml+xml")

        "KML exported successfully: $fileName"
    }
}

// Función para compartir con autoridades
private suspend fun shareWithAuthorities(context: Context, detections: List<PotholeDetection>) {
    withContext(Dispatchers.IO) {
        // Crear un reporte resumido
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fileName = "pothole_report_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.txt"

        val file = File(context.getExternalFilesDir(null), fileName)

        FileWriter(file).use { writer ->
            writer.append("POTHOLE DETECTION REPORT\n")
            writer.append("Generated: ${dateFormatter.format(Date())}\n")
            writer.append("=".repeat(50) + "\n\n")

            writer.append("SUMMARY:\n")
            writer.append("Total detections: ${detections.size}\n")
            writer.append("High severity: ${detections.count { it.severity == Severity.HIGH }}\n")
            writer.append("Medium severity: ${detections.count { it.severity == Severity.MEDIUM }}\n")
            writer.append("Low severity: ${detections.count { it.severity == Severity.LOW }}\n\n")

            writer.append("HIGH PRIORITY LOCATIONS:\n")
            detections.filter { it.severity == Severity.HIGH }
                .sortedByDescending { it.timestamp }
                .forEach { detection ->
                    writer.append("- ${detection.latitude}, ${detection.longitude} ")
                    writer.append("(${dateFormatter.format(detection.timestamp)})\n")
                }
        }

        // Crear intent para compartir
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Pothole Detection Report")
            putExtra(Intent.EXTRA_TEXT, "Please find attached the pothole detection report for your review.")

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share with Authorities"))
    }
}

// Función auxiliar para compartir archivos
private fun shareFile(context: Context, file: File, mimeType: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share File"))
}