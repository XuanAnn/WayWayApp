package com.example.waywayapp.presentation.user.booking.express

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.presentation.user.booking.express.model.ExpressStatus
import com.example.waywayapp.presentation.user.home.HomeMapPreview
import java.text.DecimalFormat

// =====================================================================
// 1. ĐỊNH NGHĨA HỆ THỐNG MÀU SẮC CHUẨN WAYWAY (Dựa theo ảnh Home)
// =====================================================================
private val WayWayDarkGreen = Color(0xFF006C4C)   // Xanh lục đậm (Nút bấm, Timeline Active)
private val WayWayLightGreen = Color(0xFF1DB954)  // Xanh lá sáng (Icon, Badge)
private val WayWayPaleGreen = Color(0xFFE8F5EE)   // Xanh cực nhạt (Nền Card Timeline)
private val WayWayRed = Color(0xFFE53935)         // Đỏ (Hủy đơn, Giao hàng)
private val AppBgColor = Color(0xFFF5F7FA)        // Màu nền xám nhạt của toàn app
private val CardBgColor = Color(0xFFFFFFFF)       // Nền trắng tinh của các Card
private val TextPrimary = Color(0xFF1D1B20)       // Chữ đen xám đậm
private val TextSecondary = Color(0xFF757575)     // Chữ xám phụ

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressConfirmScreen(
    onBackClick: () -> Unit = {},
    onDone: () -> Unit = {},
    viewModel: ExpressViewModel = ExpressSharedViewModel.viewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val formatter = DecimalFormat("#,###")

    Scaffold(
        containerColor = AppBgColor, // Nền app đồng bộ xám nhạt
        topBar = {
            TopAppBar(
                title = {
                    Text("WayWay Express", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBgColor),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) // Xóa viền thừa hệ thống
            )
        },
        bottomBar = {
            ExpressConfirmActions(
                status = uiState.status,
                isLoading = uiState.isLoading,
                onConfirm = viewModel::confirmExpressOrder,
                onCancel = viewModel::cancelExpressOrder,
                onDone = {
                    viewModel.resetOrder()
                    onDone()
                }
            )
        },
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Tăng nhẹ khoảng cách cho thoáng
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // Bản đồ thu nhỏ: Có bo viền + đổ bóng nhẹ
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = CardBgColor)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    HomeMapPreview(
                        userPhotoUrl = uiState.currentDriver?.avatarUrl,
                        onMapTouchChanged = {}
                    )
                }
            }

            // Thanh Trạng Thái (Timeline Động)
            DynamicExpressStatusCard(
                status = uiState.status,
                rideStatus = uiState.rideStatus
            )

            // Các thẻ thông tin tĩnh - UI/UX Mới
            InfoCard(title = "Chi tiết chuyến đi") {
                InfoRow(
                    label = "Lấy hàng",
                    value = uiState.pickupAddress,
                    icon = Icons.Default.ArrowUpward,
                    iconTint = WayWayLightGreen
                )
                InfoRow(
                    label = "Giao hàng",
                    value = uiState.dropoffAddress,
                    icon = Icons.Default.LocationOn,
                    iconTint = WayWayRed
                )
            }

            InfoCard(title = "Thông tin đơn hàng") {
                InfoRow("Chi tiết hàng", uiState.packageDetail.ifBlank { "Chưa có chi tiết" }, Icons.Outlined.Inventory2, WayWayDarkGreen)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MiniDataBox("Quãng đường", "${String.format("%.1f", uiState.distanceKm)} km", Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    MiniDataBox("Thời gian", "~${uiState.durationMinute.toInt()} phút", Modifier.weight(1f))
                }

                Divider(color = AppBgColor, modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tổng thanh toán", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("${formatter.format(uiState.totalPrice)}đ", color = WayWayDarkGreen, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            if (uiState.codEnabled || uiState.handDelivery || uiState.bigPackage) {
                InfoCard(title = "Dịch vụ đính kèm") {
                    if (uiState.codEnabled) InfoRow("COD", "Thu tiền hộ", Icons.Default.Payments, WayWayDarkGreen)
                    if (uiState.handDelivery) InfoRow("Giao tận tay", "Giao trực tiếp người nhận", Icons.Default.BackHand, WayWayDarkGreen)
                    if (uiState.bigPackage) InfoRow("Hàng cỡ lớn", "Cần tài xế phù hợp", Icons.Default.LocalShipping, WayWayDarkGreen)
                }
            }

            if (uiState.driverId.isNotBlank()) {
                DriverProfileCard(
                    name = uiState.driverName.ifBlank { "Đang cập nhật" },
                    plate = uiState.driverPlate.ifBlank { "Biển số" }
                )
            }

            uiState.errorMessage?.let {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                    Text(text = it, color = WayWayRed, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// =====================================================================
// COMPONENT: THẺ TRẠNG THÁI TIMELINE ĐỘNG (PREMIUM)
// =====================================================================
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DynamicExpressStatusCard(status: ExpressStatus, rideStatus: String) {
    val isCancelled = status == ExpressStatus.CANCELLED
    val bgColor = if (isCancelled) Color(0xFFFFF0F0) else WayWayPaleGreen
    val activeColor = if (isCancelled) WayWayRed else WayWayDarkGreen
    val inactiveColor = activeColor.copy(alpha = 0.2f)

    val (title, subtitle) = when (status) {
        ExpressStatus.INPUT, ExpressStatus.CONFIRMING -> "Kiểm tra đơn hàng" to "Xác nhận thông tin trước khi đặt tài xế."
        ExpressStatus.FINDING_DRIVER -> "Đang tìm tài xế" to "WayWay đang tìm tài xế phù hợp..."
        ExpressStatus.DRIVER_ACCEPTED -> {
            if (rideStatus == "arrived") "Tài xế đã đến" to "Tài xế đã có mặt tại điểm lấy hàng."
            else "Tài xế đã nhận đơn" to "Tài xế đang di chuyển đến điểm lấy."
        }
        ExpressStatus.DELIVERING -> "Đang giao hàng" to "Đơn hàng đang trên đường đến người nhận."
        ExpressStatus.COMPLETED -> "Giao hàng thành công" to "Cảm ơn bạn đã sử dụng WayWay Express."
        ExpressStatus.CANCELLED -> "Đơn hàng đã hủy" to "Chuyến đi không thể thực hiện."
    }

    val steps = listOf("Lấy hàng", "Đã lấy", "Đang giao", "Hoàn thành")
    val targetStep = when {
        status == ExpressStatus.COMPLETED -> 3f
        status == ExpressStatus.DELIVERING || rideStatus == "in_progress" -> 2f
        status == ExpressStatus.DRIVER_ACCEPTED && rideStatus == "arrived" -> 1f
        status == ExpressStatus.DRIVER_ACCEPTED -> 0f
        else -> -1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetStep,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "timeline"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val loadingProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "loading"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Text
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(targetState = status, label = "icon") { s ->
                        when (s) {
                            ExpressStatus.FINDING_DRIVER -> CircularProgressIndicator(color = activeColor, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                            ExpressStatus.COMPLETED -> Icon(Icons.Default.CheckCircle, null, tint = activeColor, modifier = Modifier.size(26.dp))
                            ExpressStatus.CANCELLED -> Icon(Icons.Default.Cancel, null, tint = activeColor, modifier = Modifier.size(26.dp))
                            else -> Icon(Icons.Default.LocalShipping, null, tint = activeColor, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    AnimatedContent(targetState = title, label = "title") { t ->
                        Text(t, fontWeight = FontWeight.ExtraBold, color = activeColor, fontSize = 17.sp)
                    }
                    AnimatedContent(targetState = subtitle, label = "sub") { s ->
                        Text(s, color = activeColor.copy(alpha = 0.8f), fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }

            // Timeline
            if (!isCancelled && status != ExpressStatus.INPUT && status != ExpressStatus.CONFIRMING) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        val stepWidth = size.width / (steps.size - 1)
                        val startY = size.height / 2

                        drawLine(color = inactiveColor, start = Offset(0f, startY), end = Offset(size.width, startY), strokeWidth = 8f, cap = StrokeCap.Round)

                        if (targetStep >= 0f) {
                            drawLine(color = activeColor, start = Offset(0f, startY), end = Offset(animatedProgress * stepWidth, startY), strokeWidth = 8f, cap = StrokeCap.Round)
                        } else if (status == ExpressStatus.FINDING_DRIVER) {
                            val barWidth = size.width * 0.25f
                            val startX = (size.width + barWidth) * loadingProgress - barWidth
                            drawLine(color = activeColor, start = Offset(startX.coerceAtLeast(0f), startY), end = Offset((startX + barWidth).coerceAtMost(size.width), startY), strokeWidth = 8f, cap = StrokeCap.Round)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        steps.forEachIndexed { index, text ->
                            val isCompleted = animatedProgress >= index.toFloat()
                            val isCurrent = (animatedProgress >= index - 0.5f && animatedProgress < index + 0.5f)
                            val nodeColor by animateColorAsState(if (isCompleted || isCurrent) activeColor else inactiveColor, label = "color")

                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(if (status == ExpressStatus.FINDING_DRIVER) inactiveColor else nodeColor, CircleShape)
                                        .border(2.dp, bgColor, CircleShape), // Viền cắt vào thanh kẻ ngang
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (animatedProgress >= index.toFloat() && status != ExpressStatus.FINDING_DRIVER) {
                                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = text,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent && status != ExpressStatus.FINDING_DRIVER) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (status == ExpressStatus.FINDING_DRIVER) activeColor.copy(0.6f) else nodeColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================
// THÀNH PHẦN CON ĐÃ ĐƯỢC REDESIGN LẠI
// =====================================================================
@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextPrimary)
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Icon bọc trong vòng tròn mờ - Style Premium
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconTint.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun MiniDataBox(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(AppBgColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun DriverProfileCard(name: String, plate: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(AppBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = WayWayDarkGreen, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextPrimary)
                Text("Tài xế WayWay", fontSize = 12.sp, color = TextSecondary)
            }
            Surface(color = WayWayPaleGreen, shape = RoundedCornerShape(8.dp)) {
                Text(plate, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = WayWayDarkGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ExpressConfirmActions(
    status: ExpressStatus, isLoading: Boolean,
    onConfirm: () -> Unit, onCancel: () -> Unit, onDone: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = CardBgColor,
        shadowElevation = 24.dp
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (status) {
                ExpressStatus.INPUT, ExpressStatus.CONFIRMING, ExpressStatus.CANCELLED -> {
                    Button(
                        onClick = onConfirm, enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WayWayDarkGreen)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Xác nhận & Đặt xe", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
                ExpressStatus.COMPLETED -> {
                    Button(
                        onClick = onDone,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WayWayDarkGreen)
                    ) {
                        Text("Về trang chủ", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
                else -> {
                    OutlinedButton(
                        onClick = onCancel, enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WayWayRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
                    ) {
                        Text("Huỷ chuyến", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}