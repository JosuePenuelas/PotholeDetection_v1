package com.example.potholedetection_v1.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.potholedetection_v1.MainActivity
import com.example.potholedetection_v1.data.PotholeDetection
import com.example.potholedetection_v1.data.Severity
import com.example.potholedetection_v1.sensor.PotholeSensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

/**
 * Foreground service to perform pothole detection even when app is in background
 */
class PotholeDetectionService : Service() {
    private val binder = LocalBinder()
    private lateinit var sensorManager: PotholeSensorManager
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Notification IDs
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "pothole_detection_channel"

    // Service state
    private var detectionCount = 0

    override fun onCreate() {
        super.onCreate()
        sensorManager = PotholeSensorManager(applicationContext)
        createNotificationChannel()

        // Observe detection events
        serviceScope.launch {
            sensorManager.detectionEvents.observeForever { event ->
                detectionCount++
                updateNotification()

                // TODO: Save the detection to database
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
                    accelerationX = 0f, // We should update the detection event to include these values
                    accelerationY = 0f,
                    accelerationZ = 0f,
                    gyroscopeX = 0f,
                    gyroscopeY = 0f,
                    gyroscopeZ = 0f,
                    speed = 0f,
                    confidence = event.confidence
                )

                // TODO: Save detection to repository
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start as a foreground service
        startForeground(NOTIFICATION_ID, createNotification())

        // Start detection
        sensorManager.startDetection()

        // Restart if killed
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        sensorManager.stopDetection()
        sensorManager.cleanup()
        serviceScope.cancel()
        super.onDestroy()
    }

    /**
     * Create the notification channel (required for Android O+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pothole Detection Service"
            val descriptionText = "Monitors road conditions to detect potholes"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create the foreground service notification
     */
    private fun createNotification(): Notification {
        // Intent to open the app when notification is tapped
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pothole Detection Active")
            .setContentText("Monitoring road conditions. Detected: $detectionCount")
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Update the notification with current detection count
     */
    private fun updateNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    /**
     * Binder class for client communication
     */
    inner class LocalBinder : Binder() {
        fun getService(): PotholeDetectionService = this@PotholeDetectionService
    }
}