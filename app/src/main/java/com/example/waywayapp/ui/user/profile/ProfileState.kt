package com.example.waywayapp.ui.user.profile

import com.example.waywayapp.data.model.AdminDriver
import com.example.waywayapp.data.model.AdminUser

data class ProfileState(
    val user: AdminUser = AdminUser(),
    val driver: AdminDriver? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
