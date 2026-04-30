package com.example.waywayapp.ui.user.booking.bike.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.ui.user.booking.bike.viewmodel.BikeViewModel
import com.example.waywayapp.ui.user.booking.bike.viewmodel.BookingStatus
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikeBookingScreen(viewModel: BikeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    // Quản lý trạng thái đóng/mở của BottomSheet
    val sheetState = rememberModalBottomSheetState()
    var showPromoSheet by remember { mutableStateOf(false) }
    // Tự động di chuyển camera đến vị trí hiện tại hoặc điểm đến
    LaunchedEffect(uiState.currentLatLng) {
        uiState.currentLatLng?.let { cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 16f) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Lớp nền 1: Bản đồ luôn hiển thị
        MainMapContent(viewModel = viewModel, cameraPositionState = cameraPositionState)

        // Lớp nền 2: Các UI phủ lên dựa theo trạng thái
        when (uiState.status) {
            BookingStatus.IDLE -> {
                BookingInputOverlay(
                    viewModel = viewModel,
                    onConfirmBooking = { viewModel.startBooking() },
                    // 1. Hàm mở PromoSheet
                    onSelectPromo = { showPromoSheet = true }
                )
            }
            BookingStatus.FINDING -> WaitingUI(onCancel = { viewModel.cancelBooking() })
            BookingStatus.ON_TRIP -> OnTripUI(state = uiState, onBack = { viewModel.cancelBooking() })
            BookingStatus.COMPLETED -> CompletedUI(state = uiState, onFinish = { viewModel.resetBooking() })

        }
        if (showPromoSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPromoSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                // Gọi file PromoBottomSheet đã sửa ở bước trước
                PromoBottomSheet(
                    viewModel = viewModel,
                    onPromoSelected = { showPromoSheet = false } // Đóng sheet sau khi chọn
                )
            }
        }
        // Lớp nền 3: Loading
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF00B1A7))
            }
        }
    }
}