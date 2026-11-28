package com.you.springsecurity.user.dto.response;

public record PermissionResponse(

        Long id,
        String name ,
        String code,

        String description

) {
}