package com.example.waywayapp.ui.user.payment

data class MomoLinkState(
    val phone: String = "",
    val isLoading: Boolean = false,
    val isLinked: Boolean = false,
    val pendingOrderId: String? = null,
    val launchUrl: String? = null,
    val verifyStatus: String = "IDLE",
    val message: String? = null,
    val error: String? = null
)
