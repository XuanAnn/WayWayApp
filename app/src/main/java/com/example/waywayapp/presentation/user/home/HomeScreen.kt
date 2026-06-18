package com.example.waywayapp.presentation.user.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.location.Location
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.R
import com.example.waywayapp.domain.model.DriverLocation
import com.example.waywayapp.data.remote.dto.geocoding.GeocodingResponseDto
import com.example.waywayapp.data.repository.DriverLocationRepository
import com.example.waywayapp.domain.model.Service
import com.example.waywayapp.presentation.components.WayWayBottomBar
import com.example.waywayapp.presentation.navigation.Routes
import com.example.waywayapp.presentation.theme.BgLight
import com.example.waywayapp.presentation.theme.CardWhite
import com.example.waywayapp.presentation.theme.TextDark
import com.example.waywayapp.presentation.theme.TextGray
import com.example.waywayapp.presentation.user.home.model.ServiceUI
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.math.roundToInt



private val GreenPrimary = Color(0xFF1DB954)
private val GreenDark    = Color(0xFF0F6E56)
private val GreenSurface = Color(0xFFF0FBF4)


@Composable
fun HomeScreen(
    currentRoute: String? = Routes.USER_HOME,
    onServiceClick: (String) -> Unit = {},
    onWalletClick: () -> Unit = {},
    onBottomNavClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSearchSuggestionClick: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onPromoClick: () -> Unit = {},
    onAiAssistantClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val userName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Minh Tú"
    val userPhotoUrl = currentUser?.photoUrl?.toString()
    var homeScrollEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.refreshMomoStatus()
    }

    Scaffold(
        containerColor = BgLight,
        bottomBar = {}
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 104.dp),
                userScrollEnabled = homeScrollEnabled
            ) {
                item {
                    HomeHeader(
                        userName = userName,
                        searchText = uiState.searchText,
                        searchSuggestions = uiState.searchSuggestions,
                        onSearchChange = viewModel::onSearchChange,
                        onSearchClick = onSearchClick,
                        onSearchSuggestionClick = { suggestion ->
                            viewModel.onSearchChange(suggestion.display_name)
                            viewModel.clearSearchSuggestions()
                            onSearchSuggestionClick(suggestion.display_name)
                        },
                        onNotificationClick = onNotificationClick
                    )
                }
                // ✦ Banner thay thế WalletCard ở trên cùng
                item { PromoBanner(onPromoClick = onPromoClick) }
                item {
                    ServiceGrid(
                        services = uiState.services,
                        onServiceClick = { service -> onServiceClick(service) }
                    )
                }
                item {
                    HomeMapPreview(
                        userPhotoUrl = userPhotoUrl,
                        onMapTouchChanged = { isTouchingMap ->
                            homeScrollEnabled = !isTouchingMap
                        }
                    )
                }
                item {
                    WalletCard(
                        isLinked = uiState.momoLinked,
                        displayName = uiState.momoDisplayName.ifBlank { userName },
                        phone = uiState.momoPhone,
                        onWalletClick = onWalletClick
                    )
                }
            }

            WayWayBottomBar(
                currentRoute = currentRoute,
                onItemClick = onBottomNavClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 15.dp)
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

        }
    }
}

// ─────────────────────────────────────────
// HomeHeader — padding chuẩn hóa về 18.dp
// ─────────────────────────────────────────
@Composable
private fun HomeHeader(
    userName: String,
    searchText: String,
    searchSuggestions: List<GeocodingResponseDto>,
    onSearchChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSearchSuggestionClick: (GeocodingResponseDto) -> Unit,
    onNotificationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgLight)
            // ✦ 18.dp nhất quán với toàn screen
            .padding(start = 18.dp, top = 24.dp, end = 18.dp, bottom = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CHÀO MỪNG TRỞ LẠI",
                    color = TextGray,
                    // ✦ nhỏ hơn, letterSpacing rõ hơn
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = userName,
                    color = TextDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold   // ✦ Bold thay ExtraBold — bớt nặng
                )
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    // ✦ avatar tối để tương phản với BgLight
                    .background(Color(0xFF111111))
                    .clickable { onNotificationClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.initials(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SearchBox(
            modifier = Modifier.fillMaxWidth(),
            searchText = searchText,
            searchSuggestions = searchSuggestions,
            onSearchChange = onSearchChange,
            onSuggestionClick = onSearchSuggestionClick,
            onSearchClick = onSearchClick
        )
    }
}


@Composable
private fun PromoBanner(onPromoClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF111111),
        onClick = onPromoClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "✦  ƯU ĐÃI HÔM NAY",
                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    color = GreenPrimary, letterSpacing = 0.6.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Liên kết MoMo — hoàn 15%",
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White
                )
                Text(
                    "Áp dụng cho 3 chuyến đầu trong ngày",
                    fontSize = 10.sp, color = Color.White.copy(alpha = 0.55f)
                )
            }
            Spacer(Modifier.width(12.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = GreenPrimary
            ) {
                Text(
                    "Chi tiết",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White
                )
            }
        }
    }
}

// ─────────────────────────────────────────
// SearchBox — giữ nguyên logic, cải thiện visual
// ─────────────────────────────────────────
@Composable
private fun SearchBox(
    modifier: Modifier = Modifier,
    searchText: String,
    searchSuggestions: List<GeocodingResponseDto>,
    onSearchChange: (String) -> Unit = {},
    onSuggestionClick: (GeocodingResponseDto) -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CardWhite,
        shadowElevation = 4.dp,  // ✦ thêm elevation thay vì border phẳng
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = searchText,
                onValueChange = onSearchChange,
                singleLine = true,
                textStyle = TextStyle(color = TextDark, fontSize = 13.sp),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (searchText.isEmpty()) {
                            Text("Bạn muốn đi đâu?", color = TextGray, fontSize = 13.sp)
                        }
                        innerTextField()
                    }
                }
            )
        }
    }

    if (searchSuggestions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                searchSuggestions.forEachIndexed { index, suggestion ->
                    SearchSuggestionItem(suggestion = suggestion, onClick = { onSuggestionClick(suggestion) })
                    if (index < searchSuggestions.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 58.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────
// ServiceGrid — dùng weight thay vì width cố định
// ─────────────────────────────────────────
@Composable
private fun ServiceGrid(
    services: List<ServiceUI>,
    onServiceClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp)  // ✦ 18.dp nhất quán
    ) {
        SectionTitle(title = "Dịch vụ", action = "Tất cả")
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                services.forEachIndexed { index, service ->
                    ServiceItem(
                        service = service ,
                        isActive = index == 0,
                        onClick = { onServiceClick(service.type) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
        }
    }
}

@Composable
private fun ServiceItem(
    service: ServiceUI,
    isActive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            // ✦ active state highlight
            .background( GreenSurface)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = service.iconRes),
            contentDescription = service.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = service.title,
            fontSize = 12.sp,
            color = GreenDark ,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}


@Composable
private fun WalletCard(
    isLinked: Boolean,
    displayName: String,
    phone: String,
    onWalletClick: () -> Unit = {}
) {
    if (isLinked) {
        LinkedMomoWalletCard(
            displayName = displayName,
            phone = phone,
            onWalletClick = onWalletClick
        )
        return
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 18.dp, vertical = 4.dp)  // ✦ 18.dp nhất quán
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onWalletClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ✦ MoMo icon có background gradient thay vì dùng drawable phẳng
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFAA0011)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.momo_icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Liên kết MoMo",
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark
                )
                // ✦ subtitle mô tả lợi ích
                Text(
                    "Thanh toán nhanh hơn · Hoàn tiền 15%",
                    fontSize = 10.sp, color = TextGray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Chi tiết", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ─────────────────────────────────────────
// SectionTitle — font bớt nặng
// ─────────────────────────────────────────
private fun String.toMaskedPhone(): String {
    val compact = trim().replace(" ", "")
    if (compact.length <= 4) return compact
    return "${compact.take(3)}***${compact.takeLast(3)}"
}

@Composable
private fun LinkedMomoWalletCard(
    displayName: String,
    phone: String,
    onWalletClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onWalletClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFAA0011)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.momo_icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MoMo đã liên kết",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = buildString {
                        append(displayName.ifBlank { "Tài khoản WayWay" })
                        val maskedPhone = phone.toMaskedPhone()
                        if (maskedPhone.isNotBlank()) append(" · ").append(maskedPhone)
                    },
                    fontSize = 10.sp,
                    color = TextGray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Quản lý", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.SemiBold)
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    action: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextDark,
            fontSize = 15.sp,           // ✦ 18 → 15, bớt chiếm không gian
            fontWeight = FontWeight.Bold // ✦ ExtraBold → Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        if (action.isNotBlank()) {
            Text(text = action, color = GreenPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
        }
    }
}
// ─── GIỮ NGUYÊN SearchSuggestionItem từ code gốc ───
@Composable
private fun SearchSuggestionItem(
    suggestion: GeocodingResponseDto,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.display_name.substringBefore(",").ifBlank { suggestion.display_name },
                color = TextDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = suggestion.display_name,
                color = TextGray,
                fontSize = 12.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun HomeMapPreview(
    userPhotoUrl: String?,
    onMapTouchChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val driverLocationRepository = remember { DriverLocationRepository() }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var driverLocations by remember { mutableStateOf<List<DriverLocation>>(emptyList()) }
    var userMarkerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var bikeMarkerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var carMarkerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    val scope = rememberCoroutineScope()
    var releaseScrollJob by remember { mutableStateOf<Job?>(null) }
    val userPulseTransition = rememberInfiniteTransition(label = "user-location-pulse")
    val userPulseProgress by userPulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "user-location-pulse-progress"
    )
    val userPulseStep = (userPulseProgress * 14f).roundToInt() / 14f
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(16.0542, 108.2140), 15f)
    }
    var hasCenteredOnUser by remember { mutableStateOf(false) }
    fun lockParentScroll() {
        releaseScrollJob?.cancel()
        onMapTouchChanged(true)
        releaseScrollJob = scope.launch {
            delay(220)
            onMapTouchChanged(false)
        }
    }

    fun unlockParentScroll() {
        releaseScrollJob?.cancel()
        onMapTouchChanged(false)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            getHomeCurrentLocation(context) { currentLocation = it }
        }
    }

    LaunchedEffect(Unit) {
        if (hasHomeLocationPermission(context)) {
            getHomeCurrentLocation(context) { currentLocation = it }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(userPhotoUrl, colors.primary, colors.onPrimary, userPulseStep) {
        userMarkerIcon = withContext(Dispatchers.IO) {
            createUserMarkerIcon(
                photoUrl = userPhotoUrl,
                backgroundColor = colors.primary.toArgb(),
                foregroundColor = colors.onPrimary.toArgb(),
                pulseProgress = userPulseStep
            )
        }
    }

    LaunchedEffect(colors.primary, colors.onPrimary) {
        val markerColors = colors.primary.toArgb() to colors.onPrimary.toArgb()
        bikeMarkerIcon = withContext(Dispatchers.IO) {
            createDriverVehicleMarkerIcon(
                vehicleType = "bike",
                backgroundColor = markerColors.first,
                foregroundColor = markerColors.second
            )
        }
        carMarkerIcon = withContext(Dispatchers.IO) {
            createDriverVehicleMarkerIcon(
                vehicleType = "car",
                backgroundColor = colors.primary.toArgb(),
                foregroundColor = colors.onPrimary.toArgb()
            )
        }
    }

    LaunchedEffect(Unit) {
        driverLocationRepository.observeDriverLocations().collect { locations ->
            driverLocations = locations.filter { it.isValidRecentLocation() }
        }
    }

    LaunchedEffect(currentLocation) {
        val latLng = currentLocation ?: return@LaunchedEffect
        if (!hasCenteredOnUser) {
            hasCenteredOnUser = true
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                durationMs = 650
            )
        }
    }

    val nearbyDrivers = remember(currentLocation, driverLocations) {
        val userLatLng = currentLocation
        if (userLatLng == null) {
            driverLocations
        } else {
            driverLocations.sortedBy { it.distanceKmTo(userLatLng) }
        }
    }
    val nearestKm = remember(currentLocation, nearbyDrivers) {
        currentLocation?.let { userLatLng ->
            nearbyDrivers.firstOrNull()?.distanceKmTo(userLatLng)
        }
    }
    val etaMinutes = nearestKm?.let { estimatePickupMinutes(it) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // ✦ padding nhỏ hơn, map thoáng hơn trong màn hình
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ✦ Header với live pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = null,
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Bản đồ gần bạn",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                LivePill()
            }

            // Map + overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)   // ✦ nhỏ hơn 286dp — đủ context, không chiếm màn
            ) {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInteropFilter { event ->
                            when (event.actionMasked) {
                                MotionEvent.ACTION_DOWN,
                                MotionEvent.ACTION_POINTER_DOWN,
                                MotionEvent.ACTION_MOVE -> lockParentScroll()
                                MotionEvent.ACTION_UP,
                                MotionEvent.ACTION_CANCEL,
                                MotionEvent.ACTION_OUTSIDE,
                                MotionEvent.ACTION_POINTER_UP -> unlockParentScroll()
                            }
                            false
                        },
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                         mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                             context, R.raw.map_style_minimal
                         )
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        compassEnabled = false,
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled = false
                    )

                ) {
                    currentLocation?.let { latLng ->
                        Marker(
                            state = MarkerState(position = latLng),
                            title = "Vị trí của bạn",
                            icon = userMarkerIcon
                        )
                    }
                    nearbyDrivers.forEach { driver ->
                        val isCar = driver.vehicleType.equals("car", ignoreCase = true)
                        Marker(
                            state = MarkerState(position = LatLng(driver.latitude, driver.longitude)),
                            title = if (isCar) "Tài xế ô tô WayWay" else "Tài xế xe máy WayWay",
                            snippet = driver.distanceLabelFrom(currentLocation),
                            icon = if (isCar) carMarkerIcon else bikeMarkerIcon,
                            rotation = driver.heading,
                            flat = true
                        )
                    }
                }

                // ✦ Fade dùng màu surface thay vì BgLight cứng
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                                )
                            )
                        )
                )

                // ✦ Button chuyển xuống BottomEnd — dễ chạm hơn TopEnd
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(38.dp),
                    shape = RoundedCornerShape(12.dp),   // ✦ rounded rect thay vì circle
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp,
                    onClick = {
                        if (hasHomeLocationPermission(context)) {
                            getHomeCurrentLocation(context) { latLng ->
                                currentLocation = latLng
                                hasCenteredOnUser = false
                            }
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MyLocation,
                        contentDescription = "Về vị trí hiện tại",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(9.dp)
                    )
                }
            }

            // ✦ Stats footer — thông tin hữu ích ngay dưới map
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            MapStatsRowData(
                nearbyCount = nearbyDrivers.size,
                etaMinutes = etaMinutes,
                nearestKm = nearestKm
            )
        }
    }
}

// ✦ Live indicator
@Composable
private fun LivePill() {
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1DB954).copy(alpha = 0.1f),
        border = BorderStroke(0.5.dp, Color(0xFF1DB954).copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1DB954).copy(alpha = alpha))
            )
            Text(
                "Trực tiếp",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F6E56)
            )
        }
    }
}

// ✦ Stats footer
@Composable
private fun MapStatsRow(nearbyCount: Int, etaMinutes: Int?, nearestKm: Float?) {
    val etaText = etaMinutes?.let { "~$it ph" } ?: "--"
    val nearestText = nearestKm?.let { "%.1f km".format(it) } ?: "--"

    Row(modifier = Modifier.fillMaxWidth()) {
        listOf(
            nearbyCount.toString()      to "Xe gần đây",
            "~$etaMinutes ph"           to "Thời gian đón",
            "$nearestKm km"             to "Xe gần nhất"
        ).forEachIndexed { i, (value, label) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(label, fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (i < 2) VerticalDivider(
                modifier = Modifier.height(36.dp).align(Alignment.CenterVertically),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
private fun MapStatsRowData(nearbyCount: Int, etaMinutes: Int?, nearestKm: Float?) {
    val stats = listOf(
        nearbyCount.toString() to "Xe gần đây",
        (etaMinutes?.let { "~$it ph" } ?: "--") to "Thời gian đón",
        (nearestKm?.let { "%.1f km".format(it) } ?: "--") to "Xe gần nhất"
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        stats.forEachIndexed { i, (value, label) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(label, fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (i < 2) VerticalDivider(
                modifier = Modifier.height(36.dp).align(Alignment.CenterVertically),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

private fun hasHomeLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

@SuppressLint("MissingPermission")
private fun getHomeCurrentLocation(
    context: Context,
    onLocation: (LatLng) -> Unit
) {
    LocationServices.getFusedLocationProviderClient(context)
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocation(LatLng(location.latitude, location.longitude))
            }
        }
}

private fun DriverLocation.isValidRecentLocation(): Boolean {
    val isValidCoordinate = latitude.isFinite() &&
        longitude.isFinite() &&
        latitude in -90.0..90.0 &&
        longitude in -180.0..180.0 &&
        !(latitude == 0.0 && longitude == 0.0)
    if (!isValidCoordinate) return false

    val tenMinutesAgo = System.currentTimeMillis() - 10 * 60 * 1000
    return updatedAt == 0L || updatedAt >= tenMinutesAgo
}

private fun DriverLocation.distanceKmTo(target: LatLng): Float {
    val result = FloatArray(1)
    Location.distanceBetween(
        latitude,
        longitude,
        target.latitude,
        target.longitude,
        result
    )
    return result[0] / 1000f
}

private fun DriverLocation.distanceLabelFrom(target: LatLng?): String? {
    target ?: return null
    return "%.1f km từ bạn".format(distanceKmTo(target))
}

private fun estimatePickupMinutes(distanceKm: Float): Int {
    val citySpeedKmPerHour = 22f
    val movingMinutes = distanceKm / citySpeedKmPerHour * 60f
    return (movingMinutes + 2f).roundToInt().coerceAtLeast(1)
}

private fun createUserMarkerIcon(
    photoUrl: String?,
    backgroundColor: Int,
    foregroundColor: Int,
    pulseProgress: Float = 0f
): BitmapDescriptor {
    val size = 160
    val center = size / 2f
    val avatarDiameter = 86
    val avatarOffset = (size - avatarDiameter) / 2f
    val avatarRadius = avatarDiameter / 2f
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    repeat(3) { index ->
        val phase = (pulseProgress + index / 3f) % 1f
        val radius = 48f + phase * 30f
        val alpha = ((1f - phase) * 80f).roundToInt().coerceIn(0, 80)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = android.graphics.Color.argb(
            alpha,
            android.graphics.Color.red(backgroundColor),
            android.graphics.Color.green(backgroundColor),
            android.graphics.Color.blue(backgroundColor)
        )
        canvas.drawCircle(center, center, radius, paint)
    }

    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(center, center, avatarRadius + 7f, paint)

    val avatarBitmap = loadCircularAvatar(photoUrl, avatarDiameter)
    if (avatarBitmap != null) {
        canvas.drawBitmap(avatarBitmap, avatarOffset, avatarOffset, null)
    } else {
        paint.color = backgroundColor
        canvas.drawCircle(center, center, avatarRadius, paint)

        paint.color = foregroundColor
        canvas.drawCircle(center, center - 14f, 13f, paint)
        val body = Path().apply {
            addRoundRect(
                center - 25f,
                center + 10f,
                center + 25f,
                center + 38f,
                22f,
                22f,
                Path.Direction.CW
            )
        }
        canvas.drawPath(body, paint)
    }

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 5f
    paint.color = backgroundColor
    canvas.drawCircle(center, center, avatarRadius + 5f, paint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private fun createDriverVehicleMarkerIcon(
    vehicleType: String,
    backgroundColor: Int,
    foregroundColor: Int
): BitmapDescriptor {
    val size = 88
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val center = size / 2f

    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(center, center, 38f, paint)

    paint.color = backgroundColor
    canvas.drawCircle(center, center, 32f, paint)

    val isCar = vehicleType.equals("car", ignoreCase = true)
    paint.color = foregroundColor
    if (isCar) {
        val carBody = Path().apply {
            addRoundRect(22f, 34f, 66f, 58f, 10f, 10f, Path.Direction.CW)
            addRoundRect(30f, 24f, 58f, 42f, 8f, 8f, Path.Direction.CW)
        }
        canvas.drawPath(carBody, paint)

        paint.color = backgroundColor
        canvas.drawRoundRect(34f, 28f, 45f, 40f, 4f, 4f, paint)
        canvas.drawRoundRect(47f, 28f, 56f, 40f, 4f, 4f, paint)

        paint.color = foregroundColor
        canvas.drawCircle(31f, 60f, 5f, paint)
        canvas.drawCircle(57f, 60f, 5f, paint)
    } else {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawCircle(29f, 58f, 8f, paint)
        canvas.drawCircle(59f, 58f, 8f, paint)
        canvas.drawLine(29f, 58f, 42f, 42f, paint)
        canvas.drawLine(42f, 42f, 51f, 58f, paint)
        canvas.drawLine(51f, 58f, 59f, 58f, paint)
        canvas.drawLine(42f, 42f, 54f, 42f, paint)
        canvas.drawLine(54f, 42f, 62f, 36f, paint)
        canvas.drawLine(42f, 42f, 38f, 34f, paint)
        paint.style = Paint.Style.FILL
    }

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 4f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(center, center, 20f, paint)
    paint.style = Paint.Style.FILL

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private fun loadCircularAvatar(
    photoUrl: String?,
    size: Int
): Bitmap? {
    if (photoUrl.isNullOrBlank()) return null

    return runCatching {
        val source = URL(photoUrl).openStream().use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return null
        val scaled = Bitmap.createScaledBitmap(source, size, size, true)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val path = Path().apply {
            addCircle(size / 2f, size / 2f, size / 2f, Path.Direction.CW)
        }
        canvas.clipPath(path)
        canvas.drawBitmap(scaled, 0f, 0f, paint)
        output
    }.getOrNull()
}

private fun String.initials(): String {
    return trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifBlank { "U" }
}
