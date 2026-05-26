package com.example.waywayapp.ui.user.booking.food.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.core.di.AppContainer

@Composable
fun FoodOrderTrackingScreen(
    onBackHomeClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: FoodOrderViewModel = viewModel(
        factory = FoodOrderViewModelFactory(
            repository = AppContainer.provideFoodRepository(
                context.applicationContext
            )
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.placeOrder()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7F2)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val icon = when (uiState.status) {
                    FoodOrderStatus.FINDING_DRIVER -> Icons.Default.HourglassTop
                    FoodOrderStatus.DRIVER_ACCEPTED -> Icons.Default.DeliveryDining
                    FoodOrderStatus.PICKING_ORDER -> Icons.Default.Restaurant
                    FoodOrderStatus.DELIVERING -> Icons.Default.DeliveryDining
                    FoodOrderStatus.COMPLETED -> Icons.Default.Check
                    else -> Icons.Default.HourglassTop
                }

                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .background(Color(0xFFD8FF4F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF20242A),
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = uiState.message,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF20242A),
                    textAlign = TextAlign.Center
                )

                if (uiState.driverName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tài xế: ${uiState.driverName}",
                        color = Color.DarkGray
                    )

                    Text(
                        text = "Biển số: ${uiState.vehiclePlate}",
                        color = Color.DarkGray
                    )

                    Text(
                        text = "Dự kiến: ${uiState.estimatedTime}",
                        color = Color.DarkGray
                    )
                }

                if (
                    uiState.status == FoodOrderStatus.COMPLETED ||
                    uiState.message == "Giỏ hàng đang trống"
                ) {
                    Spacer(modifier = Modifier.height(26.dp))

                    Button(
                        onClick = {
                            viewModel.resetOrder()
                            onBackHomeClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF20242A),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Về trang chủ")
                    }
                }
            }
        }
    }
}
