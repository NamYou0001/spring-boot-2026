//package com.you.springsecurity.token;
//
//import com.you.springsecurity.auth.token.RefreshTokenProperties;
//import com.you.springsecurity.auth.dto.SessionResponse;
//import com.you.springsecurity.auth.exception.InvalidRefreshTokenException;
//import com.you.springsecurity.auth.exception.TokenReuseException;
//import com.you.springsecurity.auth.token.RefreshToken;
//import com.you.springsecurity.user.model.Roles;
//import com.you.springsecurity.user.model.Users;
//import com.you.springsecurity.auth.token.RefreshTokenRepository;
//import com.you.springsecurity.user.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.HexFormat;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class RefreshTokenServiceTest {
//
//    @Mock
//    private RefreshTokenRepository refreshTokenRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    private RefreshTokenProperties properties;
//    private RefreshTokenService refreshTokenService;
//    private Users testUser;
//
//    @BeforeEach
//    void setUp() {
//        properties = new RefreshTokenProperties(Duration.ofDays(7));
//        refreshTokenService = new RefreshTokenService(refreshTokenRepository, userRepository, properties);
//
//        testUser = new Users("testuser", "test@example.com", "encodedPassword", Roles.USERS);
//        testUser.setId(1L);
//    }
//
//    private String hashToken(String rawToken) {
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
//            return HexFormat.of().formatHex(hash);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Test
//    @DisplayName("1. Successful initial refresh token generation")
//    void testCreateForUser_Success() {
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
//
//        IssuedRefreshToken result = refreshTokenService.createForUser(1L, "Chrome on Windows", "192.168.1.10");
//
//        assertThat(result).isNotNull();
//        assertThat(result.token()).isNotBlank();
//        assertThat(result.familyId()).isNotBlank();
//        assertThat(result.sessionId()).isNotBlank();
//        assertThat(result.expiresAt()).isAfter(Instant.now());
//
//        verify(refreshTokenRepository).save(any(RefreshToken.class));
//    }
//
//    @Test
//    @DisplayName("2. Successful token rotation generates new token and links family")
//    void testRotateRefreshToken_Success() {
//        String rawToken = "raw-refresh-token-xyz";
//        String tokenHash = hashToken(rawToken);
//        String familyId = "family-123";
//        String sessionId = "session-abc";
//
//        RefreshToken storedToken = new RefreshToken(
//                testUser,
//                tokenHash,
//                familyId,
//                sessionId,
//                "Firefox / Linux",
//                "10.0.0.1",
//                Instant.now().plus(Duration.ofDays(7))
//        );
//
//        when(refreshTokenRepository.findByTokenHashWithUser(tokenHash)).thenReturn(Optional.of(storedToken));
//
//        RefreshTokenService.RotatedTokenResult result = refreshTokenService.rotateRefreshToken(
//                rawToken,
//                "Firefox / Linux Updated",
//                "10.0.0.2"
//        );
//
//        assertThat(result).isNotNull();
//        assertThat(result.user()).isEqualTo(testUser);
//        assertThat(result.issuedRefreshToken().token()).isNotEqualTo(rawToken);
//        assertThat(result.issuedRefreshToken().familyId()).isEqualTo(familyId);
//        assertThat(result.issuedRefreshToken().sessionId()).isEqualTo(sessionId);
//
//        // Verify previous token is invalidated (ROTATED)
//        assertThat(storedToken.isRevoked()).isTrue();
//        assertThat(storedToken.getRevokedReason()).isEqualTo("ROTATED");
//        assertThat(storedToken.getReplacedByTokenHash()).isNotNull();
//
//        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
//    }
//
//    @Test
//    @DisplayName("3. Token reuse detection: reusing a revoked token revokes entire token family")
//    void testRotateRefreshToken_ReuseDetection() {
//        String rawToken = "already-revoked-token";
//        String tokenHash = hashToken(rawToken);
//        String familyId = "compromised-family-999";
//
//        RefreshToken revokedToken = new RefreshToken(
//                testUser,
//                tokenHash,
//                familyId,
//                "session-xyz",
//                "Mobile Safari",
//                "1.1.1.1",
//                Instant.now().plus(Duration.ofDays(5))
//        );
//        revokedToken.revoke(Instant.now().minusSeconds(60), "ROTATED");
//
//        when(refreshTokenRepository.findByTokenHashWithUser(tokenHash)).thenReturn(Optional.of(revokedToken));
//
//        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(rawToken, "Attacker Browser", "2.2.2.2"))
//                .isInstanceOf(TokenReuseException.class)
//                .hasMessageContaining("Token reuse detected");
//
//        // Verify that the whole family was revoked
//        verify(refreshTokenRepository).revokeFamily(eq(familyId), any(Instant.class), eq("REUSE_DETECTED"));
//    }
//
//    @Test
//    @DisplayName("4. Expired refresh token rejection")
//    void testRotateRefreshToken_ExpiredToken() {
//        String rawToken = "expired-token";
//        String tokenHash = hashToken(rawToken);
//
//        RefreshToken expiredToken = new RefreshToken(
//                testUser,
//                tokenHash,
//                "fam-1",
//                "sess-1",
//                "Chrome",
//                "127.0.0.1",
//                Instant.now().minusSeconds(3600) // expired 1 hour ago
//        );
//
//        when(refreshTokenRepository.findByTokenHashWithUser(tokenHash)).thenReturn(Optional.of(expiredToken));
//
//        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(rawToken, "Chrome", "127.0.0.1"))
//                .isInstanceOf(InvalidRefreshTokenException.class)
//                .hasMessageContaining("expired");
//
//        verify(refreshTokenRepository, never()).revokeFamily(any(), any(), any());
//    }
//
//    @Test
//    @DisplayName("5. Logout single session revokes current token")
//    void testRevokeToken_LogoutCurrentSession() {
//        String rawToken = "active-logout-token";
//        String tokenHash = hashToken(rawToken);
//
//        RefreshToken activeToken = new RefreshToken(
//                testUser,
//                tokenHash,
//                "fam-logout",
//                "sess-logout",
//                "Edge",
//                "192.168.1.50",
//                Instant.now().plus(Duration.ofDays(3))
//        );
//
//        when(refreshTokenRepository.findByTokenHashWithUser(tokenHash)).thenReturn(Optional.of(activeToken));
//
//        refreshTokenService.revokeToken(rawToken);
//
//        assertThat(activeToken.isRevoked()).isTrue();
//        assertThat(activeToken.getRevokedReason()).isEqualTo("USER_LOGOUT");
//        verify(refreshTokenRepository).save(activeToken);
//    }
//
//    @Test
//    @DisplayName("6. Logout all sessions revokes all tokens for user")
//    void testRevokeAllForUser() {
//        refreshTokenService.revokeAllForUser(1L);
//
//        verify(refreshTokenRepository).revokeAllByUserId(eq(1L), any(Instant.class), eq("LOGOUT_ALL"));
//    }
//
//    @Test
//    @DisplayName("7. Multiple device sessions listing")
//    void testGetActiveSessions() {
//        RefreshToken token1 = new RefreshToken(testUser, "hash1", "fam1", "sess1", "iPhone 15", "10.0.0.1", Instant.now().plus(Duration.ofDays(6)));
//        RefreshToken token2 = new RefreshToken(testUser, "hash2", "fam2", "sess2", "MacBook Pro", "10.0.0.2", Instant.now().plus(Duration.ofDays(7)));
//
//        when(refreshTokenRepository.findActiveSessionsByUserId(eq(1L), any(Instant.class)))
//                .thenReturn(List.of(token1, token2));
//
//        List<SessionResponse> sessions = refreshTokenService.getActiveSessions(1L);
//
//        assertThat(sessions).hasSize(2);
//        assertThat(sessions.get(0).sessionId()).isEqualTo("sess1");
//        assertThat(sessions.get(0).deviceInfo()).isEqualTo("iPhone 15");
//        assertThat(sessions.get(1).sessionId()).isEqualTo("sess2");
//        assertThat(sessions.get(1).deviceInfo()).isEqualTo("MacBook Pro");
//    }
//}
