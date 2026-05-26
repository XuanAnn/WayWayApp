package com.example.waywayapp.data.model

data class AuthUser(
    val uid: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val photoUrl: String?,
    val role: String = "USER"
)
