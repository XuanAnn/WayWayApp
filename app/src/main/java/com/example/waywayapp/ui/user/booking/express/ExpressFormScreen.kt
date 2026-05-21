package com.example.waywayapp.ui.user.booking.express

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat

@Composable
fun ExpressFormScreen(
    onBackClick: () -> Unit = {},
    onPickupClick: () -> Unit = {},
    onDropoffClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    viewModel: ExpressViewModel = ExpressSharedViewModel.viewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val formatter = DecimalFormat("#,###")

    Scaffold(
        containerColor = Color(0xFFF4F7F2),
        bottomBar = {
            ExpressBottomBar(
                totalPrice = uiState.totalPrice,
                enabled = uiState.canCheckOrder,
                onClick = {
                    if (viewModel.validateBeforeConfirm()) {
                        onConfirmClick()
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD8F8DC))
                    .padding(20.dp)
            ) {
                Column {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Cần ship hàng gấp?",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF143F36)
                    )

                    Text(
                        text = "Đặt WayWay Express ngay!",
                        color = Color(0xFF143F36)
                    )

                    Spacer(modifier = Modifier.height(44.dp))
                }
            }

            AddressInputCard(
                pickupAddress = uiState.pickupAddress,
                dropoffAddress = uiState.dropoffAddress,
                onPickupClick = onPickupClick,
                onDropoffClick = onDropoffClick
            )

            SectionTitle("Chi tiết đơn hàng")

            ExpressOptionRow(
                iconColor = Color(0xFF2196F3),
                title = "Lấy hàng trong vòng 15 phút",
                subtitle = "Siêu tốc",
                price = null,
                onClick = {}
            )

            ExpressOptionRow(
                iconColor = Color(0xFF4CAF50),
                title = "Xe máy",
                subtitle = "Đề xuất dựa trên chi tiết món hàng",
                price = null,
                onClick = {}
            )

            OutlinedTextField(
                value = uiState.packageDetail,
                onValueChange = viewModel::onPackageDetailChange,
                label = { Text("Thêm chi tiết món hàng *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(18.dp),
                singleLine = false,
                minLines = 2
            )

            SectionTitle("Áp dụng ưu đãi")

            ExpressOptionRow(
                iconColor = Color(0xFFFF9800),
                title = "Áp dụng ưu đãi để được giảm giá",
                subtitle = "",
                price = null,
                onClick = {}
            )

            SectionTitle("Dịch vụ thêm")

            ExtraServiceRow(
                checked = uiState.codEnabled,
                title = "Thu tiền hộ (COD)",
                subtitle = "",
                price = "5.000đ",
                onClick = viewModel::toggleCod
            )

            ExtraServiceRow(
                checked = uiState.handDelivery,
                title = "Giao tận tay",
                subtitle = "Cần liên hệ tòa nhà hỗ trợ",
                price = "10.000đ",
                onClick = viewModel::toggleHandDelivery
            )

            ExtraServiceRow(
                checked = uiState.bigPackage,
                title = "Giao hàng cỡ lớn",
                subtitle = "Lên đến 50kg, 60×70×60cm",
                price = "15.000đ",
                onClick = viewModel::toggleBigPackage
            )

            uiState.errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
private fun AddressInputCard(
    pickupAddress: String,
    dropoffAddress: String,
    onPickupClick: () -> Unit,
    onDropoffClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-36).dp)
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            AddressRow(
                color = Color(0xFF1E88E5),
                title = pickupAddress,
                onClick = onPickupClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            AddressRow(
                color = Color(0xFFE53935),
                title = dropoffAddress,
                onClick = onDropoffClick
            )
        }
    }
}

@Composable
private fun AddressRow(
    color: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            color = Color(0xFF20242A),
            maxLines = 1
        )

        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.ExtraBold,
        style = MaterialTheme.typography.titleLarge,
        color = Color(0xFF20242A),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
    )
}

@Composable
private fun ExpressOptionRow(
    iconColor: Color,
    title: String,
    subtitle: String,
    price: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = null,
                tint = iconColor
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = Color.Gray)
            }
        }

        price?.let {
            Text(it, fontWeight = FontWeight.Bold)
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

@Composable
private fun ExtraServiceRow(
    checked: Boolean,
    title: String,
    subtitle: String,
    price: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onClick() }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = Color.Gray)
            }
        }

        Text(price)
    }
}

@Composable
private fun ExpressBottomBar(
    totalPrice: Double,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val formatter = DecimalFormat("#,###")

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tổng cộng", color = Color(0xFF20242A))
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${formatter.format(totalPrice)}đ",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00B14F),
                    disabledContainerColor = Color(0xFFC9C9C9)
                )
            ) {
                Text(
                    text = "Kiểm tra đơn hàng",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}