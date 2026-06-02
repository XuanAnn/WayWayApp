package com.example.waywaybackend.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wayway.ai")
public record AiProperties(
        String provider,
        String endpoint,
        String apiKey,
        String model,
        String ollamaEndpoint,
        String ollamaModel
) {
}
