package com.you.springsecurity.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.you.springsecurity.jwt.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public JwtEncoder jwtEncoder(JwtProperties jwtProperties) {

        // Step 1: Decode Base64 Secret
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.secretBase64());

        // Step 2: Check Key Length
        if (keyBytes.length < 32) {
            throw new IllegalStateException("HS256 secret key must contain at least 32 bytes");
        }

        // Step 3: Create SecretKey
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        // Step 4: Create JWT Encoder
        return NimbusJwtEncoder.withSecretKey(secretKey).algorithm(MacAlgorithm.HS256).build();
    }
}