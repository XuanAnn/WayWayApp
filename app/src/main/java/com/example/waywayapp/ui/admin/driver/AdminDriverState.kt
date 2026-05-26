package com.example.waywayapp.ui.admin.driver

import com.example.waywayapp.data.model.AdminDriver

data class AdminDriverState(
    val drivers: List<AdminDriver> = emptyList(),
    val editingDriver: AdminDriver = AdminDriver(),
    val isLoading: Boolean = true,
    val message: String? = null
)
