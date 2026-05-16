package com.example.waywayapp.ui.user.booking.food.model

data class FoodItemUiModel(
    val id: Int,
    val name: String,
    val description: String,
    val store: String,
    val price: Double,
    val distance: String,
    val rating: Double,
    val imageRes: Int,
    val badge: String
)