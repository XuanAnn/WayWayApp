package com.example.waywayapp.data.repository

import android.location.Location
import com.example.waywayapp.core.firebase.FirestoreProvider
import com.example.waywayapp.data.model.DriverLocation
import com.example.waywayapp.data.remote.dto.firestore.toDriverLocationDto
import com.example.waywayapp.data.remote.dto.firestore.toDto
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Repository đồng bộ vị trí tài xế giữa app driver và app user qua Firestore.
class DriverLocationRepository {
    // Firestore lưu vị trí hiện tại tại collection driver_locations.
    private val firestore = FirestoreProvider.db

    // Gửi vị trí GPS mới nhất của tài xế lên Firestore để user thấy realtime.
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
            .set(driverLocation.toDto(), SetOptions.merge())
            .await()
    }

    // Lắng nghe document driver_locations/{driverId} để cập nhật marker tài xế.
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

                trySend(snapshot?.toDriverLocationDto()?.toDomain(driverId))
            }

        awaitClose {
            registration?.remove()
        }
    }

    // Cập nhật trạng thái online/available trong drivers để hệ thống match chuyến.
    suspend fun setDriverAvailability(
        driverId: String,
        isOnline: Boolean,
        isAvailable: Boolean
    ) {
        val data = mapOf(
            "online" to isOnline,
            "available" to isAvailable,
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
