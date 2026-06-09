package com.example.waywayapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.ui.navigation.Routes
import com.example.waywayapp.ui.user.home.BottomNavItem

private val NavBackground = Color(0xFF111111)
private val ActiveIconBg  = Color(0xFF1DB954)
private val ActiveLabel   = Color.White
private val InactiveIconBg get() = Color(0xFFF0F0F0)

@Composable
fun WayWayBottomBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem("Trang chủ",       Routes.USER_HOME,         Icons.Outlined.Home),
        BottomNavItem("Lịch sử", Routes.RECENTLY_SERVICE,  Icons.Outlined.History),
        BottomNavItem("Thông báo",  Routes.NOTIFICATION,      Icons.Outlined.NotificationsNone),
        BottomNavItem("Tôi",        Routes.PROFILE,           Icons.Outlined.Person),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(36.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Row(
                modifier = Modifier
                    .width(120.dp)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route
                    NavPill(
                        item = item,
                        selected = selected,
                        onClick = { onItemClick(item.route) },
                        // Dùng weight cố định thay vì dynamic để tránh layout jump
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavPill(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxHeight(),
        shape = RoundedCornerShape(28.dp),
        color = if (selected) NavBackground else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (selected) 10.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Icon — badge đặt ngoài Box clip để không bị cắt
            BadgedBox(
                badge = {
                    if (item.badgeCount != null && !selected) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor   = MaterialTheme.colorScheme.onError
                        ) { Text(item.badgeCount.toString(), fontSize = 9.sp) }
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (selected) ActiveIconBg else InactiveIconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (selected) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Label — chỉ hiện khi selected
            if (selected) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.title,
                    color = ActiveLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}
