package com.example.waywayapp.data.model

data class DriverLocation(
    val driverId: String = "",
    val activeRideId: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val heading: Float = 0f,
    val speed: Float = 0f,
    val updatedAt: Long = 0L
)
