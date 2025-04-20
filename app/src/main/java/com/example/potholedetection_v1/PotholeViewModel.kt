package com.example.potholedetection_v1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.potholedetection_v1.data.PotholeDetection
import com.example.potholedetection_v1.data.SensorData
import com.example.potholedetection_v1.data.Severity
import com.example.potholedetection_v1.model.ThresholdBasedDetector  // Cambiado
import com.example.potholedetection_v1.sensor.PotholeSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class PotholeViewModel(application: Application) : AndroidViewModel(application) {
    // Gestor de sensores
    private val sensorManager = PotholeSensorManager(application.applicationContext)

    // Modelo de detección de baches con el nuevo detector
    private val detectionModel = ThresholdBasedDetector(application.applicationContext)

    // Estado para mostrar en la UI
    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData.asStateFlow()

    private val _detectionCount = MutableStateFlow(0)
    val detectionCount: StateFlow<Int> = _detectionCount.asStateFlow()

    private val _detections = MutableStateFlow<List<PotholeDetection>>(emptyList())
    val detections: StateFlow<List<PotholeDetection>> = _detections.asStateFlow()

    // Estado de detección
    private var isDetectionActive = false

    // No es necesario inicializar el detector porque se carga automáticamente

    init {
        // Observar datos de sensores
        viewModelScope.launch {
            sensorManager.sensorData.collect { data ->
                data?.let {
                    _sensorData.value = it
                    if (isDetectionActive) {
                        processSensorData(it)
                    }
                }
            }
        }
    }

    fun startDetection() {
        if (!isDetectionActive) {
            isDetectionActive = true
            sensorManager.startDetection()
        }
    }

    fun stopDetection() {
        if (isDetectionActive) {
            isDetectionActive = false
            sensorManager.stopDetection()
        }
    }

    private fun processSensorData(data: SensorData) {
        // Extraer características (mismo orden que en Python)
        val features = floatArrayOf(
            data.accelerometerX,
            data.accelerometerY,
            data.accelerometerZ,
            data.gyroscopeX,
            data.gyroscopeY,
            data.gyroscopeZ,
            data.speed,
            calculateAccelMagnitude(data),
            calculateGyroMagnitude(data),
            calculateZAccelDeviation(data)
        )

        // Aumentar este valor para reducir la sensibilidad (ej: de 0.6f a 0.75f)
        val detectionThreshold = 0.75f  // Era 0.6f, ahora es más exigente

        // Predecir con el modelo
        val confidence = detectionModel.detectPothole(features)

        // Detectar bache si la confianza supera el umbral
        if (confidence > detectionThreshold) { // Ajusta este umbral según sea necesario
            // Crear detección
            val pothole = PotholeDetection(
                id = UUID.randomUUID().toString(),
                latitude = data.latitude,
                longitude = data.longitude,
                timestamp = Date(),
                severity = calculateSeverity(confidence),
                accelerationX = data.accelerometerX,
                accelerationY = data.accelerometerY,
                accelerationZ = data.accelerometerZ,
                gyroscopeX = data.gyroscopeX,
                gyroscopeY = data.gyroscopeY,
                gyroscopeZ = data.gyroscopeZ,
                speed = data.speed,
                confidence = confidence
            )

            // Incrementar contador
            _detectionCount.value += 1

            // Añadir a la lista de detecciones
            val currentList = _detections.value.toMutableList()
            currentList.add(pothole)
            _detections.value = currentList
        }
    }

    private fun calculateAccelMagnitude(data: SensorData): Float {
        return sqrt(
            data.accelerometerX.pow(2) +
                    data.accelerometerY.pow(2) +
                    data.accelerometerZ.pow(2)
        )
    }

    private fun calculateGyroMagnitude(data: SensorData): Float {
        return sqrt(
            data.gyroscopeX.pow(2) +
                    data.gyroscopeY.pow(2) +
                    data.gyroscopeZ.pow(2)
        )
    }

    private fun calculateZAccelDeviation(data: SensorData): Float {
        return abs(data.accelerometerZ - 1.0f)
    }

    private fun calculateSeverity(confidence: Float): Severity {
        return when {
            confidence > 0.85f -> Severity.HIGH
            confidence > 0.7f -> Severity.MEDIUM
            else -> Severity.LOW
        }
    }

    fun getRecentDetections(limit: Int = 50) = _detections.value.take(limit)

    fun exportDetections(): String {
        val sb = StringBuilder()
        sb.appendLine("timestamp,latitude,longitude,severity,confidence")

        _detections.value.forEach { detection ->
            sb.appendLine("${detection.timestamp.time},${detection.latitude}," +
                    "${detection.longitude},${detection.severity},${detection.confidence}")
        }

        return sb.toString()
    }

    fun clearDetectionCount() {
        _detectionCount.value = 0
    }

    override fun onCleared() {
        stopDetection()
        sensorManager.cleanup()
        super.onCleared()
    }
}