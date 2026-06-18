package com.example.waywayapp.presentation.user.profile

import com.example.waywayapp.domain.model.AdminDriver
import com.example.waywayapp.domain.model.AdminUser

data class ProfileState(
    val user: AdminUser = AdminUser(),
    val driver: AdminDriver? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val role: String = ""
)
