package com.example.waywayapp.ui.driver.income

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.data.model.Ride
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DriverIncomeScreen(
    viewModel: DriverIncomeViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val grabGreen = Color(0xFF00B14F)

    Scaffold(
        bottomBar = {
            DriverIncomeBottomNavigation(
                grabGreen = grabGreen,
                onHomeClick = onHomeClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color(0xFFF6F7F4)
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = grabGreen)
                }
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    IncomeStatusCard(
                        hasIncome = uiState.completedTrips > 0,
                        totalIncome = uiState.totalIncome
                    )
                }

                item {
                    IncomeSummaryCard(
                        title = "Cuốc xe đã hoàn tất",
                        value = "${uiState.completedTrips} cuốc xe",
                        iconTint = grabGreen
                    )
                }

                item {
                    IncomeSummaryCard(
                        title = "Tổng thu nhập",
                        value = formatCurrency(uiState.totalIncome),
                        iconTint = Color(0xFF4E7FFF)
                    )
                }

                uiState.error?.let { error ->
                    item {
                        Text(
                            text = error,
                            color = Color(0xFFD93025),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                if (uiState.rides.isNotEmpty()) {
                    item {
                        Text(
                            text = "Lịch sử thu nhập",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFF1F2328),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }

                items(uiState.rides, key = { it.id }) { ride ->
                    CompletedRideItem(ride = ride)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun IncomeTopBar(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
        }
        Text(
            text = "Thu nhập",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = Color(0xFF1F2328)
        )
        IconButton(onClick = onRefreshClick) {
            Icon(Icons.Default.Refresh, contentDescription = "Làm mới")
        }
    }
}

@Composable
private fun IncomeStatusCard(
    hasIncome: Boolean,
    totalIncome: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .background(Color(0xFFFFF2C7), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = Color(0xFF00B14F),
                    modifier = Modifier.size(42.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasIncome) "Đã có thu nhập" else "Chưa có thu nhập",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F2328)
                )
                Text(
                    text = if (hasIncome) {
                        "Bạn đã nhận ${formatCurrency(totalIncome)} từ các cuốc hoàn tất."
                    } else {
                        "Bắt đầu nhận cuốc xe để có thêm thu nhập."
                    },
                    color = Color(0xFF5F6368),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun IncomeSummaryCard(
    title: String,
    value: String,
    iconTint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = iconTint
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(title, color = Color(0xFF1F2328), fontWeight = FontWeight.SemiBold)
                Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun CompletedRideItem(
    ride: Ride
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFE8F5EE), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00B14F))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = ride.dropoffAddress.ifBlank { "Cuốc xe đã hoàn tất" },
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatDate(ride.completedAt ?: ride.updatedAt),
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp
                )
            }
            Text(
                text = formatCurrency(ride.price),
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF00B14F)
            )
        }
    }
}

@Composable
private fun DriverIncomeBottomNavigation(
    grabGreen: Color,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            label = { Text("Trang chủ", fontSize = 10.sp) },
            selected = false,
            onClick = onHomeClick
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text("Thu nhập", fontSize = 10.sp) },
            selected = true,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = grabGreen,
                selectedTextColor = grabGreen,
                indicatorColor = Color(0xFFE4F6EC)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
            label = { Text("Hồ sơ", fontSize = 10.sp) },
            selected = false,
            onClick = onProfileClick
        )
    }
}

private fun formatCurrency(
    amount: Double
): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}

private fun formatDate(
    timestamp: Long
): String {
    if (timestamp <= 0L) return "Chưa có thời gian"
    return SimpleDateFormat("HH:mm dd/MM/yyyy", Locale("vi", "VN")).format(Date(timestamp))
}
