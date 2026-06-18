package com.example.waywayapp.data.repository

import com.example.waywayapp.core.firebase.FirestoreProvider
import com.example.waywayapp.domain.model.AdminDriver
import com.example.waywayapp.domain.model.Ride
import com.example.waywayapp.data.remote.dto.firestore.toAdminDriverDto
import com.example.waywayapp.data.remote.dto.firestore.toDto
import com.example.waywayapp.data.remote.dto.firestore.toRideDto
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RideRepository {
    private val firestore = FirestoreProvider.db

    fun observeOpenRides(
        serviceType: String = "bike",
        rejectedRideIds: Set<String> = emptySet()
    ): Flow<List<Ride>> = callbackFlow {
        val registration = firestore.collection("rides")
            .whereEqualTo("status", "searching")
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val rides = snapshot?.documents
                    ?.mapNotNull { document -> document.toRideDto()?.toDomain(document.id) }
                    ?.filter { ride -> ride.serviceType == serviceType }
                    ?.filterNot { ride -> ride.id in rejectedRideIds }
                    ?.sortedBy { ride -> ride.createdAt }
                    .orEmpty()

                trySend(rides)
            }

        awaitClose {
            registration.remove()
        }
    }

    fun observeRide(
        rideId: String
    ): Flow<Ride?> = callbackFlow {
        val registration = firestore.collection("rides")
            .document(rideId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                trySend(snapshot?.takeIf { it.exists() }?.toRideDto()?.toDomain(snapshot.id))
            }

        awaitClose {
            registration.remove()
        }
    }

    fun observeCompletedRides(
        driverId: String
    ): Flow<List<Ride>> = callbackFlow {
        val registration = firestore.collection("rides")
            .whereEqualTo("driverId", driverId)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val rides = snapshot?.documents
                    ?.mapNotNull { document -> document.toRideDto()?.toDomain(document.id) }
                    ?.filter { ride -> ride.status == "completed" }
                    ?.sortedByDescending { ride -> ride.completedAt ?: ride.updatedAt }
                    .orEmpty()

                trySend(rides)
            }

        awaitClose {
            registration.remove()
        }
    }

    fun observeUserRides(
        userId: String
    ): Flow<List<Ride>> = callbackFlow {
        val registration = firestore.collection("rides")
            .whereEqualTo("userId", userId)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val rides = snapshot?.documents
                    ?.mapNotNull { document -> document.toRideDto()?.toDomain(document.id) }
                    ?.sortedByDescending { ride -> ride.completedAt ?: ride.updatedAt.takeIf { it != 0L } ?: ride.createdAt }
                    .orEmpty()

                trySend(rides)
            }

        awaitClose {
            registration.remove()
        }
    }

    fun observeAllRides(): Flow<List<Ride>> = callbackFlow {
        val registration = firestore.collection("rides")
            .limit(300)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val rides = snapshot?.documents
                    ?.mapNotNull { document -> document.toRideDto()?.toDomain(document.id) }
                    ?.sortedByDescending { ride -> ride.updatedAt.takeIf { it != 0L } ?: ride.createdAt }
                    .orEmpty()

                trySend(rides)
            }

        awaitClose {
            registration.remove()
        }
    }

    suspend fun createBikeRide(
        userId: String,
        pickup: LatLng,
        pickupAddress: String,
        dropoff: LatLng,
        dropoffAddress: String,
        price: Double,
        serviceType: String = "bike",
        paymentMethod: String = "cash",
        paymentStatus: String = "pending",
        paidAt: Long? = null
    ): Ride {
        val now = System.currentTimeMillis()
        val document = firestore.collection("rides").document()
        val ride = Ride(
            id = document.id,
            userId = userId,
            serviceType = serviceType,
            status = "searching",
            pickupLat = pickup.latitude,
            pickupLng = pickup.longitude,
            pickupAddress = pickupAddress,
            dropoffLat = dropoff.latitude,
            dropoffLng = dropoff.longitude,
            dropoffAddress = dropoffAddress,

            price = price,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            paidAt = paidAt,
            createdAt = now,
            updatedAt = now
        )

        document.set(ride.toDto()).await()
        return ride
    }

    suspend fun getDriver(
        driverId: String
    ): AdminDriver? {
        // Äá»c thÃ´ng tin driver Ä‘á»ƒ gáº¯n vÃ o cuá»‘c khi tÃ i xáº¿ nháº­n chuyáº¿n.
        val document = firestore.collection("drivers")
            .document(driverId)
            .get()
            .await()

        if (!document.exists()) return null

        return document.toAdminDriverDto()?.toDomain(document.id)
    }

    suspend fun acceptRide(
        rideId: String,
        driver: AdminDriver
    ) {
        // Transaction Ä‘áº£m báº£o chá»‰ má»™t tÃ i xáº¿ nháº­n Ä‘Æ°á»£c cuá»‘c Ä‘ang searching.
        val rideRef = firestore.collection("rides").document(rideId)
        val driverRef = firestore.collection("drivers").document(driver.id)
        val now = System.currentTimeMillis()

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(rideRef)
            val ride = snapshot.toRideDto()?.toDomain(snapshot.id)
            if (ride?.status != "searching") {
                throw FirebaseFirestoreException(
                    "Chuyen da duoc tai xe khac nhan",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }

            transaction.set(
                rideRef,
                mapOf(
                    "driverId" to driver.id,
                    "driverName" to driver.name,
                    "driverPhone" to driver.phone,
                    "driverPlate" to driver.plateNumber,
                    "status" to "accepted",
                    "acceptedAt" to now,
                    "updatedAt" to now
                ),
                SetOptions.merge()
            )

            transaction.set(
                driverRef,
                mapOf(
                    "available" to false,
                    "isAvailable" to false,
                    "activeRideId" to rideId,
                    "updatedAt" to now
                ),
                SetOptions.merge()
            )
        }.await()
    }

    suspend fun updateRideAdminFields(
        rideId: String,
        driverId: String,
        status: String,
        paymentStatus: String,
        paymentMethod: String
    ) {
        val now = System.currentTimeMillis()
        val data = mutableMapOf<String, Any?>(
            "status" to status,
            "paymentStatus" to paymentStatus,
            "paymentMethod" to paymentMethod,
            "updatedAt" to now
        )

        if (status == "completed") {
            data["completedAt"] = now
        }

        firestore.collection("rides")
            .document(rideId)
            .set(data, SetOptions.merge())
            .await()

        if ((status == "completed" || status == "cancelled") && driverId.isNotBlank()) {
            firestore.collection("drivers")
                .document(driverId)
                .set(
                    mapOf(
                        "available" to true,
                        "isAvailable" to true,
                        "activeRideId" to FieldValue.delete(),
                        "updatedAt" to now
                    ),
                    SetOptions.merge()
                )
                .await()
        }
    }
    suspend fun updateRideStatus(
        rideId: String,
        driverId: String,
        status: String
    ) {
        // Cáº­p nháº­t tráº¡ng thÃ¡i ride theo luá»“ng accepted/arrived/in_progress/completed.
        val now = System.currentTimeMillis()
        val data = mutableMapOf<String, Any?>(
            "status" to status,
            "updatedAt" to now
        )

        if (status == "completed") {
            data["completedAt"] = now
        }

        firestore.collection("rides")
            .document(rideId)
            .set(data, SetOptions.merge())
            .await()

        if ((status == "completed" || status == "cancelled") && driverId.isNotBlank()) {
            // Khi káº¿t thÃºc/há»§y chuyáº¿n thÃ¬ má»Ÿ láº¡i tÃ i xáº¿ Ä‘á»ƒ nháº­n cuá»‘c má»›i.
            firestore.collection("drivers")
                .document(driverId)
                .set(
                    mapOf(
                        "available" to true,
                        "isAvailable" to true,
                        "activeRideId" to FieldValue.delete(),
                        "updatedAt" to now
                    ),
                    SetOptions.merge()
                )
                .await()
        }
    }

    suspend fun cancelRide(
        rideId: String
    ) {
        // User hoáº·c há»‡ thá»‘ng há»§y cuá»‘c báº±ng cÃ¡ch Ä‘á»•i status sang cancelled.
        firestore.collection("rides")
            .document(rideId)
            .set(
                mapOf(
                    "status" to "cancelled",
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun submitRideRating(
        rideId: String,
        rating: Int,
        review: String
    ) {
        // LÆ°u Ä‘Ã¡nh giÃ¡ cá»§a user vÃ o ride sau khi chuyáº¿n completed.
        val normalizedRating = rating.coerceIn(1, 5)
        val now = System.currentTimeMillis()
        val rideRef = firestore.collection("rides").document(rideId)

        rideRef.set(
            mapOf(
                "userRating" to normalizedRating,
                "userReview" to review.trim(),
                "ratedAt" to now,
                "updatedAt" to now
            ),
            SetOptions.merge()
        ).await()

        runCatching {
            // Cáº­p nháº­t Ä‘iá»ƒm trung bÃ¬nh driver náº¿u rules cho phÃ©p, lá»—i thÃ¬ khÃ´ng lÃ m fail Ä‘Ã¡nh giÃ¡.
            val ride = rideRef.get().await().toRideDto()?.toDomain(rideId) ?: return
            val driverId = ride.driverId.takeIf { it.isNotBlank() } ?: return

            val ratedRides = firestore.collection("rides")
                .whereEqualTo("driverId", driverId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toRideDto()?.toDomain(it.id) }
                .filter { it.userRating > 0 }

            if (ratedRides.isNotEmpty()) {
                val average = ratedRides.map { it.userRating }.average()
                firestore.collection("drivers")
                    .document(driverId)
                    .set(
                        mapOf(
                            "rating" to average,
                            "ratingCount" to ratedRides.size,
                            "updatedAt" to now
                        ),
                        SetOptions.merge()
                    )
                    .await()
            }
        }
    }

}

