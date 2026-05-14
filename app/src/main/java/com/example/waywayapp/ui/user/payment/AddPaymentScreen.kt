package com.example.waywayapp.ui.user.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.R
import com.example.waywayapp.ui.theme.AppBg

private val PaymentBg = Color(0xFFF3F8FF)
private val PaymentTextDark = Color(0xFF10243D)
private val PaymentTextGray = Color(0xFF7A8BA0)

@Composable
fun AddPaymentScreen(
    onBackClick: () -> Unit = {},
    onMomoClick: () -> Unit = {},
    onBankClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = AppBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppBg)
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = PaymentTextDark
                    )
                }

                Text(
                    text = "Thêm thanh toán",
                    color = PaymentTextDark,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Chọn phương thức liên kết",
                color = PaymentTextGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            PaymentOptionCard(
                title = "Liên kết ví MoMo",
                subtitle = "Thanh toán nhanh bằng ví MoMo",
                iconType = "momo",
                onClick = onMomoClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            PaymentOptionCard(
                title = "Liên kết tài khoản ngân hàng",
                subtitle = "ATM, Visa, Mastercard",
                iconType = "bank",
                onClick = onBankClick
            )
        }
    }
}

@Composable
private fun PaymentOptionCard(
    title: String,
    subtitle: String,
    iconType: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (iconType == "momo") {
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFD82D8B),
                                    Color(0xFFFF8AC8)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF75D6FF),
                                    Color(0xFF5B9BFF)
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (iconType == "momo") {
                    Image(
                        painter = painterResource(id = R.drawable.momo_icon),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_credit_card),
                        contentDescription = null,
                        Modifier.size(50.dp)
                        )


                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = PaymentTextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    color = PaymentTextGray,
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF58A8E8)
            )
        }
    }
}