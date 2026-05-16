package com.example.waywayapp.ui.user.booking.food.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import foodCategories

@Composable
fun FoodCategories() {
    Column(
        modifier = Modifier
            .offset(y = (-14).dp)
            .padding(horizontal = 18.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(foodCategories) { category ->
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