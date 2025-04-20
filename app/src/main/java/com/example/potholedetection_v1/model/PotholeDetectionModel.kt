package com.example.potholedetection_v1.model

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class PotholeDetectionModel(private val context: Context) {
    private var tflite: Interpreter? = null
    private val TAG = "PotholeModel"

    // Tamaño de la entrada esperada por el modelo
    private val NUM_FEATURES = 10

    // Buffer para entrada del modelo
    private val inputBuffer = ByteBuffer.allocateDirect(NUM_FEATURES * 4)  // 4 bytes por float
        .order(ByteOrder.nativeOrder())

    // Buffer para salida del modelo
    private val outputBuffer = ByteBuffer.allocateDirect(1 * 4)  // 1 valor de salida
        .order(ByteOrder.nativeOrder())

    init {
        // Inicializar buffers
        inputBuffer.rewind()
        outputBuffer.rewind()

        // Cargar el modelo
        loadModel()
    }

    /**
     * Carga el modelo TensorFlow Lite desde assets
     */
    private fun loadModel() {
        try {
            val assetManager = context.assets
            val modelPath = "pothole_model.tflite"
            val fileDescriptor = assetManager.openFd(modelPath)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength

            // Crear el interpreter
            val tfliteOptions = Interpreter.Options()
            // tfliteOptions.setNumThreads(4) // Opcional: configurar número de hilos
            tflite = Interpreter(fileChannel.map(
                FileChannel.MapMode.READ_ONLY, startOffset, declaredLength
            ), tfliteOptions)

            Log.d(TAG, "Modelo TFLite cargado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar el modelo TFLite", e)
            // Fallback a detección basada en umbrales
        }
    }

    /**
     * Predice si un conjunto de características corresponde a un bache
     */
    fun predict(features: FloatArray): Float {
        // Verificar si el modelo está cargado
        if (tflite == null) {
            Log.w(TAG, "Modelo no cargado, usando detección por umbrales")
            return predictWithThresholds(features)
        }

        try {
            // Preparar entrada
            inputBuffer.rewind()
            for (value in features) {
                inputBuffer.putFloat(value)
            }

            // Preparar salida
            outputBuffer.rewind()
            val outputArray = FloatArray(1)

            // Ejecutar inferencia
            tflite?.run(inputBuffer, outputBuffer)

            // Obtener resultado
            outputBuffer.rewind()
            val confidence = outputBuffer.float
            Log.d(TAG, "Predicción TFLite: $confidence")

            return confidence
        } catch (e: Exception) {
            Log.e(TAG, "Error al ejecutar predicción TFLite", e)
            return predictWithThresholds(features)
        }
    }

    /**
     * Método de respaldo usando umbrales (si hay problemas con TFLite)
     */
    private fun predictWithThresholds(features: FloatArray): Float {
        // Extraer características relevantes
        val accelMagnitude = features[7]  // Índice correspondiente a accelMagnitude
        val gyroMagnitude = features[8]   // Índice correspondiente a gyroMagnitude
        val zAccelDeviation = features[9] // Índice correspondiente a zAccelDeviation

        // Aplicar reglas simples
        val isPothole = (accelMagnitude > 1.5f && zAccelDeviation > 0.5f) ||
                (gyroMagnitude > 0.3f)

        return if (isPothole) 0.8f else 0.1f
    }
}