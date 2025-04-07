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
 * Class responsible for capturing and processing gyroscope data
 */
class GyroscopeManager(private val sensorManager: SensorManager) : SensorEventListener {
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val isListening = AtomicBoolean(false)

    // Low-pass filter coefficient (higher = less filtering)
    private var filterCoefficient = 0.8f

    // Raw gyroscope data
    private val _rawData = MutableStateFlow<GyroscopeData?>(null)
    val rawData: StateFlow<GyroscopeData?> = _rawData.asStateFlow()

    // Filtered gyroscope data
    private val _filteredData = MutableStateFlow<GyroscopeData?>(null)
    val filteredData: StateFlow<GyroscopeData?> = _filteredData.asStateFlow()

    // Latest filtered data
    var latestData: GyroscopeData? = null
        private set

    // Filter state
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    /**
     * Start listening for gyroscope data
     */
    @MainThread
    fun startListening() {
        if (isListening.getAndSet(true)) {
            return // Already listening
        }

        // Start listening at SENSOR_DELAY_GAME (50Hz) rate
        sensorManager.registerListener(
            this,
            gyroscope,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    /**
     * Stop listening for gyroscope data
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
        if (event.sensor.type != Sensor.TYPE_GYROSCOPE || !isListening.get()) {
            return
        }

        // Extract raw gyroscope data (angular velocities in rad/s)
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Create raw data object
        val rawData = GyroscopeData(x, y, z, event.timestamp)
        _rawData.value = rawData

        // Apply low-pass filter for smoother readings
        lastX = applyLowPassFilter(x, lastX)
        lastY = applyLowPassFilter(y, lastY)
        lastZ = applyLowPassFilter(z, lastZ)

        // Create filtered data object
        val filteredData = GyroscopeData(lastX, lastY, lastZ, event.timestamp)
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
     * Data class representing gyroscope readings
     */
    data class GyroscopeData(
        val x: Float,         // X-axis angular velocity (rad/s)
        val y: Float,         // Y-axis angular velocity (rad/s)
        val z: Float,         // Z-axis angular velocity (rad/s)
        val timestamp: Long   // Timestamp in nanoseconds
    ) {
        /**
         * Calculate the total rotational magnitude
         */
        fun magnitude(): Float {
            return Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        }

        /**
         * Calculate pitch rate (rotation around x-axis)
         */
        fun getPitchRate(): Float = x

        /**
         * Calculate roll rate (rotation around y-axis)
         */
        fun getRollRate(): Float = y

        /**
         * Calculate yaw rate (rotation around z-axis)
         */
        fun getYawRate(): Float = z
    }
}