package com.example.waywayapp.ui.user.booking.food.cart

import com.example.waywayapp.ui.user.booking.food.model.CartItemUiModel


data class FoodCartState(
    val cartItems: List<CartItemUiModel> = emptyList(),
    val isCheckoutSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val totalPrice: Double
        get() = cartItems.sumOf {
            it.food.price * it.quantity
        }

    val totalQuantity: Int
        get() = cartItems.sumOf {
            it.quantity
        }
}