package com.example.waywayapp.ui.user.booking.bike

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.ui.chat.RideMessagePopupListener
import com.example.waywayapp.ui.user.booking.bike.components.BookingInputOverlay
import com.example.waywayapp.ui.user.booking.bike.components.CompletedUI
import com.example.waywayapp.ui.user.booking.bike.components.MainMapContent
import com.example.waywayapp.ui.user.booking.bike.components.OnTripUI
import com.example.waywayapp.ui.user.booking.bike.components.PromoBottomSheet
import com.example.waywayapp.ui.user.booking.bike.components.WaitingUI
import com.example.waywayapp.ui.user.booking.bike.model.BookingStatus
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikeBookingScreen(
    viewModel: BikeViewModel = viewModel(),
    onOpenChat: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val sheetState = rememberModalBottomSheetState()

    var showPromoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.currentLatLng) {
        if (uiState.status == BookingStatus.IDLE) {
            uiState.currentLatLng?.let { currentLocation ->
                cameraPositionState.position =
                    CameraPosition.fromLatLngZoom(currentLocation, 18f)
            }
        }
    }

    LaunchedEffect(uiState.status, uiState.driverLatLng, uiState.pickupLatLng, uiState.dropoffLatLng) {
        if (uiState.status == BookingStatus.ON_TRIP) {
            uiState.driverLatLng?.let { driverLocation ->
                if (
                    !driverLocation.latitude.isFinite() ||
                    !driverLocation.longitude.isFinite() ||
                    driverLocation.latitude !in -90.0..90.0 ||
                    driverLocation.longitude !in -180.0..180.0 ||
                    (driverLocation.latitude == 0.0 && driverLocation.longitude == 0.0)
                ) {
                    return@let
                }

                runCatching {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(driverLocation, 17.5f),
                        durationMs = 700
                    )
                }.onFailure {
                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(driverLocation, 17.5f)
                }
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val tripCardHeight = maxHeight * 0.4f

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
                        cardHeight = tripCardHeight,
                        onBack = viewModel::cancelBooking,
                        onCallClick = {
                            openDialer(context, uiState.driverPhone)
                        },
                        onChatClick = {
                            uiState.currentRideId?.takeIf { it.isNotBlank() }?.let(onOpenChat)
                        }
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

            RideMessagePopupListener(
                rideId = uiState.currentRideId,
                enabled = uiState.status == BookingStatus.ON_TRIP,
                onOpenChat = onOpenChat
            )
        }
    }
}

private fun openDialer(
    context: Context,
    phone: String
) {
    val normalizedPhone = phone.trim()
    if (normalizedPhone.isBlank()) return

    val intent = Intent(
        Intent.ACTION_DIAL,
        Uri.parse("tel:$normalizedPhone")
    )
    context.startActivity(intent)
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
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )
    }
}
