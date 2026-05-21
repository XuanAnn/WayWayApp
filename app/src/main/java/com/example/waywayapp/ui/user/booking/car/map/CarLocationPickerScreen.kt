package com.example.waywayapp.ui.user.booking.car.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.waywayapp.ui.user.booking.car.CarSharedViewModel
import com.example.waywayapp.ui.user.booking.car.CarViewModel
import com.example.waywayapp.ui.user.booking.car.components.CarMapContent
import com.example.waywayapp.ui.user.booking.car.model.CarLocationType
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun CarLocationPickerScreen(
    type: CarLocationType,
    onBackClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    viewModel: CarViewModel = CarSharedViewModel.viewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()

    val isPickup = type == CarLocationType.PICKUP

    LaunchedEffect(uiState.currentLatLng) {
        val target =
            if (isPickup) {
                uiState.pickupLatLng ?: uiState.currentLatLng
            } else {
                uiState.dropoffLatLng ?: uiState.currentLatLng
            }

        target?.let {
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(it, 18f)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CarMapContent(
            viewModel = viewModel,
            cameraPositionState = cameraPositionState
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .height(56.dp)
                .background(Color.White, RoundedCornerShape(28.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }

            Text(
                text = if (isPickup) "Chọn điểm đón" else "Chọn điểm đến",
                modifier = Modifier.weight(1f)
            )
        }

        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = if (isPickup) Color.Blue else Color.Red,
            modifier = Modifier
                .align(Alignment.Center)
                .size(72.dp)
                .offset(y = (-36).dp)
        )

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = if (isPickup) {
                        uiState.pickupAddress
                    } else {
                        uiState.dropoffAddress.ifBlank { "Chọn điểm đến" }
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val center =
                            cameraPositionState.position.target

                        viewModel.setCarLocationFromMap(
                            type = type,
                            latLng = center
                        )

                        onConfirmClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        if (isPickup) {
                            "Chọn điểm đón này"
                        } else {
                            "Chọn điểm đến này"
                        }
                    )
                }
            }
        }
    }
}