package com.example.waywayapp.data.remote.api

import com.example.waywayapp.data.remote.dto.auth.LoginRequestDto
import com.example.waywayapp.data.remote.dto.auth.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/login")
    suspend fun login(
        @Body loginRequestDto: LoginRequestDto):
            LoginResponseDto
}