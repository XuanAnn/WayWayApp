package com.example.waywayapp.ui.auth.register

data class RegisterState(
    val fullName: String = "",
    val age: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val otpCode: String = "",
    val verificationId: String? = null,
    val isOtpSent: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val isLoading: Boolean = false,
    val isRegisterSuccess: Boolean = false,
    val error: String? = null
)
