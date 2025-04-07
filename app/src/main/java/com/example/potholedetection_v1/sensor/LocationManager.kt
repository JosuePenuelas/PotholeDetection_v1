package com.example.potholedetection_v1.sensor

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.annotation.MainThread
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Class responsible for handling GPS location updates
 */
class LocationManager(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val isTracking = AtomicBoolean(false)

    // Location data
    private val _locationData = MutableStateFlow<LocationData?>(null)
    val locationData: StateFlow<LocationData?> = _locationData.asStateFlow()

    // Latest location data
    var latestLocation: LocationData? = null
        private set

    // Location request configuration
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
        .setMinUpdateIntervalMillis(500L)
        .setMaxUpdateDelayMillis(2000L)
        .build()

    // Location callback
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                val data = LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    speed = location.speed,
                    accuracy = location.accuracy,
                    bearing = location.bearing,
                    timestamp = location.time
                )
                _locationData.value = data
                latestLocation = data
            }
        }
    }

    /**
     * Start receiving location updates
     * Note: Permission handling should be done before calling this method
     */
    @SuppressLint("MissingPermission")
    @MainThread
    fun startLocationUpdates() {
        if (isTracking.getAndSet(true)) {
            return // Already tracking
        }

        // Request location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    /**
     * Stop receiving location updates
     */
    @MainThread
    fun stopLocationUpdates() {
        if (!isTracking.getAndSet(false)) {
            return // Already stopped
        }

        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Set the update interval for location updates
     * @param intervalMs Interval in milliseconds
     */
    fun setUpdateInterval(intervalMs: Long) {
        // To update the interval, we need to stop and restart tracking
        val wasTracking = isTracking.get()

        if (wasTracking) {
            stopLocationUpdates()
        }

        // Create a new location request with the desired interval
        val newLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setMaxUpdateDelayMillis(intervalMs * 2)
            .build()

        if (wasTracking) {
            startLocationUpdates()
        }
    }

    /**
     * Get last known location (one-time request)
     * Note: Permission handling should be done before calling this method
     */
    @SuppressLint("MissingPermission")
    fun getLastLocation(callback: (LocationData?) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val data = LocationData(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        altitude = it.altitude,
                        speed = it.speed,
                        accuracy = it.accuracy,
                        bearing = it.bearing,
                        timestamp = it.time
                    )
                    callback(data)
                } ?: callback(null)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    /**
     * Data class representing location data
     */
    data class LocationData(
        val latitude: Double,      // Latitude in degrees
        val longitude: Double,     // Longitude in degrees
        val altitude: Double,      // Altitude in meters above WGS84 reference ellipsoid
        val speed: Float,          // Speed in meters per second
        val accuracy: Float,       // Estimated accuracy in meters
        val bearing: Float,        // Bearing in degrees (0-360)
        val timestamp: Long        // Timestamp in milliseconds (system time)
    ) {
        /**
         * Calculate distance to another location in meters
         */
        fun distanceTo(other: LocationData): Float {
            val results = FloatArray(1)
            Location.distanceBetween(
                latitude, longitude,
                other.latitude, other.longitude,
                results
            )
            return results[0]
        }

        /**
         * Convert speed from m/s to km/h
         */
        fun getSpeedKmh(): Float {
            return speed * 3.6f
        }

        /**
         * Format location as a string
         */
        fun formatCoordinates(): String {
            return String.format("%.6f, %.6f", latitude, longitude)
        }
    }
}