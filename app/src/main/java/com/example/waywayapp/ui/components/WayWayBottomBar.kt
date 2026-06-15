package com.example.waywayapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
private val ActiveIconBg = Color(0xFF1DB954)
private val ActiveLabel = Color.White
private val InactiveIconBg get() = Color(0xFFF0F0F0)

@Composable
fun WayWayBottomBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem("Home", Routes.USER_HOME, Icons.Outlined.Home),
        BottomNavItem("History", Routes.RECENTLY_SERVICE, Icons.Outlined.History),
        BottomNavItem("Notify", Routes.NOTIFICATION, Icons.Outlined.NotificationsNone),
        BottomNavItem("Profile", Routes.PROFILE, Icons.Outlined.Person),
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
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    NavPill(
                        item = item,
                        selected = currentRoute == item.route,
                        onClick = { onItemClick(item.route) },
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
    val pillColor by animateColorAsState(
        targetValue = if (selected) NavBackground else Color.Transparent,
        animationSpec = tween(220),
        label = "nav-pill-color"
    )
    val iconBg by animateColorAsState(
        targetValue = if (selected) ActiveIconBg else InactiveIconBg,
        animationSpec = tween(220),
        label = "nav-icon-bg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "nav-icon-tint"
    )
    val iconSize by animateDpAsState(
        targetValue = if (selected) 34.dp else 30.dp,
        animationSpec = tween(220),
        label = "nav-icon-size"
    )
    val contentPadding by animateDpAsState(
        targetValue = if (selected) 10.dp else 0.dp,
        animationSpec = tween(220),
        label = "nav-content-padding"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(28.dp),
        color = pillColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            BadgedBox(
                badge = {
                    if (item.badgeCount != null && !selected) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Text(item.badgeCount.toString(), fontSize = 9.sp)
                        }
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(160)) + expandHorizontally(tween(180)) + scaleIn(initialScale = 0.92f),
                exit = fadeOut(tween(120)) + shrinkHorizontally(tween(140))
            ) {
                Spacer(Modifier.width(8.dp))
            }
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(160)) + expandHorizontally(tween(180)) + scaleIn(initialScale = 0.92f),
                exit = fadeOut(tween(120)) + shrinkHorizontally(tween(140))
            ) {
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
