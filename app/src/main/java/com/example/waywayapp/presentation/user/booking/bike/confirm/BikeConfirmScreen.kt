package com.example.waywayapp.presentation.user.booking.bike.confirm

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.waywayapp.presentation.chat.RideMessagePopupListener
import com.example.waywayapp.presentation.user.booking.bike.BikeSharedViewModel
import com.example.waywayapp.presentation.user.booking.bike.BikeViewModel
import com.example.waywayapp.presentation.user.booking.bike.components.BookingConfirmCard
import com.example.waywayapp.presentation.user.booking.bike.components.MainMapContent
import com.example.waywayapp.presentation.user.booking.bike.components.OnTripUI
import com.example.waywayapp.presentation.user.booking.bike.components.WaitingUI
import com.example.waywayapp.presentation.user.booking.bike.components.CompletedUI
import com.example.waywayapp.presentation.user.booking.bike.model.BookingStatus
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.DecimalFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.LocalOffer
import com.example.waywayapp.domain.model.Promo
@Composable
fun BikeConfirmScreen(
    onBackHomeClick: () -> Unit = {},
    viewModel: BikeViewModel = BikeSharedViewModel.viewModel,
    onBackClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    onOpenChat: (String) -> Unit = {},
    onRateRide: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    val snackbarHostState = remember { SnackbarHostState() }
    val colors = MaterialTheme.colorScheme
    var showPromoSheet by remember { mutableStateOf(false) }
    val availablePromos by viewModel.availablePromos.collectAsState()
    LaunchedEffect(
        uiState.pickupLatLng,
        uiState.dropoffLatLng
    ) {
        val pickup = uiState.pickupLatLng
        val dropoff = uiState.dropoffLatLng
        uiState.polylinePoints.isEmpty()
        if (pickup != null && dropoff != null) {
            viewModel.calculateRoute()
            val bounds = LatLngBounds.builder()
                .include(pickup)
                .include(dropoff)
                .build()

            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    screenWidth,
                    screenHeight,
                    150
                ),
                durationMs = 1000
            )
        } else if (pickup != null) {
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(pickup, 16f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MainMapContent(
            viewModel = viewModel,
            cameraPositionState = cameraPositionState
        )

        when (uiState.status) {
            BookingStatus.IDLE -> {
                Surface(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 16.dp, top = 12.dp)
                        .size(48.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    shadowElevation = 6.dp,
                    onClick = onBackClick
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = colors.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                BookingConfirmCard(
                    viewModel = viewModel,
                    onConfirmBooking = viewModel::startBooking,
                    onSelectPromo = {
                        viewModel.loadPromos()
                        showPromoSheet = true
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            BookingStatus.FINDING -> {
                WaitingUI(
                    onCancel = viewModel::cancelBooking
                )
            }

            BookingStatus.ON_TRIP -> {
                OnTripUI(
                    state = uiState,
                    cardHeight = (screenHeight * 0.4f).dp,
                    onBack = viewModel::cancelBooking,
                    onCallClick = {
                        openDialer(context, uiState.driverPhone)
                    },
                    onChatClick = {
                        uiState.currentRideId?.takeIf { it.isNotBlank() }?.let(onOpenChat)
                    }
                )
            }

            BookingStatus.COMPLETED -> {
                CompletedUI(
                    state = uiState,
                    onRateRide = {
                        uiState.currentRideId?.takeIf { it.isNotBlank() }?.let(onRateRide)
                    },
                    onFinish = {
                        viewModel.resetBooking()
                        onBackHomeClick()
                    }
                )
            }
        }

        RideMessagePopupListener(
            rideId = uiState.currentRideId,
            enabled = uiState.status == BookingStatus.ON_TRIP,
            onOpenChat = onOpenChat
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    LaunchedEffect(uiState.status, uiState.rideStatus) {
        val message = when {
            uiState.status == BookingStatus.FINDING -> "Đang tìm tài xế phù hợp."
            uiState.rideStatus == "accepted" -> "Tài xế đã chấp nhận và đang đến điểm đón"
            uiState.rideStatus == "arrived" -> "Tài xế đã đến điểm đón."
            uiState.rideStatus == "in_progress" -> "Chuyến xe của bạn đang được thực hiện."
            uiState.status == BookingStatus.COMPLETED -> "Chuyến xe của bạn đã hoàn thành."
            else -> null
        }
        message?.let { snackbarHostState.showSnackbar(it) }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    if (showPromoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPromoSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            PromoSelectionSheetContent(
                promos = availablePromos,
                onPromoSelect = { promo ->
                    viewModel.applyPromo(promo)
                    showPromoSheet = false
                },
                onClearPromo = {
                    viewModel.clearPromo()
                    showPromoSheet = false
                }
            )
        }
    }
}


@Composable
private fun PromoSelectionSheetContent(
    promos: List<Promo>,
    onPromoSelect: (Promo) -> Unit,
    onClearPromo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Khuyến mãi dành cho bạn",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Nút xoá khuyến mãi đang áp dụng
        OutlinedButton(
            onClick = onClearPromo,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Không dùng mã khuyến mãi")
        }

        if (promos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Hiện không có mã giảm giá nào khả dụng",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(promos) { promo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPromoSelect(promo) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = promo.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Text(
                                    text = "Mã: ${promo.code}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
private fun openDialer(
    context: Context,
    phone: String
) {
    val normalizedPhone = phone.trim()
    if (normalizedPhone.isBlank()) return

    val intent = Intent(
        Intent.ACTION_DIAL,
        Uri.parse("tel:$normalizedPhone")
    )
    context.startActivity(intent)
}
