package com.example.waywayapp.ui.user.booking


import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.waywayapp.ui.user.booking.bike.BikeSharedViewModel
import com.example.waywayapp.ui.user.booking.bike.search.BikeSearchScreen
import com.example.waywayapp.ui.user.booking.express.ExpressFormScreen
@Composable
fun BookingRoute(
    type: String,
    onBackClick: () -> Unit = {},
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
    LaunchedEffect(type) {
        if (type == "bike" || type == "car") {
            BikeSharedViewModel.viewModel.selectServiceType(type)
        }
    }

    when (type) {
        "bike", "car" -> {
            BikeSearchScreen(
                onBackClick = onBackClick,
                onPickupMapClick = onBikePickupClick,
                onDropoffMapClick = onBikeDropoffClick,
                onConfirmClick = onBikeConfirmClick
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

