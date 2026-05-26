package com.example.waywayapp.ui.user.booking.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.repository.FoodRepository
import com.example.waywayapp.ui.user.booking.food.model.CartItemUiModel
import com.example.waywayapp.ui.user.booking.food.model.FoodItemUiModel
import foodList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FoodViewModel(
    private val repository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodState())
    val uiState = _uiState.asStateFlow()

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
            repository.getCartItems().collect { cart ->
                _uiState.update {
                    it.copy(cartItems = cart)
                }
            }
        }
    }

    fun addToCart(food: FoodItemUiModel) {
        val currentCart = _uiState.value.cartItems

        val existedItem = currentCart.find {
            it.food.id == food.id
        }

        val newQuantity =
            if (existedItem == null) 1
            else existedItem.quantity + 1

        val newItem = CartItemUiModel(
            food = food,
            quantity = newQuantity
        )

        viewModelScope.launch {
            repository.addToCart(newItem)
        }
    }

    fun removeFromCart(foodId: Int) {
        val item = _uiState.value.cartItems.find {
            it.food.id == foodId
        } ?: return

        val newQuantity = item.quantity - 1

        viewModelScope.launch {
            repository.updateQuantity(
                foodId = foodId,
                quantity = newQuantity
            )
        }
    }

    fun onQuantityChange(
        foodId: Int,
        quantity: Int
    ) {
        viewModelScope.launch {
            repository.updateQuantity(
                foodId = foodId,
                quantity = quantity
            )
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun addFoodToCartById(foodId: Int) {
        val food = _uiState.value.foods.find {
            it.id == foodId
        }

        food?.let {
            addToCart(it)
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }
}