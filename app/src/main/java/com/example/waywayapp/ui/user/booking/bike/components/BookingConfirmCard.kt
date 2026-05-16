package com.example.waywayapp.ui.user.booking.bike.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.R
import com.example.waywayapp.ui.user.booking.bike.BikeViewModel
import java.text.DecimalFormat


@Composable
fun BookingConfirmCard(
    viewModel: BikeViewModel,
    onConfirmBooking: () -> Unit,
    onSelectPromo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val formatter = DecimalFormat("#,###")

    var expanded by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf("Tiền mặt") }

    data class PaymentMethod(val name: String, val icon: Int)

    val payments = listOf(
        PaymentMethod("Tiền mặt", R.drawable.dollar),
        PaymentMethod("Momo", R.drawable.momo_icon),
        PaymentMethod("Thẻ ngân hàng", R.drawable.ic_credit_card)
    )

    val selectedIcon =
        payments.find { it.name == selectedPayment }?.icon ?: R.drawable.dollar

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 26.dp, bottomEnd = 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF5F7F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.bike_icon),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Bike",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF20242A)
                    )

                    Text(
                        text = "${uiState.distance} • ${uiState.duration}",
                        fontSize = 12.sp,
                        color = Color(0xFF8B918A)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${formatter.format(uiState.finalPrice.toInt())}đ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF20242A)
                    )

                    if (uiState.promoCode != "Áp mã") {
                        Text(
                            text = "${formatter.format(uiState.price.toInt())}đ",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Điểm đến",
                fontSize = 12.sp,
                color = Color(0xFF8B918A),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = uiState.dropoffAddress,
                fontSize = 14.sp,
                color = Color(0xFF20242A),
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Thanh toán", fontSize = 12.sp, color = Color(0xFF8B918A))

                    Spacer(modifier = Modifier.height(6.dp))

                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFF5F7F2))
                                .clickable { expanded = true }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(selectedIcon),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = selectedPayment,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF20242A),
                                maxLines = 1
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            payments.forEach { payment ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Image(
                                                painter = painterResource(payment.icon),
                                                contentDescription = null,
                                                modifier = Modifier.size(26.dp)
                                            )

                                            Spacer(Modifier.width(8.dp))

                                            Text(payment.name)
                                        }
                                    },
                                    onClick = {
                                        selectedPayment = payment.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Ưu đãi", fontSize = 12.sp, color = Color(0xFF8B918A))

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFFFF4D8))
                            .clickable {
                                viewModel.loadPromos()
                                onSelectPromo()
                            }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.promo),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = uiState.promoCode,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF7A00),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onConfirmBooking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF20242A),
                    disabledContainerColor = Color(0xFFB6BBB3)
                ),
                shape = RoundedCornerShape(20.dp),
                enabled = uiState.dropoffAddress != "Đang xác định vị trí..." &&
                        uiState.dropoffAddress.isNotBlank()
            ) {
                Text(
                    text = "Xác nhận đặt xe",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}