package com.example.waywayapp.presentation.user.history

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

private enum class HistoryFilter(val label: String) {
    SCHEDULED("Scheduled"),
    RECENT("Recent"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    currentRoute: String? = Routes.RECENTLY_SERVICE,
    onBottomNavClick: (String) -> Unit = {},
    onRateRide: (String) -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme
    var selectedRide by remember { mutableStateOf<Ride?>(null) }
    var selectedFilter by remember { mutableStateOf(HistoryFilter.RECENT) }

    val filteredRides = remember(uiState.rides, selectedFilter) {
        uiState.rides.filterBy(selectedFilter)
    }
    val completedCount = uiState.rides.count { it.status.equals("completed", ignoreCase = true) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hoạt động",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onBackground
                        )
                        Text(
                            text = "$completedCount chuyến đã hoàn thành",
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant
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
                    EmptyHistory(
                        title = "Không tải được lịch sử",
                        message = uiState.error.orEmpty(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.rides.isEmpty() -> {
                    EmptyHistory(
                        title = "Chưa có hoạt động",
                        message = "Các chuyến xe và đơn giao hàng sẽ xuất hiện tại đây.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 14.dp,
                            top = 10.dp,
                            end = 14.dp,
                            bottom = 108.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            HistoryFilters(
                                selected = selectedFilter,
                                onSelected = { selectedFilter = it }
                            )
                        }

                        if (filteredRides.isEmpty()) {
                            item {
                                EmptyHistory(
                                    title = "Không có dữ liệu",
                                    message = "Nhóm ${selectedFilter.label} hiện chưa có chuyến nào.",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 52.dp)
                                )
                            }
                        } else {
                            val grouped = filteredRides.groupBy { it.monthLabel() }
                            grouped.forEach { (month, rides) ->
                                item(key = "month_$month") {
                                    MonthLabel(month)
                                }
                                items(rides, key = { it.id }) { ride ->
                                    RideHistoryCard(
                                        ride = ride,
                                        onClick = { selectedRide = ride },
                                        onRate = { onRateRide(ride.id) }
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

    selectedRide?.let { ride ->
        RideDetailDialog(
            ride = ride,
            onDismiss = { selectedRide = null },
            onRateRide = {
                selectedRide = null
                onRateRide(ride.id)
            }
        )
    }
}

@Composable
private fun HistoryFilters(
    selected: HistoryFilter,
    onSelected: (HistoryFilter) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HistoryFilter.entries.forEach { filter ->
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
private fun MonthLabel(text: String) {
    Text(
        text = text.uppercase(Locale("vi", "VN")),
        modifier = Modifier.padding(start = 6.dp, top = 14.dp, bottom = 4.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.6.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun RideHistoryCard(
    ride: Ride,
    onClick: () -> Unit,
    onRate: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val status = ride.statusInfo()
    val canRate = ride.status.equals("completed", ignoreCase = true) && ride.driverId.isNotBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LocalIconBubble(iconRes = serviceIconRes(ride.serviceType), accent = serviceAccent(ride.serviceType))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = serviceName(ride.serviceType),
                            modifier = Modifier.weight(1f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = formatCurrency(ride.price),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = formatDate(ride.displayTime()),
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            RoutePreview(
                pickup = ride.pickupAddress.ifBlank { "Điểm đón chưa cập nhật" },
                dropoff = ride.dropoffAddress.ifBlank { "Điểm đến chưa cập nhật" }
            )

            if (ride.driverName.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = buildString {
                        append(ride.driverName)
                        if (ride.driverPlate.isNotBlank()) append(" · ").append(ride.driverPlate)
                    },
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusPill(status)
                when {
                    ride.userRating > 0 -> Text("★".repeat(ride.userRating), color = Color(0xFFF59E0B), fontSize = 14.sp)
                    canRate -> TextButton(onClick = onRate, contentPadding = PaddingValues(horizontal = 0.dp)) {
                        Text("Đánh giá", fontSize = 12.sp)
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
            .size(48.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun RoutePreview(pickup: String, dropoff: String) {
    val colors = MaterialTheme.colorScheme
    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
            )
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(24.dp)
                    .background(colors.outlineVariant)
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(pickup, fontSize = 12.sp, color = colors.onSurfaceVariant, lineHeight = 16.sp, maxLines = 1)
            Text(dropoff, fontSize = 12.sp, color = colors.onSurfaceVariant, lineHeight = 16.sp, maxLines = 1)
        }
    }
}

@Composable
private fun StatusPill(status: StatusInfo) {
    Surface(shape = RoundedCornerShape(20.dp), color = status.container) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = status.content
        )
    }
}

@Composable
private fun EmptyHistory(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(modifier = Modifier.size(66.dp), shape = CircleShape, color = colors.primaryContainer) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.bike_icon),
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

@Composable
private fun RideDetailDialog(
    ride: Ride,
    onDismiss: () -> Unit,
    onRateRide: () -> Unit
) {
    val canRate = ride.status.equals("completed", ignoreCase = true) && ride.driverId.isNotBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chi tiết hoạt động", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                DetailRow("Dịch vụ", serviceName(ride.serviceType))
                DetailRow("Trạng thái", ride.statusInfo().label)
                DetailRow("Tài xế", ride.driverName.ifBlank { "Chưa cập nhật" })
                DetailRow("Biển số", ride.driverPlate.ifBlank { "Chưa cập nhật" })
                DetailRow("SĐT tài xế", ride.driverPhone.ifBlank { "Chưa cập nhật" })
                DetailRow("Giá", formatCurrency(ride.price))
                DetailRow("Thanh toán", "${paymentMethodName(ride.paymentMethod)} · ${paymentStatusName(ride.paymentStatus)}")
                DetailRow("Thời gian", formatDate(ride.displayTime()))
                if (ride.userRating > 0) {
                    DetailRow("Đánh giá", "${ride.userRating}/5")
                    if (ride.userReview.isNotBlank()) DetailRow("Nhận xét", ride.userReview)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                DetailRow("Điểm đón", ride.pickupAddress.ifBlank { "Chưa cập nhật" })
                DetailRow("Điểm đến", ride.dropoffAddress.ifBlank { "Chưa cập nhật" })
            }
        },
        dismissButton = {
            if (canRate) {
                TextButton(onClick = onRateRide) {
                    Text(if (ride.userRating > 0) "Sửa đánh giá" else "Đánh giá")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(label, color = colors.onSurfaceVariant, fontSize = 12.sp)
        Text(value, color = colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

private data class StatusInfo(
    val label: String,
    val container: Color,
    val content: Color
)

private fun Ride.statusInfo(): StatusInfo = when (status.lowercase()) {
    "scheduled" -> StatusInfo("Đã lên lịch", Color(0xFFEDE9FE), Color(0xFF5B21B6))
    "completed" -> StatusInfo("Hoàn thành", Color(0xFFEAF7EE), Color(0xFF15803D))
    "cancelled" -> StatusInfo("Đã hủy", Color(0xFFFEE2E2), Color(0xFFB91C1C))
    "accepted" -> StatusInfo("Đã nhận", Color(0xFFEFF6FF), Color(0xFF1D4ED8))
    "arrived" -> StatusInfo("Đã đến", Color(0xFFEFF6FF), Color(0xFF1D4ED8))
    "in_progress" -> StatusInfo("Đang diễn ra", Color(0xFFFFF7ED), Color(0xFFC2410C))
    else -> StatusInfo("Đang tìm tài xế", Color(0xFFFFF7ED), Color(0xFFC2410C))
}

private fun List<Ride>.filterBy(filter: HistoryFilter): List<Ride> = when (filter) {
    HistoryFilter.SCHEDULED -> filter { it.status.equals("scheduled", ignoreCase = true) }
    HistoryFilter.RECENT -> filter {
        it.status.lowercase() !in setOf("scheduled", "completed", "cancelled")
    }
    HistoryFilter.COMPLETED -> filter { it.status.equals("completed", ignoreCase = true) }
    HistoryFilter.CANCELLED -> filter { it.status.equals("cancelled", ignoreCase = true) }
}

private fun Ride.displayTime(): Long {
    return completedAt ?: updatedAt.takeIf { it != 0L } ?: createdAt
}

private fun Ride.monthLabel(): String {
    return SimpleDateFormat("MMMM, yyyy", Locale("vi", "VN"))
        .format(Date(displayTime()))
        .replaceFirstChar { it.uppercase(Locale("vi", "VN")) }
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
    value.contains("cash", ignoreCase = true) -> "Tiền mặt"
    else -> value.ifBlank { "Chưa cập nhật" }
}

private fun paymentStatusName(value: String): String = when (value.lowercase()) {
    "paid" -> "Đã thanh toán"
    "failed" -> "Thất bại"
    else -> "Đang chờ"
}

private fun formatDate(value: Long): String {
    if (value <= 0L) return "Chưa cập nhật"
    return SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale("vi", "VN")).format(Date(value))
}

private fun formatCurrency(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(value)
}
