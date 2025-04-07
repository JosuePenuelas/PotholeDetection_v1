package com.example.potholedetection_v1.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.annotation.MainThread
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Class responsible for capturing and processing accelerometer data
 */
class AccelerometerManager(private val sensorManager: SensorManager) : SensorEventListener {
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val isListening = AtomicBoolean(false)

    // Low-pass filter coefficient (higher = less filtering)
    private var filterCoefficient = 0.8f

    // Raw accelerometer data
    private val _rawData = MutableStateFlow<AccelerometerData?>(null)
    val rawData: StateFlow<AccelerometerData?> = _rawData.asStateFlow()

    // Filtered accelerometer data
    private val _filteredData = MutableStateFlow<AccelerometerData?>(null)
    val filteredData: StateFlow<AccelerometerData?> = _filteredData.asStateFlow()

    // Latest filtered data
    var latestData: AccelerometerData? = null
        private set

    // Filter state
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    /**
     * Start listening for accelerometer data
     */
    @MainThread
    fun startListening() {
        if (isListening.getAndSet(true)) {
            return // Already listening
        }

        // Start listening at SENSOR_DELAY_GAME (50Hz) rate
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    /**
     * Stop listening for accelerometer data
     */
    @MainThread
    fun stopListening() {
        if (!isListening.getAndSet(false)) {
            return // Already stopped
        }

        sensorManager.unregisterListener(this)
    }

    /**
     * Set the filter coefficient for the low-pass filter
     * @param alpha Value between 0 and 1 (0 = heavy filtering, 1 = no filtering)
     */
    fun setFilterCoefficient(alpha: Float) {
        if (alpha < 0f || alpha > 1f) {
            throw IllegalArgumentException("Filter coefficient must be between 0 and 1")
        }
        filterCoefficient = alpha
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER || !isListening.get()) {
            return
        }

        // Extract raw accelerometer data
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Create raw data object
        val rawData = AccelerometerData(x, y, z, event.timestamp)
        _rawData.value = rawData

        // Apply low-pass filter for smoother readings
        lastX = applyLowPassFilter(x, lastX)
        lastY = applyLowPassFilter(y, lastY)
        lastZ = applyLowPassFilter(z, lastZ)

        // Create filtered data object
        val filteredData = AccelerometerData(lastX, lastY, lastZ, event.timestamp)
        _filteredData.value = filteredData
        latestData = filteredData
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used in this implementation
    }

    /**
     * Apply a low-pass filter to smooth out sensor readings
     */
    private fun applyLowPassFilter(input: Float, lastOutput: Float): Float {
        return lastOutput + filterCoefficient * (input - lastOutput)
    }

    /**
     * Data class representing accelerometer readings
     */
    data class AccelerometerData(
        val x: Float,         // X-axis acceleration (m/s²)
        val y: Float,         // Y-axis acceleration (m/s²)
        val z: Float,         // Z-axis acceleration (m/s²)
        val timestamp: Long   // Timestamp in nanoseconds
    ) {
        /**
         * Calculate the total acceleration magnitude
         */
        fun magnitude(): Float {
            return Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        }

        /**
         * Calculate vertical acceleration (considering device orientation)
         * This is a simplified approach; for more accuracy, consider using
         * rotation vector sensor to determine device orientation
         */
        fun verticalAcceleration(): Float {
            // Assuming vertical is mostly represented by Z-axis when phone is flat
            return z - SensorManager.GRAVITY_EARTH
        }
    }
}