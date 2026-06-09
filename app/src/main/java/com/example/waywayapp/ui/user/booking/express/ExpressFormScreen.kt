package com.example.waywayapp.ui.user.booking.express

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
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
                    .background(colors.primaryContainer)
                    .padding(20.dp)
            ) {
                Column {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = colors.onPrimaryContainer)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Cần ship hàng gấp?",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onPrimaryContainer
                    )

                    Text(
                        text = "Đặt WayWay Express ngay!",
                        color = colors.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyLarge
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
                iconColor = colors.tertiary,
                title = "Lấy hàng trong vòng 15 phút",
                subtitle = "Siêu tốc",
                price = null,
                onClick = {}
            )

            ExpressOptionRow(
                iconColor = colors.primary,
                title = "Xe máy",
                subtitle = "Đề xuất dựa trên chi tiết kiện hàng",
                price = null,
                onClick = {}
            )

            OutlinedTextField(
                value = uiState.packageDetail,
                onValueChange = viewModel::onPackageDetailChange,
                label = { Text("Thêm chi tiết kiện hàng *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(18.dp),
                singleLine = false,
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    focusedLabelColor = colors.primary,
                    cursorColor = colors.primary,
                    unfocusedBorderColor = colors.outlineVariant
                )
            )

            SectionTitle("Áp dụng ưu đãi")

            ExpressOptionRow(
                iconColor = colors.secondary,
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
                subtitle = "Lên đến 50kg, 60x70x60cm",
                price = "15.000đ",
                onClick = viewModel::toggleBigPackage
            )

            uiState.errorMessage?.let {
                Text(
                    text = it,
                    color = colors.error,
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
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-36).dp)
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            AddressRow(color = colors.primary, title = pickupAddress, onClick = onPickupClick)
            Spacer(modifier = Modifier.height(18.dp))
            AddressRow(color = colors.error, title = dropoffAddress, onClick = onDropoffClick)
        }
    }
}

@Composable
private fun AddressRow(
    color: Color,
    title: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

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
                tint = colors.onPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            maxLines = 1
        )

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.ExtraBold,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
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
    val colors = MaterialTheme.colorScheme

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
                .background(iconColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = iconColor)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = colors.onBackground, fontWeight = FontWeight.Bold)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = colors.onSurfaceVariant)
            }
        }

        price?.let {
            Text(it, color = colors.onBackground, fontWeight = FontWeight.Bold)
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
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
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { onClick() })
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = colors.onBackground, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = colors.onSurfaceVariant)
            }
        }

        Text(price, color = colors.onBackground)
    }
}

@Composable
private fun ExpressBottomBar(
    totalPrice: Double,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val formatter = DecimalFormat("#,###")
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tổng cộng", color = colors.onSurface)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${formatter.format(totalPrice)}đ",
                    color = colors.onSurface,
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
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    disabledContainerColor = colors.surfaceVariant
                )
            ) {
                Text("Kiểm tra đơn hàng", fontWeight = FontWeight.Bold)
            }
        }
    }
}
