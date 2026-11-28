package com.you.springsecurity.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
public class JwtAuthenticationConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        // Step 1: Create authorities converter
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // Step 2: Read custom claim
        authoritiesConverter.setAuthoritiesClaimName("authorities");

        // Step 3: Do not add another prefix
        authoritiesConverter.setAuthorityPrefix("");

        // Step 4: Create JWT authentication converter
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Step 5: Set authorities converter
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        // Step 6: Use username as principal name
        converter.setPrincipalClaimName("username");

        return converter;
    }
}