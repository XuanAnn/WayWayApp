package com.example.waywayapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.ui.navigation.Routes
import com.example.waywayapp.ui.theme.BadgeRed
import com.example.waywayapp.ui.theme.CardWhite
import com.example.waywayapp.ui.theme.DarkCard
import com.example.waywayapp.ui.theme.Lime
import com.example.waywayapp.ui.theme.TextDark
import com.example.waywayapp.ui.theme.TextGray
import com.example.waywayapp.ui.user.home.BottomNavItem

@Composable
fun WayWayBottomBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem(
            title = "Trang chủ",
            route = Routes.USER_HOME,
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            title = "Gần đây",
            route = Routes.RECENTLY_SERVICE,
            icon = Icons.Default.History
        ),
        BottomNavItem(
            title = "Thông báo",
            route = Routes.NOTIFICATION,
            icon = Icons.Default.Notifications
        ),
        BottomNavItem(
            title = "Tôi",
            route = Routes.PROFILE,
            icon = Icons.Default.Person,
            badgeCount = 2
        )
    )

    NavigationBar(
        containerColor = CardWhite,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    onItemClick(item.route)
                },
                icon = {
                    if (item.badgeCount != null) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = BadgeRed,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = item.badgeCount.toString(),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        ) {
                            BottomIcon(
                                icon = item.icon,
                                selected = selected
                            )
                        }
                    } else {
                        BottomIcon(
                            icon = item.icon,
                            selected = selected
                        )
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 10.sp,
                        fontWeight = if (selected) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = TextDark,
                    unselectedTextColor = TextGray,
                    unselectedIconColor = TextGray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun BottomIcon(
    icon: ImageVector,
    selected: Boolean
) {
    if (selected) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(DarkCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Lime,
                modifier = Modifier.size(21.dp)
            )
        }
    } else {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(22.dp)
        )
    }
}