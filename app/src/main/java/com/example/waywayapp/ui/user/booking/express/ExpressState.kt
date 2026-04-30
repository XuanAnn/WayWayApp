package com.example.waywayapp.ui.user.booking.express

data class ExpressState(
    val pickupLocation: String = "",
    val dropoffLocation: String = "",


    val isFindingDriver: Boolean = false,
    val price: Double = 0.0
)



