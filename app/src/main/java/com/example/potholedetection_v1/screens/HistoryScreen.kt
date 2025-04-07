package com.example.potholedetection_v1.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HistoryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Detection History",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Sample history items
        HistoryItem(
            date = "Apr 5, 2025",
            location = "Calle Universidad, Mexicali",
            severity = "High",
            color = Color.Red.copy(alpha = 0.8f)
        )

        HistoryItem(
            date = "Apr 5, 2025",
            location = "Blvd. Benito Juárez, Mexicali",
            severity = "Medium",
            color = Color.Yellow.copy(alpha = 0.8f)
        )

        HistoryItem(
            date = "Apr 4, 2025",
            location = "Calzada Cetys, Mexicali",
            severity = "Low",
            color = Color.Green.copy(alpha = 0.8f)
        )

        HistoryItem(
            date = "Apr 4, 2025",
            location = "Av. Reforma, Mexicali",
            severity = "High",
            color = Color.Red.copy(alpha = 0.8f)
        )

        HistoryItem(
            date = "Apr 3, 2025",
            location = "Blvd. López Mateos, Mexicali",
            severity = "Medium",
            color = Color.Yellow.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun HistoryItem(date: String, location: String, severity: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, shape = MaterialTheme.shapes.small)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = location,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = date,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Severity",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = severity,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}