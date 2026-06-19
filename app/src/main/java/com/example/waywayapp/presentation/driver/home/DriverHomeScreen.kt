package com.example.waywayapp.presentation.driver.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.presentation.chat.RideMessagePopupListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- BẢNG MÀU WAYWAY ĐỒNG BỘ ---
private val WayWayDarkGreen = Color(0xFF006C4C)
private val WayWayLightGreen = Color(0xFF1DB954)
private val WayWayRed = Color(0xFFE53935)
private val AppBgColor = Color(0xFFF5F7FA)
private val CardBgColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1D1B20)
private val TextSecondary = Color(0xFF757575)

@SuppressLint("UnrememberedMutableState")
@Composable
fun DriverHomeScreen(
    viewModel: DriverViewModel = viewModel(),
    onNavigateToTrip: () -> Unit = {},
    onIncomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onOpenChat: (String) -> Unit = {},
    onAiAssistantClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val isOnline = uiState.status != DriverStatus.OFFLINE
    val hasAcceptedRide = uiState.currentRideId != null && uiState.status != DriverStatus.ONLINE
    var showWallet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var previousDriverStatus by remember { mutableStateOf<DriverStatus?>(null) }
    var previousWalletBalance by remember { mutableStateOf<Double?>(null) }

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

    LaunchedEffect(uiState.currentLatLng) {
        uiState.currentLatLng?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 16f)
        }
    }

    LaunchedEffect(uiState.polylinePoints) {
        if (uiState.polylinePoints.size >= 2) {
            val boundsBuilder = LatLngBounds.builder()
            uiState.polylinePoints.forEach(boundsBuilder::include)
            runCatching {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120)
                )
            }
        }
    }

    LaunchedEffect(uiState.status) {
        val previous = previousDriverStatus
        if (previous != null && previous != uiState.status) {
            val message = when (uiState.status) {
                DriverStatus.ON_THE_WAY_TO_PICKUP -> "Đã nhận cuốc. Vui lòng di chuyển đến điểm đón."
                DriverStatus.ARRIVED_AT_PICKUP -> "Đã đến điểm đón."
                DriverStatus.ON_TRIP -> "Chuyến xe bắt đầu."
                DriverStatus.ONLINE -> if (previous == DriverStatus.ON_TRIP) "Đã hoàn thành chuyến xe." else "Đã kết nối. Đang tìm chuyến."
                DriverStatus.OFFLINE -> "Đã ngắt kết nối."
            }
            snackbarHostState.showSnackbar(message)
        }
        previousDriverStatus = uiState.status
    }

    LaunchedEffect(uiState.walletBalance) {
        val previous = previousWalletBalance
        if (previous != null && uiState.walletBalance > previous) {
            val delta = uiState.walletBalance - previous
            snackbarHostState.showSnackbar("Thu nhập mới: +${formatWalletCurrency(delta)}")
        }
        previousWalletBalance = uiState.walletBalance
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (!hasAcceptedRide) {
                DriverBottomNavigation(
                    onIncomeClick = onIncomeClick,
                    onProfileClick = onProfileClick
                )
            }
        },
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = uiState.currentLatLng != null),
                uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false, compassEnabled = false, mapToolbarEnabled = false)
            ) {
                uiState.pickupLatLng?.let { Marker(state = MarkerState(position = it), title = "Điểm đón", snippet = uiState.pickupAddress) }
                uiState.dropoffLatLng?.let { Marker(state = MarkerState(position = it), title = "Điểm đến", snippet = uiState.dropoffAddress) }
                if (uiState.polylinePoints.isNotEmpty()) {
                    Polyline(points = uiState.polylinePoints, color = WayWayDarkGreen, width = 12f)
                }
            }

            // Top Right Controls (Wallet & AI)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(52.dp).clickable { showWallet = true },
                    shape = CircleShape, color = CardBgColor, shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Ví tài xế", tint = WayWayDarkGreen, modifier = Modifier.size(24.dp))
                    }
                }
                Surface(
                    modifier = Modifier.size(52.dp).clickable { onAiAssistantClick() },
                    shape = CircleShape, shadowElevation = 6.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(WayWayLightGreen, WayWayDarkGreen))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }

            // Map Controls
            Column(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp, bottom = 180.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MapActionFab(icon = Icons.Default.MyLocation, onClick = viewModel::refreshCurrentLocation)
                MapActionFab(Icons.Default.Layers)
            }

            // --- GIỮ LẠI KHỐI ĐIỀU KHIỂN TRẠNG THÁI CŨ THEO YÊU CẦU ---
            if (!hasAcceptedRide) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = paddingValues.calculateBottomPadding())
                ) {
                    // Nút bật/tắt kết nối dạng cũ (Tròn bên trái khi Online, Pill ở giữa khi Offline)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        if (isOnline) {
                            Surface(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clickable { viewModel.toggleOnlineStatus() },
                                shape = CircleShape,
                                color = WayWayDarkGreen,
                                shadowElevation = 6.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .height(48.dp)
                                    .width(160.dp)
                                    .clickable { viewModel.toggleOnlineStatus() },
                                shape = RoundedCornerShape(24.dp),
                                color = Color(0xFF212121),
                                shadowElevation = 6.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Bật kết nối", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    // Thanh thông báo trạng thái dạng dải chữ ngang cũ
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(
                                if (isOnline) WayWayDarkGreen else CardBgColor,
                                RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                            )
                            .then(
                                if (!isOnline) Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                else Modifier
                            )
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (isOnline) Color.White else WayWayRed, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = driverStatusText(uiState),
                                color = if (isOnline) Color.White else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(if (isOnline) WayWayDarkGreen else CardBgColor))
                }
            }

            if (uiState.pickupLatLng != null && isOnline && uiState.status == DriverStatus.ONLINE) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))) {
                    IncomingRideOverlay(
                        uiState = uiState,
                        onAccept = {
                            viewModel.acceptTrip()
                            onNavigateToTrip()
                        },
                        onReject = { viewModel.rejectTrip() },
                        modifier = Modifier.align(Alignment.Center).padding(20.dp)
                    )
                }
            }

            if (hasAcceptedRide) {
                ActiveRidePanel(
                    uiState = uiState,
                    onArrived = viewModel::arrivedAtPickup,
                    onStartTrip = viewModel::startTrip,
                    onCompleteTrip = viewModel::completeTrip,
                    onOpenNavigation = {
                        uiState.navigationTargetLatLng?.let { target ->
                            val googleMapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${target.latitude},${target.longitude}&mode=d")).setPackage("com.google.android.apps.maps")
                            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${target.latitude},${target.longitude}?q=${target.latitude},${target.longitude}"))
                            runCatching { context.startActivity(googleMapsIntent) }.onFailure { context.startActivity(fallbackIntent) }
                        }
                    },
                    onCallPassenger = { openDialer(context, uiState.passengerPhone) },
                    onOpenChat = { uiState.currentRideId?.takeIf { it.isNotBlank() }?.let(onOpenChat) },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = paddingValues.calculateBottomPadding() + 16.dp)
                )
            }

            uiState.error?.takeIf { it.isNotBlank() }?.let { error ->
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(16.dp),
                    shape = RoundedCornerShape(16.dp), color = Color(0xFFFFEBEE), shadowElevation = 4.dp
                ) {
                    Text(text = error, modifier = Modifier.padding(16.dp), color = WayWayRed, fontWeight = FontWeight.SemiBold)
                }
            }

            RideMessagePopupListener(rideId = uiState.currentRideId, enabled = hasAcceptedRide, onOpenChat = onOpenChat)

            if (showWallet) {
                DriverWalletDialog(uiState = uiState, onDismiss = { showWallet = false })
            }
        }
    }
}

@Composable
fun MapActionFab(icon: ImageVector, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier.size(48.dp).clickable(onClick = onClick),
        shape = CircleShape, color = CardBgColor, shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = TextPrimary)
        }
    }
}

@Composable
fun DriverBottomNavigation(
    onHomeClick: () -> Unit = {},
    onIncomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    NavigationBar(containerColor = CardBgColor, tonalElevation = 16.dp) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Trang chủ", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            selected = true,
            onClick = onHomeClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = WayWayDarkGreen, selectedTextColor = WayWayDarkGreen, indicatorColor = WayWayLightGreen.copy(alpha = 0.15f))
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text("Thu nhập", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            selected = false,
            onClick = onIncomeClick,
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary)
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            label = { Text("Hồ sơ", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            selected = false,
            onClick = onProfileClick,
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary)
        )
    }
}

@Composable
fun IncomingRideOverlay(
    uiState: DriverState,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        elevation = CardDefaults.cardElevation(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Surface(color = WayWayLightGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
                Text(" CHUYẾN XE MỚI ", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.ExtraBold, color = WayWayDarkGreen, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = formatWalletCurrency(uiState.tripPrice), fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = TextPrimary)
                Surface(color = AppBgColor, shape = RoundedCornerShape(12.dp)) {
                    Text(text = uiState.passengerName.ifBlank { "Khách hàng" }, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            RoutePreviewLine(pickup = uiState.pickupAddress, dropoff = uiState.dropoffAddress)
            Spacer(modifier = Modifier.height(28.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onReject, modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Text("Từ chối", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Button(
                    onClick = onAccept, modifier = Modifier.weight(1.5f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WayWayDarkGreen), shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Chấp nhận", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ActiveRidePanel(
    uiState: DriverState,
    onArrived: () -> Unit,
    onStartTrip: () -> Unit,
    onCompleteTrip: () -> Unit,
    onOpenNavigation: () -> Unit,
    onCallPassenger: () -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        elevation = CardDefaults.cardElevation(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(color = WayWayLightGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
                    Text("ĐANG THỰC HIỆN", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.ExtraBold, color = WayWayDarkGreen, fontSize = 11.sp)
                }
                Surface(
                    shape = CircleShape,
                    color = if (uiState.canNavigate) Color(0xFFE8F4FF) else AppBgColor,
                    onClick = onOpenNavigation,
                    enabled = uiState.canNavigate
                ) {
                    Icon(Icons.Default.Navigation, null, tint = if (uiState.canNavigate) Color(0xFF2563EB) else Color.LightGray, modifier = Modifier.padding(12.dp).size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(AppBgColor, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Person, null, tint = WayWayDarkGreen, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(uiState.passengerName.ifBlank { "Khách hàng" }, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextPrimary)
                    Text(uiState.passengerPhone, color = TextSecondary, fontSize = 13.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = CircleShape, color = AppBgColor, onClick = onCallPassenger) {
                        Icon(Icons.Default.Call, null, tint = WayWayDarkGreen, modifier = Modifier.padding(12.dp).size(20.dp))
                    }
                    Surface(shape = CircleShape, color = AppBgColor, onClick = onOpenChat) {
                        Icon(Icons.Default.Chat, null, tint = WayWayDarkGreen, modifier = Modifier.padding(12.dp).size(20.dp))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = AppBgColor)

            RoutePreviewLine(pickup = uiState.pickupAddress, dropoff = uiState.dropoffAddress)

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = when (uiState.status) {
                    DriverStatus.ON_THE_WAY_TO_PICKUP -> onArrived
                    DriverStatus.ARRIVED_AT_PICKUP -> onStartTrip
                    DriverStatus.ON_TRIP -> onCompleteTrip
                    else -> ({})
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WayWayDarkGreen)
            ) {
                Text(
                    text = when (uiState.status) {
                        DriverStatus.ON_THE_WAY_TO_PICKUP -> "Đã đến điểm đón"
                        DriverStatus.ARRIVED_AT_PICKUP -> "Bắt đầu chuyến"
                        DriverStatus.ON_TRIP -> "Hoàn thành chuyến"
                        else -> "Tiếp tục"
                    }, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun RoutePreviewLine(pickup: String, dropoff: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(WayWayLightGreen))
            Box(modifier = Modifier.width(2.dp).height(28.dp).background(Color.LightGray))
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(WayWayRed))
        }
        Spacer(Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text("Điểm đón", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Text(pickup.ifBlank { "Chưa cập nhật" }, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column {
                Text("Điểm đến", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Text(dropoff.ifBlank { "Chưa cập nhật" }, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun DriverWalletDialog(
    uiState: DriverState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBgColor,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Ví tài xế", fontWeight = FontWeight.ExtraBold, color = TextPrimary) },
        text = {
            Column {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = WayWayDarkGreen
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Số dư hiện tại", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = formatWalletCurrency(uiState.walletBalance), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "${uiState.walletCompletedTrips} cuốc xe đã hoàn thành", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Giao dịch gần đây", fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                val recentRides = uiState.walletRides.take(4)
                if (recentRides.isEmpty()) {
                    Text(text = "Chưa có cuốc hoàn thành.", color = TextSecondary, fontSize = 13.sp)
                } else {
                    recentRides.forEach { ride ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(AppBgColor, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = WayWayLightGreen, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(text = ride.dropoffAddress.ifBlank { "Cuốc xe hoàn thành" }, maxLines = 1, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, overflow = TextOverflow.Ellipsis)
                                Text(text = formatWalletDate(ride.completedAt ?: ride.updatedAt), color = TextSecondary, fontSize = 12.sp)
                            }
                            Text(text = "+${formatWalletCurrency(ride.price)}", color = WayWayDarkGreen, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng", color = WayWayDarkGreen, fontWeight = FontWeight.Bold) }
        }
    )
}

private fun openDialer(context: android.content.Context, phone: String) {
    val normalizedPhone = phone.trim()
    if (normalizedPhone.isBlank()) return
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$normalizedPhone")))
}

private fun formatWalletCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(amount)
}

private fun formatWalletDate(timestamp: Long): String {
    if (timestamp <= 0L) return "Chưa cập nhật"
    return SimpleDateFormat("HH:mm dd/MM", Locale("vi", "VN")).format(Date(timestamp))
}
private fun driverStatusText(uiState: DriverState): String {
    return when (uiState.status) {
        DriverStatus.OFFLINE -> "Bạn đang tắt kết nối."
        DriverStatus.ONLINE -> "Bạn đang bật kết nối, đang chờ chuyến mới."
        DriverStatus.ON_THE_WAY_TO_PICKUP -> "Đang di chuyển tới điểm đón."
        DriverStatus.ARRIVED_AT_PICKUP -> "Đã tới điểm đón, chờ bắt đầu chuyến."
        DriverStatus.ON_TRIP -> "Đang trong chuyến."
    }
}