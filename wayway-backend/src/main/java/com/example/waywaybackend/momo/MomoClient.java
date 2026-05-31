package com.example.waywaybackend.momo;

import com.example.waywaybackend.momo.api.MomoGatewayCreatePayload;
import com.example.waywaybackend.momo.api.MomoGatewayCreateResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class MomoClient {
    private final WebClient webClient;
    private final MomoProperties props;
    private final ObjectMapper objectMapper;

    public MomoClient(
            WebClient.Builder builder,
            MomoProperties props,
            ObjectMapper objectMapper
    ) {
        this.webClient = builder.build();
        this.props = props;
        this.objectMapper = objectMapper;
    }

    public Mono<MomoGatewayCreateResult> create(
            MomoGatewayCreatePayload payload
    ) {
        logPayload(payload);

        return webClient.post()
                .uri(props.endpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body ->
                                        Mono.error(
                                                new IllegalStateException(
                                                        "MoMo HTTP error: "
                                                                + resp.statusCode()
                                                                + " "
                                                                + body
                                                )
                                        )
                                )
                )
                .bodyToMono(MomoGatewayCreateResult.class)
                .doOnNext(result -> {
                    System.out.println("MOMO resultCode = " + result.resultCode());
                    System.out.println("MOMO message = " + result.message());
                    System.out.println("MOMO payUrl = " + result.payUrl());
                    System.out.println("MOMO deeplink = " + result.deeplink());
                })
                .onErrorMap(WebClientResponseException.class, e ->
                        new IllegalStateException(
                                "MoMo call failed: "
                                        + e.getStatusCode()
                                        + " "
                                        + e.getResponseBodyAsString(),
                                e
                        )
                );
    }

    private void logPayload(
            MomoGatewayCreatePayload payload
    ) {
        try {
            System.out.println(
                    "MOMO payload JSON = "
                            + objectMapper.writeValueAsString(payload)
            );
        } catch (JsonProcessingException e) {
            System.out.println(
                    "Cannot serialize MOMO payload: " + e.getMessage()
            );
        }
    }
}