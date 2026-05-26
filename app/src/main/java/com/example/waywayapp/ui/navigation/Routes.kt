package com.example.waywayapp.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val OTP = "otp"
    const val REGISTER = "register"

    // user
    const val USER_HOME = "user_home"
    const val FOOD = "food/{foodId}"
    const val FOOD_CART = "food_cart"
    const val CHECKOUT_SUCCESS = "checkout_success"
    const val FOOD_ORDER_TRACKING = "food_order_tracking"

    const val EXPRESS_FORM = "express_form"
    const val EXPRESS_PICKUP = "express_pickup"
    const val EXPRESS_DROPOFF = "express_dropoff"
    const val EXPRESS_CONFIRM = "express_confirm"

    const val BIKE_HOME = "bike_home"
    const val BIKE_SEARCH = "bike_search"
    const val BIKE_PICKUP_MAP = "bike_pickup_map"
    const val BIKE_DROPOFF_MAP = "bike_dropoff_map"
    const val BIKE_CONFIRM = "bike_confirm"

    const val CAR_SEARCH = "car_search"
    const val CAR_PICKUP_MAP = "car_pickup_map"
    const val CAR_DROPOFF_MAP = "car_dropoff_map"
    const val CAR_CONFIRM = "car_confirm"
    fun createFoodRoute(
        foodId: Int
    ): String {

        return "food/$foodId"
    }
    const val BOOKING = "booking/{type}"
    fun createBookingRoute(type: String): String {
        return "booking/$type"
    }

    const val TRACKING = "tracking"

    // driver
    const val DRIVER_HOME = "driver_home"
    const val INCOMING = "incoming_ride"
    const val TRIP = "trip"

    //Bottom Nav
    const val NOTIFICATION = "notification"
    const val RECENTLY_SERVICE = "recent_service"
    const val PROFILE = "profile"

    // admin
    const val ADMIN_DRIVERS = "admin_drivers"
}
