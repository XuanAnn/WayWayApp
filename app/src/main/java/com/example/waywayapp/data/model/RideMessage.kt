package com.example.waywayapp.data.model

data class RideMessage(
    val id: String = "",
    val rideId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val createdAt: Long = 0L
)
