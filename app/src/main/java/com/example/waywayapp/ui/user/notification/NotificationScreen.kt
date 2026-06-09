package com.example.waywayapp.ui.user.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.ui.components.WayWayBottomBar
import com.example.waywayapp.ui.navigation.Routes
import com.example.waywayapp.ui.theme.*

data class NotificationItem(
    val title: String,
    val message: String,
    val time: String,
    val icon: ImageVector,
    val iconBg: Color
)

private val notifications = listOf(

    NotificationItem(
        title = "Đặt xe thành công",
        message = "Tài xế đang đến điểm đón của bạn.",
        time = "2 phút trước",
        icon = Icons.Default.DirectionsBike,
        iconBg = WayWayPrimaryContainer
    ),

    NotificationItem(
        title = "Khuyến mãi mới",
        message = "Giảm 30% cho chuyến đi hôm nay.",
        time = "10 phút trước",
        icon = Icons.Default.LocalOffer,
        iconBg = WayWaySecondaryContainer
    ),

    NotificationItem(
        title = "Thanh toán thành công",
        message = "Bạn đã thanh toán bằng MoMo.",
        time = "1 giờ trước",
        icon = Icons.Default.Payments,
        iconBg = WayWayTertiaryContainer
    ),

    NotificationItem(
        title = "Giao hàng đang được xử lý",
        message = "WayWay Express đang cập nhật thông tin đơn hàng.",
        time = "3 giờ trước",
        icon = Icons.Default.LocalOffer,
        iconBg = WayWaySecondaryContainer
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    currentRoute: String? = Routes.NOTIFICATION,
    onBottomNavClick: (String) -> Unit = {}
) {

    Scaffold(
        containerColor = AppBg,
        topBar = {

            TopAppBar(
                title = {
                    Text(
                        text = "Thông báo",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = TextDark
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBg
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                contentPadding = PaddingValues(bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(notifications) { notification ->

                    NotificationCard(notification)
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            WayWayBottomBar(
                currentRoute = currentRoute,
                onItemClick = onBottomNavClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationItem
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(notification.iconBg),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = notification.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = notification.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = notification.time,
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(14.dp))

                HorizontalDivider(
                    color = BorderGray,
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "WayWay Notification",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
            }
        }
    }
}
