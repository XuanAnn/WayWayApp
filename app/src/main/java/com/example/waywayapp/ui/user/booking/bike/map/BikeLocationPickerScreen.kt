package com.example.waywayapp.ui.user.booking.bike.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waywayapp.ui.user.booking.bike.BikeSharedViewModel
import com.example.waywayapp.ui.user.booking.bike.BikeViewModel
import com.example.waywayapp.ui.user.booking.bike.components.MainMapContent
import com.example.waywayapp.ui.user.booking.bike.model.BikeLocationType
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun BikeLocationPickerScreen(
    type: BikeLocationType,
    onBackClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    viewModel: BikeViewModel = BikeSharedViewModel.viewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()

    val isPickup =
        type == BikeLocationType.PICKUP

    LaunchedEffect(uiState.currentLatLng) {
        val target =
            when (type) {
                BikeLocationType.PICKUP -> uiState.pickupLatLng ?: uiState.currentLatLng
                BikeLocationType.DROPOFF -> uiState.dropoffLatLng ?: uiState.currentLatLng
            }

        target?.let {
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(it, 18f)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MainMapContent(
            viewModel = viewModel,
            cameraPositionState = cameraPositionState
        )

        TopPickerBar(
            isPickup = isPickup,
            onBackClick = onBackClick
        )

        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = if (isPickup) Color(0xFF1E73E8) else Color(0xFFE53935),
            modifier = Modifier
                .align(Alignment.Center)
                .size(70.dp)
                .offset(y = (-35).dp)
        )

        FloatingActionButton(
            onClick = {
                uiState.currentLatLng?.let {
                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(it, 18f)
                }
            },
            containerColor = Color.White,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp)
        ) {
            Icon(
                Icons.Default.GpsFixed,
                contentDescription = null,
                tint = Color(0xFF20242A)
            )
        }

        BottomConfirmPanel(
            title = if (isPickup) {
                uiState.pickupAddress
            } else {
                uiState.dropoffAddress.ifBlank { "Chọn điểm đến" }
            },
            isPickup = isPickup,
            onConfirmClick = {
                val center =
                    cameraPositionState.position.target

                viewModel.setBikeLocationFromMap(
                    type = type,
                    latLng = center
                )

                onConfirmClick()
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TopPickerBar(
    isPickup: Boolean,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
        }

        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = if (isPickup) Color(0xFF1E73E8) else Color(0xFFE53935)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = if (isPickup) "Đón tại?" else "Bạn muốn đến đâu?",
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )

        Icon(
            Icons.Default.PhotoCamera,
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
private fun BottomConfirmPanel(
    title: String,
    isPickup: Boolean,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Kiểm tra vị trí ghim trước khi xác nhận",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onConfirmClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00B14F)
                )
            ) {
                Text(
                    text = if (isPickup) {
                        "Chọn điểm đón này"
                    } else {
                        "Chọn điểm đến này"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}