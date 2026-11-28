package com.you.springsecurity.auth.token;

import java.time.Duration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(

        @NotBlank String issuer,

        @NotBlank String audience,

        @NotNull Duration accessTokenTtl,

        @NotBlank String secretBase64) {

    public JwtProperties {

        if (accessTokenTtl != null && (accessTokenTtl.isNegative() || accessTokenTtl.isZero())) {

            throw new IllegalArgumentException("JWT access token TTL must be positive");
        }

    }
}