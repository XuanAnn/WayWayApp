package com.example.waywayapp.ui.user.booking.car.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.waywayapp.ui.user.booking.car.CarViewModel
import com.google.maps.android.compose.*

@SuppressLint("UnrememberedMutableState")
@Composable
fun CarMapContent(
    viewModel: CarViewModel,
    cameraPositionState: CameraPositionState
) {
    val state by viewModel.uiState.collectAsState()

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false
        )
    ) {
        state.pickupLatLng?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Điểm đón"
            )
        }

        state.dropoffLatLng?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Điểm đến"
            )
        }

        if (state.polylinePoints.isNotEmpty()) {
            Polyline(
                points = state.polylinePoints
            )
        }
    }
}