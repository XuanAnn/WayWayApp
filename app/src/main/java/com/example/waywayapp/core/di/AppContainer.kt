package com.example.waywayapp.core.di

import com.example.waywayapp.data.repository.AdminRepository
import com.example.waywayapp.data.repository.AdminDriverRepository
import com.example.waywayapp.data.repository.DriverLocationRepository
import com.example.waywayapp.data.repository.FirebaseAuthRepository

object AppContainer {

    private var authRepository:
            FirebaseAuthRepository? = null
    private var driverLocationRepository:
            DriverLocationRepository? = null
    private var adminDriverRepository:
            AdminDriverRepository? = null
    private var adminRepository:
            AdminRepository? = null

    fun provideAuthRepository(): FirebaseAuthRepository {
        return authRepository
            ?: synchronized(this) {
                val repo = FirebaseAuthRepository()
                authRepository = repo
                repo
            }
    }

    fun provideDriverLocationRepository(): DriverLocationRepository {
        return driverLocationRepository
            ?: synchronized(this) {
                val repo = DriverLocationRepository()
                driverLocationRepository = repo
                repo
            }
    }

    fun provideAdminDriverRepository(): AdminDriverRepository {
        return adminDriverRepository
            ?: synchronized(this) {
                val repo = AdminDriverRepository()
                adminDriverRepository = repo
                repo
            }
    }

    fun provideAdminRepository(): AdminRepository {
        return adminRepository
            ?: synchronized(this) {
                val repo = AdminRepository()
                adminRepository = repo
                repo
            }
    }
}
