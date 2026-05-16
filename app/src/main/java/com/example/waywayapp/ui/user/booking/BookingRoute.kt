package com.example.waywayapp.ui.user.booking


import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.waywayapp.ui.user.booking.bike.BikeBookingScreen
import com.example.waywayapp.ui.user.booking.car.CarScreen
import com.example.waywayapp.ui.user.booking.express.ExpressBookingScreen
import com.example.waywayapp.ui.user.booking.food.FoodBookingScreen
@Composable
fun BookingRoute(
    type: String,
    onBackClick: () -> Unit = {}
) {
    when (type) {
        "bike" -> {
            BikeBookingScreen(
            )
        }

        "food" -> {
            FoodBookingScreen(
                onBackClick = onBackClick
            )
        }

        "car" -> {
            CarScreen()
        }

        "express" -> {
            ExpressBookingScreen()
        }

        else -> {
            Text(text = "Không tìm thấy dịch vụ: $type")
        }
    }
}

