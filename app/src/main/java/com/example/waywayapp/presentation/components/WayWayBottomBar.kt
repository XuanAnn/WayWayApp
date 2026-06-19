package com.example.waywayapp.presentation.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.presentation.common.components.BottomNavItem
import com.example.waywayapp.presentation.navigation.Routes

// Tuỳ biến struct để chứa cả icon thường và icon khi được chọn (đậm hơn)
private data class LocalNavItem(
    val title: String,
    val route: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val badgeCount: Int? = null
)

private val NavBackground = Color(0xFF161616) // Nền viên thuốc lúc Active (Đen xám)
private val ActiveIconBg = Color(0xFF1DB954)  // Xanh lá đặc trưng
private val ActiveLabel = Color.White
private val InactiveIconBg = Color.Transparent
private val InactiveIconTint = Color(0xFF9E9E9E)

@Composable
fun WayWayBottomBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Chuyển đổi nhãn sang Tiếng Việt
    val items = listOf(
        LocalNavItem("Trang chủ", Routes.USER_HOME, Icons.Outlined.Home, Icons.Filled.Home),
        LocalNavItem("Lịch sử", Routes.RECENTLY_SERVICE, Icons.Outlined.History, Icons.Filled.History),
        LocalNavItem("Thông báo", Routes.NOTIFICATION, Icons.Outlined.NotificationsNone, Icons.Filled.Notifications),
        LocalNavItem("Tài khoản", Routes.PROFILE, Icons.Outlined.Person, Icons.Filled.Person),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            // Tăng padding dưới để tạo cảm giác Floating Pill
//            .padding(horizontal = 20.dp, vertical = 24.dp)
        ,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(40.dp), // Bo tròn lớn hơn
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp, // Đổ bóng đậm hơn giúp thanh nổi bật
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    NavPill(
                        item = item,
                        selected = currentRoute == item.route,
                        onClick = { onItemClick(item.route) },
                        modifier = Modifier.weight(if (currentRoute == item.route) 1.7f else 1f)
                    )
                }
            }
        }
    }
}

@SuppressLint("RememberInComposition")
@Composable
private fun NavPill(
    item: LocalNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 3. Hiệu ứng mượt mà sử dụng Spring thay vì Tween cứng
    val animationSpec = spring<Color>(stiffness = Spring.StiffnessLow)
    val floatSpec = spring<androidx.compose.ui.unit.Dp>(stiffness = Spring.StiffnessLow)

    val pillColor by animateColorAsState(
        targetValue = if (selected) NavBackground else Color.Transparent,
        animationSpec = animationSpec,
        label = "nav-pill-color"
    )
    val iconBg by animateColorAsState(
        targetValue = if (selected) ActiveIconBg else InactiveIconBg,
        animationSpec = animationSpec,
        label = "nav-icon-bg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) Color.White else InactiveIconTint,
        animationSpec = animationSpec,
        label = "nav-icon-tint"
    )
    val iconSize by animateDpAsState(
        targetValue = if (selected) 38.dp else 28.dp,
        animationSpec = floatSpec,
        label = "nav-icon-size"
    )
    val contentPadding by animateDpAsState(
        targetValue = if (selected) 12.dp else 0.dp,
        animationSpec = floatSpec,
        label = "nav-content-padding"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(32.dp),
        color = pillColor,
        // Loại bỏ hiệu ứng Ripple mặc định khi bấm trên pill không chọn để UI sạch hơn
        interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource()
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
                            contentColor = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.padding(top = 4.dp, end = 4.dp)
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
                        // Sử dụng Filled Icon khi chọn, Outlined Icon khi không chọn
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        tint = iconTint,
                        modifier = Modifier.size(if (selected) 20.dp else 24.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(250)) + expandHorizontally(tween(250)) + scaleIn(initialScale = 0.8f, animationSpec = tween(250)),
                exit = fadeOut(tween(150)) + shrinkHorizontally(tween(150))
            ) {
                Spacer(Modifier.width(6.dp))
            }

            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(250)) + expandHorizontally(tween(250)),
                exit = fadeOut(tween(150)) + shrinkHorizontally(tween(150))
            ) {
                Text(
                    text = item.title,
                    color = ActiveLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}