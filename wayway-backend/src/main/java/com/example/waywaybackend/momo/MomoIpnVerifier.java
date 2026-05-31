package com.example.waywaybackend.momo;

import com.example.waywaybackend.momo.api.MomoIpnPayload;
import com.example.waywaybackend.momo.crypto.HmacSigner;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class MomoIpnVerifier {
    private final MomoProperties props;

    public MomoIpnVerifier(MomoProperties props) {
        this.props = props;
    }

    public boolean verify(MomoIpnPayload ipn) {
        if (ipn == null || ipn.signature() == null) return false;

        String rawSignature = "accessKey=" + props.accessKey()
                + "&amount=" + Objects.requireNonNullElse(ipn.amount(), 0L)
                + "&extraData=" + Objects.requireNonNullElse(ipn.extraData(), "")
                + "&message=" + Objects.requireNonNullElse(ipn.message(), "")
                + "&orderId=" + Objects.requireNonNullElse(ipn.orderId(), "")
                + "&orderInfo=" + Objects.requireNonNullElse(ipn.orderInfo(), "")
                + "&orderType=" + Objects.requireNonNullElse(ipn.orderType(), "")
                + "&partnerCode=" + Objects.requireNonNullElse(ipn.partnerCode(), "")
                + "&payType=" + Objects.requireNonNullElse(ipn.payType(), "")
                + "&requestId=" + Objects.requireNonNullElse(ipn.requestId(), "")
                + "&responseTime=" + Objects.requireNonNullElse(ipn.responseTime(), 0L)
                + "&resultCode=" + Objects.requireNonNullElse(ipn.resultCode(), 0)
                + "&transId=" + Objects.requireNonNullElse(ipn.transId(), 0L);

        String expected = HmacSigner.hmacSha256Hex(props.secretKey(), rawSignature);
        return expected.equalsIgnoreCase(ipn.signature());
    }
}

