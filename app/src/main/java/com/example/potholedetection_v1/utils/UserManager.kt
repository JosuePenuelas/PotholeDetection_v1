// utils/UserManager.kt
package com.example.potholedetection_v1.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.util.UUID

class UserManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val USER_ID_KEY = "user_id"
        private const val USER_NAME_KEY = "user_name"
        private const val DEVICE_ID_KEY = "device_id"
    }

    // Obtener o crear ID único del usuario
    fun getUserId(): String {
        var userId = prefs.getString(USER_ID_KEY, null)
        if (userId == null) {
            userId = UUID.randomUUID().toString()
            prefs.edit().putString(USER_ID_KEY, userId).apply()
        }
        return userId
    }

    // Obtener o crear nombre del usuario
    fun getUserName(): String {
        var userName = prefs.getString(USER_NAME_KEY, null)
        if (userName == null) {
            userName = "User_${getUserId().take(8)}"
            prefs.edit().putString(USER_NAME_KEY, userName).apply()
        }
        return userName
    }

    // Establecer nombre del usuario
    fun setUserName(name: String) {
        prefs.edit().putString(USER_NAME_KEY, name).apply()
    }

    // Obtener modelo del dispositivo
    fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    // Obtener ID único del dispositivo
    fun getDeviceId(): String? {
        var deviceId = prefs.getString(DEVICE_ID_KEY, null)
        if (deviceId == null) {
            deviceId = try {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (e: Exception) {
                UUID.randomUUID().toString()
            }
            prefs.edit().putString(DEVICE_ID_KEY, deviceId).apply()
        }
        return deviceId
    }
}