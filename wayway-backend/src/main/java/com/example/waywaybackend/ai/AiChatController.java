package com.example.waywaybackend.ai;

import com.example.waywaybackend.ai.api.AiChatRequest;
import com.example.waywaybackend.ai.api.AiChatResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {
    private final AiChatService service;

    public AiChatController(
            AiChatService service
    ) {
        this.service = service;
    }

    @PostMapping("/chat")
    public Mono<AiChatResponse> chat(
            @Valid @RequestBody AiChatRequest request
    ) {
        return service.chat(request);
    }
}
