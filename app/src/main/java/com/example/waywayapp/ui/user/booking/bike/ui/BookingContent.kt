package com.example.waywayapp.ui.user.booking.bike.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.ui.user.booking.bike.BikeViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun BookingUI(
    viewModel: BikeViewModel,
    onConfirmBooking: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()

    // Request permissions and init location
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.initLocationClient(context)
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Update camera when current location is found
    LaunchedEffect(uiState.currentLatLng) {
        uiState.currentLatLng?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 16f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Map Background
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                viewModel.setDropoffLocation(latLng, "Vị trí đã chọn")
            },
            properties = MapProperties(isMyLocationEnabled = uiState.currentLatLng != null),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true)
        ) {
            uiState.pickupLatLng?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Điểm đón",
                    snippet = uiState.pickupAddress
                )
            }

            uiState.dropoffLatLng?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Điểm đến",
                    snippet = uiState.dropoffAddress
                )
            }

            if (uiState.polylinePoints.isNotEmpty()) {
                Polyline(
                    points = uiState.polylinePoints,
                    color = Color(0xFF00B1A7),
                    width = 10f
                )
            }
        }

        // Top Header Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Đón: ${uiState.pickupAddress}", fontSize = 14.sp, color = Color.Gray)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = if (uiState.dropoffAddress.isEmpty()) "Chạm bản đồ để chọn điểm đến" else "Đến: ${uiState.dropoffAddress}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.dropoffAddress.isEmpty()) Color.Red else Color.Black
                    )
                }
            }
        }

        // Bottom Booking Card
        if (uiState.dropoffLatLng != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "WayWay Bike", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(
                                text = "${uiState.distance} • ${uiState.duration}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "${uiState.price.toInt()}đ",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = Color(0xFF00B1A7)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onConfirmBooking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B1A7)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Xác nhận đặt xe", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00B1A7))
            }
        }
    }
}
