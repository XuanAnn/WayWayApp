package com.example.waywayapp.ui.auth.login

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val role: String = "USER",
    val error: String? = null
)
