package com.example.waywaybackend.momo.order;

public record PaymentOrder(
        String orderId,
        long amount,
        String orderInfo,
        PaymentStatus status,
        Long transId,
        Integer resultCode,
        String message,
        long updatedAt
) {
}

