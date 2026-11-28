package com.you.springsecurity.auth;

import com.you.springsecurity.auth.dto.*;
import com.you.springsecurity.user.dto.response.UserResponse;
import com.you.springsecurity.user.service.UserRoleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private  final UserRoleService userRoleService ;
    public AuthController(AuthService authService ,UserRoleService userRoleService ) {
        this.authService = authService;
        this.userRoleService =  userRoleService ;
    }

    @GetMapping
    public  ResponseEntity<List<UserResponse>> getAll(){
        return ResponseEntity.ok(userRoleService.findAll());
    }


    // 1. Login API
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String deviceInfo = extractUserAgent(httpRequest);
        String ipAddress = extractClientIp(httpRequest);
        return ResponseEntity.ok(authService.login(request, deviceInfo, ipAddress));
    }

    // 2. Refresh API (Rotation & Reuse Detection)
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest
    ) {
        String deviceInfo = extractUserAgent(httpRequest);
        String ipAddress = extractClientIp(httpRequest);
        return ResponseEntity.ok(authService.refresh(request, deviceInfo, ipAddress));
    }

    // 3. Logout Current Session API
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        authService.logout(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully logged out from current session"
        ));
    }

    // 4. Logout All Sessions API (Requires Authentication)
    @PostMapping("/logout-all")
    public ResponseEntity<Map<String, Object>> logoutAll(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        authService.logoutAll(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully logged out from all active sessions"
        ));
    }

    // 5. Get Active Sessions API (Requires Authentication)
//
    // 6. Terminate Specific Session API (Requires Authentication)
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Map<String, Object>> revokeSession(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String sessionId
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        authService.revokeSession(userId, sessionId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Session " + sessionId + " terminated successfully"
        ));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
        }
        return xfHeader.split(",")[0].trim();
    }

    private String extractUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return (ua != null && !ua.isBlank()) ? ua : "Unknown Client";
    }
}
