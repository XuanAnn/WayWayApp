package com.example.waywayapp.ui.user.booking.bike.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.waywayapp.R
import com.example.waywayapp.BuildConfig
import com.example.waywayapp.ui.user.booking.bike.BikeViewModel
import java.text.DecimalFormat

@Composable
fun BookingConfirmCard(
    viewModel: BikeViewModel,
    onConfirmBooking: (String) -> Unit,
    onSelectPromo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val formatter = remember { DecimalFormat("#,###") }
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf("Tien mat") }
    var showMomoDialog by remember { mutableStateOf(false) }

    data class PaymentMethod(val name: String, val icon: Int)

    val payments = buildList {
        add(PaymentMethod("Tien mat", R.drawable.dollar))
        add(PaymentMethod("MoMo UAT", R.drawable.momo_icon))
        if (BuildConfig.DEBUG) {
            add(PaymentMethod("MoMo Test", R.drawable.momo_icon))
        }
    }

    val selectedIcon = payments.find { it.name == selectedPayment }?.icon ?: R.drawable.dollar
    val amount = uiState.finalPrice.takeIf { it > 0.0 } ?: uiState.price

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 26.dp, bottomEnd = 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        text = "${uiState.distance} - ${uiState.duration}",
                        fontSize = 12.sp,
                        color = Color(0xFF8B918A)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${formatter.format(amount.toInt())}d",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF20242A)
                    )

                    if (uiState.discount > 0.0) {
                        Text(
                            text = "${formatter.format(uiState.price.toInt())}d",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Diem den",
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

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Thanh toan", fontSize = 12.sp, color = Color(0xFF8B918A))

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
                    Text("Uu dai", fontSize = 12.sp, color = Color(0xFF8B918A))

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
                onClick = {
                    when (selectedPayment) {
                        "MoMo UAT" -> {
                            viewModel.beginMomoGatewayPayment()
                            showMomoDialog = true
                        }
                        "MoMo Test" -> viewModel.payWithMomoTestGateway()
                        else -> onConfirmBooking("cash")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF20242A),
                    disabledContainerColor = Color(0xFFB6BBB3)
                ),
                shape = RoundedCornerShape(20.dp),
                enabled = uiState.dropoffAddress.isNotBlank()
            ) {
                Text(
                    text = "Xac nhan dat xe",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }

    if (showMomoDialog) {
        MomoGatewayVerifyDialog(
            amount = amount,
            orderTitle = "WayWay Bike",
            pickupAddress = uiState.pickupAddress,
            dropoffAddress = uiState.dropoffAddress,
            status = uiState.momoStatus,
            message = uiState.momoMessage,
            payUrl = uiState.momoPayUrl,
            onDismiss = { showMomoDialog = false },
            onOpenMomo = {
                val url = uiState.momoPayUrl ?: return@MomoGatewayVerifyDialog
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            },
            onCheckStatus = { viewModel.pollMomoGatewayStatusOnce() },
            onDevConfirm = { viewModel.devConfirmMomoGatewayPayment() },
            onOpenAndDevConfirm = {
                val url = uiState.momoPayUrl
                if (!url.isNullOrBlank()) {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }
                viewModel.devConfirmMomoGatewayPayment()
            }
        )
    }
}

@Composable
private fun MomoGatewayVerifyDialog(
    amount: Double,
    orderTitle: String,
    pickupAddress: String,
    dropoffAddress: String,
    status: String,
    message: String?,
    payUrl: String?,
    onDismiss: () -> Unit,
    onOpenMomo: () -> Unit,
    onCheckStatus: () -> Unit,
    onDevConfirm: () -> Unit,
    onOpenAndDevConfirm: () -> Unit
) {
    val formatter = remember { DecimalFormat("#,###") }
    val isCreating = status == "CREATING"
    val isWaiting = status == "WAITING"
    val isPaid = status == "PAID"
    val isFailed = status == "FAILED"

    Dialog(
        onDismissRequest = {
            if (!isCreating) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isCreating
                ) {
                    Text("Dong", color = Color(0xFFD82D8B), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painter = painterResource(R.drawable.momo_icon),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Xac nhan thanh toan",
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF241B2F)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "MoMo UAT: mo MoMo de thanh toan, quay lai va bam kiem tra trang thai.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color(0xFF837889)
            )

            Spacer(modifier = Modifier.height(26.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEFF8)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = orderTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A324C)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${formatter.format(amount.toInt())}d",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD82D8B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    VerifyAddressLine("Diem don", pickupAddress)
                    Spacer(modifier = Modifier.height(10.dp))
                    VerifyAddressLine("Diem den", dropoffAddress)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onOpenMomo,
                enabled = !isCreating && !payUrl.isNullOrBlank() && !isPaid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD82D8B),
                    disabledContainerColor = Color(0xFFE7B7D2)
                )
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Dang tao thanh toan...")
                } else {
                    Text(
                        text = if (isPaid) "Da thanh toan" else "Mo MoMo UAT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onCheckStatus,
                enabled = !isCreating && (isWaiting || isFailed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = if (isFailed) "Thu kiem tra lai" else "Toi da thanh toan, kiem tra ngay",
                    color = Color(0xFF4A324C)
                )
            }

            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onOpenAndDevConfirm,
                    enabled = !isCreating && !payUrl.isNullOrBlank() && !isPaid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF241B2F),
                        disabledContainerColor = Color(0xFFB6BBB3)
                    )
                ) {
                    Text("Mo UAT + confirm test", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDevConfirm,
                    enabled = !isCreating && !isPaid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Dev confirm PAID", color = Color(0xFFD82D8B), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message ?: when {
                    isPaid -> "Thanh toan thanh cong."
                    isFailed -> "Thanh toan that bai."
                    isWaiting -> "Dang cho thanh toan..."
                    else -> ""
                },
                fontSize = 12.sp,
                color = if (isFailed) Color(0xFFB00020) else Color(0xFF837889),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun VerifyAddressLine(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF837889),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value.ifBlank { "Chua co dia chi" },
            fontSize = 14.sp,
            color = Color(0xFF241B2F),
            fontWeight = FontWeight.SemiBold,
            lineHeight = 18.sp,
            maxLines = 2
        )
    }
}
