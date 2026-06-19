package com.example.waywayapp.presentation.driver.income

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.domain.model.Ride
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- BẢNG MÀU WAYWAY ---
private val WayWayDarkGreen = Color(0xFF006C4C)
private val WayWayLightGreen = Color(0xFF1DB954)
private val WayWayTeal = Color(0xFF138A72)
private val AppBgColor = Color(0xFFF5F7FA)
private val CardBgColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1D1B20)
private val TextSecondary = Color(0xFF757575)

@Composable
fun DriverIncomeScreen(
    viewModel: DriverIncomeViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            DriverIncomeBottomNavigation(
                onHomeClick = onHomeClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = AppBgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            IncomeTopBar(
                onBackClick = onBackClick,
                onRefreshClick = viewModel::refresh
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = WayWayDarkGreen)
                }
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Thẻ Tổng Thu Nhập Đẹp
                item {
                    IncomeStatusCard(
                        hasIncome = uiState.completedTrips > 0,
                        totalIncome = uiState.totalIncome
                    )
                }

                // Nhóm Thông Kê
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IncomeSummaryCard(
                            title = "Cuốc hoàn tất",
                            value = "${uiState.completedTrips}",
                            icon = Icons.Default.CheckCircle,
                            iconTint = WayWayLightGreen,
                            modifier = Modifier.weight(1f)
                        )

                        IncomeSummaryCard(
                            title = "Tổng thu nhập",
                            value = formatCurrencyCompact(uiState.totalIncome),
                            icon = Icons.Default.TrendingUp,
                            iconTint = WayWayTeal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                uiState.error?.let { error ->
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                            Text(
                                text = error,
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                if (uiState.rides.isNotEmpty()) {
                    item {
                        Text(
                            text = "Lịch sử thu nhập",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                }

                items(uiState.rides, key = { it.id }) { ride ->
                    CompletedRideItem(ride = ride)
                }
            }
        }
    }
}

@Composable
private fun IncomeTopBar(onBackClick: () -> Unit, onRefreshClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Quay lại", tint = TextPrimary) }
        Text("Thu nhập", modifier = Modifier.weight(1f), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = TextPrimary)
        IconButton(onClick = onRefreshClick) { Icon(Icons.Default.Refresh, "Làm mới", tint = WayWayDarkGreen) }
    }
}

@Composable
private fun IncomeStatusCard(hasIncome: Boolean, totalIncome: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WayWayDarkGreen),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Wallet, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Số dư khả dụng", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text(
                        text = if (hasIncome) formatCurrency(totalIncome) else "0 đ",
                        color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Text(
                    text = if (hasIncome) "Thu nhập hôm nay tuyệt vời! Tiếp tục phát huy nhé." else "Bắt đầu nhận cuốc xe để có thêm thu nhập.",
                    color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun IncomeSummaryCard(
    title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun CompletedRideItem(ride: Ride) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(WayWayLightGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = WayWayDarkGreen, modifier = Modifier.size(24.dp))
            }
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 14.dp)
            ) {
                Text(
                    text = ride.dropoffAddress.ifBlank { "Cuốc xe đã hoàn tất" },
                    fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatDate(ride.completedAt ?: ride.updatedAt),
                    color = TextSecondary, fontSize = 12.sp
                )
            }
            Text(
                text = "+${formatCurrency(ride.price)}",
                fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = WayWayDarkGreen
            )
        }
    }
}

@Composable
private fun DriverIncomeBottomNavigation(
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = CardBgColor,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Trang chủ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            selected = false,
            onClick = onHomeClick,
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary)
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.BarChart, null) },
            label = { Text("Thu nhập", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            selected = true,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WayWayDarkGreen,
                selectedTextColor = WayWayDarkGreen,
                indicatorColor = WayWayLightGreen.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Hồ sơ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            selected = false,
            onClick = onProfileClick,
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary)
        )
    }
}

private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(amount)
}

private fun formatCurrencyCompact(amount: Double): String {
    return if (amount >= 1_000_000) {
        String.format("%.1f Tr", amount / 1_000_000).replace(".0 Tr", " Tr")
    } else if (amount >= 1_000) {
        String.format("%.1f K", amount / 1_000).replace(".0 K", " K")
    } else {
        "${amount.toInt()} đ"
    }
}

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) return "Chưa có thời gian"
    return SimpleDateFormat("HH:mm · dd/MM/yyyy", Locale("vi", "VN")).format(Date(timestamp))
}