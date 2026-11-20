package com.you.springsecurity.auth;


import java.time.Duration;
import java.time.Instant;
import java.util.List;


import com.you.springsecurity.auth.dto.LoginRequest;
import com.you.springsecurity.auth.dto.LoginResponse;
import com.you.springsecurity.jwt.IssuedAccessToken;
import com.you.springsecurity.jwt.TokenService;
import com.you.springsecurity.security.CustomUserDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final TokenService tokenService;

    public AuthService(
            AuthenticationManager authenticationManager,
            TokenService tokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {

        // Step 1: Create authentication request
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.username(),
                        request.password()
                );

        // Step 2: Authenticate username and password
        Authentication authenticationResult =
                authenticationManager.authenticate(
                        authenticationRequest
                );

        // Step 3: Get authenticated user
        Object principal = authenticationResult.getPrincipal();

        if (!(principal instanceof CustomUserDetail userDetails)) {
            throw new AuthenticationServiceException(
                    "Unexpected authenticated principal"
            );
        }

        // Step 4: Generate JWT Access Token
        IssuedAccessToken issuedToken =
                tokenService.generateAccessToken(userDetails);

        // Step 5: Get authorities
        List<String> authorities =
                authenticationResult.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList();

        // Step 6: Calculate remaining lifetime
        long expiresIn = Math.max(0,
                Duration.between(
                Instant.now(),
                issuedToken.expiresAt()).toSeconds());

        // Step 7: Return response
        return new LoginResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                authorities,
                "Bearer",
                issuedToken.token(),
                expiresIn,
                authenticationResult.isAuthenticated(),
                "Login successful"
        );
    }
}





