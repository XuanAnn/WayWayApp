package com.example.waywayapp.ui.user.payment

data class MomoLinkState(
    val phone: String = "",
    val isLoading: Boolean = false,
    val isLinked: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
