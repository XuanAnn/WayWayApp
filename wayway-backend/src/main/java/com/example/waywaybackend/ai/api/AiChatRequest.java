package com.example.waywaybackend.ai.api;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
        @NotBlank
        @Size(max = 2000)
        String message,
        String role,
        String locale,
        JsonNode context
) {
}
