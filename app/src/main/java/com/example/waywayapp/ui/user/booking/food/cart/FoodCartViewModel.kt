package com.example.waywayapp.ui.user.booking.food.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.ui.user.booking.food.model.CartItemUiModel
import com.example.waywayapp.ui.user.booking.food.model.FoodItemUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FoodCartViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FoodCartState())
    val uiState = _uiState.asStateFlow()

    init {
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            FoodCartStore.cartItems.collect { cart ->
                _uiState.update {
                    it.copy(cartItems = cart)
                }
            }
        }
    }

    fun addFood(food: FoodItemUiModel) {
        FoodCartStore.addFood(food)
    }

    fun removeFood(foodId: Int) {
        FoodCartStore.removeFood(foodId)
    }

    fun deleteFood(foodId: Int) {
        FoodCartStore.removeFood(foodId)
    }

    fun checkout() {
        // Sau này gọi backend tạo order
        FoodCartStore.clearCart()
    }
}

