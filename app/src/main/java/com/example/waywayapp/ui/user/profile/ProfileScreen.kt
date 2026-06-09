package com.example.waywayapp.ui.user.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.waywayapp.ui.components.WayWayBottomBar
import com.example.waywayapp.ui.navigation.Routes
import com.example.waywayapp.ui.theme.BgLight
import com.example.waywayapp.ui.theme.CardWhite
import com.example.waywayapp.ui.theme.TextDark
import com.example.waywayapp.ui.theme.TextGray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Surface

// ── Palette khớp với app theme ──────────────────────────
private val GreenPrimary  = Color(0xFF1DB954)
private val GreenDark     = Color(0xFF0F6E56)
private val GreenSurface  = Color(0xFFF0FBF4)
private val AppDark       = Color(0xFF111111)

private data class ProfileActionCallbacks(
    val onPaymentClick: () -> Unit = {},
    val onNotificationClick: () -> Unit = {},
    val onSecurityClick: () -> Unit = {}
)

private val LocalProfileActionCallbacks = staticCompositionLocalOf {
    ProfileActionCallbacks()
}
// Xoá ProfileBlue, ProfileBlueDeep — không dùng nữa

// ────────────────────────────────────────────────────────
// ProfileScreen — không đổi logic
// ────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onSignOut: () -> Unit = {},
    currentRoute: String? = Routes.PROFILE,
    onBottomNavClick: (String) -> Unit = {},
    onPaymentClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
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

    Scaffold(containerColor = BgLight, bottomBar = {}) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
            return@Scaffold
        }

        val user   = uiState.user
        val name   = user.name.ifBlank { "WayWay User" }
        val email  = user.email.ifBlank { "user@wayway.app" }
        val phone  = user.phone.ifBlank { "Chưa cập nhật" }

        Box(Modifier.fillMaxSize().padding(padding).background(BgLight)) {
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
                        onBackClick = { onBottomNavClick(Routes.USER_HOME) },
                        onSettingsClick = { viewModel.loadProfile() }
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

            WayWayBottomBar(
                currentRoute = currentRoute,
                onItemClick = onBottomNavClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ────────────────────────────────────────────────────────
// ProfileHero — theme tối, stats liên quan đến ride-hailing
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

    LaunchedEffect(avatarUrl) {
        avatarBitmap = withContext(Dispatchers.IO) {
            loadAvatarBitmap(avatarUrl)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // ✦ Theme tối (#111) thay vì xanh dương — khớp với bottom nav và banner
            .background(AppDark)
    ) {
        // Decorative circles
        Canvas(modifier = Modifier.fillMaxWidth().height(280.dp)) {
            drawCircle(GreenPrimary.copy(alpha = 0.10f), radius = size.width * 0.55f,
                center = Offset(size.width * 0.95f, size.height * 0.15f))
            drawCircle(GreenPrimary.copy(alpha = 0.07f), radius = size.width * 0.4f,
                center = Offset(size.width * 0.1f, -size.height * 0.05f))
        }

        Column {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Box(
                        Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.1f)),
                        Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White,
                            modifier = Modifier.size(18.dp))
                    }
                }
                Text(
                    "Hồ sơ của tôi",
                    modifier = Modifier.weight(1f),
                    color = Color.White, fontSize = 14.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                )
                IconButton(onClick = onSettingsClick) {
                    Box(
                        Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.1f)),
                        Alignment.Center
                    ) {
                        Icon(Icons.Default.Settings, null, tint = Color.White,
                            modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Avatar + name
            Column(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ✦ Avatar với verified badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier.size(86.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.12f),
                        border = BorderStroke(2.dp, GreenPrimary.copy(alpha = 0.5f))
                    ) {
                        Box(Modifier.padding(5.dp).clip(CircleShape)
                            .background(GreenPrimary), Alignment.Center) {
                            // Nếu có avatar: dùng ProfileAvatar cũ
                            // Nếu không:
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
                                    fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    // Verified badge
                    Box(
                        Modifier.size(22.dp).clip(CircleShape)
                            .background(GreenPrimary)
                            .then(Modifier.offset(x = 2.dp, y = 2.dp)),
                        Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color.White,
                            modifier = Modifier.size(12.dp))
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                // ✦ Role pill với màu xanh lá
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GreenPrimary.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, GreenPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(top = 5.dp)
                ) {
                    Text(role, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = GreenPrimary)
                }
            }

            // ✦ Stats liên quan ride-hailing thay vì Followers/Following
            Row(
                Modifier.fillMaxWidth()
                    .padding(top = 16.dp)
                    .border(BorderStroke(0.dp, Color.Transparent))
            ) {
                listOf(
                    "$tripCount"           to "Chuyến đi",
                    "${"%.1f".format(rating)} ★" to "Đánh giá",
                    "$memberMonths th"     to "Thành viên"
                ).forEachIndexed { i, (num, label) ->
                    Column(
                        Modifier.weight(1f).padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(num, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(label, color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp, fontWeight = FontWeight.Medium,
                            letterSpacing = 0.4.sp)
                    }
                    if (i < 2) VerticalDivider(
                        modifier = Modifier.height(36.dp).align(Alignment.CenterVertically),
                        thickness = 0.5.dp, color = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────
// ProfileBody — nhóm thành cards, bỏ Twitter/Behance
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
            .background(BgLight)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
            shape = RoundedCornerShape(18.dp),
            color = CardWhite,
            border = BorderStroke(0.5.dp, Color(0xFFFFE0E0)),
            onClick = onLogout
        ) {
            Row(
                Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFF0F0)),
                    Alignment.Center
                ) {
                    Icon(Icons.Default.Logout, null,
                        tint = Color(0xFFE24B4A), modifier = Modifier.size(17.dp))
                }
                Text("Đăng xuất", fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold, color = Color(0xFFE24B4A),
                    modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null,
                    tint = Color(0xFFCCCCCC), modifier = Modifier.size(16.dp))
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
        shape = RoundedCornerShape(18.dp),
        color = CardWhite,
        border = BorderStroke(0.5.dp, Color(0xFFEBEBEB))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(GreenPrimary, GreenDark))),
                Alignment.Center
            ) {
                Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("WayWay Standard", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text("Nâng cấp để nhận ưu đãi tốt hơn", fontSize = 10.sp, color = TextGray)
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = GreenSurface,
                border = BorderStroke(0.5.dp, Color(0xFF9FE1CB)),
                onClick = onUpgradeClick
            ) {
                Text("Nâng cấp",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GreenDark)
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
        shape = RoundedCornerShape(18.dp),
        color = CardWhite,
        border = BorderStroke(0.5.dp, Color(0xFFEBEBEB))
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
    // ✦ Bỏ 2 nhánh if/else giống hệt nhau, dùng clickable conditional
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
        .padding(horizontal = 16.dp, vertical = 13.dp)

    val (iconBg, iconTint) = when (iconStyle) {
        IconStyle.Green -> Color(0xFFF0FBF4) to GreenPrimary
        IconStyle.Blue  -> Color(0xFFE8F4FF) to Color(0xFF378ADD)
        IconStyle.Amber -> Color(0xFFFFF7E6) to Color(0xFFEF9F27)
        IconStyle.Red   -> Color(0xFFFFF0F0) to Color(0xFFE24B4A)
        IconStyle.Gray  -> Color(0xFFF5F5F5) to Color(0xFF888888)
    }

    Column {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
                Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(17.dp))
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                    color = TextGray, letterSpacing = 0.3.sp)
                Text(value, fontSize = 13.sp,
                    fontWeight = if (valueMuted) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (valueMuted) TextGray else TextDark,
                    modifier = Modifier.padding(top = 1.dp))
            }
            if (resolvedOnClick != null) {
                Icon(Icons.Default.ChevronRight, null,
                    tint = Color(0xFFCCCCCC), modifier = Modifier.size(16.dp))
            }
        }
        if (!isLast) HorizontalDivider(
            modifier = Modifier.padding(start = 64.dp, end = 16.dp),
            thickness = 0.5.dp, color = Color(0xFFF5F5F5)
        )
    }
}

// roleTitle, String.initials(), loadAvatarBitmap — giữ nguyên
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

private fun loadAvatarBitmap(url: String): Bitmap? {
    if (url.isBlank()) return null

    return runCatching {
        URL(url).openStream().use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }.getOrNull()
}
