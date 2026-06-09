package com.example.waywayapp.data.repository

import com.example.waywayapp.core.firebase.FirestoreProvider
import com.example.waywayapp.data.model.AdminDriver
import com.example.waywayapp.data.model.Ride
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

// Repository xử lý dữ liệu chuyến xe trên Firestore.
class RideRepository {
    // Firestore dùng chung để đọc/ghi rides, drivers và messages.
    private val firestore = FirestoreProvider.db

    // Theo dõi realtime các cuốc đang searching để driver nhìn thấy cuốc mới.
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
        // Lắng nghe một document ride để user/driver cập nhật trạng thái tức thời.
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
        // Lấy các cuốc đã hoàn thành để tính thu nhập và ví tài xế.
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
        // Lấy lịch sử chuyến đi của user để hiển thị ở màn History.
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
        // Tạo document ride mới sau khi user xác nhận đặt xe.
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
        // Đọc thông tin driver để gắn vào cuốc khi tài xế nhận chuyến.
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
        // Transaction đảm bảo chỉ một tài xế nhận được cuốc đang searching.
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

    suspend fun updateRideStatus(
        rideId: String,
        driverId: String,
        status: String
    ) {
        // Cập nhật trạng thái ride theo luồng accepted/arrived/in_progress/completed.
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

        if (status == "completed" || status == "cancelled") {
            // Khi kết thúc/hủy chuyến thì mở lại tài xế để nhận cuốc mới.
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
        // User hoặc hệ thống hủy cuốc bằng cách đổi status sang cancelled.
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
        // Lưu đánh giá của user vào ride sau khi chuyến completed.
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
            // Cập nhật điểm trung bình driver nếu rules cho phép, lỗi thì không làm fail đánh giá.
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
