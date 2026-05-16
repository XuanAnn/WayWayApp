package com.example.waywayapp.ui.user.booking.bike.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.waywayapp.ui.user.booking.bike.model.BookingStatus
import com.example.waywayapp.ui.user.booking.bike.BikeViewModel
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline

@Composable
fun MainMapContent(
    viewModel: BikeViewModel,
    cameraPositionState: CameraPositionState
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Xử lý quyền truy cập vị trí
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.initLocationClient(context)
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // Google Map nền
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            if (uiState.status == BookingStatus.IDLE) {
                viewModel.setDropoffLocation(latLng, "Vị trí đã chọn")
            }
        },
        properties = MapProperties(isMyLocationEnabled = uiState.currentLatLng != null),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = uiState.status == BookingStatus.IDLE,
            zoomControlsEnabled = false
        )
    ) {
        // Điểm đón
        uiState.pickupLatLng?.let {
            Marker(state = MarkerState(position = it), title = "Điểm đón", snippet = uiState.pickupAddress)
        }

        // Điểm đến
        uiState.dropoffLatLng?.let {
            Marker(state = MarkerState(position = it), title = "Điểm đến", snippet = uiState.dropoffAddress)
        }

        // Đường kẻ lộ trình
        if (uiState.polylinePoints.isNotEmpty()) {
            Polyline(points = uiState.polylinePoints, color = Color(0xFF00B1A7), width = 12f)
        }
    }
}