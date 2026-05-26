package com.example.waywayapp.data.repository

import android.location.Location
import com.example.waywayapp.core.firebase.FirestoreProvider
import com.example.waywayapp.data.model.DriverLocation
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DriverLocationRepository {
    private val firestore = FirestoreProvider.db

    suspend fun publishDriverLocation(
        driverId: String,
        activeRideId: String?,
        location: Location
    ) {
        val driverLocation = DriverLocation(
            driverId = driverId,
            activeRideId = activeRideId,
            latitude = location.latitude,
            longitude = location.longitude,
            heading = location.bearing,
            speed = location.speed,
            updatedAt = System.currentTimeMillis()
        )

        firestore.collection("driver_locations")
            .document(driverId)
            .set(driverLocation, SetOptions.merge())
            .await()
    }

    fun observeDriverLocation(
        driverId: String
    ): Flow<DriverLocation?> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = firestore.collection("driver_locations")
            .document(driverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                trySend(snapshot?.toObject(DriverLocation::class.java))
            }

        awaitClose {
            registration?.remove()
        }
    }

    suspend fun setDriverAvailability(
        driverId: String,
        isOnline: Boolean,
        isAvailable: Boolean
    ) {
        val data = mapOf(
            "isOnline" to isOnline,
            "isAvailable" to isAvailable,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore.collection("drivers")
            .document(driverId)
            .set(data, SetOptions.merge())
            .await()
    }
}
