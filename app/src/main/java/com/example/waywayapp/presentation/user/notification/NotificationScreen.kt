package com.example.waywayapp.presentation.user.notification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.R
import com.example.waywayapp.domain.model.Ride
import com.example.waywayapp.presentation.components.WayWayBottomBar
import com.example.waywayapp.presentation.navigation.Routes
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.presentation.components.WayWayBottomBar
private val WayWayDarkGreen = Color(0xFF006C4C)
private val WayWayLightGreen = Color(0xFF1DB954)
private val AppBgColor = Color(0xFFF5F7FA)
private val CardBgColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1D1B20)
private val TextSecondary = Color(0xFF757575)

private enum class NotificationFilter(val label: String) {
    ALL("Tất cả"), RIDE("Cuốc xe"), PAYMENT("Thanh toán"), EXPRESS("Giao hàng")
}

private data class WayWayNotification(
    val id: String, val title: String, val message: String, val time: Long,
    val filter: NotificationFilter, @DrawableRes val iconRes: Int, val accent: Color, val isUnreadByDefault: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    currentRoute: String? = Routes.NOTIFICATION,
    onBottomNavClick: (String) -> Unit = {},
    viewModel: NotificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(NotificationFilter.ALL) }
    var readIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val notifications = remember(uiState.rides) {
        uiState.rides.flatMap { it.toNotifications() }.sortedByDescending { it.time }
    }
    val filteredNotifications = remember(notifications, selectedFilter) {
        if (selectedFilter == NotificationFilter.ALL) notifications else notifications.filter { it.filter == selectedFilter }
    }
    val unreadCount = notifications.count { it.isUnreadByDefault && it.id !in readIds }

    Scaffold(
        containerColor = AppBgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Thông báo", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text(
                            text = if (unreadCount > 0) "$unreadCount thông báo mới" else "Bạn đã xem hết thông báo",
                            fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                actions = {
                    IconButton(onClick = { readIds = notifications.map { it.id }.toSet() }) {
                        Icon(Icons.Outlined.DoneAll, contentDescription = "Đã đọc", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBgColor)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = WayWayDarkGreen)
                uiState.error != null -> EmptyNotification("Lỗi", uiState.error.orEmpty(), Modifier.align(Alignment.Center))
                notifications.isEmpty() -> EmptyNotification("Chưa có thông báo", "Các cập nhật sẽ xuất hiện tại đây.", Modifier.align(Alignment.Center))
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 104.dp)
                    ) {
                        item { NotificationFilters(selectedFilter) { selectedFilter = it } }

                        if (filteredNotifications.isEmpty()) {
                            item { EmptyNotification("Trống", "Thử chọn nhóm thông báo khác.", Modifier.padding(top = 56.dp)) }
                        } else {
                            val grouped = filteredNotifications.groupBy { it.sectionLabel() }
                            grouped.forEach { (section, items) ->
                                item(key = "section_$section") { SectionLabel(section) }
                                items(items, key = { it.id }) { item ->
                                    NotificationCard(
                                        item = item,
                                        isRead = item.id in readIds || !item.isUnreadByDefault,
                                        onClick = { readIds = readIds + item.id },
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            WayWayBottomBar(currentRoute = currentRoute, onItemClick = onBottomNavClick, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun NotificationFilters(selected: NotificationFilter, onSelected: (NotificationFilter) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NotificationFilter.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = { Text(filter.label, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WayWayDarkGreen,
                    selectedLabelColor = Color.White,
                    containerColor = CardBgColor,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected == filter, borderColor = Color.Transparent)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(Locale("vi", "VN")),
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
        fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp, color = TextSecondary
    )
}

@Composable
private fun NotificationCard(item: WayWayNotification, isRead: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val container = if (isRead) CardBgColor else Color(0xFFE8F5EE) // Nền xanh nhạt nếu chưa đọc

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(if (isRead) 1.dp else 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
            LocalIconBubble(item.iconRes, item.accent)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.title, modifier = Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text(item.relativeTime(), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                }
                Spacer(Modifier.height(4.dp))
                Text(item.message, fontSize = 13.sp, lineHeight = 20.sp, color = TextSecondary)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${item.filter.label} · WayWay", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    if (!isRead) {
                        Spacer(Modifier.width(8.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WayWayLightGreen))
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalIconBubble(@DrawableRes iconRes: Int, accent: Color) {
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).background(accent.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Image(painterResource(iconRes), null, contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun EmptyNotification(title: String, message: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(80.dp).background(CardBgColor, CircleShape), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.express_icon), null, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text(message, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}



private fun Ride.toNotifications(): List<WayWayNotification> {
    val statusTime = completedAt ?: updatedAt.takeIf { it != 0L } ?: createdAt
    val serviceFilter = if (serviceType.equals("express", ignoreCase = true)) {
        NotificationFilter.EXPRESS
    } else {
        NotificationFilter.RIDE
    }
    val serviceIcon = serviceIconRes(serviceType)
    val accent = serviceAccent(serviceType)
    val serviceTitle = serviceName(serviceType)

    val statusNotification = WayWayNotification(
        id = "ride-$id-${status.lowercase()}",
        title = statusTitle(status, serviceTitle),
        message = statusMessage(this, serviceTitle),
        time = statusTime,
        filter = serviceFilter,
        iconRes = serviceIcon,
        accent = accent,
        isUnreadByDefault = status !in setOf("completed", "cancelled")
    )

    val paymentNotification = if (
        paymentStatus.equals("paid", ignoreCase = true) ||
        paymentMethod.contains("momo", ignoreCase = true)
    ) {
        WayWayNotification(
            id = "payment-$id",
            title = if (paymentStatus.equals("paid", ignoreCase = true)) "Thanh toán thành công" else "Thanh toán đang xử lý",
            message = "${formatCurrency(price)} qua ${paymentMethodName(paymentMethod)} cho $serviceTitle.",
            time = paidAt ?: statusTime,
            filter = NotificationFilter.PAYMENT,
            iconRes = if (paymentMethod.contains("momo", ignoreCase = true)) R.drawable.momo_icon else R.drawable.dollar,
            accent = Color(0xFF22C55E),
            isUnreadByDefault = paymentStatus.equals("paid", ignoreCase = true)
        )
    } else {
        null
    }

    return listOfNotNull(statusNotification, paymentNotification)
}

private fun WayWayNotification.sectionLabel(): String {
    val now = System.currentTimeMillis()
    val age = now - time
    return when {
        age < 24L * 60L * 60L * 1000L -> "Hôm nay"
        age < 7L * 24L * 60L * 60L * 1000L -> "Tuần này"
        else -> "Trước đó"
    }
}

private fun WayWayNotification.relativeTime(): String {
    val diff = (System.currentTimeMillis() - time).coerceAtLeast(0L)
    val minute = 60_000L
    val hour = 60L * minute
    val day = 24L * hour
    return when {
        time <= 0L -> "--"
        diff < minute -> "Vừa xong"
        diff < hour -> "${diff / minute} phút"
        diff < day -> "${diff / hour} giờ"
        diff < 7L * day -> "${diff / day} ngày"
        else -> SimpleDateFormat("dd/MM", Locale("vi", "VN")).format(Date(time))
    }
}

private fun statusTitle(status: String, serviceName: String): String = when (status.lowercase()) {
    "searching" -> "Đang tìm tài xế"
    "accepted" -> "Tài xế đã nhận cuốc"
    "arrived" -> "Tài xế đã đến điểm đón"
    "in_progress" -> "$serviceName đang diễn ra"
    "completed" -> "$serviceName hoàn thành"
    "cancelled" -> "$serviceName đã hủy"
    else -> "Cập nhật $serviceName"
}

private fun statusMessage(ride: Ride, serviceName: String): String = when (ride.status.lowercase()) {
    "searching" -> "WayWay đang tìm tài xế phù hợp cho điểm đến ${ride.dropoffAddress.shortAddress()}."
    "accepted" -> "${ride.driverName.ifBlank { "Tài xế" }} đang đến điểm đón của bạn."
    "arrived" -> "${ride.driverName.ifBlank { "Tài xế" }} đã tới điểm đón ${ride.pickupAddress.shortAddress()}."
    "in_progress" -> "Bạn đang di chuyển tới ${ride.dropoffAddress.shortAddress()}."
    "completed" -> "$serviceName đã hoàn thành. Cảm ơn bạn đã sử dụng WayWay."
    "cancelled" -> "$serviceName đã được hủy. Bạn có thể đặt lại bất cứ lúc nào."
    else -> "WayWay vừa cập nhật trạng thái cuốc xe của bạn."
}

@DrawableRes
private fun serviceIconRes(serviceType: String): Int = when (serviceType.lowercase()) {
    "car" -> R.drawable.car_icon
    "express" -> R.drawable.express_icon
    else -> R.drawable.bike_icon
}

private fun serviceAccent(serviceType: String): Color = when (serviceType.lowercase()) {
    "car" -> Color(0xFF2563EB)
    "express" -> Color(0xFFF59E0B)
    else -> Color(0xFF22C55E)
}

private fun serviceName(serviceType: String): String = when (serviceType.lowercase()) {
    "car" -> "WayWay Car"
    "express" -> "WayWay Express"
    else -> "WayWay Bike"
}

private fun paymentMethodName(value: String): String = when {
    value.contains("momo", ignoreCase = true) -> "MoMo"
    value.contains("cash", ignoreCase = true) -> "tiền mặt"
    else -> value.ifBlank { "phương thức thanh toán" }
}

private fun String.shortAddress(): String {
    return substringBefore(",").ifBlank { ifBlank { "điểm đến" } }
}

private fun formatCurrency(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(value)
}
