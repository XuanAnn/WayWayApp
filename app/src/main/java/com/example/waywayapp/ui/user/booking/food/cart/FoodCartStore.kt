package com.example.waywayapp.ui.user.booking.food.cart

import com.example.waywayapp.ui.user.booking.food.model.CartItemUiModel
import com.example.waywayapp.ui.user.booking.food.model.FoodItemUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object FoodCartStore {

    private val _cartItems =
        MutableStateFlow<List<CartItemUiModel>>(emptyList())

    val cartItems =
        _cartItems.asStateFlow()

    fun addFood(food: FoodItemUiModel) {
        _cartItems.update { currentCart ->
            val existedItem = currentCart.find {
                it.food.id == food.id
            }

            if (existedItem == null) {
                currentCart + CartItemUiModel(
                    food = food,
                    quantity = 1
                )
            } else {
                currentCart.map { item ->
                    if (item.food.id == food.id) {
                        item.copy(quantity = item.quantity + 1)
                    } else {
                        item
                    }
                }
            }
        }
    }

    fun removeFood(foodId: Int) {
        _cartItems.update { currentCart ->
            currentCart.mapNotNull { item ->
                when {
                    item.food.id != foodId -> item
                    item.quantity > 1 -> item.copy(
                        quantity = item.quantity - 1
                    )
                    else -> null
                }
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }
}