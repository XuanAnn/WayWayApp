package com.example.waywayapp.ui.user.booking.food.order

data class FoodOrderState(
    val status: FoodOrderStatus = FoodOrderStatus.CART,
    val driverName: String = "",
    val driverRating: Double = 0.0,
    val vehiclePlate: String = "",
    val estimatedTime: String = "",
    val message: String = ""
)