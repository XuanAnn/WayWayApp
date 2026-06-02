package com.example.waywayapp.ui.admin.driver

import com.example.waywayapp.data.model.AdminDriver
import com.example.waywayapp.data.model.AdminUser

enum class AdminSection {
    USERS,
    DRIVERS
}

data class AdminDriverState(
    val users: List<AdminUser> = emptyList(),
    val drivers: List<AdminDriver> = emptyList(),
    val editingUser: AdminUser = AdminUser(),
    val editingDriver: AdminDriver = AdminDriver(),
    val selectedSection: AdminSection = AdminSection.USERS,
    val searchQuery: String = "",
    val isUserDetailOpen: Boolean = false,
    val isDriverDetailOpen: Boolean = false,
    val isLoadingUsers: Boolean = true,
    val isLoadingDrivers: Boolean = true,
    val isSaving: Boolean = false,
    val message: String? = null
)
