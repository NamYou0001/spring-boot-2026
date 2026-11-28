package com.you.springsecurity.auth.token;

import java.time.Instant;

public record IssuedAccessToken(
        String token,
        Instant expiresAt
) {}