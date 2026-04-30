package com.example.waywayapp.ui.user.booking.food

data class CartItem(
    val id: String,
    val foodName: String,
    val price: Double,
    val quantity: Int,
) {
    val itemTotalPrice: Double
        get() = price * quantity
}
data class FoodState(
    val dropoffLocation: String = "",

    val restaurantId: String = "",
    val restaurantName: String = "",
    val restaurantAddress: String = "",

    val distanceInKm: Double = 0.0,

    val cartItems: List<CartItem> = emptyList(),

    val isFindingDriver: Boolean = false,

    ) {
    val deliveryFee: Double
        get() = 16000 + Math.floor(distanceInKm) * 4000
    val foodPrice: Double
        get() = cartItems.sumOf { it.price * it.quantity }
    val totalPrice: Double
        get() = foodPrice + deliveryFee
}
