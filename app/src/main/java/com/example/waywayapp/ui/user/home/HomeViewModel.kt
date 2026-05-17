package com.example.waywayapp.ui.user.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.ui.user.booking.food.cart.FoodCartStore
import com.example.waywayapp.ui.user.home.mock.mockBanners
import com.example.waywayapp.ui.user.home.mock.mockFoods
import com.example.waywayapp.ui.user.home.mock.mockServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        observeCart()
    }

    private fun loadHomeData() {
        _uiState.update {
            it.copy(
                services = mockServices,
                banners = mockBanners,
                foods = mockFoods
            )
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            FoodCartStore.cartItems.collect { cart ->
                _uiState.update {
                    it.copy(cartItems = cart)
                }
            }
        }
    }

    fun onSearchChange(value: String) {
        _uiState.update {
            it.copy(searchText = value)
        }
    }
}