package com.example.waywayapp.ui.user.booking


import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.waywayapp.ui.user.booking.bike.ui.BikeBookingScreen
import com.example.waywayapp.ui.user.booking.express.ExpressBookingScreen
import com.example.waywayapp.ui.user.booking.food.FoodBookingScreen

@Composable
fun BookingRoute(
    type: String,
    onBackClick: () -> Unit = {},

) {
    when(type){
        "bike" -> {
            BikeBookingScreen(
            )
        }

        "food" -> {
            FoodBookingScreen()
        }

        "express" -> {
            ExpressBookingScreen()
        }

        else -> {
            Text(text = "Lỗi")
        }
    }
}
