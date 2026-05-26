package com.example.waywayapp.ui.user.booking.food.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.waywayapp.data.repository.FoodRepository

class FoodOrderViewModelFactory(
    private val repository: FoodRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(FoodOrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodOrderViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
