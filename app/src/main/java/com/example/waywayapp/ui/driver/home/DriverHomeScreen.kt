package com.example.waywayapp.ui.driver.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.ui.chat.RideMessagePopupListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val grabGreen = androidx.compose.material3.MaterialTheme.colorScheme.primary
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
                DriverStatus.ON_THE_WAY_TO_PICKUP -> "Ban da nhan cuoc. Hay di den diem don."
                DriverStatus.ARRIVED_AT_PICKUP -> "Ban da den diem don."
                DriverStatus.ON_TRIP -> "Chuyen xe da bat dau. Hay di den diem tra."
                DriverStatus.ONLINE -> if (previous == DriverStatus.ON_TRIP) {
                    "Ban da hoan thanh don."
                } else {
                    "Ban dang san sang nhan cuoc."
                }
                DriverStatus.OFFLINE -> "Ban da tat ket noi."
            }
            snackbarHostState.showSnackbar(message)
        }
        previousDriverStatus = uiState.status
    }

    LaunchedEffect(uiState.walletBalance) {
        val previous = previousWalletBalance
        if (previous != null && uiState.walletBalance > previous) {
            val delta = uiState.walletBalance - previous
            snackbarHostState.showSnackbar("So du vi vua cong ${formatWalletCurrency(delta)}")
        }
        previousWalletBalance = uiState.walletBalance
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            if (!hasAcceptedRide) {
                DriverBottomNavigation(
                    grabGreen = grabGreen,
                    onIncomeClick = onIncomeClick,
                    onProfileClick = onProfileClick
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()


        ) {


            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = uiState.currentLatLng != null),
                uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false)
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
                        color = Color(0xFF0288D1),
                        width = 8f
                    )
                }
            }
            // 2. Right Side Floating Controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = 150.dp)
                ,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MapActionFab(
                    icon = Icons.Default.MyLocation,
                    onClick = viewModel::refreshCurrentLocation
                )
                MapActionFab(Icons.Default.Layers)
                MapActionFab(Icons.Default.Sync)
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = paddingValues.calculateTopPadding() + 16.dp, end = 16.dp)
                    .size(52.dp)
                    .clickable { showWallet = true },
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Ví tài xế",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }


            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = paddingValues.calculateTopPadding() + 80.dp, end = 16.dp)
                    .size(52.dp)
                    .clickable { onAiAssistantClick() },
                shape = CircleShape,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF26C6DA), Color(0xFF00897B))
                            )
                        )
                        .clickable { onAiAssistantClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }

            if (!hasAcceptedRide) {
                // 3. Bottom UI Layer
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = paddingValues.calculateBottomPadding())
                ) {
                    // Connection toggle (Pill or Circle)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        if (isOnline) {
                            // Online state: Green circle on the left
                            Surface(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clickable { viewModel.toggleOnlineStatus() },
                                shape = CircleShape,
                                color = Color(0xFF0288D1),
                                shadowElevation = 6.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                                }
                            }
                        } else {
                            // Offline state: Black pill button centered
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

                    // Status Message Strip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(
                                if (isOnline) Color(0xFF0288D1) else Color.White,
                                RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (isOnline) Color.White else androidx.compose.material3.MaterialTheme.colorScheme.error, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = driverStatusText(uiState),
                                color = if (isOnline) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // White action grid and notice section
                    Column(
                        modifier = Modifier
                            .background(Color.White)
                            .fillMaxWidth()
                    ) {
                        // Action Grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ActionMenuItem(Icons.Default.DirectionsCar, "Loại dịch vụ")
                            ActionMenuItem(Icons.Default.LocationOn, "Điểm đến\nyêu thích")
                            ActionMenuItem(Icons.Default.FlashOn, "Tự động nhận")
                            ActionMenuItem(Icons.Default.MoreHoriz, "Xem thêm")
                        }

                        // Notice and Sparkle FAB overlay
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text("Thông tin tạm thời gián đoạn", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Bạn vẫn có thể tiếp tục nhận cuốc.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            }


                        }
                    }
                }
            }

            // Incoming ride card (as a center overlay)
            if (uiState.pickupLatLng != null && isOnline && uiState.status == DriverStatus.ONLINE) {
                IncomingRideOverlay(
                    uiState = uiState,
                    onAccept = {
                        viewModel.acceptTrip()
                        onNavigateToTrip()
                    },
                    onReject = { viewModel.rejectTrip() },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            if (hasAcceptedRide) {
                ActiveRidePanel(
                    uiState = uiState,
                    onArrived = viewModel::arrivedAtPickup,
                    onStartTrip = viewModel::startTrip,
                    onCompleteTrip = viewModel::completeTrip,
                    onOpenNavigation = {
                        uiState.navigationTargetLatLng?.let { target ->
                            val googleMapsIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("google.navigation:q=${target.latitude},${target.longitude}&mode=d")
                            ).setPackage("com.google.android.apps.maps")
                            val fallbackIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("geo:${target.latitude},${target.longitude}?q=${target.latitude},${target.longitude}")
                            )

                            runCatching {
                                context.startActivity(googleMapsIntent)
                            }.onFailure {
                                context.startActivity(fallbackIntent)
                            }
                        }
                    },
                    onCallPassenger = {
                        openDialer(context, uiState.passengerPhone)
                    },
                    onOpenChat = {
                        uiState.currentRideId?.takeIf { it.isNotBlank() }?.let(onOpenChat)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = paddingValues.calculateBottomPadding() + 12.dp)
                        .padding(horizontal = 16.dp)
                )
            }

            uiState.error?.takeIf { it.isNotBlank() }?.let { error ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3F0),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            RideMessagePopupListener(
                rideId = uiState.currentRideId,
                enabled = hasAcceptedRide,
                onOpenChat = onOpenChat
            )

            if (showWallet) {
                DriverWalletDialog(
                    uiState = uiState,
                    onDismiss = {
                        showWallet = false
                    }
                )
            }
        }
    }
}

private fun openDialer(
    context: android.content.Context,
    phone: String
) {
    val normalizedPhone = phone.trim()
    if (normalizedPhone.isBlank()) return

    context.startActivity(
        Intent(
            Intent.ACTION_DIAL,
            Uri.parse("tel:$normalizedPhone")
        )
    )
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

@Composable
fun MapActionFab(
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color(0xFF424242))
        }
    }
}

@Composable
fun ActionMenuItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(Color(0xFFF0F4F2), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF004D40), modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun DriverBottomNavigation(
    grabGreen: Color,
    onHomeClick: () -> Unit = {},
    onIncomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Trang chủ", fontSize = 10.sp) },
            selected = true,
            onClick = onHomeClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = grabGreen,
                selectedTextColor = grabGreen,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text("Thu nhập", fontSize = 10.sp) },
            selected = false,
            onClick = onIncomeClick
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            label = { Text("Hồ sơ", fontSize = 10.sp) },
            selected = false,
            onClick = onProfileClick
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("CHUYẾN XE MỚI", fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier
                    .size(10.dp)
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.primary, CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Text(uiState.pickupAddress, fontSize = 14.sp, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier
                    .size(10.dp)
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.error, CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Text(uiState.dropoffAddress, fontSize = 14.sp, maxLines = 1)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.tripPrice.toInt()}đ",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
                Text(text = uiState.passengerName, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Từ chối", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(2f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Chấp nhận", fontWeight = FontWeight.Bold)
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
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "CHUYẾN ĐANG THỰC HIỆN",
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0288D1),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(uiState.pickupAddress, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(modifier = Modifier.height(6.dp))
            Text(uiState.dropoffAddress, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            if (uiState.navigationTitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEAF4FF)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color(0xFF0288D1)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.isRouting) "Đang tính đường..." else uiState.navigationTitle,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF163B5C)
                            )
                            if (uiState.routeDistance.isNotBlank() || uiState.routeDuration.isNotBlank()) {
                                Text(
                                    text = listOf(uiState.routeDistance, uiState.routeDuration)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" • "),
                                    color = Color(0xFF416985),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(uiState.passengerName, fontWeight = FontWeight.Bold)
                    Text(
                        text = uiState.passengerPhone.ifBlank { "Chưa có SĐT" },
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text("${uiState.tripPrice.toInt()}đ", color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                IconButton(
                    onClick = onCallPassenger,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE0F2F1), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                }
                IconButton(
                    onClick = onOpenChat,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFEAF4FF), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF0288D1))
                }
                OutlinedButton(
                    onClick = onOpenNavigation,
                    enabled = uiState.navigationTargetLatLng != null,
                    modifier = Modifier
                        .weight(1.25f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dẫn đường")
                }
                Button(
                    onClick = when (uiState.status) {
                        DriverStatus.ON_THE_WAY_TO_PICKUP -> onArrived
                        DriverStatus.ARRIVED_AT_PICKUP -> onStartTrip
                        DriverStatus.ON_TRIP -> onCompleteTrip
                        else -> ({})
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when (uiState.status) {
                            DriverStatus.ON_THE_WAY_TO_PICKUP -> "Đã tới"
                            DriverStatus.ARRIVED_AT_PICKUP -> "Bắt đầu"
                            DriverStatus.ON_TRIP -> "Hoàn thành"
                            else -> "Tiếp tục"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

}

@Composable
private fun DriverWalletDialog(
    uiState: DriverState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Ví tài xế")
        },
        text = {
            Column {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE4F6EC)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Số dư hiện tại",
                            color = Color(0xFF416152),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatWalletCurrency(uiState.walletBalance),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.walletCompletedTrips} cuốc xe đã hoàn thành",
                            color = Color(0xFF416152),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Giao dịch gần đây",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F2328)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val recentRides = uiState.walletRides.take(4)
                if (recentRides.isEmpty()) {
                    Text(
                        text = "Chưa có cuốc hoàn thành.",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                } else {
                    recentRides.forEach { ride ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFE8F5EE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp)
                            ) {
                                Text(
                                    text = ride.dropoffAddress.ifBlank { "Cuốc xe hoàn thành" },
                                    maxLines = 1,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = formatWalletDate(ride.completedAt ?: ride.updatedAt),
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "+${formatWalletCurrency(ride.price)}",
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

private fun formatWalletCurrency(
    amount: Double
): String {
    return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(amount)
}

private fun formatWalletDate(
    timestamp: Long
): String {
    if (timestamp <= 0L) return "Chưa có thời gian"
    return SimpleDateFormat("HH:mm dd/MM/yyyy", Locale("vi", "VN")).format(Date(timestamp))
}
