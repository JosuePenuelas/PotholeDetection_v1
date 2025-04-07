package com.example.potholedetection_v1.data

/**
 * Data class representing user preferences for the application
 */
data class UserPreferences(
    val detectionSensitivity: Float = 0.7f,    // 0.0 (low) to 1.0 (high)
    val backgroundDetectionEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val saveLocationData: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val dataUploadWifiOnly: Boolean = true,
    val maxStorageSize: Int = 100              // Maximum MB to use for storage
)