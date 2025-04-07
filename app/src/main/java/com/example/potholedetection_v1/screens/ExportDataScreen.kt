package com.example.potholedetection_v1.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExportDataScreen() {
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
                        text = "Last 30 days",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Total detections: 78")
                Text(text = "High severity: 23")
                Text(text = "Medium severity: 35")
                Text(text = "Low severity: 20")

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Data size: 1.2 MB")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Export Options",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Export as CSV
        ExportOptionItem(
            icon = Icons.Default.Create,
            title = "Export as CSV",
            description = "Spreadsheet format for data analysis",
            onClick = { /* Export as CSV */ }
        )

        // Export as KML
        ExportOptionItem(
            icon = Icons.Default.Place,
            title = "Export as KML",
            description = "For Google Earth and other mapping applications",
            onClick = { /* Export as KML */ }
        )

        // Share with authorities
        ExportOptionItem(
            icon = Icons.Default.Share,
            title = "Share with Authorities",
            description = "Send data directly to city maintenance department",
            onClick = { /* Share with authorities */ }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )

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

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Export",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}