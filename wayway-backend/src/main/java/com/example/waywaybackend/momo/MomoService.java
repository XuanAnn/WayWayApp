package com.example.waywaybackend.momo;

import com.example.waywaybackend.momo.api.MomoCreateRequest;
import com.example.waywaybackend.momo.api.MomoCreateResponse;
import com.example.waywaybackend.momo.api.MomoGatewayCreatePayload;
import com.example.waywaybackend.momo.api.MomoGatewayCreateResult;
import com.example.waywaybackend.momo.crypto.HmacSigner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Objects;

@Service
public class MomoService {
    private final MomoProperties props;
    private final MomoClient client;

    public MomoService(MomoProperties props, MomoClient client) {
        this.props = props;
        this.client = client;
    }

    public Mono<MomoCreateResponse> createPayment(MomoCreateRequest req) {
        requireConfigured();

        String requestId = req.orderId() + "-" + Instant.now().toEpochMilli();
        String extraData = Objects.requireNonNullElse(req.extraData(), "");

        String rawSignature = "accessKey=" + props.accessKey()
                + "&amount=" + req.amount()
                + "&extraData=" + extraData
                + "&ipnUrl=" + props.ipnUrl()
                + "&orderId=" + req.orderId()
                + "&orderInfo=" + req.orderInfo()
                + "&partnerCode=" + props.partnerCode()
                + "&redirectUrl=" + props.redirectUrl()
                + "&requestId=" + requestId
                + "&requestType=" + props.requestType();

        String signature = HmacSigner.hmacSha256Hex(props.secretKey(), rawSignature);

        MomoGatewayCreatePayload payload = new MomoGatewayCreatePayload(
                props.partnerCode(),
                "WayWay",
                "WayWay",
                requestId,
                req.amount(),
                req.orderId(),
                req.orderInfo(),
                props.redirectUrl(),
                props.ipnUrl(),
                props.lang(),
                props.requestType(),
                extraData,
                signature
        );
        System.out.println("MOMO rawSignature = " + rawSignature);
        System.out.println("MOMO signature = " + signature);
        System.out.println("MOMO requestType = " + props.requestType());
        System.out.println("MOMO redirectUrl = " + props.redirectUrl());
        System.out.println("MOMO ipnUrl = " + props.ipnUrl());
        return client.create(payload)
                .map(result -> mapCreateResult(req, requestId, result));
    }

    private MomoCreateResponse mapCreateResult(MomoCreateRequest req, String requestId, MomoGatewayCreateResult result) {
        if (result == null) {
            throw new IllegalStateException("MoMo returned empty response");
        }
        if (result.resultCode() != 0) {
            throw new IllegalStateException("MoMo create failed: " + result.resultCode() + " - " + result.message());
        }
        return new MomoCreateResponse(
                req.orderId(),
                requestId,
                req.amount(),
                result.payUrl(),
                result.deeplink(),
                result.qrCodeUrl()
        );
    }

    private void requireConfigured() {
        if (isBlank(props.accessKey()) || isBlank(props.secretKey()) || isBlank(props.partnerCode())) {
            throw new IllegalStateException("MoMo keys are not configured. Set MOMO_PARTNER_CODE, MOMO_ACCESS_KEY, MOMO_SECRET_KEY.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

