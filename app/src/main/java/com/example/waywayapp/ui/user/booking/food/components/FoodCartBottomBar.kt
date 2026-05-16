package com.example.waywayapp.ui.user.booking.food.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

@Composable
fun FoodCartBottomBar(
    totalQuantity: Int,
    totalPrice: Double,
    onCartClick: () -> Unit,
) {
    val formatter = DecimalFormat("#,###")

    Card(
        modifier = Modifier
            .padding(16.dp)
            .wrapContentWidth().offset(y = 700.dp),
        shape = RoundedCornerShape(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF20242A)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        )
    ) {
        Row(
            modifier = Modifier
                .clickable { onCartClick() }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD8FF4F)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = totalQuantity.toString(),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${formatter.format(totalPrice)}đ",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
