package com.example.waywayapp.ui.user.booking.bike.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val colors = MaterialTheme.colorScheme
    val isPickup = type == BikeLocationType.PICKUP

    LaunchedEffect(uiState.currentLatLng) {
        val target = when (type) {
            BikeLocationType.PICKUP -> uiState.pickupLatLng ?: uiState.currentLatLng
            BikeLocationType.DROPOFF -> uiState.dropoffLatLng ?: uiState.currentLatLng
        }

        target?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 18f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
            tint = if (isPickup) colors.primary else colors.error,
            modifier = Modifier
                .align(Alignment.Center)
                .size(70.dp)
                .offset(y = (-35).dp)
        )

        FloatingActionButton(
            onClick = {
                uiState.currentLatLng?.let {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 18f)
                }
            },
            containerColor = colors.surface,
            contentColor = colors.onSurface,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp)
        ) {
            Icon(Icons.Default.GpsFixed, contentDescription = "Về vị trí hiện tại")
        }

        BottomConfirmPanel(
            title = if (isPickup) {
                uiState.pickupAddress
            } else {
                uiState.dropoffAddress.ifBlank { "Chọn điểm đến" }
            },
            isPickup = isPickup,
            onConfirmClick = {
                viewModel.setBikeLocationFromMap(
                    type = type,
                    latLng = cameraPositionState.position.target
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
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(colors.surface)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = colors.onSurface)
        }

        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = if (isPickup) colors.primary else colors.error
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = if (isPickup) "Đón tại?" else "Bạn muốn đến đâu?",
            color = colors.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = colors.onSurfaceVariant)
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
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                color = colors.onSurface,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Kiểm tra vị trí ghim trước khi xác nhận",
                color = colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onConfirmClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Text(
                    text = if (isPickup) "Chọn điểm đón này" else "Chọn điểm đến này",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
