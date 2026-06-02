package com.example.waywayapp.ui.user.booking.bike.confirm

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.waywayapp.ui.chat.RideMessagePopupListener
import com.example.waywayapp.ui.user.booking.bike.BikeSharedViewModel
import com.example.waywayapp.ui.user.booking.bike.BikeViewModel
import com.example.waywayapp.ui.user.booking.bike.components.BookingConfirmCard
import com.example.waywayapp.ui.user.booking.bike.components.MainMapContent
import com.example.waywayapp.ui.user.booking.bike.components.OnTripUI
import com.example.waywayapp.ui.user.booking.bike.components.WaitingUI
import com.example.waywayapp.ui.user.booking.bike.components.CompletedUI
import com.example.waywayapp.ui.user.booking.bike.model.BookingStatus
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.DecimalFormat

@Composable
fun BikeConfirmScreen(
    onBackHomeClick: () -> Unit = {},
    viewModel: BikeViewModel = BikeSharedViewModel.viewModel,
    onBackClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    onOpenChat: (String) -> Unit = {},
    onRateRide: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(
        uiState.pickupLatLng,
        uiState.dropoffLatLng
    ) {
        val pickup = uiState.pickupLatLng
        val dropoff = uiState.dropoffLatLng
        uiState.polylinePoints.isEmpty()
        if (pickup != null && dropoff != null) {
            viewModel.calculateRoute()
            val bounds = LatLngBounds.builder()
                .include(pickup)
                .include(dropoff)
                .build()

            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    screenWidth,
                    screenHeight,
                    150
                ),
                durationMs = 1000
            )
        } else if (pickup != null) {
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(pickup, 16f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MainMapContent(
            viewModel = viewModel,
            cameraPositionState = cameraPositionState
        )

        when (uiState.status) {
            BookingStatus.IDLE -> {
                BookingConfirmCard(
                    viewModel = viewModel,
                    onConfirmBooking = viewModel::startBooking,
                    onSelectPromo = {
                        viewModel.loadPromos()
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
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
                    cardHeight = (screenHeight * 0.4f).dp,
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
                    onRateRide = {
                        uiState.currentRideId?.takeIf { it.isNotBlank() }?.let(onRateRide)
                    },
                    onFinish = {
                        viewModel.resetBooking()
                        onBackHomeClick()
                    }
                )
            }
        }

        RideMessagePopupListener(
            rideId = uiState.currentRideId,
            enabled = uiState.status == BookingStatus.ON_TRIP,
            onOpenChat = onOpenChat
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    LaunchedEffect(uiState.status, uiState.rideStatus) {
        val message = when {
            uiState.status == BookingStatus.FINDING -> "Dang tim tai xe phu hop."
            uiState.rideStatus == "accepted" -> "Tai xe da nhan cuoc va dang den diem don."
            uiState.rideStatus == "arrived" -> "Tai xe da den diem don."
            uiState.rideStatus == "in_progress" -> "Chuyen xe cua ban dang duoc thuc hien."
            uiState.status == BookingStatus.COMPLETED -> "Chuyen xe cua ban da hoan thanh."
            else -> null
        }
        message?.let { snackbarHostState.showSnackbar(it) }
    }
}

@Composable
private fun RideConfirmBottomSheet(
    price: Double,
    distance: String,
    duration: String,
    onBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DecimalFormat("#,###")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 30.dp,
            topEnd = 30.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = Color(0xFF00B14F),
                    modifier = Modifier.size(42.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Bike Phổ Thông",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "$distance • $duration",
                        color = Color.Gray
                    )
                }

                Text(
                    text = "${formatter.format(price)}đ",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF20242A)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onBookClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00B14F)
                )
            ) {
                Text("Đặt Xe")
            }
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
