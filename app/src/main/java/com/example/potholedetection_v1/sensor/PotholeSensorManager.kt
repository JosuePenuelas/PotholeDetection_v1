package com.example.potholedetection_v1.sensor

import android.content.Context
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.potholedetection_v1.data.SensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Main class responsible for managing all sensor data collection
 */
class PotholeSensorManager(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometerManager = AccelerometerManager(sensorManager)
    private val gyroscopeManager = GyroscopeManager(sensorManager)
    private val locationManager = LocationManager(context)

    private val isRunning = AtomicBoolean(false)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // Combined sensor data as a StateFlow for real-time updates
    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData.asStateFlow()

    // Detection events as LiveData
    private val _detectionEvents = MutableLiveData<PotholeDetectionEvent>()
    val detectionEvents: LiveData<PotholeDetectionEvent> = _detectionEvents

    // Sampling rate in milliseconds
    private var samplingRate = 20L // 50Hz

    /**
     * Start collecting sensor data
     */
    fun startDetection() {
        if (isRunning.getAndSet(true)) {
            return // Already running
        }

        accelerometerManager.startListening()
        gyroscopeManager.startListening()
        locationManager.startLocationUpdates()

        // Start combining data
        coroutineScope.launch {
            while (isRunning.get()) {
                updateSensorData()
                kotlinx.coroutines.delay(samplingRate)
            }
        }
    }

    /**
     * Stop collecting sensor data
     */
    fun stopDetection() {
        if (!isRunning.getAndSet(false)) {
            return // Already stopped
        }

        accelerometerManager.stopListening()
        gyroscopeManager.stopListening()
        locationManager.stopLocationUpdates()
    }

    /**
     * Combine the latest data from all sensors
     */
    private fun updateSensorData() {
        val accelData = accelerometerManager.latestData
        val gyroData = gyroscopeManager.latestData
        val locationData = locationManager.latestLocation

        if (accelData != null && gyroData != null) {
            val data = SensorData(
                accelerometerX = accelData.x,
                accelerometerY = accelData.y,
                accelerometerZ = accelData.z,
                gyroscopeX = gyroData.x,
                gyroscopeY = gyroData.y,
                gyroscopeZ = gyroData.z,
                latitude = locationData?.latitude ?: 0.0,
                longitude = locationData?.longitude ?: 0.0,
                speed = locationData?.speed ?: 0f,
                timestamp = System.currentTimeMillis()
            )

            _sensorData.value = data

            // Here we would detect potholes using the sensor data
            // This is a placeholder for the actual detection algorithm
            detectPothole(data)
        }
    }

    /**
     * Set the sampling rate for sensor data collection
     * @param rateHz Sampling rate in Hertz (default is 50Hz)
     */
    fun setSamplingRate(rateHz: Int) {
        samplingRate = (1000 / rateHz).toLong()
    }

    /**
     * Example detection algorithm (to be replaced with ML model)
     */
    private fun detectPothole(data: SensorData) {
        // Simple threshold-based detection (for demonstration)
        // In reality, you would use a trained ML model here
        val accelMagnitude = Math.sqrt(
            (data.accelerometerX * data.accelerometerX +
                    data.accelerometerY * data.accelerometerY +
                    data.accelerometerZ * data.accelerometerZ).toDouble()
        ).toFloat()

        // Detect sudden vertical acceleration changes
        if (accelMagnitude > 15f && data.speed > 5f) { // Only detect when moving
            val severity = when {
                accelMagnitude > 25f -> PotholeDetectionEvent.Severity.HIGH
                accelMagnitude > 20f -> PotholeDetectionEvent.Severity.MEDIUM
                else -> PotholeDetectionEvent.Severity.LOW
            }

            _detectionEvents.postValue(
                PotholeDetectionEvent(
                    latitude = data.latitude,
                    longitude = data.longitude,
                    timestamp = System.currentTimeMillis(),
                    severity = severity,
                    confidence = (accelMagnitude - 15f) / 15f // Simple confidence score
                )
            )
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        stopDetection()
    }

    /**
     * Data class representing a pothole detection event
     */
    data class PotholeDetectionEvent(
        val latitude: Double,
        val longitude: Double,
        val timestamp: Long,
        val severity: Severity,
        val confidence: Float
    ) {
        enum class Severity {
            LOW, MEDIUM, HIGH
        }
    }
}