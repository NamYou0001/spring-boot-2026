package com.you.springsecurity.auth;

import com.you.springsecurity.auth.dto.*;
import com.you.springsecurity.auth.token.IssuedAccessToken;
import com.you.springsecurity.auth.token.IssuedRefreshToken;
import com.you.springsecurity.auth.token.RefreshTokenService;
import com.you.springsecurity.auth.token.TokenService;
import com.you.springsecurity.security.CustomUserDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AuthenticationManager authenticationManager,
                       TokenService tokenService,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
    }

    // =========================
    // 1. LOGIN
    // =========================
    public LoginResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
        // Step 1: Create Authentication Request
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                request.username(),
                request.password()
        );

        // Step 2: Verify Username and Password
        Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);

        // Step 3: Get authenticated user
        Object principal = authenticationResult.getPrincipal();
        if (!(principal instanceof CustomUserDetail userDetails)) {
            throw new AuthenticationServiceException("Unexpected authenticated principal");
        }

        // Step 4: Generate Access Token
        IssuedAccessToken accessToken = tokenService.generateAccessToken(userDetails);

        // Step 5: Generate Refresh Token with Device and IP tracking
        IssuedRefreshToken refreshToken = refreshTokenService.createForUser(
                userDetails.getId(),
                deviceInfo,
                ipAddress
        );

        // Step 6: Get Authorities
        List<String> authorities = authenticationResult.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // Step 7: Calculate Expiration
        long accessTokenExpiresIn = Duration.between(Instant.now(), accessToken.expiresAt()).toSeconds();
        long refreshTokenExpiresIn = Duration.between(Instant.now(), refreshToken.expiresAt()).toSeconds();

        // Step 8: Return Response
        return new LoginResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                authorities,
                "Bearer",
                accessToken.token(),
                Math.max(0, accessTokenExpiresIn),
                refreshToken.token(),
                Math.max(0, refreshTokenExpiresIn),
                refreshToken.sessionId(),
                authenticationResult.isAuthenticated(),
                "Login successful"
        );
    }

    public LoginResponse login(LoginRequest request) {
        return login(request, "Unknown Client", "0.0.0.0");
    }

    // ===================================
    // 2. REFRESH ACCESS TOKEN (ROTATION)
    // ===================================
    public RefreshResponse refresh(RefreshRequest request, String deviceInfo, String ipAddress) {
        // Step 1: Rotate Refresh Token (Includes Reuse Detection)
        RefreshTokenService.RotatedTokenResult result = refreshTokenService.rotateRefreshToken(
                request.refreshToken(),
                deviceInfo,
                ipAddress
        );

        // Step 2: Build CustomUserDetail for Token Generation
        CustomUserDetail userDetails = new CustomUserDetail(result.user());

        // Step 3: Generate New Access Token
        IssuedAccessToken newAccessToken = tokenService.generateAccessToken(userDetails);

        // Step 4: Get Current Authorities
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // Step 5: Calculate Expiration times
        long accessExpiresIn = Duration.between(Instant.now(), newAccessToken.expiresAt()).toSeconds();
        long refreshExpiresIn = Duration.between(Instant.now(), result.issuedRefreshToken().expiresAt()).toSeconds();

        // Step 6: Return Response with Both Tokens Rotated
        return new RefreshResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                authorities,
                "Bearer",
                newAccessToken.token(),
                Math.max(0, accessExpiresIn),
                result.issuedRefreshToken().token(),
                Math.max(0, refreshExpiresIn),
                result.issuedRefreshToken().sessionId(),
                "Access and refresh tokens rotated successfully"
        );
    }

    public RefreshResponse refresh(RefreshRequest request) {
        return refresh(request, "Unknown Client", "0.0.0.0");
    }

    // =========================
    // 3. LOGOUT CURRENT SESSION
    // =========================
    public void logout(LogoutRequest request) {
        refreshTokenService.revokeToken(request.refreshToken());
    }

    // =========================
    // 4. LOGOUT ALL SESSIONS
    // =========================
    public void logoutAll(Long userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    // =========================
    // 5. GET ACTIVE SESSIONS
    // =========================
//    public List<SessionResponse> getActiveSessions(Long userId) {
//        return refreshTokenService.getActiveSessions(userId);
//    }

    // =========================
    // 6. REVOKE SPECIFIC SESSION
    // =========================
    public void revokeSession(Long userId, String sessionId) {
        refreshTokenService.revokeSession(userId, sessionId);
    }
}
