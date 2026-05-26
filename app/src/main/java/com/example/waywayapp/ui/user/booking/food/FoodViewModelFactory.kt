package com.example.waywayapp.ui.user.booking.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.waywayapp.data.repository.FoodRepository

class FoodViewModelFactory(
    private val repository: FoodRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}