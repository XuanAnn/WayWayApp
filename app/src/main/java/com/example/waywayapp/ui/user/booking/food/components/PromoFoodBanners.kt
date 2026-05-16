package com.example.waywayapp.ui.user.booking.food.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.waywayapp.ui.user.home.SectionTitle
import foodBanners

@Composable
fun PromoFoodBanners() {
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
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 5.dp
                    )
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