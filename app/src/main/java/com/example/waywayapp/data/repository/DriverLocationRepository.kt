package com.example.waywayapp.data.repository

import android.location.Location
import com.example.waywayapp.core.firebase.FirestoreProvider
import com.example.waywayapp.data.model.AdminDriver
import com.example.waywayapp.data.model.DriverLocation
import com.example.waywayapp.data.remote.dto.firestore.toAdminDriverDto
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
        vehicleType: String,
        location: Location
    ) {
        val driverLocation = DriverLocation(
            driverId = driverId,
            activeRideId = activeRideId,
            latitude = location.latitude,
            longitude = location.longitude,
            vehicleType = vehicleType.ifBlank { "bike" },
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
    fun observeDriverLocations(): Flow<List<DriverLocation>> = callbackFlow {
        var locationRegistration: ListenerRegistration? = null
        var driverRegistration: ListenerRegistration? = null
        var locations = emptyList<DriverLocation>()
        var onlineDrivers = emptyMap<String, AdminDriver>()

        fun emitOnlineLocations() {
            trySend(
                locations.mapNotNull { location ->
                    val driver = onlineDrivers[location.driverId] ?: return@mapNotNull null
                    location.copy(vehicleType = driver.vehicleType.ifBlank { "bike" })
                }
            )
        }

        locationRegistration = firestore.collection("driver_locations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                locations = snapshot?.documents.orEmpty()
                    .mapNotNull { document ->
                        document.toDriverLocationDto()?.toDomain(document.id)
                    }
                emitOnlineLocations()
            }

        driverRegistration = firestore.collection("drivers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                onlineDrivers = snapshot?.documents.orEmpty()
                    .mapNotNull { document ->
                        document.toAdminDriverDto()?.toDomain(document.id)
                    }
                    .filter { driver ->
                        driver.isActive && driver.isOnline && driver.isAvailable
                    }
                    .flatMap { driver ->
                        listOf(driver.id, driver.userId)
                            .filter { it.isNotBlank() }
                            .map { id -> id to driver }
                    }
                    .toMap()
                emitOnlineLocations()
            }

        awaitClose {
            locationRegistration?.remove()
            driverRegistration?.remove()
        }
    }

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
