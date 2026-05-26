package com.example.waywayapp.ui.user.booking.food.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.core.di.AppContainer
import com.example.waywayapp.ui.user.booking.food.FoodViewModel
import com.example.waywayapp.ui.user.booking.food.FoodViewModelFactory
import com.example.waywayapp.ui.user.booking.food.component.QuantityControl
import java.text.DecimalFormat

@Composable
fun FoodCartScreen(
    onBackClick: () -> Unit = {},
    onPlaceOrderClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val viewModel: FoodViewModel = viewModel(
        factory = FoodViewModelFactory(
            repository = AppContainer.provideFoodRepository(
                context.applicationContext
            )
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val formatter = DecimalFormat("#,###")

    Scaffold(
        containerColor = Color(0xFFF5F7F2),
        bottomBar = {
            if (uiState.cartItems.isNotEmpty()) {
                CartCheckoutBar(
                    totalPrice = uiState.totalPrice,
                    onCheckoutClick = onPlaceOrderClick
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            CartHeader(onBackClick = onBackClick)

            if (uiState.cartItems.isEmpty()) {
                EmptyCartContent()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.cartItems) { item ->
                        CartItemCard(
                            name = item.food.name,
                            store = item.food.store,
                            imageRes = item.food.imageRes,
                            priceText = "${formatter.format(item.food.price)}đ",
                            quantity = item.quantity,
                            onAddClick = {
                                viewModel.addToCart(item.food)
                            },
                            onRemoveClick = {
                                viewModel.removeFromCart(item.food.id)
                            },
                            onDeleteClick = {
                                viewModel.onQuantityChange(
                                    foodId = item.food.id,
                                    quantity = 0
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartHeader(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = Color(0xFF20242A)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Giỏ hàng",
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF20242A)
        )
    }
}

@Composable
private fun CartItemCard(
    name: String,
    store: String,
    imageRes: Int,
    priceText: String,
    quantity: Int,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(18.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF20242A)
                )

                if (store.isNotBlank()) {
                    Text(
                        text = store,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = priceText,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF20242A)
                )

                QuantityControl(
                    quantity = quantity,
                    onAddClick = onAddClick,
                    onRemoveClick = onRemoveClick,
                    onQuantityChange = { },
                    modifier = Modifier.offset(y = (-25).dp)
                )
            }

            IconButton(
                onClick = onDeleteClick
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
private fun CartCheckoutBar(
    totalPrice: Double,
    onCheckoutClick: () -> Unit
) {
    val formatter = DecimalFormat("#,###")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(72.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF20242A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tổng thanh toán",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "${formatter.format(totalPrice)}đ",
                    color = Color(0xFFD8FF4F),
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onCheckoutClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD8FF4F),
                    contentColor = Color(0xFF20242A)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = "Đặt hàng",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyCartContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Giỏ hàng đang trống",
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
    }
}