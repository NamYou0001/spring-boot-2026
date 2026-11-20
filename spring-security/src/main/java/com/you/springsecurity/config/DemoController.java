package com.you.springsecurity.config;


import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DemoController {

    @GetMapping("/public/ping")
    public Map<String, Object> publicApi() {

        return Map.of(
                "message", "This is public API",
                "success", true
        );

    }

        @GetMapping("/private/me")
    public Map<String, Object> privateApi(
            Authentication authentication
    ) {

        return Map.of(
                "username", authentication.getName(),

                "authorities",
                authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList(),

                "message", "Authenticated successfully"
        );

    }

    @GetMapping("/admin/ping")
    public Map<String, Object> adminApi() {

        return Map.of(
                "message", "Welcome Admin",
                "success", true
        );

    }
}