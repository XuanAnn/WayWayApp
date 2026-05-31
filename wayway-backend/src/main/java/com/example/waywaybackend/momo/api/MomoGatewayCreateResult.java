package com.example.waywaybackend.momo.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MomoGatewayCreateResult(
        int resultCode,
        String message,
        String payUrl,
        String deeplink,
        String qrCodeUrl,
        String orderId,
        String requestId,
        Long amount,
        String signature
) {
}

