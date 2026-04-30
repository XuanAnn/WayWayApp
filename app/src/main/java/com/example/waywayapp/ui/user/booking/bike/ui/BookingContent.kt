package com.example.waywayapp.ui.user.booking.bike.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.ui.user.booking.bike.viewmodel.BikeViewModel
import com.example.waywayapp.ui.user.booking.bike.viewmodel.BookingStatus
import com.google.maps.android.compose.*
import java.text.DecimalFormat

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

@Composable
fun BookingInputOverlay(
    viewModel: BikeViewModel,
    onConfirmBooking: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Phần nhập địa chỉ (Phía trên)
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = uiState.pickupAddress,
                    onValueChange = { viewModel.onPickupAddressChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Điểm đón") },
                    leadingIcon = { Icon(Icons.Default.MyLocation, null, tint = Color(0xFF00B1A7)) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.dropoffAddress,
                    onValueChange = { viewModel.onDropoffAddressChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Điểm đến") },
                    placeholder = { Text("Chạm bản đồ để chọn") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Red) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }

        // Bảng giá và nút đặt xe (Phía dưới)
        if (uiState.dropoffLatLng != null && !uiState.isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.BottomCenter),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("WayWay Bike", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("${uiState.distance} • ${uiState.duration}", color = Color.Gray)
                        }
                        val formatter = DecimalFormat("#,###")
                        Text("${formatter.format(uiState.price.toInt())} đ", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(
                            0xFF0097A7
                        )
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onConfirmBooking,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B1A7)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Xác nhận đặt xe", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}