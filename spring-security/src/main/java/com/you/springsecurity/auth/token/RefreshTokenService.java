package com.you.springsecurity.auth.token;

import com.you.springsecurity.auth.exception.InvalidRefreshTokenException;
import com.you.springsecurity.auth.exception.TokenReuseException;
import com.you.springsecurity.user.model.Users;
import com.you.springsecurity.user.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    private final RefreshTokenProperties properties;

    private final SecureRandom secureRandom;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, RefreshTokenProperties properties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.properties = properties;
        this.secureRandom = new SecureRandom();
    }

    public record RotatedTokenResult(IssuedRefreshToken issuedRefreshToken, Users user) {
    }

    // ==========================================
    // 1. CREATE INITIAL REFRESH TOKEN
    // ==========================================

    @Transactional
    public IssuedRefreshToken createForUser(Long userId, String deviceInfo, String ipAddress) {

        Users user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User not found with id: " + userId));

        String rawToken = generateRawToken();

        String tokenHash = hashToken(rawToken);

        Instant expiresAt = Instant.now().plus(properties.ttl());

        String familyId = UUID.randomUUID().toString();

        String sessionId = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken(user, tokenHash, familyId, sessionId, deviceInfo != null ? deviceInfo : "Unknown Device", ipAddress != null ? ipAddress : "0.0.0.0", expiresAt);

        refreshTokenRepository.save(refreshToken);

        return new IssuedRefreshToken(rawToken, expiresAt, familyId, sessionId);
    }

    @Transactional
    public IssuedRefreshToken createForUser(Long userId) {

        return createForUser(userId, "Unknown Device", "0.0.0.0");
    }

    // ==========================================
    // 2. REFRESH TOKEN ROTATION
    // ==========================================

    @Transactional(noRollbackFor = TokenReuseException.class)
    public RotatedTokenResult rotateRefreshToken(String rawToken, String clientDevice, String clientIp) {

        // Step 1: Validate raw refresh token

        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token cannot be empty");
        }

        // Step 2: Hash refresh token

        String tokenHash = hashToken(rawToken);

        // Step 3: Find token with database row lock

        RefreshToken storedToken = refreshTokenRepository.findByTokenHashWithUserForUpdate(tokenHash).orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        Instant now = Instant.now();

        // Step 4: Detect refresh token reuse

        if (storedToken.isRevoked()) {

            log.warn("SECURITY ALERT: Refresh token reuse detected. " + "Token ID: {}, Family ID: {}, User: {}", storedToken.getId(), storedToken.getFamilyId(), storedToken.getUser().getUsername());

            // Revoke all active tokens in the same family

            refreshTokenRepository.revokeFamily(storedToken.getFamilyId(), now, "REUSE_DETECTED");

            // noRollbackFor makes sure this revocation is committed.

            throw new TokenReuseException("Refresh token reuse detected. All sessions " + "associated with this token family have been revoked.", storedToken.getFamilyId());
        }

        // Step 5: Check expiration

        if (storedToken.isExpired(now)) {

            throw new InvalidRefreshTokenException("Refresh token has expired. Please login again.");
        }

        // Step 6: Check user account status

        Users user = storedToken.getUser();

        if (!user.isEnable() || !user.isAccountNonlocked()) {

            throw new InvalidRefreshTokenException("User account is inactive or locked");
        }

        // Step 7: Generate new refresh token

        String newRawToken = generateRawToken();

        String newTokenHash = hashToken(newRawToken);

        Instant newExpiresAt = now.plus(properties.ttl());

        // Step 8: Keep the same session and token family

        String deviceInfo = clientDevice != null ? clientDevice : storedToken.getDeviceInfo();

        String ipAddress = clientIp != null ? clientIp : storedToken.getIpAddress();

        RefreshToken newRefreshToken = new RefreshToken(user, newTokenHash, storedToken.getFamilyId(), storedToken.getSessionId(), deviceInfo, ipAddress, newExpiresAt);

        // Step 9: Revoke old token and link replacement

        storedToken.replaceWith(newTokenHash, now);

        // Step 10: Save old and new refresh tokens

        refreshTokenRepository.save(storedToken);

        refreshTokenRepository.save(newRefreshToken);

        // Step 11: Create response token

        IssuedRefreshToken issuedRefreshToken = new IssuedRefreshToken(newRawToken, newExpiresAt, storedToken.getFamilyId(), storedToken.getSessionId());

        // Step 12: Return new token and user

        return new RotatedTokenResult(issuedRefreshToken, user);
    }

    // ==========================================
    // 3. VALIDATE REFRESH TOKEN
    // ==========================================

    @Transactional(readOnly = true)
    public RefreshToken validate(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {

            throw new InvalidRefreshTokenException("Refresh token is required");
        }

        String tokenHash = hashToken(rawToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHashWithUser(tokenHash).orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        Instant now = Instant.now();

        if (storedToken.isExpired(now)) {

            throw new InvalidRefreshTokenException("Refresh token is expired");
        }

        if (storedToken.isRevoked()) {

            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }

        Users user = storedToken.getUser();

        if (!user.isEnable() || !user.isAccountNonlocked()) {

            throw new InvalidRefreshTokenException("User account is inactive or locked");
        }

        return storedToken;
    }

    // ==========================================
    // 4. LOGOUT CURRENT SESSION
    // ==========================================

    @Transactional
    public void revokeToken(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        String tokenHash = hashToken(rawToken);

        refreshTokenRepository.findByTokenHashWithUser(tokenHash).ifPresent(token -> {

            if (!token.isRevoked()) {

                token.revoke(Instant.now(), "USER_LOGOUT");

                refreshTokenRepository.save(token);
            }
        });
    }

    // ==========================================
    // 5. LOGOUT ALL USER SESSIONS
    // ==========================================

    @Transactional
    public void revokeAllForUser(Long userId) {

        refreshTokenRepository.revokeAllByUserId(userId, Instant.now(), "LOGOUT_ALL");
    }

    // ==========================================
    // 6. TERMINATE SPECIFIC SESSION
    // ==========================================

    @Transactional
    public void revokeSession(Long userId, String sessionId) {

        RefreshToken token = refreshTokenRepository.findBySessionId(sessionId).orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!token.getUser().getId().equals(userId)) {

            throw new IllegalArgumentException("Session does not belong to the authenticated user");
        }

        refreshTokenRepository.revokeBySessionId(sessionId, Instant.now(), "SESSION_TERMINATED");
    }

    // ==========================================
    // 7. GET ACTIVE USER SESSIONS
    // ==========================================

//    @Transactional(readOnly = true)
//    public List<SessionResponse> getActiveSessions(Long userId) {
//
//        return refreshTokenRepository.findActiveSessionsByUserId(userId, Instant.now()).stream().map(token -> new SessionResponse(token.getSessionId(), token.getDeviceInfo(), token.getIpAddress(), token.getCreatedAt(), token.getExpiresAt())).toList();
//    }

    // ==========================================
    // 8. GENERATE SECURE RANDOM TOKEN
    // ==========================================

    private String generateRawToken() {

        byte[] randomBytes = new byte[64];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    // ==========================================
    // 9. HASH REFRESH TOKEN USING SHA-256
    // ==========================================

    private String hashToken(String rawToken) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }
}