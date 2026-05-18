package com.example.waywayapp.ui.user.booking.food

import com.example.waywayapp.ui.user.booking.food.model.CartItemUiModel
import com.example.waywayapp.ui.user.booking.food.model.FoodItemUiModel
import com.example.waywayapp.ui.user.booking.food.model.FoodStatus

data class FoodState(
    val status: FoodStatus = FoodStatus.BROWSING,
    val searchQuery: String = "",
    val foods: List<FoodItemUiModel> = emptyList(),
    val cartItems: List<CartItemUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val errorMessage: String? = null
) {
    val totalPrice: Double
        get() = cartItems.sumOf { item ->
            item.food.price * item.quantity.toDouble()
        }

    val totalQuantity: Int
        get() = cartItems.sumOf { item ->
            item.quantity
        }
}