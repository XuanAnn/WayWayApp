package com.example.waywayapp.ui.auth.register

data class RegisterState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val isRegisterSuccess: Boolean = false,
    val error: String? = null
)
