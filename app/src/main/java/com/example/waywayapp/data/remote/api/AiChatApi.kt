package com.example.waywayapp.data.remote.api

import com.example.waywayapp.data.remote.dto.ai.AiChatRequestDto
import com.example.waywayapp.data.remote.dto.ai.AiChatResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AiChatApi {
    @POST("api/ai/chat")
    suspend fun chat(
        @Body request: AiChatRequestDto
    ): AiChatResponseDto
}
