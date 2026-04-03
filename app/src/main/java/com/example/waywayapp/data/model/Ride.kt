package com.example.waywayapp.data.model

data class Ride(
    val id: String = "",

    val userId: String = "",
    val driverId: String = "",

    val status: String = "REQUESTED",
    // REQUESTED → ACCEPTED → ONGOING → COMPLETED

    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,

    val destLat: Double = 0.0,
    val destLng: Double = 0.0,

    val price: Double = 0.0,

    val createdAt: Long = 0L,
    val completedAt: Long? = null
)