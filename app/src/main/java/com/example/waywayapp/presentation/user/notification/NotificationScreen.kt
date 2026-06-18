package com.example.waywayapp.presentation.user.notification

import androidx.annotation.DrawableRes
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

private enum class NotificationFilter(val label: String) {
    ALL("Tất cả"),
    RIDE("Cuốc xe"),
    PAYMENT("Thanh toán"),
    EXPRESS("Giao hàng")
}

private data class WayWayNotification(
    val id: String,
    val title: String,
    val message: String,
    val time: Long,
    val filter: NotificationFilter,
    @DrawableRes val iconRes: Int,
    val accent: Color,
    val isUnreadByDefault: Boolean
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    currentRoute: String? = Routes.NOTIFICATION,
    onBottomNavClick: (String) -> Unit = {},
    viewModel: NotificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme
    var selectedFilter by remember { mutableStateOf(NotificationFilter.ALL) }
    var readIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val notifications = remember(uiState.rides) {
        uiState.rides.flatMap { it.toNotifications() }
            .sortedByDescending { it.time }
    }
    val filteredNotifications = remember(notifications, selectedFilter) {
        if (selectedFilter == NotificationFilter.ALL) {
            notifications
        } else {
            notifications.filter { it.filter == selectedFilter }
        }
    }
    val unreadCount = notifications.count { it.isUnreadByDefault && it.id !in readIds }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Thông báo",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onBackground
                        )
                        Text(
                            text = if (unreadCount > 0) "$unreadCount thông báo mới" else "Bạn đã xem hết thông báo",
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { readIds = notifications.map { it.id }.toSet() }) {
                        Icon(
                            imageVector = Icons.Outlined.DoneAll,
                            contentDescription = "Đánh dấu đã đọc",
                            tint = colors.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colors.primary
                    )
                }

                uiState.error != null -> {
                    EmptyNotification(
                        title = "Không tải được thông báo",
                        message = uiState.error.orEmpty(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                notifications.isEmpty() -> {
                    EmptyNotification(
                        title = "Chưa có thông báo",
                        message = "Các cập nhật về cuốc xe, giao hàng và thanh toán sẽ xuất hiện tại đây.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 104.dp)
                    ) {
                        item {
                            NotificationFilters(
                                selected = selectedFilter,
                                onSelected = { selectedFilter = it }
                            )
                        }

                        if (filteredNotifications.isEmpty()) {
                            item {
                                EmptyNotification(
                                    title = "Không có mục phù hợp",
                                    message = "Thử chọn nhóm thông báo khác.",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 56.dp)
                                )
                            }
                        } else {
                            val grouped = filteredNotifications.groupBy { it.sectionLabel() }
                            grouped.forEach { (section, items) ->
                                item(key = "section_$section") {
                                    SectionLabel(section)
                                }
                                items(items, key = { it.id }) { item ->
                                    NotificationCard(
                                        item = item,
                                        isRead = item.id in readIds || !item.isUnreadByDefault,
                                        onClick = { readIds = readIds + item.id },
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
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

@Composable
private fun NotificationFilters(
    selected: NotificationFilter,
    onSelected: (NotificationFilter) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NotificationFilter.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = { Text(filter.label, fontSize = 12.sp) },
                shape = RoundedCornerShape(18.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primaryContainer,
                    selectedLabelColor = colors.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(Locale("vi", "VN")),
        modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 4.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.6.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun NotificationCard(
    item: WayWayNotification,
    isRead: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val container = if (isRead) colors.surface else colors.primaryContainer.copy(alpha = 0.28f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            LocalIconBubble(iconRes = item.iconRes, accent = item.accent)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = item.relativeTime(),
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.message,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(9.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.filter.label} · WayWay",
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                    if (!isRead) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalIconBubble(@DrawableRes iconRes: Int, accent: Color) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun EmptyNotification(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(66.dp),
            shape = CircleShape,
            color = colors.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.express_icon),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(title, color = colors.onBackground, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Spacer(Modifier.height(6.dp))
        Text(message, color = colors.onSurfaceVariant, fontSize = 13.sp)
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
