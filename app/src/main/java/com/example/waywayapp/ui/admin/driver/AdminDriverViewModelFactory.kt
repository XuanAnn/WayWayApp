package com.example.waywayapp.ui.admin.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.waywayapp.data.repository.AdminRepository

class AdminDriverViewModelFactory(
    private val repository: AdminRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(AdminDriverViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminDriverViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
