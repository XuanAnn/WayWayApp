package com.example.waywayapp.data.model

// Model hồ sơ người dùng lưu trong collection users để admin quản lý và phân quyền.
data class AdminUser(
    val id: String = "",
    val name: String = "",
    val age: String = "",
    val email: String = "",
    val phone: String = "",
    // Role quyết định app điều hướng sang USER, DRIVER hoặc ADMIN.
    val role: String = "USER",
    val avatarUrl: String = "",
    // Trạng thái ví MoMo UAT dùng ở luồng thanh toán demo.
    val momoLinked: Boolean = false,
    val momoPhone: String = "",
    val momoLinkedAt: Long = 0L,
    // Admin dùng field này để khoá/mở tài khoản.
    val isActive: Boolean = false,
    val updatedAt: Long = 0L,
    val createdAt: Long = 0L
)
