package com.example.waywayapp.presentation.user.booking.express

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

// =====================================================================
// 1. ĐỊNH NGHĨA HỆ THỐNG MÀU SẮC CHUẨN WAYWAY
// =====================================================================
private val WayWayDarkGreen = Color(0xFF006C4C)
private val WayWayLightGreen = Color(0xFF1DB954)
private val WayWayTeal = Color(0xFF138A72)
private val WayWayRed = Color(0xFFE53935)
private val WayWayOrange = Color(0xFFFF9800)
private val AppBgColor = Color(0xFFF5F7FA)
private val CardBgColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1D1B20)
private val TextSecondary = Color(0xFF757575)

@Composable
fun ExpressFormScreen(
    onBackClick: () -> Unit = {},
    onPickupClick: () -> Unit = {},
    onDropoffClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    viewModel: ExpressViewModel = ExpressSharedViewModel.viewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AppBgColor,
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
        },
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // --- HEADER WAYWAY ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WayWayDarkGreen)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.offset(x = (-12).dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Cần ship hàng gấp?",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Đặt WayWay Express ngay!",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            // --- ĐỊA CHỈ ---
            AddressInputCard(
                pickupAddress = uiState.pickupAddress,
                dropoffAddress = uiState.dropoffAddress,
                onPickupClick = onPickupClick,
                onDropoffClick = onDropoffClick
            )

            SectionTitle("Chi tiết đơn hàng")

            ExpressOptionRow(
                iconColor = WayWayTeal,
                iconVector = Icons.Default.Schedule,
                title = "Lấy hàng trong vòng 15 phút",
                subtitle = "Siêu tốc",
                price = null,
                onClick = {}
            )

            ExpressOptionRow(
                iconColor = WayWayLightGreen,
                iconVector = Icons.Default.Inventory2,
                title = "Xe máy",
                subtitle = "Đề xuất dựa trên chi tiết kiện hàng",
                price = null,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.packageDetail,
                onValueChange = viewModel::onPackageDetailChange,
                label = { Text("Thêm chi tiết kiện hàng *", color = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = false,
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WayWayDarkGreen,
                    focusedLabelColor = WayWayDarkGreen,
                    cursorColor = WayWayDarkGreen,
                    unfocusedBorderColor = Color.LightGray,
                    unfocusedContainerColor = CardBgColor,
                    focusedContainerColor = CardBgColor
                )
            )

            SectionTitle("Áp dụng ưu đãi")

            ExpressOptionRow(
                iconColor = WayWayOrange,
                iconVector = Icons.Default.LocalOffer,
                title = "Áp dụng ưu đãi để được giảm giá",
                subtitle = "",
                price = null,
                onClick = {}
            )

            SectionTitle("Dịch vụ thêm")

            // Gom các Checkbox vào 1 khối Card trắng
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBgColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
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
                }
            }

            uiState.errorMessage?.let {
                Card(
                    modifier = Modifier.padding(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = it,
                        color = WayWayRed,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            AddressRow(color = WayWayLightGreen, title = pickupAddress, onClick = onPickupClick)
            Spacer(modifier = Modifier.height(18.dp))
            AddressRow(color = WayWayRed, title = dropoffAddress, onClick = onDropoffClick)
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
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.ExtraBold,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ExpressOptionRow(
    iconColor: Color,
    iconVector: ImageVector,
    title: String,
    subtitle: String,
    price: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = iconVector, contentDescription = null, tint = iconColor)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, color = TextSecondary, fontSize = 12.sp)
                }
            }

            price?.let {
                Text(it, color = TextPrimary, fontWeight = FontWeight.Bold)
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(checkedColor = WayWayDarkGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
        }

        Text(price, color = WayWayDarkGreen, fontWeight = FontWeight.Bold)
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tổng cộng", color = TextSecondary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${formatter.format(totalPrice)}đ",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WayWayDarkGreen,
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.White
                )
            ) {
                Text("Kiểm tra đơn hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}