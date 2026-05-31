package com.example.waywaybackend.momo.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record MomoCreateRequest(
        @NotBlank String orderId,
        @Min(1000) long amount,
        @NotBlank String orderInfo,
        String extraData,
        String userId
) {
}

