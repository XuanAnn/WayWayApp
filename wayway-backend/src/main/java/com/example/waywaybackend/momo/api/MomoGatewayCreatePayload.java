package com.example.waywaybackend.momo.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MomoGatewayCreatePayload(
        String partnerCode,
        String partnerName,
        String storeId,
        String requestId,
        long amount,
        String orderId,
        String orderInfo,
        String redirectUrl,
        String ipnUrl,
        String lang,
        String requestType,
        String extraData,
        String signature
) {
}

