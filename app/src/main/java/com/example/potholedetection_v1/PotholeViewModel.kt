package com.example.potholedetection_v1

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.potholedetection_v1.data.PotholeDetection
import com.example.potholedetection_v1.data.SensorData
import com.example.potholedetection_v1.data.Severity
import com.example.potholedetection_v1.model.PotholeDetectionModel
import com.example.potholedetection_v1.model.ThresholdBasedDetector  // Cambiado
import com.example.potholedetection_v1.repository.FirebasePotholeRepository
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

    // Modelo de detección de baches
    private val detectionModel = PotholeDetectionModel(application.applicationContext)

    // Repositorio de Firebase
    private val repository = FirebasePotholeRepository()

    // Estados para la UI
    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData.asStateFlow()

    private val _detectionCount = MutableStateFlow(0)
    val detectionCount: StateFlow<Int> = _detectionCount.asStateFlow()

    // Observar detecciones desde Firebase
    val detections = repository.potholes

    // Estado de detección
    private var isDetectionActive = false
    private var detectionSensitivity = 0.6f // Ajustado para ser menos sensible

    init {
        // Inicializar con preferencias guardadas
        val prefs = application.getSharedPreferences("pothole_prefs", Context.MODE_PRIVATE)
        detectionSensitivity = prefs.getFloat("detection_sensitivity", 0.6f)

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
        // Extraer características
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

        // Predecir con el modelo
        val confidence = detectionModel.predict(features)

        // Detectar bache si la confianza supera el umbral
        if (confidence > detectionSensitivity) {
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
                confidence = confidence,
                isSynced = false
            )

            // Incrementar contador
            _detectionCount.value += 1

            // Guardar en Firebase
            viewModelScope.launch {
                repository.savePotholeDetection(pothole)
            }
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

    fun setDetectionSensitivity(value: Float) {
        detectionSensitivity = value
        // Guardar en SharedPreferences
        val prefs = getApplication<Application>().getSharedPreferences("pothole_prefs", Context.MODE_PRIVATE)
        prefs.edit().putFloat("detection_sensitivity", value).apply()
    }

    fun getDetectionSensitivity() = detectionSensitivity

    override fun onCleared() {
        stopDetection()
        sensorManager.cleanup()
        super.onCleared()
    }
}