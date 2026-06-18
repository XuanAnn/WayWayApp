package com.example.waywayapp.domain.model

data class AiChatMessage(
    val id: Long = System.nanoTime(),
    val text: String,
    val isUser: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val mode: String = ""
)
