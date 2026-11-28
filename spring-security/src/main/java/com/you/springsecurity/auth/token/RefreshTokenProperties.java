package com.you.springsecurity.auth.token;


import java.time.Duration;

import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.refresh-token")
public record RefreshTokenProperties(

        @NotNull Duration ttl

) {

    public RefreshTokenProperties {

        if (ttl != null && (ttl.isZero() || ttl.isNegative())) {
            throw new IllegalArgumentException("Refresh token TTL must be positive");
        }
    }
}