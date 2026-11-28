package com.you.springsecurity.auth.dto;

import java.util.List;

public record RefreshResponse(
        Long userId,
        String username,
        List<String> authorities,
        String tokenType,
        String accessToken,
        long expiresIn,
        String refreshToken,
        long refreshTokenExpiresIn,
        String sessionId,
        String message
) {
    public RefreshResponse(Long userId, String username, List<String> authorities, String tokenType, String accessToken, long expiresIn, String message) {
        this(userId, username, authorities, tokenType, accessToken, expiresIn, null, 0, null, message);
    }
}
