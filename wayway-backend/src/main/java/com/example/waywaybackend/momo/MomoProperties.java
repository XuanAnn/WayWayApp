package com.example.waywaybackend.momo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wayway.momo")
public record MomoProperties(
        String endpoint,
        String partnerCode,
        String accessKey,
        String secretKey,
        String redirectUrl,
        String ipnUrl,
        String requestType,
        String lang
) {
}

