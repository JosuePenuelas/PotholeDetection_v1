package com.example.potholedetection_v1.data

import java.util.Date

/**
 * Data class representing a pothole detection event
 */
data class PotholeDetection(
    val id: String,                // Unique identifier for the detection
    val latitude: Double,          // GPS latitude
    val longitude: Double,         // GPS longitude
    val timestamp: Date,           // When the pothole was detected
    val severity: Severity,        // Calculated severity of the pothole
    val accelerationX: Float,      // Raw accelerometer data
    val accelerationY: Float,
    val accelerationZ: Float,
    val gyroscopeX: Float,         // Raw gyroscope data
    val gyroscopeY: Float,
    val gyroscopeZ: Float,
    val speed: Float,              // Vehicle speed at detection time (km/h)
    val confidence: Float,         // Detection confidence (0-1)
    val isSynced: Boolean = false  // Whether the detection has been synced to the server
)

/**
 * Enum representing pothole severity levels
 */
enum class Severity {
    LOW,
    MEDIUM,
    HIGH
}