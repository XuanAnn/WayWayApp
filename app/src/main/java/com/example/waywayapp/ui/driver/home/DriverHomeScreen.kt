package com.example.waywayapp.ui.driver.home

import android.Manifest
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.ui.theme.SuccessGreen
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*

@Composable
fun DriverHomeScreen(
    viewModel: DriverViewModel = viewModel(),
    onNavigateToTrip: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val isOnline = uiState.status != DriverStatus.OFFLINE
    val grabGreen = Color(0xFF00B14F)

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

    Scaffold(
        bottomBar = { DriverBottomNavigation(grabGreen) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Map Layer
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = uiState.currentLatLng != null),
                uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false)
            )

            // 2. Right Side Floating Controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = 150.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MapActionFab(Icons.Default.MyLocation)
                MapActionFab(Icons.Default.Layers)
                MapActionFab(Icons.Default.Sync)
            }

            // 3. Bottom UI Layer
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
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
                                .background(if (isOnline) Color.White else Color.Red, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isOnline) "Bạn đang bật kết nối." else "Bạn đang tắt kết nối.",
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
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text("Thông tin tạm thời gián đoạn", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Bạn vẫn có thể tiếp tục nhận cuốc.", color = Color.Gray, fontSize = 14.sp)
                        }
                        
                        // Sparkle FAB (Bottom Right)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 12.dp)
                                .size(52.dp),
                            shape = CircleShape,
                            shadowElevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF26C6DA), Color(0xFF00897B))
                                        )
                                    )
                                    .clickable { },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
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
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun MapActionFab(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(44.dp),
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
fun DriverBottomNavigation(grabGreen: Color) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Trang chủ", fontSize = 10.sp) },
            selected = true,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = grabGreen,
                selectedTextColor = grabGreen,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null) },
            label = { Text("Thu nhập", fontSize = 10.sp) },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Mail, contentDescription = null) },
            label = { Text("Hộp thư", fontSize = 10.sp) },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
            label = { Text("Lịch", fontSize = 10.sp) },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            label = { Text("Hồ sơ", fontSize = 10.sp) },
            selected = false,
            onClick = { }
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
            Text("CHUYẾN XE MỚI", fontWeight = FontWeight.ExtraBold, color = Color(0xFF00B14F), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(Color(0xFF00B14F), CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Text(uiState.pickupAddress, fontSize = 14.sp, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(Color.Red, CircleShape))
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
                Text(text = uiState.passengerName, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Từ chối", color = Color.Gray)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(2f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Chấp nhận", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
