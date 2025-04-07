package com.example.potholedetection_v1.data

/**
 * Data class representing raw sensor data at a given moment
 */
data class SensorData(
    val accelerometerX: Float,
    val accelerometerY: Float,
    val accelerometerZ: Float,
    val gyroscopeX: Float,
    val gyroscopeY: Float,
    val gyroscopeZ: Float,
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val timestamp: Long
)