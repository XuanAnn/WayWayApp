package com.example.waywayapp.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val OTP = "otp"
    const val REGISTER = "register"

    // user
    const val USER_HOME = "user_home"
    const val BOOKING = "booking/{type}"
    fun createBookingRoute(type: String): String {
        return "booking/$type"
    }

    const val TRACKING = "tracking"

    // driver
    const val DRIVER_HOME = "driver_home"
    const val INCOMING = "incoming_ride"
    const val TRIP = "trip"
}