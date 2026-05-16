package com.example.waywayapp.ui.user.booking.food

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.R

@Composable
fun FoodBookingScreen(
    onBackClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onFoodClick: (FoodItem) -> Unit = {}
) {
    val bgLight = Color(0xFFF5F7F2)

    Scaffold(
        containerColor = bgLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            item {
                FoodHeader(
                    onBackClick = onBackClick,
                    onCartClick = onCartClick
                )
            }

            item { FoodCategories() }

            item { PromoFoodBanners() }

            item {
                SectionTitle(
                    title = "Món ngon gần bạn",
                    action = "Tất cả",
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            items(foodList) { food ->
                FoodRestaurantCard(
                    data = food,
                    onClick = { onFoodClick(food) }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun FoodHeader(
    onBackClick: () -> Unit,
    onCartClick: () -> Unit
) {
    val cardWhite = Color.White
    val darkCard = Color(0xFF20242A)
    val lime = Color(0xFFD8FF4F)
    val textDark = Color(0xFF20242A)
    val textGray = Color(0xFF8B918A)

    var searchText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF4CD08D),
                        Color(0xFFE9FF9A)
                    )
                )
            )
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(cardWhite)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = textDark
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardWhite.copy(alpha = 0.96f))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = textGray,
                        modifier = Modifier.size(19.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    BasicTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = textDark,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchText.isEmpty()) {
                                Text(
                                    text = "Bạn muốn ăn gì?",
                                    color = textGray,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(darkCard)
                        .clickable { onCartClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = lime,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Đặt đồ ăn",
                color = textDark,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Món ngon giao nhanh đến tận tay bạn",
                color = textDark.copy(alpha = 0.72f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

data class FoodCategory(
    val name: String,
    val icon: Int
)

private val categories = listOf(
    FoodCategory("Cơm", R.drawable.food_icon),
    FoodCategory("Trà sữa", R.drawable.food_icon),
    FoodCategory("Burger", R.drawable.food_icon),
    FoodCategory("Bún/Phở", R.drawable.food_icon),
    FoodCategory("Healthy", R.drawable.food_icon)
)

@Composable
private fun FoodCategories() {
    Column(
        modifier = Modifier
            .offset(y = (-14).dp)
            .padding(horizontal = 18.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(categories) { category ->
                    Column(
                        modifier = Modifier
                            .width(72.dp)
                            .clickable { },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFE9FF9A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(category.icon),
                                contentDescription = null,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        Text(
                            text = category.name,
                            fontSize = 12.sp,
                            color = Color(0xFF20242A),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

data class FoodBanner(
    val image: Int
)

private val foodBanners = listOf(
    FoodBanner(R.drawable.banner_promo1),
    FoodBanner(R.drawable.banner_promo1),
    FoodBanner(R.drawable.banner_promo1)
)

@Composable
private fun PromoFoodBanners() {
    Column(
        modifier = Modifier.padding(bottom = 18.dp)
    ) {
        SectionTitle(
            title = "Ưu đãi đồ ăn",
            action = "Xem thêm",
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(foodBanners) { banner ->
                Card(
                    modifier = Modifier
                        .width(250.dp)
                        .height(120.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                ) {
                    Image(
                        painter = painterResource(banner.image),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

data class FoodItem(
    val name: String,
    val store: String,
    val price: String,
    val distance: String,
    val rating: String,
    val image: Int,
    val badge: String
)

private val foodList = listOf(
    FoodItem(
        name = "Burger bò phô mai",
        store = "WayWay Food",
        price = "từ 39.000đ",
        distance = "1.2 km",
        rating = "4.8",
        image = R.drawable.banner_promo1,
        badge = "Hot"
    ),
    FoodItem(
        name = "Trà sữa trân châu",
        store = "Milk Tea House",
        price = "từ 29.000đ",
        distance = "0.8 km",
        rating = "4.9",
        image = R.drawable.banner_promo1,
        badge = "Mới"
    ),
    FoodItem(
        name = "Cơm gà sốt cay",
        store = "Cơm ngon Đà Nẵng",
        price = "từ 45.000đ",
        distance = "2.1 km",
        rating = "4.7",
        image = R.drawable.banner_promo1,
        badge = "Sale"
    )
)

@Composable
private fun FoodRestaurantCard(
    data: FoodItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 7.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Image(
                    painter = painterResource(data.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(20.dp))
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFD8FF4F))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = data.badge,
                        fontSize = 10.sp,
                        color = Color(0xFF20242A),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = data.name,
                    fontSize = 16.sp,
                    color = Color(0xFF20242A),
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = data.store,
                    fontSize = 12.sp,
                    color = Color(0xFF8B918A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = data.price,
                    fontSize = 13.sp,
                    color = Color(0xFF20242A),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(15.dp)
                    )

                    Spacer(modifier = Modifier.width(3.dp))

                    Text(
                        text = data.rating,
                        fontSize = 12.sp,
                        color = Color(0xFF20242A),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = " • ${data.distance}",
                        fontSize = 12.sp,
                        color = Color(0xFF8B918A)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
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
            color = Color(0xFF20242A),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = action,
            color = Color(0xFF8B918A),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}