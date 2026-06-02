package com.example.waywayapp.ui.user.booking.car

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.ui.user.booking.bike.components.CompletedUI
import com.example.waywayapp.ui.user.booking.bike.components.OnTripUI
import com.example.waywayapp.ui.user.booking.bike.components.WaitingUI
import com.example.waywayapp.ui.user.booking.bike.model.BookingStatus
import com.example.waywayapp.ui.user.booking.car.components.CarInputOverlay
import com.example.waywayapp.ui.user.booking.car.components.CarMapContent
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun CarScreen(
    viewModel: CarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    val configuration = LocalConfiguration.current

    LaunchedEffect(uiState.currentLatLng) {
        uiState.currentLatLng?.let { current ->
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(
                    current,
                    18f
                )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CarMapContent(
            viewModel = viewModel,
            cameraPositionState = cameraPositionState
        )

        when (uiState.status) {
            BookingStatus.IDLE -> {
                CarInputOverlay(
                    viewModel = viewModel,
                    onConfirmBooking = viewModel::startBooking
                )
            }

            BookingStatus.FINDING -> {
                WaitingUI(
                    onCancel = viewModel::cancelBooking
                )
            }

            BookingStatus.ON_TRIP -> {
                OnTripUI(
                    state = uiState.toBikeState(),
                    cardHeight = (configuration.screenHeightDp * 0.4f).dp,
                    onBack = viewModel::cancelBooking
                )
            }

            BookingStatus.COMPLETED -> {
                CompletedUI(
                    state = uiState.toBikeState(),
                    onFinish = viewModel::resetBooking
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF00B1A7)
                )
            }
        }
    }
}

