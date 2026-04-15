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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.ui.theme.GoFoodBg
import com.example.waywayapp.ui.theme.GoFoodGreen
import com.example.waywayapp.ui.theme.GoFoodSurface
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import com.example.waywayapp.R
import com.example.waywayapp.ui.theme.GoFoodTextDark
import com.example.waywayapp.ui.theme.GoFoodTextGray

@Composable
fun HomeScreen(onServiceClick: (String) -> Unit = {}) {
    Scaffold(
        bottomBar = { HomeBottomNavigation() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .background(GoFoodBg)
        ) {
            item { HeaderSection() }
            item { ServiceGrid(onServiceClick = onServiceClick) }
            item { WalletSection() }
            item { PromoBanners() }
            item { FoodSection() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GoFoodGreen)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "Scan QR",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            var text by remember { mutableStateOf("") }
            var defaultText = listOf("Bạn muốn đi đâu?","Hôm nay ăn gì?")
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                modifier = Modifier.weight(1f),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (text.isEmpty()) {
                            val randIndex = (0..1).random()
                            Text(text = defaultText[randIndex], color = Color.Gray, fontSize = 14.sp)
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(35.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFB300)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "W", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}
@Composable
fun ServiceGrid(onServiceClick: (String) -> Unit = {}) {
    val services = listOf(
            ServiceItemData("Đồ ăn", "🍔", Color(0xFFE0F2F1)),
            ServiceItemData("Xe máy", "🛵", Color(0xFFE0F2F1)),
            ServiceItemData("Ô tô", "🚗", Color(0xFFE0F2F1)),
            ServiceItemData("Đi chợ", "🛒", Color(0xFFE0F2F1)),
            ServiceItemData("Giao hàng", "📦", Color(0xFFE0F2F1)),
            ServiceItemData("Đi Ăn Nhà Hàng", "🍲", Color(0xFFE0F2F1)),
            ServiceItemData("Đặt xe trước", "📅", Color(0xFFE0F2F1)),
            ServiceItemData("Tất cả", "🔳", Color(0xFFE0F2F1))
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        for (row in services.chunked(4)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEach { service ->
                    ServiceItem(
                        service = service,
                        onClick = { onServiceClick(service.name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class ServiceItemData(val name: String, val icon: String, val bgColor: Color)

@Composable
fun ServiceItem(service: ServiceItemData,onClick:() -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable{onClick()}
            .padding(vertical = 4.dp),

    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(service.bgColor)
            ,
            contentAlignment = Alignment.Center
        ) {
            Text(text = service.icon, fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = service.name,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
fun WalletSection() {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "View", fontSize = 12.sp, color = Color.Gray)
                Text(text = "MoMo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            // MoMo Logo Placeholder
            Box(
                modifier = Modifier.size(50.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_momo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(shape = RoundedCornerShape(10.dp))
                )
            }
        }
    }
}

@Composable
fun PromoBanners() {
    val bannerList = listOf(
        R.drawable.banner_promo1,
        R.drawable.banner_promo2,
    )

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Săn ưu đãi ngay", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GoFoodTextDark)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(bannerList) { banner ->
                BannerItem(banner)
            }
        }
    }
}

@Composable
fun BannerItem(imageRes: Int) {
    Column(modifier = Modifier.width(280.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Da không sợ nắng, trắng mịn ngày dài",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Text(
            text = "QC - Chống nắng trắng mịn Skin Aqua",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun FoodSection() {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Đặt ăn vặt từ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF004D2C))
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(5) {
                FoodItem()
            }
        }
    }
}

@Composable
fun FoodItem() {
    Box(
        modifier = Modifier
            .size(width = 140.dp, height = 180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.LightGray)
            ) {
                Text("Food", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
            }
            // Badge placeholder
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .background(Color(0xFF00C853), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text("Mới lên sàn", color = Color.White, fontSize = 8.sp)
            }
        }
    }
}

@Composable
fun HomeBottomNavigation() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB300)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👑", fontSize = 16.sp)
                }
            },
            label = { Text("Trang chủ", fontSize = 10.sp) },
            selected = true,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFFB300),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Wallet, contentDescription = null) },
            label = { Text("Thanh toán", fontSize = 10.sp) },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Description, contentDescription = null) },
            label = { Text("Hoạt động", fontSize = 10.sp) },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = {
                BadgedBox(badge = { Badge { Text("2") } }) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null)
                }
            },
            label = { Text("Tin nhắn", fontSize = 10.sp) },
            selected = false,
            onClick = { }
        )
    }
}
