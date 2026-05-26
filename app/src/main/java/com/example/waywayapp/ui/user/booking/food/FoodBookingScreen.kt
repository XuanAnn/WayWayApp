package com.example.waywayapp.ui.user.booking.food

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.ui.user.booking.food.components.FoodCategories
import com.example.waywayapp.ui.user.booking.food.components.FoodHeader
import com.example.waywayapp.ui.user.booking.food.component.FoodRestaurantCard
import com.example.waywayapp.ui.user.booking.food.component.PromoFoodBanners
import com.example.waywayapp.ui.user.booking.food.components.FoodCartBottomBar
import androidx.compose.ui.platform.LocalContext
import com.example.waywayapp.core.di.AppContainer

@Composable
fun FoodBookingScreen(
    selectedFoodId: Int = 0,
    onBackClick: () -> Unit = {},
    onCartClick: () -> Unit = {}
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


    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {

            Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT
            ).show()

            viewModel.clearError()
        }
    }

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
                        onCartClick = onCartClick
                    )
                }

                item { FoodCategories() }
                item { PromoFoodBanners() }

                items(uiState.foods) { food ->

                    val quantity =
                        uiState.cartItems
                            .find { it.food.id == food.id }
                            ?.quantity ?: 0

                    FoodRestaurantCard(
                        data = food,
                        quantity = quantity,
                        onAddClick = {
                            viewModel.addToCart(food)
                        },
                        onRemoveClick = {
                            viewModel.removeFromCart(food.id)
                        },
                        onQuantityChange = { newQuantity ->
                            viewModel.onQuantityChange(
                                foodId = food.id,
                                quantity = newQuantity
                            )
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
                    onCartClick = onCartClick,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(
                            start = 16.dp,
                            bottom = 18.dp
                        )
                        .width(220.dp)
                )

            }
        }
    }
}