package com.example.waywayapp.ui.user.booking.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.ui.user.booking.food.components.FoodCategories
import com.example.waywayapp.ui.user.booking.food.components.FoodHeader
import com.example.waywayapp.ui.user.booking.food.component.FoodRestaurantCard
import com.example.waywayapp.ui.user.booking.food.component.PromoFoodBanners
import com.example.waywayapp.ui.user.booking.food.components.FoodCartBottomBar

@Composable
fun FoodBookingScreen(
    selectedFoodId: Int = 0,
    viewModel: FoodViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(selectedFoodId) {

        if (selectedFoodId != 0) {

            viewModel.addFoodToCartById(
                selectedFoodId
            )
        }
    }
    Scaffold(
        containerColor = Color(0xF2F3FFFF)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()).background(Color(0xF2EBFFFF))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    FoodHeader(
                        onBackClick = onBackClick,
                        onCartClick = {}
                    )
                }

                item { FoodCategories() }
                item { PromoFoodBanners() }

                items(uiState.foods) { food ->
                    FoodRestaurantCard(
                        data = food,
                        onClick = {
                            viewModel.addToCart(food)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }

            if (uiState.cartItems.isNotEmpty()) {
                FoodCartBottomBar(
                    totalQuantity = uiState.totalQuantity,
                    totalPrice = uiState.totalPrice,
                    onCartClick = {}
                )
            }
        }
    }
}