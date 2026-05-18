package com.example.waywayapp.ui.user.booking.food.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.ui.user.booking.food.FoodViewModel
import com.example.waywayapp.ui.user.booking.food.model.FoodItemUiModel
import java.text.DecimalFormat

@Composable
fun FoodRestaurantCard(
    data: FoodItemUiModel,
    quantity: Int,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onClick: () -> Unit = {},
    onQuantityChange: (Int) -> Unit
) {
    val formatter = DecimalFormat("#,###")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 7.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Image(
                    painter = painterResource(data.imageRes),
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
                    text = "từ ${formatter.format(data.price)}đ",
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
                        text = data.rating.toString(),
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

            QuantityControl(
                quantity = quantity,
                onAddClick = onAddClick,
                onRemoveClick = onRemoveClick,
                onQuantityChange = onQuantityChange
            )
        }
    }
}

@Composable
private fun QuantityControl(
    quantity: Int,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,

    ) {
        if (quantity > 0) {
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFEFEF))
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null,
                    tint = Color(0xFF20242A),
                    modifier = Modifier.size(18.dp)
                )
            }

        }
        Spacer(Modifier.size(10.dp ))

        BasicTextField(
            value = quantity.toString(),
            onValueChange = { value ->
                val newQuantity = value.toIntOrNull()

                if (newQuantity != null) {
                    onQuantityChange(newQuantity)
                }
            },
            modifier = Modifier.size(24.dp).offset(x = 4.dp),
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
            singleLine = true
        )
        Spacer(Modifier.size(10.dp ))
        IconButton(
            onClick = onAddClick,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF20242A))
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color(0xFFD8FF4F),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}