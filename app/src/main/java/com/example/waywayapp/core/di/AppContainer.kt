package com.example.waywayapp.core.di

import com.example.waywayapp.data.repository.AdminPromoRepositoryImpl
import com.example.waywayapp.data.repository.AdminRepositoryImpl
import com.example.waywayapp.data.repository.DriverLocationRepository
import com.example.waywayapp.data.repository.FirebaseAuthRepository
import com.example.waywayapp.data.repository.RideRepositoryImpl

object AppContainer {

    private var authRepository:
            FirebaseAuthRepository? = null
    private var driverLocationRepository:
            DriverLocationRepository? = null

    private var adminRepositoryImpl:
            AdminRepositoryImpl? = null

    private var rideRepositoryImpl:
            RideRepositoryImpl? = null
    private var adminPromoRepositoryImpl:
            AdminPromoRepositoryImpl? = null

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


    fun provideAdminRepository(): AdminRepositoryImpl {
        return adminRepositoryImpl
            ?: synchronized(this) {
                val repo = AdminRepositoryImpl()
                adminRepositoryImpl = repo
                repo
            }
    }
    fun provideRideRepository(): RideRepositoryImpl {
        return rideRepositoryImpl
            ?: synchronized(this) {
                val repo = RideRepositoryImpl()
                rideRepositoryImpl = repo
                repo
            }
    }
    fun provideAdminPromoRepository(): AdminPromoRepositoryImpl {
        return adminPromoRepositoryImpl
            ?: synchronized(this) {
                val repo = AdminPromoRepositoryImpl()
                adminPromoRepositoryImpl = repo
                repo
            }
    }
}
