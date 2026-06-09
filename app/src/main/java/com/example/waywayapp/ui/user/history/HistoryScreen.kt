package com.example.waywayapp.ui.user.history

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.data.model.Ride
import com.example.waywayapp.ui.components.WayWayBottomBar
import com.example.waywayapp.ui.navigation.Routes
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    currentRoute: String? = Routes.RECENTLY_SERVICE,
    onBottomNavClick: (String) -> Unit = {},
    onRateRide: (String) -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedRide by remember { mutableStateOf<Ride?>(null) }
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lịch sử cuốc",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = colors.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        bottomBar = {}
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        title = "Chưa có cuốc xe",
                        message = "Các chuyến đã đặt sẽ xuất hiện tại đây.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 104.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.rides, key = { it.id }) { ride ->
                            RideHistoryCard(
                                ride = ride,
                                onClick = {
                                    selectedRide = ride
                                }
                            )
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
            onDismiss = {
                selectedRide = null
            },
            onRateRide = {
                selectedRide = null
                onRateRide(ride.id)
            }
        )
    }
}

@Composable
private fun RideHistoryCard(
    ride: Ride,
    onClick: () -> Unit
) {
    val statusInfo = statusInfo(ride.status)
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(statusInfo.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusInfo.icon,
                    contentDescription = null,
                    tint = statusInfo.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = serviceName(ride.serviceType),
                        color = colors.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatCurrency(ride.price),
                        color = colors.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatDate(ride.completedAt ?: ride.updatedAt.takeIf { it != 0L } ?: ride.createdAt),
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                AddressLine(
                    icon = Icons.Default.LocationOn,
                    text = ride.pickupAddress.ifBlank { "Điểm đón chưa cập nhật" }
                )
                Spacer(modifier = Modifier.height(6.dp))
                AddressLine(
                    icon = Icons.Default.Route,
                    text = ride.dropoffAddress.ifBlank { "Điểm trả chưa cập nhật" }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = statusInfo.background
                ) {
                    Text(
                        text = statusInfo.label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = statusInfo.color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressLine(
    icon: ImageVector,
    text: String
) {
    val colors = MaterialTheme.colorScheme

    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = text,
            color = colors.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 17.sp
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
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBike,
                contentDescription = null,
                tint = colors.onPrimaryContainer,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(title, color = colors.onBackground, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(message, color = colors.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
private fun RideDetailDialog(
    ride: Ride,
    onDismiss: () -> Unit,
    onRateRide: () -> Unit = {}
) {
    val canRateRide = ride.status == "completed" && ride.driverId.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Chi tiết cuốc xe")
        },
        text = {
            Column {
                DetailRow("Trạng thái", statusInfo(ride.status).label)
                DetailRow("Dịch vụ", serviceName(ride.serviceType))
                DetailRow("Tài xế", ride.driverName.ifBlank { "Chưa cập nhật" })
                DetailRow("Biển số", ride.driverPlate.ifBlank { "Chưa cập nhật" })
                DetailRow("SĐT tài xế", ride.driverPhone.ifBlank { "Chưa cập nhật" })
                DetailRow("Giá", formatCurrency(ride.price))
                DetailRow("Thanh toán", "${ride.paymentMethod.ifBlank { "cash" }} - ${ride.paymentStatus.ifBlank { "pending" }}")
                DetailRow("Thời gian", formatDate(ride.completedAt ?: ride.updatedAt.takeIf { it != 0L } ?: ride.createdAt))
                if (ride.userRating > 0) {
                    DetailRow("Đánh giá", "${ride.userRating}/5")
                    if (ride.userReview.isNotBlank()) {
                        DetailRow("Nhận xét", ride.userReview)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                DetailRow("Điểm đón", ride.pickupAddress.ifBlank { "Chưa cập nhật" })
                DetailRow("Điểm trả", ride.dropoffAddress.ifBlank { "Chưa cập nhật" })
            }
        },
        dismissButton = {
            if (canRateRide) {
                TextButton(onClick = onRateRide) {
                    Text(if (ride.userRating > 0) "Sửa đánh giá" else "Đánh giá tài xế")
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

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, color = colors.onSurfaceVariant, fontSize = 12.sp)
        Text(value, color = colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

private data class RideStatusInfo(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val background: Color
)

@Composable
private fun statusInfo(status: String): RideStatusInfo {
    val colors = MaterialTheme.colorScheme
    return when (status) {
        "completed" -> RideStatusInfo("Đã hoàn thành", Icons.Default.CheckCircle, colors.primary, colors.primaryContainer)
        "cancelled" -> RideStatusInfo("Đã hủy", Icons.Default.Close, colors.error, colors.errorContainer)
        "accepted" -> RideStatusInfo("Tài xế đã nhận", Icons.Default.HourglassTop, colors.tertiary, colors.tertiaryContainer)
        "arrived" -> RideStatusInfo("Tài xế đã đến", Icons.Default.Person, colors.secondary, colors.secondaryContainer)
        "in_progress" -> RideStatusInfo("Đang di chuyển", Icons.Default.DirectionsBike, colors.primary, colors.primaryContainer)
        else -> RideStatusInfo("Đang tìm tài xế", Icons.Default.HourglassTop, colors.tertiary, colors.tertiaryContainer)
    }
}

private fun serviceName(serviceType: String): String {
    return when (serviceType.lowercase()) {
        "bike" -> "Bike"
        "car" -> "Car"
        "express" -> "Express"
        else -> serviceType.ifBlank { "Cuốc xe" }
    }
}

private fun formatDate(value: Long): String {
    if (value <= 0L) return "Chưa cập nhật"
    return SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault()).format(Date(value))
}

private fun formatCurrency(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(value)
}
