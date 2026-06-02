package com.example.waywaybackend.momo;

import com.example.waywaybackend.momo.api.MomoCreateRequest;
import com.example.waywaybackend.momo.api.MomoCreateResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/momo")
// Controller nhận request từ app Android và callback từ MoMo UAT.
public class MomoController {
    // Service tạo chữ ký và gọi MoMo gateway.
    private final MomoService momoService;
    // Bộ nhớ order demo để app Android kiểm tra trạng thái thanh toán.
    private final com.example.waywaybackend.momo.order.OrderStore orderStore;
    // Kiểm tra chữ ký IPN MoMo gửi về backend.
    private final MomoIpnVerifier ipnVerifier;

    public MomoController(MomoService momoService,
                          com.example.waywaybackend.momo.order.OrderStore orderStore,
                          MomoIpnVerifier ipnVerifier) {
        this.momoService = momoService;
        this.orderStore = orderStore;
        this.ipnVerifier = ipnVerifier;
    }

    // App Android gọi endpoint này để tạo giao dịch MoMo mới.
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<MomoCreateResponse> create(@Valid @RequestBody MomoCreateRequest req) {
        orderStore.upsertCreated(req.orderId(), req.amount(), req.orderInfo());
        return momoService.createPayment(req);
    }

    // MoMo gọi IPN về backend để báo giao dịch thành công hoặc thất bại.
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

    // App Android polling endpoint này để biết order đã PAID/FAILED/WAITING.
    @GetMapping(value = "/orders/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getOrder(@PathVariable String orderId) {
        return orderStore.get(orderId)
                .<Map<String, Object>>map(o -> Map.of("order", o))
                .orElseGet(() -> Map.of("order", null));
    }

    // Endpoint demo để đánh dấu order đã thanh toán khi không dùng app MoMo thật.
    @PostMapping(value = "/orders/{orderId}/dev-confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> devConfirm(@PathVariable String orderId) {
        return Map.of("order", orderStore.markPaidForDev(orderId));
    }

    // Redirect từ MoMo về deep link app Android sau khi người dùng thanh toán.
    @GetMapping("/return")
    public ResponseEntity<Void> returnUrl(@RequestParam Map<String, String> params) {
        String orderId = params.getOrDefault("orderId", "");
        String resultCode = params.getOrDefault("resultCode", "");
        String message = params.getOrDefault("message", "");
        String deepLink = "wayway://momo-return"
                + "?orderId=" + encode(orderId)
                + "&resultCode=" + encode(resultCode)
                + "&message=" + encode(message);

        return ResponseEntity.status(302)
                .location(URI.create(deepLink))
                .build();
    }

    // Encode query param để deep link không lỗi khi message có dấu hoặc ký tự đặc biệt.
    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
