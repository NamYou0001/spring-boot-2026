package com.you.springsecurity.jwt;

import java.time.Instant;

public record IssuedAccessToken(
        String token,
        Instant expiresAt
) {}