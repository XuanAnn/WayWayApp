package com.example.waywayapp.domain.repository

import com.example.waywayapp.domain.model.AdminDriver
import com.example.waywayapp.domain.model.AdminUser
import kotlinx.coroutines.flow.Flow

interface AdminRepository {

    fun observeDrivers(): Flow<List<AdminDriver>>
    suspend fun saveDriver(driver: AdminDriver)
    suspend fun deleteDriver(driverId: String)
    suspend fun upsertDriverFromUser(user: AdminUser, now: Long)

    fun observeUsers(): Flow<List<AdminUser>>
    suspend fun saveUser(user: AdminUser)
    suspend fun deleteUser(userId: String)
}

