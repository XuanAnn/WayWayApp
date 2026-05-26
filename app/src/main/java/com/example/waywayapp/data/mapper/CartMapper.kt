package com.example.waywayapp.data.mapper

import com.example.waywayapp.data.local.entity.CartEntity
import com.example.waywayapp.ui.user.booking.food.model.CartItemUiModel
import com.example.waywayapp.ui.user.booking.food.model.FoodItemUiModel

fun CartEntity.toUiModel(): CartItemUiModel {
    return CartItemUiModel(
        food = FoodItemUiModel(
            id = foodId,
            name = name,
            imageRes = imageRes,
            price = price,
            description = description,
            store = store,
            distance = distance,
            rating = rating,
            badge = badge
        ),
        quantity = quantity
    )
}

fun CartItemUiModel.toEntity(): CartEntity {
    return CartEntity(
        foodId = food.id,
        name = food.name,
        description = food.description,
        store = food.store,
        imageRes = food.imageRes,
        price = food.price,
        distance = food.distance,
        rating = food.rating,
        badge = food.badge,
        quantity = quantity
    )
}
