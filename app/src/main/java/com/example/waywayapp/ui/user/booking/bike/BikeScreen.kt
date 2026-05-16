package com.example.waywayapp.ui.user.booking.bike

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.ui.user.booking.bike.components.BookingInputOverlay
import com.example.waywayapp.ui.user.booking.bike.components.CompletedUI
import com.example.waywayapp.ui.user.booking.bike.components.MainMapContent
import com.example.waywayapp.ui.user.booking.bike.components.OnTripUI
import com.example.waywayapp.ui.user.booking.bike.components.PromoBottomSheet
import com.example.waywayapp.ui.user.booking.bike.components.WaitingUI
import com.example.waywayapp.ui.user.booking.bike.model.BookingStatus
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikeBookingScreen(
    viewModel: BikeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    val sheetState = rememberModalBottomSheetState()

    var showPromoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.currentLatLng) {
        uiState.currentLatLng?.let { currentLocation ->
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(currentLocation, 18f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MainMapContent(
            viewModel = viewModel,
            cameraPositionState = cameraPositionState
        )

        when (uiState.status) {
            BookingStatus.IDLE -> {
                BookingInputOverlay(
                    viewModel = viewModel,
                    onConfirmBooking = viewModel::startBooking,
                    onSelectPromo = {
                        showPromoSheet = true
                    }
                )
            }

            BookingStatus.FINDING -> {
                WaitingUI(
                    onCancel = viewModel::cancelBooking
                )
            }

            BookingStatus.ON_TRIP -> {
                OnTripUI(
                    state = uiState,
                    onBack = viewModel::cancelBooking
                )
            }

            BookingStatus.COMPLETED -> {
                CompletedUI(
                    state = uiState,
                    onFinish = viewModel::resetBooking
                )
            }
        }

        if (showPromoSheet) {
            ModalBottomSheet(
                sheetState = sheetState,
                containerColor = Color.White,
                onDismissRequest = {
                    showPromoSheet = false
                }
            ) {
                PromoBottomSheet(
                    viewModel = viewModel,
                    onPromoSelected = {
                        showPromoSheet = false
                    }
                )
            }
        }

        if (uiState.isLoading) {
            LoadingOverlay()
        }
    }
}

@Composable
private fun LoadingOverlay() {
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