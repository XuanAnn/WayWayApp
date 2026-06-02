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
import com.example.waywayapp.ui.theme.AppBg
import com.example.waywayapp.ui.theme.CardWhite
import com.example.waywayapp.ui.theme.TextDark
import com.example.waywayapp.ui.theme.TextGray
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

    Scaffold(
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lich su cuoc",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = TextDark
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBg)
            )
        },
        bottomBar = {
            WayWayBottomBar(
                currentRoute = currentRoute,
                onItemClick = onBottomNavClick
            )
        }
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
                        color = Color(0xFF00B1A7)
                    )
                }

                uiState.error != null -> {
                    EmptyHistory(
                        title = "Khong tai duoc lich su",
                        message = uiState.error.orEmpty(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.rides.isEmpty() -> {
                    EmptyHistory(
                        title = "Chua co cuoc xe",
                        message = "Cac chuyen da dat se xuat hien tai day.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
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
                        color = TextDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatCurrency(ride.price),
                        color = TextDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatDate(ride.completedAt ?: ride.updatedAt.takeIf { it != 0L } ?: ride.createdAt),
                    color = TextGray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                AddressLine(
                    icon = Icons.Default.LocationOn,
                    text = ride.pickupAddress.ifBlank { "Diem don chua cap nhat" }
                )
                Spacer(modifier = Modifier.height(6.dp))
                AddressLine(
                    icon = Icons.Default.Route,
                    text = ride.dropoffAddress.ifBlank { "Diem tra chua cap nhat" }
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
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF00B1A7),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = text,
            color = TextGray,
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
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0F2F1)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBike,
                contentDescription = null,
                tint = Color(0xFF00B1A7),
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(title, color = TextDark, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(message, color = TextGray, fontSize = 13.sp)
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
            Text("Chi tiet cuoc xe")
        },
        text = {
            Column {
                DetailRow("Trang thai", statusInfo(ride.status).label)
                DetailRow("Dich vu", serviceName(ride.serviceType))
                DetailRow("Tai xe", ride.driverName.ifBlank { "Chua cap nhat" })
                DetailRow("Bien so", ride.driverPlate.ifBlank { "Chua cap nhat" })
                DetailRow("SDT tai xe", ride.driverPhone.ifBlank { "Chua cap nhat" })
                DetailRow("Gia", formatCurrency(ride.price))
                DetailRow("Thanh toan", "${ride.paymentMethod.ifBlank { "cash" }} - ${ride.paymentStatus.ifBlank { "pending" }}")
                DetailRow("Thoi gian", formatDate(ride.completedAt ?: ride.updatedAt.takeIf { it != 0L } ?: ride.createdAt))
                if (ride.userRating > 0) {
                    DetailRow("Danh gia", "${ride.userRating}/5")
                    if (ride.userReview.isNotBlank()) {
                        DetailRow("Nhan xet", ride.userReview)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                DetailRow("Diem don", ride.pickupAddress.ifBlank { "Chua cap nhat" })
                DetailRow("Diem tra", ride.dropoffAddress.ifBlank { "Chua cap nhat" })
            }
        },
        dismissButton = {
            if (canRateRide) {
                TextButton(onClick = onRateRide) {
                    Text(if (ride.userRating > 0) "Sua danh gia" else "Danh gia tai xe")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dong")
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, color = TextGray, fontSize = 12.sp)
        Text(value, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

private data class RideStatusInfo(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val background: Color
)

private fun statusInfo(status: String): RideStatusInfo {
    return when (status) {
        "completed" -> RideStatusInfo("Da hoan thanh", Icons.Default.CheckCircle, Color(0xFF008A4B), Color(0xFFE0F7EA))
        "cancelled" -> RideStatusInfo("Da huy", Icons.Default.Close, Color(0xFFD93025), Color(0xFFFFE6E2))
        "accepted" -> RideStatusInfo("Tai xe da nhan", Icons.Default.HourglassTop, Color(0xFF0B65C2), Color(0xFFEAF4FF))
        "arrived" -> RideStatusInfo("Tai xe da den", Icons.Default.Person, Color(0xFF7C3AED), Color(0xFFF1E8FF))
        "in_progress" -> RideStatusInfo("Dang di chuyen", Icons.Default.DirectionsBike, Color(0xFF00B1A7), Color(0xFFE0F2F1))
        else -> RideStatusInfo("Dang tim tai xe", Icons.Default.HourglassTop, Color(0xFFB7791F), Color(0xFFFFF4D6))
    }
}

private fun serviceName(serviceType: String): String {
    return when (serviceType.lowercase()) {
        "bike" -> "Bike"
        "car" -> "Car"
        "express" -> "Express"
        else -> serviceType.ifBlank { "Cuoc xe" }
    }
}

private fun formatDate(value: Long): String {
    if (value <= 0L) return "Chua cap nhat"
    return SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault()).format(Date(value))
}

private fun formatCurrency(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(value)
}
