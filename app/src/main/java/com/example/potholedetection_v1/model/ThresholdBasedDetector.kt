package com.example.potholedetection_v1.model


import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Detector de baches basado en umbrales extraídos del modelo Random Forest
 */
class ThresholdBasedDetector(private val context: Context) {

    companion object {
        private const val TAG = "ThresholdDetector"
        // Umbral mínimo de confianza para detectar un bache
        private const val DETECTION_THRESHOLD = 0.6f
    }

    private var modelConfig: ModelConfig? = null

    init {
        loadModelConfig()
    }

    /**
     * Carga la configuración del modelo desde assets
     */
    private fun loadModelConfig() {
        try {
            val json = context.assets.open("model_config.json").bufferedReader().use { it.readText() }
            modelConfig = Gson().fromJson(json, ModelConfig::class.java)
            Log.d(TAG, "Configuración del modelo cargada exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando configuración del modelo", e)
            // Usar configuración de respaldo
            modelConfig = getDefaultConfig()
        }
    }

    /**
     * Detecta si las características representan un bache
     * @param features Array de características en el orden correcto
     * @return Confianza de detección (0-1)
     */
    fun detectPothole(features: FloatArray): Float {
        if (modelConfig == null) {
            Log.w(TAG, "Configuración no disponible, usando detección simple")
            return simpleDetection(features)
        }

        var score = 0f
        var totalWeight = 0f

        modelConfig?.features?.forEachIndexed { index, featureName ->
            val featureValue = features[index]
            modelConfig?.thresholds?.get(featureName)?.let { threshold ->
                val importance = modelConfig?.decision_weights?.get(featureName) ?: 0f

                // Comparar con umbrales
                if (isPositiveEvidence(featureName, featureValue, threshold)) {
                    score += importance
                }

                totalWeight += importance
            }
        }

        // Normalizar score entre 0 y 1
        val confidence = if (totalWeight > 0) score / totalWeight else 0f
        Log.d(TAG, "Score: $score, totalWeight: $totalWeight, confidence: $confidence")

        return confidence
    }

    /**
     * Determina si un valor de característica es evidencia positiva de un bache
     */
    private fun isPositiveEvidence(
        featureName: String,
        value: Float,
        threshold: ThresholdConfig
    ): Boolean {
        return when (featureName) {
            "gyroMagnitude", "accelMagnitude", "zAccelDeviation" ->
                value > (threshold.pos_mean + threshold.neg_mean) / 2
            "speed" -> value > 5f && value < 50f // Rango óptimo para detección
            else -> {
                // Para otras características, calcular distancia a las medias
                val posDist = abs(value - threshold.pos_mean)
                val negDist = abs(value - threshold.neg_mean)
                posDist < negDist
            }
        }
    }

    /**
     * Detección simple basada en umbrales fijos (respaldo)
     */
    private fun simpleDetection(features: FloatArray): Float {
        // Verificar que tenemos suficientes características
        if (features.size < 10) return 0f

        // Extraer características principales (índices correspondientes)
        val accelMagnitude = features[7]
        val gyroMagnitude = features[8]
        val zAccelDeviation = features[9]
        val speed = features[6]

        var score = 0f

        // Umbrales simples
        if (accelMagnitude > 1.5f) score += 0.3f
        if (gyroMagnitude > 0.3f) score += 0.3f
        if (zAccelDeviation > 0.5f) score += 0.2f
        if (speed > 5f) score += 0.2f

        return score.coerceIn(0f, 1f)
    }

    /**
     * Obtiene una configuración predeterminada
     */
    private fun getDefaultConfig(): ModelConfig {
        return ModelConfig(
            features = listOf(
                "accelerometerX", "accelerometerY", "accelerometerZ",
                "gyroX", "gyroY", "gyroZ",
                "speed",
                "accelMagnitude", "gyroMagnitude", "zAccelDeviation"
            ),
            thresholds = mapOf(),
            decision_weights = mapOf(
                "gyroMagnitude" to 0.3f,
                "accelMagnitude" to 0.2f,
                "zAccelDeviation" to 0.2f,
                "speed" to 0.1f,
                "gyroZ" to 0.1f,
                "accelerometerZ" to 0.1f
            )
        )
    }
}

// Data classes para configuración
data class ModelConfig(
    val features: List<String>,
    val thresholds: Map<String, ThresholdConfig>,
    val decision_weights: Map<String, Float>
)

data class ThresholdConfig(
    val pos_mean: Float,
    val pos_std: Float,
    val neg_mean: Float,
    val neg_std: Float,
    val threshold: Float,
    val importance: Float
)