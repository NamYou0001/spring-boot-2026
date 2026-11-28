
package com.you.springsecurity.auth.token;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.you.springsecurity.security.CustomUserDetail;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public TokenService(
            JwtEncoder jwtEncoder,
            JwtProperties jwtProperties
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public IssuedAccessToken generateAccessToken(
            CustomUserDetail userDetails
    ) {

        Instant now = Instant.now();

        Instant expiresAt = now.plus(
                jwtProperties.accessTokenTtl()
        );

        // Get roles and authorities (roles + permissions) from CustomUserDetail
        List<String> roles = userDetails.getRoles();
        List<String> authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // Create JWT Claims
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(userDetails.getId().toString())
                .audience(List.of(jwtProperties.audience()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("username", userDetails.getUsername())
                .claim("roles", roles)
                .claim("authorities", authorities)
                .build();

        // Create JWT Header
        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        // Encode and Sign JWT
        Jwt jwt = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        );

        return new IssuedAccessToken(
                jwt.getTokenValue(),
                expiresAt
        );
    }
}
