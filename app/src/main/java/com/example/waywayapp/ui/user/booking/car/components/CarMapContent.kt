package com.example.waywayapp.ui.user.booking.car.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.waywayapp.ui.user.booking.car.CarViewModel
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline

@Composable
fun CarMapContent(
    viewModel: CarViewModel,
    cameraPositionState: CameraPositionState
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val hasLocationPermission =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = hasLocationPermission
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
            Polyline(points = state.polylinePoints)
        }
    }
}
