package com.example.waywaybackend.momo;

import com.example.waywaybackend.momo.api.MomoCreateRequest;
import com.example.waywaybackend.momo.api.MomoCreateResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/momo")
public class MomoController {
    private final MomoService momoService;
    private final com.example.waywaybackend.momo.order.OrderStore orderStore;
    private final MomoIpnVerifier ipnVerifier;

    public MomoController(MomoService momoService,
                          com.example.waywaybackend.momo.order.OrderStore orderStore,
                          MomoIpnVerifier ipnVerifier) {
        this.momoService = momoService;
        this.orderStore = orderStore;
        this.ipnVerifier = ipnVerifier;
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<MomoCreateResponse> create(@Valid @RequestBody MomoCreateRequest req) {
        orderStore.upsertCreated(req.orderId(), req.amount(), req.orderInfo());
        return momoService.createPayment(req);
    }

    @PostMapping(value = "/ipn", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> ipn(@RequestBody com.example.waywaybackend.momo.api.MomoIpnPayload payload) {
        boolean ok = ipnVerifier.verify(payload);
        if (!ok) {
            return Map.of("status", "invalid_signature");
        }

        int resultCode = payload.resultCode() == null ? -1 : payload.resultCode();
        com.example.waywaybackend.momo.order.PaymentStatus status =
                resultCode == 0 ? com.example.waywaybackend.momo.order.PaymentStatus.PAID : com.example.waywaybackend.momo.order.PaymentStatus.FAILED;

        orderStore.updateFromIpn(
                payload.orderId(),
                payload.amount() == null ? 0L : payload.amount(),
                payload.orderInfo(),
                status,
                payload.transId(),
                payload.resultCode(),
                payload.message()
        );

        return Map.of("status", "ok");
    }

    @GetMapping(value = "/orders/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getOrder(@PathVariable String orderId) {
        return orderStore.get(orderId)
                .<Map<String, Object>>map(o -> Map.of("order", o))
                .orElseGet(() -> Map.of("order", null));
    }

    @PostMapping(value = "/orders/{orderId}/dev-confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> devConfirm(@PathVariable String orderId) {
        return Map.of("order", orderStore.markPaidForDev(orderId));
    }

    @GetMapping("/return")
    public Map<String, Object> returnUrl(@RequestParam Map<String, String> params) {
        // For debugging redirects; real confirmation should use IPN verification.
        return Map.of(
                "received", params
        );
    }
}
