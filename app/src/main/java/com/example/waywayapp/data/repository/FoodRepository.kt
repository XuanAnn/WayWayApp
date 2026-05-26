package com.example.waywayapp.data.repository

import com.example.waywayapp.data.local.dao.CartDao
import com.example.waywayapp.data.mapper.toEntity
import com.example.waywayapp.data.mapper.toUiModel
import com.example.waywayapp.ui.user.booking.food.model.CartItemUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FoodRepository(
    private val cartDao: CartDao
) {

    fun getCartItems():
            Flow<List<CartItemUiModel>> {

        return cartDao
            .getCartItems()
            .map { list ->

                list.map {
                    it.toUiModel()
                }
            }
    }

    suspend fun addToCart(
        item: CartItemUiModel
    ) {
        cartDao.insertCartItem(
            item.toEntity()
        )
    }

    suspend fun updateQuantity(
        foodId: Int,
        quantity: Int
    ) {

        if (quantity <= 0) {
            cartDao.deleteCartItem(foodId)
        } else {
            cartDao.updateQuantity(
                foodId,
                quantity
            )
        }
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }
}