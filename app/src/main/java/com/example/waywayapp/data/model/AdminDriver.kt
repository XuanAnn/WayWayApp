package com.example.waywayapp.data.model

data class AdminDriver(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val vehicleType: String = "bike",
    val plateNumber: String = "",
    val isOnline: Boolean = false,
    val isAvailable: Boolean = false,
    val rating: Double = 5.0,
    val updatedAt: Long = 0L,
    val createdAt: Long = 0L
)
