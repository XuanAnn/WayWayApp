package com.example.waywayapp.data.repository
import com.example.waywayapp.domain.model.Driver
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DriverProfileRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getDriverProfile(driverId: String): Driver? {
        return try {
            val snapshot = firestore.collection("drivers")
                .document(driverId)
                .get()
                .await()

            // Map dữ liệu từ Firestore sang object Driver
            snapshot.toObject(Driver::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}