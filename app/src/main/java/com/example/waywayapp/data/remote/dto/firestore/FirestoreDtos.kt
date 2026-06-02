package com.example.waywayapp.data.remote.dto.firestore

import com.example.waywayapp.data.model.AdminDriver
import com.example.waywayapp.data.model.AdminUser
import com.example.waywayapp.data.model.DriverLocation
import com.example.waywayapp.data.model.Ride
import com.example.waywayapp.data.model.RideMessage
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName

data class RideDto(
    val id: String? = null,
    val userId: String? = null,
    val driverId: String? = null,
    val serviceType: String? = null,
    val status: String? = null,
    val pickupLat: Double? = null,
    val pickupLng: Double? = null,
    val pickupAddress: String? = null,
    val dropoffLat: Double? = null,
    val dropoffLng: Double? = null,
    val destLat: Double? = null,
    val destLng: Double? = null,
    val dropoffAddress: String? = null,
    val passengerName: String? = null,
    val passengerPhone: String? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val driverPlate: String? = null,
    val price: Double? = null,
    val paymentMethod: String? = null,
    val paymentStatus: String? = null,
    val paidAt: Long? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val acceptedAt: Long? = null,
    val completedAt: Long? = null,
    val userRating: Int? = null,
    val userReview: String? = null,
    val ratedAt: Long? = null
) {
    fun toDomain(documentId: String = id.orEmpty()): Ride {
        return Ride(
            id = documentId.ifBlank { id.orEmpty() },
            userId = userId.orEmpty(),
            driverId = driverId.orEmpty(),
            serviceType = serviceType.orEmpty().ifBlank { "bike" },
            status = status.orEmpty().ifBlank { "searching" },
            pickupLat = pickupLat ?: 0.0,
            pickupLng = pickupLng ?: 0.0,
            pickupAddress = pickupAddress.orEmpty(),
            dropoffLat = dropoffLat ?: destLat ?: 0.0,
            dropoffLng = dropoffLng ?: destLng ?: 0.0,
            dropoffAddress = dropoffAddress.orEmpty(),
            passengerName = passengerName.orEmpty(),
            passengerPhone = passengerPhone.orEmpty(),
            driverName = driverName.orEmpty(),
            driverPhone = driverPhone.orEmpty(),
            driverPlate = driverPlate.orEmpty(),
            price = price ?: 0.0,
            paymentMethod = paymentMethod.orEmpty().ifBlank { "cash" },
            paymentStatus = paymentStatus.orEmpty().ifBlank { "pending" },
            paidAt = paidAt,
            createdAt = createdAt ?: 0L,
            updatedAt = updatedAt ?: 0L,
            acceptedAt = acceptedAt,
            completedAt = completedAt,
            userRating = userRating ?: 0,
            userReview = userReview.orEmpty(),
            ratedAt = ratedAt
        )
    }
}

data class AdminDriverDto(
    val id: String? = null,
    val userId: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val vehicleType: String? = null,
    val plateNumber: String? = null,
    val active: Boolean? = null,
    val online: Boolean? = null,
    val available: Boolean? = null,
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var legacyIsActive: Boolean? = null,
    @get:PropertyName("isOnline")
    @set:PropertyName("isOnline")
    var legacyIsOnline: Boolean? = null,
    @get:PropertyName("isAvailable")
    @set:PropertyName("isAvailable")
    var legacyIsAvailable: Boolean? = null,
    val rating: Double? = null,
    val updatedAt: Long? = null,
    val createdAt: Long? = null
) {
    fun toDomain(documentId: String = id.orEmpty()): AdminDriver {
        return AdminDriver(
            id = documentId.ifBlank { id.orEmpty() },
            userId = userId.orEmpty(),
            name = name.orEmpty(),
            phone = phone.orEmpty(),
            email = email.orEmpty(),
            vehicleType = vehicleType.orEmpty().ifBlank { "bike" },
            plateNumber = plateNumber.orEmpty(),
            isActive = active ?: legacyIsActive ?: true,
            isOnline = online ?: legacyIsOnline ?: false,
            isAvailable = available ?: legacyIsAvailable ?: false,
            rating = rating ?: 5.0,
            updatedAt = updatedAt ?: 0L,
            createdAt = createdAt ?: 0L
        )
    }
}

data class AdminUserDto(
    val id: String? = null,
    val name: String? = null,
    val age: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val avatarUrl: String? = null,
    val momoLinked: Boolean? = null,
    val momoPhone: String? = null,
    val momoLinkedAt: Long? = null,
    val active: Boolean? = null,
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var legacyIsActive: Boolean? = null,
    val updatedAt: Long? = null,
    val createdAt: Long? = null
) {
    fun toDomain(documentId: String = id.orEmpty()): AdminUser {
        return AdminUser(
            id = documentId.ifBlank { id.orEmpty() },
            name = name.orEmpty(),
            age = age.orEmpty(),
            email = email.orEmpty(),
            phone = phone.orEmpty(),
            role = role.orEmpty().ifBlank { "USER" },
            avatarUrl = avatarUrl.orEmpty(),
            momoLinked = momoLinked ?: false,
            momoPhone = momoPhone.orEmpty(),
            momoLinkedAt = momoLinkedAt ?: 0L,
            isActive = active ?: legacyIsActive ?: true,
            updatedAt = updatedAt ?: 0L,
            createdAt = createdAt ?: 0L
        )
    }
}

data class DriverLocationDto(
    val driverId: String? = null,
    val activeRideId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val heading: Double? = null,
    val speed: Double? = null,
    val updatedAt: Long? = null
) {
    fun toDomain(documentId: String = driverId.orEmpty()): DriverLocation {
        return DriverLocation(
            driverId = driverId.orEmpty().ifBlank { documentId },
            activeRideId = activeRideId,
            latitude = latitude ?: 0.0,
            longitude = longitude ?: 0.0,
            heading = heading?.toFloat() ?: 0f,
            speed = speed?.toFloat() ?: 0f,
            updatedAt = updatedAt ?: 0L
        )
    }
}

data class RideMessageDto(
    val id: String? = null,
    val rideId: String? = null,
    val senderId: String? = null,
    val senderName: String? = null,
    val text: String? = null,
    val createdAt: Long? = null
) {
    fun toDomain(documentId: String = id.orEmpty()): RideMessage {
        return RideMessage(
            id = documentId.ifBlank { id.orEmpty() },
            rideId = rideId.orEmpty(),
            senderId = senderId.orEmpty(),
            senderName = senderName.orEmpty(),
            text = text.orEmpty(),
            createdAt = createdAt ?: 0L
        )
    }
}

fun Ride.toDto(): RideDto {
    return RideDto(
        id = id,
        userId = userId,
        driverId = driverId,
        serviceType = serviceType,
        status = status,
        pickupLat = pickupLat,
        pickupLng = pickupLng,
        pickupAddress = pickupAddress,
        dropoffLat = dropoffLat,
        dropoffLng = dropoffLng,
        dropoffAddress = dropoffAddress,
        passengerName = passengerName,
        passengerPhone = passengerPhone,
        driverName = driverName,
        driverPhone = driverPhone,
        driverPlate = driverPlate,
        price = price,
        paymentMethod = paymentMethod,
        paymentStatus = paymentStatus,
        paidAt = paidAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        acceptedAt = acceptedAt,
        completedAt = completedAt,
        userRating = userRating,
        userReview = userReview,
        ratedAt = ratedAt
    )
}

fun AdminDriver.toDto(): AdminDriverDto {
    return AdminDriverDto(
        id = id,
        userId = userId,
        name = name,
        phone = phone,
        email = email,
        vehicleType = vehicleType,
        plateNumber = plateNumber,
        active = isActive,
        online = isOnline,
        available = isAvailable,
        legacyIsActive = isActive,
        legacyIsOnline = isOnline,
        legacyIsAvailable = isAvailable,
        rating = rating,
        updatedAt = updatedAt,
        createdAt = createdAt
    )
}

fun AdminUser.toDto(): AdminUserDto {
    return AdminUserDto(
        id = id,
        name = name,
        age = age,
        email = email,
        phone = phone,
        role = role,
        avatarUrl = avatarUrl,
        momoLinked = momoLinked,
        momoPhone = momoPhone,
        momoLinkedAt = momoLinkedAt,
        active = isActive,
        legacyIsActive = isActive,
        updatedAt = updatedAt,
        createdAt = createdAt
    )
}

fun DriverLocation.toDto(): DriverLocationDto {
    return DriverLocationDto(
        driverId = driverId,
        activeRideId = activeRideId,
        latitude = latitude,
        longitude = longitude,
        heading = heading.toDouble(),
        speed = speed.toDouble(),
        updatedAt = updatedAt
    )
}

fun RideMessage.toDto(): RideMessageDto {
    return RideMessageDto(
        id = id,
        rideId = rideId,
        senderId = senderId,
        senderName = senderName,
        text = text,
        createdAt = createdAt
    )
}

fun DocumentSnapshot.toRideDto(): RideDto? {
    return toObject(RideDto::class.java)
}

fun DocumentSnapshot.toAdminDriverDto(): AdminDriverDto? {
    return toObject(AdminDriverDto::class.java)
}

fun DocumentSnapshot.toAdminUserDto(): AdminUserDto? {
    return toObject(AdminUserDto::class.java)
}

fun DocumentSnapshot.toDriverLocationDto(): DriverLocationDto? {
    return toObject(DriverLocationDto::class.java)
}

fun DocumentSnapshot.toRideMessageDto(): RideMessageDto? {
    return toObject(RideMessageDto::class.java)
}
