package com.example.waywayapp.presentation.user.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.presentation.components.WayWayBottomBar
import com.example.waywayapp.presentation.navigation.Routes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

// --- BẢNG MÀU ĐẶC TRƯNG WAYWAY ---
private val WayWayDarkGreen = Color(0xFF006C4C)
private val WayWayLightGreen = Color(0xFF1DB954)
private val AppBgColor = Color(0xFFF5F7FA)
private val CardBgColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1D1B20)
private val TextSecondary = Color(0xFF757575)

private data class ProfileActionCallbacks(
    val onPaymentClick: () -> Unit = {},
    val onNotificationClick: () -> Unit = {},
    val onSecurityClick: () -> Unit = {}
)

private val LocalProfileActionCallbacks = staticCompositionLocalOf {
    ProfileActionCallbacks()
}

// ────────────────────────────────────────────────────────
// ProfileScreen
// ────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onSignOut: () -> Unit = {},
    currentRoute: String? = Routes.PROFILE,
    onBottomNavClick: (String) -> Unit = {},
    onPaymentClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onBackClick: () -> Unit = { onBottomNavClick(Routes.USER_HOME) },
    showBottomNav: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.message, uiState.error) {
        val message = uiState.message ?: uiState.error
        if (!message.isNullOrBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(containerColor = AppBgColor, bottomBar = {}) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator(color = WayWayDarkGreen)
            }
            return@Scaffold
        }

        val user   = uiState.user
        val name   = user.name.ifBlank { "WayWay User" }
        val email  = user.email.ifBlank { "user@wayway.app" }
        val phone  = user.phone.ifBlank { "Chưa cập nhật" }

        Box(Modifier.fillMaxSize().padding(padding).background(AppBgColor)) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 112.dp)
            ) {
                item {
                    ProfileHero(
                        name = name,
                        role = roleTitle(user.role),
                        avatarUrl = user.avatarUrl,
                        tripCount = 42,        // TODO: từ uiState
                        rating = 4.9f,
                        memberMonths = 6,
                        onBackClick = onBackClick,
                        onSettingsClick = onEditProfileClick
                    )
                }
                item {
                    ProfileBody(
                        email = email,
                        phone = phone,
                        onPaymentClick = onPaymentClick,
                        onNotificationClick = onNotificationClick,
                        onSecurityClick = { viewModel.sendPasswordResetEmail() },
                        onLogout = { viewModel.signOut(); onSignOut() }
                    )
                }
            }

            if (showBottomNav) {
                WayWayBottomBar(
                    currentRoute = currentRoute,
                    onItemClick = onBottomNavClick,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────
// ProfileHero — Đồng bộ hệ thống màu WayWayDarkGreen
// ────────────────────────────────────────────────────────
@Composable
private fun ProfileHero(
    name: String,
    role: String,
    avatarUrl: String,
    tripCount: Int,
    rating: Float,
    memberMonths: Int,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var avatarBitmap by remember(avatarUrl) { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(avatarUrl) {
        avatarBitmap = withContext(Dispatchers.IO) {
            loadAvatarBitmap(context, avatarUrl)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(WayWayDarkGreen) // Thay AppDark bằng WayWayDarkGreen
    ) {
        // Decorative circles
        Canvas(modifier = Modifier.fillMaxWidth().height(280.dp)) {
            drawCircle(Color.White.copy(alpha = 0.06f), radius = size.width * 0.5f,
                center = Offset(size.width * 0.9f, size.height * 0.1f))
            drawCircle(Color.White.copy(alpha = 0.04f), radius = size.width * 0.4f,
                center = Offset(size.width * 0.1f, size.height * 0.8f))
        }

        Column {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 8.dp, start = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                Text(
                    "Hồ sơ của tôi",
                    modifier = Modifier.weight(1f),
                    color = Color.White, fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center
                )
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, null, tint = Color.White)
                }
            }

            // Avatar + name
            Column(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar với verified badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier.size(96.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(3.dp, Color.White)
                    ) {
                        Box(Modifier.padding(4.dp).clip(CircleShape)
                            .background(WayWayLightGreen), Alignment.Center) {

                            val bitmap = avatarBitmap
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(name.initials(), color = Color.White,
                                    fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    // Verified badge
                    Box(
                        Modifier.size(26.dp).clip(CircleShape)
                            .background(WayWayLightGreen)
                            .border(2.dp, WayWayDarkGreen, CircleShape),
                        Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color.White,
                            modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)

                // Role pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(role, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Stats
            Row(
                Modifier.fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp)
            ) {
                listOf(
                    "$tripCount" to "Chuyến đi",
                    "${"%.1f".format(rating)} ★" to "Đánh giá",
                    "$memberMonths th"     to "Thành viên"
                ).forEachIndexed { i, (num, label) ->
                    Column(
                        Modifier.weight(1f).padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(num, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text(label, color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    if (i < 2) VerticalDivider(
                        modifier = Modifier.height(30.dp).align(Alignment.CenterVertically),
                        thickness = 0.5.dp, color = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────
// ProfileBody
// ────────────────────────────────────────────────────────
@Composable
private fun ProfileBody(
    email: String,
    phone: String,
    onPaymentClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onLogout: () -> Unit
) {
    CompositionLocalProvider(
        LocalProfileActionCallbacks provides ProfileActionCallbacks(
            onPaymentClick = onPaymentClick,
            onNotificationClick = onNotificationClick,
            onSecurityClick = onSecurityClick
        )
    ) {
        Column(
            Modifier.fillMaxWidth()
                .background(AppBgColor)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Membership card
            MembershipCard(onUpgradeClick = onPaymentClick)

            // Contact info group
            InfoCard {
                InfoRow(Icons.Outlined.Email,       "Email",            email,          iconStyle = IconStyle.Green)
                InfoRow(Icons.Outlined.Phone,        "Số điện thoại",   phone,          iconStyle = IconStyle.Blue, isLast = false)
                InfoRow(Icons.Outlined.LocationOn,   "Địa chỉ mặc định","Chưa cập nhật",iconStyle = IconStyle.Amber, isLast = true, valueMuted = true)
            }

            // Settings group
            InfoCard {
                InfoRow(Icons.Outlined.AccountBalanceWallet, "Thanh toán",  "MoMo · Thẻ tín dụng", iconStyle = IconStyle.Gray)
                InfoRow(Icons.Outlined.Notifications,        "Thông báo",   "Đã bật",               iconStyle = IconStyle.Gray)
                InfoRow(Icons.Outlined.Shield,               "Bảo mật",     "Đổi mật khẩu",         iconStyle = IconStyle.Gray, isLast = true)
            }

            // Logout
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFEBEE),
                shadowElevation = 1.dp,
                onClick = onLogout
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(Color.White),
                        Alignment.Center
                    ) {
                        Icon(Icons.Default.Logout, null, tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                    }
                    Text("Đăng xuất", fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, color = Color(0xFFE53935),
                        modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFE53935))
                }
            }
        }
    }
}

@Composable
private fun MembershipCard(
    onUpgradeClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardBgColor,
        shadowElevation = 2.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier.size(48.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(WayWayLightGreen, WayWayDarkGreen))),
                Alignment.Center
            ) {
                Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("WayWay Standard", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("Nâng cấp để nhận ưu đãi tốt hơn", fontSize = 12.sp, color = TextSecondary)
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFE8F5EE),
                onClick = onUpgradeClick
            ) {
                Text("Nâng cấp",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WayWayDarkGreen)
            }
        }
    }
}

// ── Helper components ─────────────────────────────────
enum class IconStyle { Green, Blue, Amber, Gray, Red }

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardBgColor,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconStyle: IconStyle = IconStyle.Gray,
    isLast: Boolean = false,
    valueMuted: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val profileActions = LocalProfileActionCallbacks.current
    val resolvedOnClick = onClick ?: when (icon.name) {
        Icons.Outlined.AccountBalanceWallet.name -> profileActions.onPaymentClick
        Icons.Outlined.Notifications.name -> profileActions.onNotificationClick
        Icons.Outlined.Shield.name -> profileActions.onSecurityClick
        else -> null
    }
    val modifier = Modifier
        .fillMaxWidth()
        .then(if (resolvedOnClick != null) Modifier.clickable { resolvedOnClick() } else Modifier)
        .padding(horizontal = 16.dp, vertical = 14.dp)

    val (iconBg, iconTint) = when (iconStyle) {
        IconStyle.Green -> Color(0xFFE8F5EE) to WayWayDarkGreen
        IconStyle.Blue  -> Color(0xFFE8F4FF) to Color(0xFF2563EB)
        IconStyle.Amber -> Color(0xFFFFF7E6) to Color(0xFFF59E0B)
        IconStyle.Red   -> Color(0xFFFFEBEE) to Color(0xFFE53935)
        IconStyle.Gray  -> AppBgColor to TextSecondary
    }

    Column {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(iconBg),
                Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                Text(value, fontSize = 15.sp,
                    fontWeight = if (valueMuted) FontWeight.Medium else FontWeight.Bold,
                    color = if (valueMuted) TextSecondary else TextPrimary)
            }
            if (resolvedOnClick != null) {
                Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
            }
        }
        if (!isLast) HorizontalDivider(
            modifier = Modifier.padding(start = 70.dp, end = 16.dp),
            thickness = 0.5.dp, color = AppBgColor
        )
    }
}

private fun roleTitle(role: String): String {
    return when (role.uppercase()) {
        "DRIVER" -> "WayWay Driver"
        "ADMIN" -> "WayWay Administrator"
        else -> "WayWay Customer"
    }
}

private fun String.initials(): String {
    return trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifBlank { "U" }
}

private fun loadAvatarBitmap(context: Context, value: String): Bitmap? {
    if (value.isBlank()) return null

    return runCatching {
        if (value.startsWith("content://")) {
            context.contentResolver.openInputStream(Uri.parse(value))?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } else {
            URL(value).openStream().use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }
    }.getOrNull()
}