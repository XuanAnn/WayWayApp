package com.example.waywayapp.ui.user.booking.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.ui.user.booking.food.cart.FoodCartStore
import com.example.waywayapp.ui.user.booking.food.model.FoodItemUiModel
import foodList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FoodViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FoodState())
    val uiState: StateFlow<FoodState> = _uiState.asStateFlow()

    init {
        loadMockFoods()
        observeCart()
    }


    private fun loadMockFoods() {
        _uiState.update {
            it.copy(
                foods = foodList,
                isLoading = false
            )
        }
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
    fun onQuantityChange(foodId: Int, quantity: Int) {
        FoodCartStore.setFoodQuantity(
            foodId = foodId,
            quantity = quantity
        )
    }
    fun addToCart(
        food: FoodItemUiModel
    ) {

        val success =
            FoodCartStore.addFood(food)

        if (!success) {

            _uiState.update {
                it.copy(
                    errorMessage =
                        "Đơn hàng vượt quá 1.000.000đ"
                )
            }
        }
    }
    fun clearError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    fun removeFromCart(foodId: Int) {
        FoodCartStore.removeFood(foodId)
    }

    fun clearCart() {
        FoodCartStore.clearCart()
    }

    fun addFoodToCartById(foodId: Int) {
        val food = _uiState.value.foods.find {
            it.id == foodId
        }

        food?.let {
            addToCart(it)
        }
    }
}