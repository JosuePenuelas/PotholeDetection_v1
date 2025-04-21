package com.example.potholedetection_v1.repository


import com.example.potholedetection_v1.data.PotholeDetection
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class FirebasePotholeRepository {
    private val firestore = Firebase.firestore
    private val potholeCollection = firestore.collection("potholes")

    private val _potholes = MutableStateFlow<List<PotholeDetection>>(emptyList())
    val potholes: Flow<List<PotholeDetection>> = _potholes.asStateFlow()

    init {
        // Observar cambios en la colección
        potholeCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val potholeList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PotholeDetection::class.java)
                }
                _potholes.value = potholeList
            }
        }
    }

    suspend fun savePotholeDetection(detection: PotholeDetection) {
        withContext(Dispatchers.IO) {
            try {
                // Guardar con el ID generado
                potholeCollection.document(detection.id).set(detection).await()
            } catch (e: Exception) {
                // Manejar error
                e.printStackTrace()
            }
        }
    }

    suspend fun getPotholeDetections(): List<PotholeDetection> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = potholeCollection
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PotholeDetection::class.java)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun getPotholeDetectionsByDate(startDate: Date, endDate: Date): List<PotholeDetection> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = potholeCollection
                    .whereGreaterThanOrEqualTo("timestamp", startDate)
                    .whereLessThanOrEqualTo("timestamp", endDate)
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PotholeDetection::class.java)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun deletePotholeDetection(id: String) {
        withContext(Dispatchers.IO) {
            try {
                potholeCollection.document(id).delete().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}