package com.example.waywayapp.ui.user.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.R
import com.example.waywayapp.ui.navigation.Routes
import com.example.waywayapp.ui.theme.BadgeRed
import com.example.waywayapp.ui.theme.BgLight
import com.example.waywayapp.ui.theme.CardWhite
import com.example.waywayapp.ui.theme.DarkCard
import com.example.waywayapp.ui.theme.DarkGradient
import com.example.waywayapp.ui.theme.Lime
import com.example.waywayapp.ui.theme.LimeDark
import com.example.waywayapp.ui.theme.LimeGradient
import com.example.waywayapp.ui.theme.StarYellow
import com.example.waywayapp.ui.theme.TextDark
import com.example.waywayapp.ui.theme.TextGray

@Composable
fun HomeScreen(
    currentRoute: String? = Routes.USER_HOME,
    onServiceClick: (String) -> Unit = {},
    onWalletClick: () -> Unit = {},
    onBottomNavClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = BgLight,
        bottomBar = { BottomNavigation(
            currentRoute = currentRoute,
            onItemClick = onBottomNavClick
        ) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            item { HeaderSection() }
            item { FloatingWalletCard(onWalletClick = onWalletClick) }
            item { ServiceGrid(onServiceClick) }
            item { PromoBanners() }
            item { FoodSection() }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgLight)
    ) {
        Column {

            // TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CardWhite)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = null,
                        tint = TextDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                SearchBox(
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-5).dp, y = 5.dp)
                            .clip(CircleShape)
                            .background(BadgeRed)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BANNER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .offset(y= (-10).dp),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                onClick = {  }

            ) {
                Image(
                    painter = painterResource(id = R.drawable.banner),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun SearchBox(
    modifier: Modifier = Modifier,
    onSearchClick:() -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(CardWhite.copy(alpha = 0.96f))
            .padding(horizontal = 14.dp)
            .clickable { onSearchClick() },

        verticalAlignment = Alignment.CenterVertically,

    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        BasicTextField(
            value = searchText,
            onValueChange = { searchText = it },
            singleLine = true,
            textStyle = TextStyle(
                color = TextDark,
                fontSize = 13.sp
            ),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchText.isEmpty()) {
                        Text(
                            text = "Bạn muốn đi đâu?",
                            color = TextGray,
                            fontSize = 13.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun FloatingWalletCard(
    onWalletClick:() -> Unit = {}
) {
    Card(
    modifier = Modifier
        .padding(horizontal = 18.dp)
        .offset(y = (-3).dp)
        .fillMaxWidth(),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(
        containerColor = CardWhite.copy(alpha = 0.85f)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    onClick = { onWalletClick() }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.momo_icon),
            contentDescription = null,
            modifier = Modifier.size(38.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Liên kết MoMo",
            fontSize = 13.sp,
            color = TextDark,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Chi tiết",
            fontSize = 12.sp,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(18.dp)
        )
    }

    }
    Spacer(modifier = Modifier.height(20.dp))
}

data class ServiceItemData(
    val name: String,
    val icon: Int,
)
private val serviceList = listOf(

    ServiceItemData(
        "Xe máy",
        R.drawable.bike_icon,
    ),
    ServiceItemData(
        "Đồ ăn",
        R.drawable.food_icon,
    ),
    ServiceItemData(
        "Ô tô",
        R.drawable.car_icon,
    ),
    ServiceItemData(
        "Giao hàng",
        R.drawable.express_icon,
    ),
)

@Composable
fun ServiceGrid(
    onServiceClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-18).dp)
            .padding(horizontal = 18.dp)
    ) {
        SectionTitle(
            title = "Dịch vụ",
            action = "Tất cả"
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardWhite
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(vertical = 18.dp)
            ) {
                serviceList.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { service ->
                            ServiceItem(
                                service = service,
                                onClick = { onServiceClick(service.name) }
                            )
                        }
                    }

                    if (row != serviceList.chunked(4).last()) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceItem(
    service: ServiceItemData,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(22.dp)) ,
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = service.icon),
                contentDescription = service.name,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = service.name,
            fontSize = 13.sp,
            color = TextDark,
            fontWeight = FontWeight.Medium  ,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}

private data class BannerData(
    val tag: String,
    val title: String,
    val imageUrl: Int,
)

private val banners = listOf(
    BannerData(
        tag = "Khuyến mãi",
        title = "Giảm 30% đơn đầu tiên",
        imageUrl = R.drawable.banner
    ),
    BannerData(
        tag = "Hot deal",
        title = "Freeship cuối tuần",
        imageUrl = R.drawable.banner

    )
)

@Composable
fun PromoBanners() {
    Column(
        modifier = Modifier.padding(bottom = 10.dp)
    ) {
        SectionTitle(
            title = "Ưu đãi hôm nay",
            action = "Xem thêm",
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(banners) { banner ->
                BannerItem(data = banner)
            }
        }
    }
}
@Composable
private fun BannerItem(
    data: BannerData
) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Image(
            painter = painterResource(data.imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

    }
}
data class FoodItemData(
    val name: String,
    val price: String,
    val badge: String,
    val imageUrl: Int
)

private val foodItems = listOf(
    FoodItemData("Trà sữa Sài Gòn",
        "từ 35.000đ",
        "Mới",
        R.drawable.banner_promo1,
         ),
    FoodItemData("Cơm thố Nhật",
        "từ 55.000đ",
        "Mới",
        R.drawable.banner_promo1,
    ),
    FoodItemData("Trà sữa Sài Gòn",
        "từ 35.000đ",
        "Mới",
        R.drawable.banner_promo1,
    ),
    FoodItemData("Trà sữa Sài Gòn",
        "từ 35.000đ",
        "Mới",
        R.drawable.banner_promo1,
    ),
)

@Composable
fun FoodSection() {
    Column(
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        SectionTitle(
            title = "Đặt ăn gần bạn",
            action = "Tất cả",
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(foodItems) { food ->
                FoodCard(data = food)
            }
        }
    }
}

@Composable
fun FoodCard(
    data: FoodItemData
) {
    Card(
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(data.imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier.padding(11.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Lime)
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = data.badge,
                        color = TextDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = data.name,
                    color = TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = data.price,
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

            }
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    action: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = action,
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(18.dp)
        )
    }
}
@Composable
fun BottomNavigation(
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
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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
fun BottomIcon(
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
