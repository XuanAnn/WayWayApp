package com.example.waywayapp.data.model

// Model user sau khi đăng nhập Firebase, dùng để đồng bộ sang Firestore users.
data class AuthUser(
    val uid: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val photoUrl: String?,
    // Role lấy từ users/{uid} hoặc ADMIN_EMAILS để điều hướng màn hình.
    val role: String = "USER"
)
