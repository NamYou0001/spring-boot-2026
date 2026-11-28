package com.you.springsecurity.auth.token;

import java.time.Instant;

public record IssuedRefreshToken(
        String token,
        Instant expiresAt,
        String familyId,
        String sessionId
) {
    public IssuedRefreshToken(String token, Instant expiresAt) {
        this(token, expiresAt, null, null);
    }
}
