package com.example.waywayapp.presentation.admin.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.waywayapp.data.repository.AdminPromoRepositoryImpl
import com.example.waywayapp.data.repository.AdminRepositoryImpl
import com.example.waywayapp.data.repository.RideRepositoryImpl
import com.example.waywayapp.domain.repository.AdminPromoRepository
import com.example.waywayapp.domain.repository.AdminRepository
import com.example.waywayapp.domain.repository.RideRepository

class AdminDriverViewModelFactory(
    private val adminRepository: AdminRepositoryImpl,
    private val promoRepository: AdminPromoRepositoryImpl,
    private val rideRepository: RideRepositoryImpl
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminDriverViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminDriverViewModel(adminRepository, promoRepository, rideRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}