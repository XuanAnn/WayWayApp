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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.R
import com.example.waywayapp.ui.components.WayWayBottomBar
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
import com.example.waywayapp.ui.user.booking.food.components.FoodCartBottomBar
import com.example.waywayapp.ui.user.home.model.ServiceUiModel
import com.example.waywayapp.ui.user.home.model.BannerUiModel
import com.example.waywayapp.ui.user.home.model.FoodPreviewUiModel
@Composable
fun HomeScreen(
    currentRoute: String? = Routes.USER_HOME,
    onServiceClick: (String) -> Unit = {},
    onWalletClick: () -> Unit = {},
    onBottomNavClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onPromoClick: () -> Unit = {},
    onFoodClick: (Int) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BgLight,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgLight)
            ) {
                if (uiState.cartItems.isNotEmpty()) {
                    FoodCartBottomBar(
                        totalQuantity = uiState.totalCartQuantity,
                        totalPrice = uiState.totalCartPrice,
                        onCartClick = {
                            onServiceClick("food")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                WayWayBottomBar(
                    currentRoute = currentRoute,
                    onItemClick = onBottomNavClick
                )
            }
        }
    ) {
        padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            item {
                HeaderSection(
                    onSearchClick = onSearchClick,
                    onNotificationClick = onNotificationClick
                )
            }
            item { FloatingWalletCard(onWalletClick = onWalletClick) }

            item {
                ServiceGrid(
                    services = uiState.services,
                    onServiceClick = { service ->
                        onServiceClick(service.type)
                    }
                )
            }

            item {
                PromoBanners(
                    banners = uiState.banners,
                    onPromoClick = onPromoClick
                )
            }

            item {
                FoodSection(
                    foods = uiState.foods,
                    onFoodClick = onFoodClick
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun HeaderSection(
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgLight)
    ) {
        Column {
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
                    modifier = Modifier.weight(1f),
                    onSearchClick = onSearchClick
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                        .clickable { onNotificationClick() },
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .offset(y = (-10).dp),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                onClick = {}
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

@Composable
fun ServiceGrid(
    services: List<ServiceUiModel>,
    onServiceClick: (ServiceUiModel) -> Unit
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
                services.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { service ->
                            ServiceItem(
                                service = service,
                                onClick = {
                                    onServiceClick(service)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ServiceItem(
    service: ServiceUiModel,
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
                .clip(RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = service.iconRes),
                contentDescription = service.title,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = service.title,
            fontSize = 13.sp,
            color = TextDark,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}@Composable
fun PromoBanners(
    banners: List<BannerUiModel>,
    onPromoClick: () -> Unit = {}
) {
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
                BannerItem(
                    data = banner,
                    onClick = onPromoClick
                )
            }
        }
    }
}

@Composable
private fun BannerItem(
    data: BannerUiModel,
    onClick: () -> Unit
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
        ),
        onClick = onClick
    ) {
        Image(
            painter = painterResource(data.imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
@Composable
fun FoodSection(
    foods: List<FoodPreviewUiModel>,
    onFoodClick: (Int) -> Unit = {}
) {
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
            items(foods) { food ->
                FoodCard(
                    data = food,
                    onClick =  {onFoodClick(food.id)}
                )
            }
        }
    }
}
@Composable
fun FoodCard(
    data: FoodPreviewUiModel,
    onClick: () -> Unit = {},

) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() },
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
                    painter = painterResource(data.imageRes),
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