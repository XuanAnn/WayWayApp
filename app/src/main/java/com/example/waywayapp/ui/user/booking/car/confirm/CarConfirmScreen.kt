package com.example.waywayapp.ui.user.booking.car.confirm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.waywayapp.ui.user.booking.bike.components.CompletedUI
import com.example.waywayapp.ui.user.booking.bike.components.WaitingUI
import com.example.waywayapp.ui.user.booking.bike.model.BookingStatus
import com.example.waywayapp.ui.user.booking.car.CarSharedViewModel
import com.example.waywayapp.ui.user.booking.car.CarViewModel
import com.example.waywayapp.ui.user.booking.car.components.CarMapContent
import com.example.waywayapp.ui.user.booking.car.toBikeState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.DecimalFormat

@Composable
fun CarConfirmScreen(
    onBackHomeClick: () -> Unit = {},
    viewModel: CarViewModel = CarSharedViewModel.viewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    val config = LocalConfiguration.current

    LaunchedEffect(uiState.pickupLatLng, uiState.dropoffLatLng) {
        if (
            uiState.pickupLatLng != null &&
            uiState.dropoffLatLng != null &&
            uiState.polylinePoints.isEmpty()
        ) {
            viewModel.calculateRoute()
        }

        val pickup = uiState.pickupLatLng
        val dropoff = uiState.dropoffLatLng

        if (pickup != null && dropoff != null) {
            val bounds = LatLngBounds.builder()
                .include(pickup)
                .include(dropoff)
                .build()

            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    config.screenWidthDp,
                    config.screenHeightDp,
                    180
                ),
                durationMs = 800
            )
        } else if (pickup != null) {
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(pickup, 16f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CarMapContent(
            viewModel = viewModel,
            cameraPositionState = cameraPositionState
        )

        when (uiState.status) {
            BookingStatus.IDLE -> {
                CarConfirmCard(
                    price = uiState.finalPrice,
                    distance = uiState.distance,
                    duration = uiState.duration,
                    onBookClick = viewModel::startBooking,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            BookingStatus.FINDING -> {
                WaitingUI(
                    onCancel = viewModel::cancelBooking
                )
            }

            BookingStatus.ON_TRIP -> {
                Text(
                    text = "Tài xế ô tô đang đến đón bạn",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            BookingStatus.COMPLETED -> {
                CompletedUI(
                    state = uiState.toBikeState(),
                    onFinish = {
                        viewModel.resetBooking()
                        onBackHomeClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun CarConfirmCard(
    price: Double,
    distance: String,
    duration: String,
    onBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DecimalFormat("#,###")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Car 4 chỗ", style = MaterialTheme.typography.titleMedium)
                    Text("$distance • $duration")
                }

                Text(
                    text = "${formatter.format(price)}đ",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onBookClick,
                enabled = price > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Đặt xe ô tô")
            }
        }
    }
}