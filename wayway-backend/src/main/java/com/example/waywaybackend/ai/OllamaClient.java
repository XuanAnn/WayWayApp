package com.example.waywaybackend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
// Client gọi Ollama local để backend có AI miễn phí không cần quota OpenAI.
public class OllamaClient {
    // WebClient gửi HTTP request đến Ollama server.
    private final WebClient webClient;
    // Cấu hình endpoint và model Ollama trong application.yml.
    private final AiProperties props;

    public OllamaClient(
            WebClient.Builder builder,
            AiProperties props
    ) {
        this.webClient = builder.build();
        this.props = props;
    }

    public Mono<String> chat(
            String systemPrompt,
            String userMessage
    ) {
        // Payload theo chuẩn /api/chat của Ollama gồm system prompt và câu hỏi user.
        Map<String, Object> payload = Map.of(
                "model", props.ollamaModel(),
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                )
        );

        // Gọi Ollama local và lấy nội dung trả lời trong message.content.
        return webClient.post()
                .uri(props.ollamaEndpoint() + "/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(
                                        new IllegalStateException(
                                                "Ollama HTTP error: "
                                                        + response.statusCode()
                                                        + " "
                                                        + body
                                        )
                                ))
                )
                .bodyToMono(JsonNode.class)
                .map(root -> root.path("message").path("content").asText())
                .filter(reply -> !reply.isBlank())
                .switchIfEmpty(Mono.error(new IllegalStateException("Ollama returned empty reply")));
    }
}
