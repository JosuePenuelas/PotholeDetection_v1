// data/PotholeDetection.kt
package com.example.potholedetection_v1.data

import java.util.Date

// No-arg constructor necesario para Firestore
data class PotholeDetection(
    val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Date = Date(),
    val severity: Severity = Severity.LOW,
    val accelerationX: Float = 0f,
    val accelerationY: Float = 0f,
    val accelerationZ: Float = 0f,
    val gyroscopeX: Float = 0f,
    val gyroscopeY: Float = 0f,
    val gyroscopeZ: Float = 0f,
    val speed: Float = 0f,
    val confidence: Float = 0f,
    val isSynced: Boolean = false,

    // NUEVOS CAMPOS PARA IDENTIFICAR AL USUARIO
    val userId: String = "",
    val userName: String = "",
    val deviceModel: String = "",
    val deviceId: String = ""
)

enum class Severity {
    LOW, MEDIUM, HIGH
}