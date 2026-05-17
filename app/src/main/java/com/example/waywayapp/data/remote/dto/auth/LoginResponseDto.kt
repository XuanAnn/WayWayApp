package com.example.waywayapp.data.remote.dto.auth

data class LoginResponseDto(
    val success: Boolean,
    val message: String,
    val token: String?
)