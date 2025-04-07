package com.example.potholedetection_v1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.potholedetection_v1.data.PotholeDetection
import com.example.potholedetection_v1.data.SensorData
import com.example.potholedetection_v1.data.Severity
import com.example.potholedetection_v1.sensor.PotholeSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

/**
 * ViewModel responsible for handling pothole detection data and operations
 */
class PotholeViewModel(application: Application) : AndroidViewModel(application) {

    // Sensor manager for handling all sensor operations
    private val sensorManager = PotholeSensorManager(application.applicationContext)

    // State flows for UI updates
    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData.asStateFlow()

    private val _detectionCount = MutableStateFlow(0)
    val detectionCount: StateFlow<Int> = _detectionCount.asStateFlow()

    private val _detections = MutableStateFlow<List<PotholeDetection>>(emptyList())
    val detections: StateFlow<List<PotholeDetection>> = _detections.asStateFlow()

    // Repository would be injected here in a production app
    // private val repository: PotholeRepository

    init {
        // Collect sensor data
        viewModelScope.launch {
            sensorManager.sensorData.collect { data ->
                _sensorData.value = data
            }
        }

        // Observe detection events
        sensorManager.detectionEvents.observeForever { event ->
            // Increment detection count
            _detectionCount.value += 1

            // Create PotholeDetection object
            val detection = PotholeDetection(
                id = UUID.randomUUID().toString(),
                latitude = event.latitude,
                longitude = event.longitude,
                timestamp = Date(event.timestamp),
                severity = when (event.severity) {
                    PotholeSensorManager.PotholeDetectionEvent.Severity.LOW -> Severity.LOW
                    PotholeSensorManager.PotholeDetectionEvent.Severity.MEDIUM -> Severity.MEDIUM
                    PotholeSensorManager.PotholeDetectionEvent.Severity.HIGH -> Severity.HIGH
                },
                // In a real app, you'd want to include the actual sensor data here
                accelerationX = 0f,
                accelerationY = 0f,
                accelerationZ = 0f,
                gyroscopeX = 0f,
                gyroscopeY = 0f,
                gyroscopeZ = 0f,
                speed = _sensorData.value?.speed ?: 0f,
                confidence = event.confidence
            )

            // Add to current list
            val currentList = _detections.value.toMutableList()
            currentList.add(detection)
            _detections.value = currentList

            // In a real app, save to repository
            // viewModelScope.launch {
            //    repository.insertDetection(detection)
            // }
        }
    }

    /**
     * Start pothole detection
     */
    fun startDetection() {
        sensorManager.startDetection()
    }

    /**
     * Stop pothole detection
     */
    fun stopDetection() {
        sensorManager.stopDetection()
    }

    /**
     * Set detection sensitivity
     */
    fun setSensitivity(sensitivity: Float) {
        // Implement based on your detection algorithm
    }

    /**
     * Get detections within a date range
     */
    fun getDetectionsInRange(startDate: Date, endDate: Date) {
        // In a real app, load from repository
        // viewModelScope.launch {
        //    val detections = repository.getDetectionsInRange(startDate, endDate)
        //    _detections.value = detections
        // }
    }

    /**
     * Clear detection count
     */
    fun clearDetectionCount() {
        _detectionCount.value = 0
    }

    override fun onCleared() {
        // Clean up resources
        sensorManager.cleanup()
        super.onCleared()
    }
}