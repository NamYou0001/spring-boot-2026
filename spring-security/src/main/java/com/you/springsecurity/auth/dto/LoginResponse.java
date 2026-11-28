package com.you.springsecurity.auth.dto;

import java.util.List;

public record LoginResponse(
        Long userId,
        String username,
        List<String> authorities,
        String tokenType,
        String accessToken,
        long expiresIn,
        String refreshToken,
        long refreshTokenExpiresIn,
        String sessionId,
        boolean authenticated,
        String message
) {
    public LoginResponse(Long userId, String username, List<String> authorities, String tokenType, String accessToken, long expiresIn, String refreshToken, long refreshTokenExpiresIn, boolean authenticated, String message) {
        this(userId, username, authorities, tokenType, accessToken, expiresIn, refreshToken, refreshTokenExpiresIn, null, authenticated, message);
    }
}
