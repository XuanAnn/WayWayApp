package com.example.waywayapp.ui.user.booking.food.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat

@Composable
fun FoodCartBottomBar(
    totalQuantity: Int,
    totalPrice: Double,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DecimalFormat("#,###")

    Card(
        modifier = modifier
            .width(250.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(58.dp)
            .clickable { onCartClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF20242A)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = Color(0xFFD8FF4F)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "$totalQuantity món",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${formatter.format(totalPrice)}đ",
                color = Color(0xFFD8FF4F),
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}