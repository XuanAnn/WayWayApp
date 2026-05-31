package com.example.waywaybackend.momo.api;

public record MomoCreateResponse(
        String orderId,
        String requestId,
        long amount,
        String payUrl,
        String deeplink,
        String qrCodeUrl
) {
}

