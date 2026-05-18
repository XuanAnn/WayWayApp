package com.example.waywayapp.ui.user.booking.food.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.ui.user.booking.food.model.FoodItemUiModel
import kotlinx.coroutines.delay
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
        FoodCartStore.deleteFood(foodId)
    }

    fun checkout() {
        val currentCart = _uiState.value.cartItems

        if (currentCart.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Giỏ hàng đang trống")
            }
            return
        }

        viewModelScope.launch {
            delay(800) // mock gọi backend tạo order

            FoodCartStore.clearCart()

            _uiState.update {
                it.copy(
                    isCheckoutSuccess = true,
                    errorMessage = null
                )
            }
        }
    }

    fun clearCheckoutSuccess() {
        _uiState.update {
            it.copy(isCheckoutSuccess = false)
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }
}