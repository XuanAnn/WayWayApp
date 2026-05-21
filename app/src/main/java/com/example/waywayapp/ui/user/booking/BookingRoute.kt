package com.example.waywayapp.ui.user.booking


import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.waywayapp.ui.user.booking.bike.BikeBookingScreen
import com.example.waywayapp.ui.user.booking.bike.search.BikeSearchScreen
import com.example.waywayapp.ui.user.booking.car.CarScreen
import com.example.waywayapp.ui.user.booking.car.search.CarSearchScreen
import com.example.waywayapp.ui.user.booking.express.ExpressFormScreen
import com.example.waywayapp.ui.user.booking.food.FoodBookingScreen
@Composable
fun BookingRoute(
    type: String,
    onBackClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onBikePickupClick: () -> Unit = {},
    onBikeDropoffClick: () -> Unit = {},
    onBikeConfirmClick: () -> Unit = {},
    onExpressPickupClick: () -> Unit,
    onExpressDropoffClick: () -> Unit,
    onExpressConfirmClick: () -> Unit,
    onCarPickupClick: () -> Unit = {},
    onCarDropoffClick: () -> Unit = {},
    onCarConfirmClick: () -> Unit = {}
) {
    when (type) {
        "bike" -> {
            BikeSearchScreen(
                onBackClick = onBackClick,
                onPickupMapClick = onBikePickupClick,
                onDropoffMapClick = onBikeDropoffClick,
                onConfirmClick = onBikeConfirmClick
            )
        }

        "food" -> {
            FoodBookingScreen(
                onBackClick = onBackClick,
                onCartClick = onCartClick
            )
        }

        "car" -> {
            CarSearchScreen(
                onBackClick = onBackClick,
                onPickupMapClick = onCarPickupClick,
                onDropoffMapClick = onCarDropoffClick,
                onConfirmClick = onCarConfirmClick
            )
        }

        "express" -> {
            ExpressFormScreen(
                onBackClick = onBackClick,
                onPickupClick = onExpressPickupClick,
                onDropoffClick = onExpressDropoffClick,
                onConfirmClick = onExpressConfirmClick
            )
        }

        else -> {
            Text(text = "Không tìm thấy dịch vụ: $type")
        }
    }
}

