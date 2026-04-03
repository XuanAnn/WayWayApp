package com.example.waywayapp.data.model

data class User(
    val id: String = "",                 // Firebase UID
    val name: String = "",               // tên hiển thị
    val age: Int = 0,                    // tuổi
    val phone: String? = null,           // login bằng phone
    val email: String? = null,           // login bằng email

    val role: String = "USER",           // USER | DRIVER

    val avatarUrl: String? = null,       // ảnh đại diện

    val createdAt: Long = 0L,            // thời điểm tạo tài khoản
    val updatedAt: Long = 0L,            // cập nhật gần nhất

    val isActive: Boolean = true,        // tài khoản còn hoạt động

    // liên quan đến ride
    val currentRideId: String? = null,   // cuốc hiện tại (nếu có)

    val totalRides: Int = 0              // tổng số cuốc đã đi
)