package com.example.waywaybackend.momo.order;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderStore {
    private final Map<String, PaymentOrder> orders = new ConcurrentHashMap<>();

    public PaymentOrder upsertCreated(String orderId, long amount, String orderInfo) {
        PaymentOrder created = new PaymentOrder(
                orderId,
                amount,
                orderInfo,
                PaymentStatus.CREATED,
                null,
                null,
                null,
                Instant.now().toEpochMilli()
        );
        orders.put(orderId, created);
        return created;
    }

    public PaymentOrder updateFromIpn(String orderId, long amount, String orderInfo, PaymentStatus status, Long transId, Integer resultCode, String message) {
        PaymentOrder updated = new PaymentOrder(
                orderId,
                amount,
                orderInfo,
                status,
                transId,
                resultCode,
                message,
                Instant.now().toEpochMilli()
        );
        orders.put(orderId, updated);
        return updated;
    }

    public Optional<PaymentOrder> get(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    public PaymentOrder markPaidForDev(String orderId) {
        PaymentOrder current = orders.get(orderId);
        if (current == null) {
            throw new IllegalStateException("Order not found: " + orderId);
        }
        PaymentOrder updated = new PaymentOrder(
                current.orderId(),
                current.amount(),
                current.orderInfo(),
                PaymentStatus.PAID,
                System.currentTimeMillis(),
                0,
                "DEV confirmed payment",
                Instant.now().toEpochMilli()
        );
        orders.put(orderId, updated);
        return updated;
    }
}
