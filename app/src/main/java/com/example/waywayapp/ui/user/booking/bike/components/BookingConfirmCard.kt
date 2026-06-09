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
import androidx.compose.material3.MaterialTheme
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
    val colors = MaterialTheme.colorScheme

    var expanded by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf("Tiền mặt") }
    var showMomoDialog by remember { mutableStateOf(false) }

    data class PaymentMethod(val name: String, val icon: Int)
    data class RideOption(
        val type: String,
        val title: String,
        val icon: Int,
        val price: Double
    )

    val payments = buildList {
        add(PaymentMethod("Tiền mặt", R.drawable.dollar))
        add(PaymentMethod("MoMo", R.drawable.momo_icon))
        if (BuildConfig.DEBUG) {
            add(PaymentMethod("MoMo Test", R.drawable.momo_icon))
        }
    }

    val selectedIcon = payments.find { it.name == selectedPayment }?.icon ?: R.drawable.dollar
    val amount = uiState.finalPrice.takeIf { it > 0.0 } ?: uiState.price
    val rideOptions = listOf(
        RideOption("bike", "Bike", R.drawable.bike_icon, uiState.bikePrice.takeIf { it > 0.0 } ?: uiState.price),
        RideOption("car", "Car", R.drawable.car_icon, uiState.carPrice.takeIf { it > 0.0 } ?: uiState.price)
    ).sortedBy { option ->
        if (option.type == uiState.selectedServiceType) 0 else 1
    }
    val selectedRide = rideOptions.firstOrNull { it.type == uiState.selectedServiceType } ?: rideOptions.first()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 26.dp, bottomEnd = 26.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {





                Column(horizontalAlignment = Alignment.End) {

                    Row() {
                        Text(
                            text = "Bạn đang chọn dịch vụ: ${selectedRide.title}",
                            fontSize = 16.sp,
                            color = colors.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    if (uiState.discount > 0.0) {
                        Text(
                            text = "${formatter.format(uiState.price.toInt())}đ",
                            fontSize = 13.sp,
                            color = colors.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rideOptions.forEach { option ->
                    val selected = option.type == uiState.selectedServiceType
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (selected) colors.primaryContainer else colors.surfaceVariant.copy(alpha = 0.55f))
                            .clickable { viewModel.selectServiceType(option.type) }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(option.icon),
                            contentDescription = option.title,
                            modifier = Modifier.size(38.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column() {
                        Text(
                            text = option.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.onSurface
                        )

                            Text(
                                text = "${uiState.distance} - ${uiState.duration}",
                                fontSize = 12.sp,
                                color = colors.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "${formatter.format(option.price.toInt())}đ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (selected) colors.primary else colors.onSurface
                        )

                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Điểm đến",
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = uiState.dropoffAddress,
                fontSize = 14.sp,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Thanh toán", fontSize = 12.sp, color = colors.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(6.dp))

                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(colors.surfaceVariant.copy(alpha = 0.55f))
                                .clickable { expanded = true }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(selectedIcon),
                                contentDescription = selectedPayment,
                                modifier = Modifier.size(28.dp)
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = selectedPayment,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurface,
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
                                                contentDescription = payment.name,
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
                    Text("Ưu đãi", fontSize = 12.sp, color = colors.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.secondaryContainer)
                            .clickable {
                                viewModel.loadPromos()
                                onSelectPromo()
                            }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.promo),
                            contentDescription = "Ưu đãi",
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = uiState.promoCode,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSecondaryContainer,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    when (selectedPayment) {
                        "MoMo" -> {
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
                    containerColor = colors.primary,
                    disabledContainerColor = colors.surfaceVariant
                ),
                shape = RoundedCornerShape(20.dp),
                enabled = uiState.dropoffAddress.isNotBlank()
            ) {
                Text(
                    text = "Xác nhận đặt xe",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.onPrimary
                )
            }
        }
    }

    if (showMomoDialog) {
        MomoGatewayVerifyDialog(
            amount = amount,
            orderTitle = "WayWay ${selectedRide.title}",
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
    val colors = MaterialTheme.colorScheme
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
                .background(colors.background)
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
                    Text("Đóng", color = colors.primary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painter = painterResource(R.drawable.momo_icon),
                    contentDescription = "MoMo",
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Xác nhận thanh toán",
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "MoMo UAT: mở MoMo để thanh toán, quay lại và bấm kiểm tra trạng thái.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(26.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.primaryContainer.copy(alpha = 0.55f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = orderTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${formatter.format(amount.toInt())}đ",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    VerifyAddressLine("Điểm đón", pickupAddress)
                    Spacer(modifier = Modifier.height(10.dp))
                    VerifyAddressLine("Điểm đến", dropoffAddress)
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
                    containerColor = colors.primary,
                    disabledContainerColor = colors.surfaceVariant
                )
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Đang tạo thanh toán...")
                } else {
                    Text(
                        text = if (isPaid) "Đã thanh toán" else "Mở MoMo UAT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onPrimary
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
                    text = if (isFailed) "Thử kiểm tra lại" else "Tôi đã thanh toán, kiểm tra ngay",
                    color = colors.primary
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
                        containerColor = colors.inverseSurface,
                        disabledContainerColor = colors.surfaceVariant
                    )
                ) {
                    Text("Mở UAT + confirm test", color = colors.inverseOnSurface, fontWeight = FontWeight.Bold)
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
                    Text("Dev confirm PAID", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message ?: when {
                    isPaid -> "Thanh toán thành công."
                    isFailed -> "Thanh toán thất bại."
                    isWaiting -> "Đang chờ thanh toán..."
                    else -> ""
                },
                fontSize = 12.sp,
                color = if (isFailed) colors.error else colors.onSurfaceVariant,
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
    val colors = MaterialTheme.colorScheme

    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = colors.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value.ifBlank { "Chưa có địa chỉ" },
            fontSize = 14.sp,
            color = colors.onSurface,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 18.sp,
            maxLines = 2
        )
    }
}
