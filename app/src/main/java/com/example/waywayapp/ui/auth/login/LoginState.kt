package com.example.waywayapp.ui.auth.login

data class LoginState(
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val otpCode: String = "",
    val verificationId: String? = null,
    val isOtpSent: Boolean = false,
    val isForgotPasswordMode: Boolean = false,
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val role: String = "USER",
    val error: String? = null,
    val message: String? = null
)
