package com.you.springsecurity.auth.dto;



import java.util.List;

public record LoginResponse(

        Long userId,

        String username,

        List<String> authorities,

        String tokenType,

        String accessToken,

        long expiresIn,

        boolean authenticated,

        String message

) {}