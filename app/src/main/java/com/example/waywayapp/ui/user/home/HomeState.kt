package com.example.waywayapp.ui.user.home

import com.example.waywayapp.ui.user.booking.food.model.CartItemUiModel
import com.example.waywayapp.ui.user.home.model.BannerUiModel
import com.example.waywayapp.ui.user.home.model.FoodPreviewUiModel
import com.example.waywayapp.ui.user.home.model.ServiceUiModel

data class HomeState(
    val searchText: String = "",
    val services: List<ServiceUiModel> = emptyList(),
    val banners: List<BannerUiModel> = emptyList(),
    val foods: List<FoodPreviewUiModel> = emptyList(),
    val cartItems: List<CartItemUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val totalCartQuantity: Int
        get() = cartItems.sumOf { item ->
            item.quantity
        }

    val totalCartPrice: Double
        get() = cartItems.sumOf { item ->
            item.food.price * item.quantity
        }
}