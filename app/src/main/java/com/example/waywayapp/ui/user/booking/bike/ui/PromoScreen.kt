package com.example.waywayapp.ui.user.booking.bike.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Cần thiết để sử dụng items trong LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber // Icon phù hợp hơn cho mã giảm giá
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.ui.user.booking.bike.viewmodel.BikeViewModel

@Composable
fun PromoBottomSheet(
    viewModel: BikeViewModel,
    onPromoSelected: () -> Unit
) {
    // Sử dụng "by" để code gọn hơn
    val promos by viewModel.availablePromos.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp, top = 8.dp)
    ) {
        // Thanh gạch nhỏ phía trên ModalBottomSheet (Handle)
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color.LightGray, shape = MaterialTheme.shapes.extraLarge)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ưu đãi hiện có",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2) // Màu xanh đồng bộ với app
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (promos.isEmpty()) {
            Text(
                text = "Hiện chưa có mã giảm giá nào dành cho bạn.",
                modifier = Modifier.padding(vertical = 32.dp).align(Alignment.CenterHorizontally),
                color = Color.Gray
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                val sortedPromos = promos.sortedByDescending { promo ->
                    promo.minPrice == null || uiState.price >= promo.minPrice
                }
                items(sortedPromos) { promo ->

                    val isValid = promo.minPrice == null || uiState.price >= promo.minPrice
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.applyPromo(promo)
                                onPromoSelected()
                            }.alpha(if (isValid) 1f else 0.5f),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF1F8E9) // Màu xanh lá nhạt cho cảm giác tiết kiệm
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = "Promo Icon",
                                tint = Color(0xFF4CAF50)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = promo.code,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )

                                val desc = when {
                                    promo.percent != null -> "Giảm ngay ${promo.percent}%"
                                    promo.amount != null -> "Giảm trực tiếp ${promo.amount}đ"
                                    else -> "Ưu đãi đặc biệt"
                                }

                                Text(
                                    text = desc,
                                    fontSize = 13.sp,
                                    color = Color.DarkGray
                                )

                                if (promo.minPrice != null) {
                                    Text(
                                        text = "Áp dụng cho đơn từ ${promo.minPrice}đ",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // Nút giả để kích thích hành động nhấn
                                if (uiState.error != null){
                                    Text(
                                    text = if (isValid) "Áp dụng" else "Không đủ điều kiện",
                                    color = if (isValid) Color(0xFF00B1A7) else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                            }
                        }
                    }
                }
            }
        }
    }
}