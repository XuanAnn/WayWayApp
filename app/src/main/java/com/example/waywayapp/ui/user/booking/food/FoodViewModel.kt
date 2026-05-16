package com.example.waywayapp.ui.user.booking.food

import androidx.lifecycle.ViewModel
import com.example.waywayapp.ui.user.booking.food.model.CartItemUiModel
import com.example.waywayapp.ui.user.booking.food.model.FoodItemUiModel
import foodList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FoodViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FoodState())
    val uiState: StateFlow<FoodState> = _uiState.asStateFlow()

    init {
        loadMockFoods()
    }

    private fun loadMockFoods() {
        _uiState.update {
            it.copy(
                foods = foodList,
                isLoading = false
            )
        }
    }
    fun addToCart(food: FoodItemUiModel) {
        _uiState.update { state ->
            val existedItem = state.cartItems.find {
                it.food.id == food.id
            }

            val newCart = if (existedItem == null) {
                state.cartItems + CartItemUiModel(
                    food = food,
                    quantity = 1
                )
            } else {
                state.cartItems.map { item ->
                    if (item.food.id == food.id) {
                        item.copy(quantity = item.quantity + 1)
                    } else {
                        item
                    }
                }
            }

            state.copy(cartItems = newCart)
        }
    }
}