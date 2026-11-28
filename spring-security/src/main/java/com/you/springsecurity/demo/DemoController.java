package com.you.springsecurity.demo;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class DemoController {

    @GetMapping("/public/ping")
    public Map<String, Object> publicApi() {

        return Map.of("success", true, "message", "Public API");
    }

    @GetMapping("/private/me")
    public Map<String, Object> privateApi(Authentication authentication) {

        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        return Map.of("username", authentication.getName(), "authorities", authorities, "authenticated", authentication.isAuthenticated());
    }

    @GetMapping("/admin/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> adminApi(Authentication authentication) {

        return Map.of("success", true, "username", authentication.getName(), "message", "Welcome Admin");
    }

    @GetMapping("/users/read")
    @PreAuthorize("hasAuthority('USER_READ')")
    public Map<String, Object> readUsers() {

        return Map.of("success", true, "message", "You can read users");
    }

    @PostMapping("/users/create")
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public Map<String, Object> createUser() {

        return Map.of("success", true, "message", "You can create users");
    }
}