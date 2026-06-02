package com.example.waywayapp.core.di

import android.content.Context
import com.example.waywayapp.core.database.DatabaseProvider
import com.example.waywayapp.data.repository.AdminRepository
import com.example.waywayapp.data.repository.AdminDriverRepository
import com.example.waywayapp.data.repository.DriverLocationRepository
import com.example.waywayapp.data.repository.FirebaseAuthRepository
import com.example.waywayapp.data.repository.FoodRepository

object AppContainer {

    private var foodRepository:
            FoodRepository? = null
    private var authRepository:
            FirebaseAuthRepository? = null
    private var driverLocationRepository:
            DriverLocationRepository? = null
    private var adminDriverRepository:
            AdminDriverRepository? = null
    private var adminRepository:
            AdminRepository? = null

    fun provideFoodRepository(
        context: Context
    ): FoodRepository {

        return foodRepository
            ?: synchronized(this) {

                val database =
                    DatabaseProvider
                        .getDatabase(context)

                val repo =
                    FoodRepository(
                        database.cartDao()
                    )

                foodRepository = repo

                repo
            }
    }

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
