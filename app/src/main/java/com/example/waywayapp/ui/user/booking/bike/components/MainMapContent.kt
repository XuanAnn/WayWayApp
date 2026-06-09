package com.example.waywayapp.ui.user.booking.bike.components

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.waywayapp.ui.user.booking.bike.model.BookingStatus
import com.example.waywayapp.ui.user.booking.bike.BikeViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline

@SuppressLint("UnrememberedMutableState")
@Composable
fun MainMapContent(
    viewModel: BikeViewModel,
    cameraPositionState: CameraPositionState
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val driverVehicleBitmap = remember { createDriverVehicleBitmap() }
    val userPersonBitmap = remember { createUserPersonBitmap() }

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
        val driverVehicleIcon = remember(driverVehicleBitmap) {
            BitmapDescriptorFactory.fromBitmap(driverVehicleBitmap)
        }
        val userPersonIcon = remember(userPersonBitmap) {
            BitmapDescriptorFactory.fromBitmap(userPersonBitmap)
        }

        // Điểm đón
        uiState.pickupLatLng?.takeIf { it.isValidLatLng() }?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Bạn",
                snippet = uiState.pickupAddress,
                icon = userPersonIcon,
                anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.95f)
            )
        }

        // Điểm đến
        uiState.dropoffLatLng?.let {
            Marker(state = MarkerState(position = it), title = "Điểm đến", snippet = uiState.dropoffAddress)
        }

        uiState.driverLatLng?.takeIf { it.isValidLatLng() }?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Tài xế",
                snippet = uiState.driverName,
                icon = driverVehicleIcon,
                anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f)
            )
        }

        // Đường kẻ lộ trình
        if (uiState.polylinePoints.isNotEmpty()) {
            Polyline(points = uiState.polylinePoints, color = androidx.compose.material3.MaterialTheme.colorScheme.primary, width = 12f)
        }
    }
}

private fun createDriverVehicleBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(55, 0, 0, 0)
    }
    canvas.drawOval(RectF(18f, 62f, 78f, 82f), shadowPaint)

    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            22f,
            18f,
            74f,
            72f,
            android.graphics.Color.rgb(0, 177, 79),
            android.graphics.Color.rgb(0, 117, 95),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRoundRect(RectF(24f, 28f, 72f, 66f), 14f, 14f, bodyPaint)

    val topPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(205, 250, 231)
    }
    canvas.drawRoundRect(RectF(35f, 20f, 62f, 42f), 10f, 10f, topPaint)

    val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(35, 99, 120)
    }
    canvas.drawRoundRect(RectF(39f, 24f, 58f, 38f), 6f, 6f, glassPaint)

    val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(255, 241, 118)
    }
    canvas.drawCircle(30f, 43f, 4f, lightPaint)
    canvas.drawCircle(66f, 43f, 4f, lightPaint)

    val wheelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(24, 31, 39)
    }
    canvas.drawCircle(32f, 68f, 7f, wheelPaint)
    canvas.drawCircle(64f, 68f, 7f, wheelPaint)

    val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(235, 239, 243)
    }
    canvas.drawCircle(32f, 68f, 3f, rimPaint)
    canvas.drawCircle(64f, 68f, 3f, rimPaint)

    return bitmap
}

private fun createUserPersonBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(88, 88, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(45, 0, 0, 0)
    }
    canvas.drawOval(RectF(20f, 66f, 68f, 80f), shadowPaint)

    val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            20f,
            12f,
            68f,
            72f,
            android.graphics.Color.rgb(76, 129, 255),
            android.graphics.Color.rgb(24, 82, 204),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawCircle(44f, 36f, 28f, pinPaint)
    canvas.drawRoundRect(RectF(36f, 56f, 52f, 78f), 8f, 8f, pinPaint)

    val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(44f, 29f, 8f, facePaint)
    canvas.drawRoundRect(RectF(30f, 41f, 58f, 55f), 10f, 10f, facePaint)

    val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(65, 255, 255, 255)
    }
    canvas.drawCircle(34f, 24f, 6f, highlightPaint)

    return bitmap
}

private fun com.google.android.gms.maps.model.LatLng.isValidLatLng(): Boolean {
    return latitude.isFinite() &&
        longitude.isFinite() &&
        latitude in -90.0..90.0 &&
        longitude in -180.0..180.0 &&
        !(latitude == 0.0 && longitude == 0.0)
}
