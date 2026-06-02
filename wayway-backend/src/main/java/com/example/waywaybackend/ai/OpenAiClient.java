package com.example.waywaybackend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
// Client gọi OpenAI Responses API khi backend có API key hợp lệ.
public class OpenAiClient {
    // WebClient gửi request HTTP đến endpoint OpenAI.
    private final WebClient webClient;
    // Cấu hình model, endpoint và API key OpenAI.
    private final AiProperties props;

    public OpenAiClient(
            WebClient.Builder builder,
            AiProperties props
    ) {
        this.webClient = builder.build();
        this.props = props;
    }

    public boolean isConfigured() {
        // Kiểm tra có key hay không trước khi gọi OpenAI.
        return props.apiKey() != null && !props.apiKey().isBlank();
    }

    public Mono<String> chat(
            String systemPrompt,
            String userMessage
    ) {
        // Payload dùng Responses API với instructions là system prompt.
        Map<String, Object> payload = Map.of(
                "model", props.model(),
                "instructions", systemPrompt,
                "input", userMessage,
                "max_output_tokens", 500
        );

        // Gửi request lên OpenAI và chuyển JSON response thành text trả lời.
        return webClient.post()
                .uri(props.endpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiKey())
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(
                                        new IllegalStateException(
                                                "OpenAI HTTP error: "
                                                        + response.statusCode()
                                                        + " "
                                                        + body
                                        )
                                ))
                )
                .bodyToMono(JsonNode.class)
                .map(this::extractText);
    }

    private String extractText(
            JsonNode root
    ) {
        // Ưu tiên output_text nếu OpenAI trả về dạng text trực tiếp.
        JsonNode outputText = root.path("output_text");
        if (outputText.isTextual() && !outputText.asText().isBlank()) {
            return outputText.asText();
        }

        // Fallback đọc mảng output/content khi response không có output_text.
        JsonNode output = root.path("output");
        if (output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.path("content");
                if (!content.isArray()) continue;
                for (JsonNode contentItem : content) {
                    JsonNode text = contentItem.path("text");
                    if (text.isTextual() && !text.asText().isBlank()) {
                        return text.asText();
                    }
                }
            }
        }

        return "Minh chua tao duoc cau tra loi luc nay. Ban thu hoi lai ngan gon hon nhe.";
    }
}
