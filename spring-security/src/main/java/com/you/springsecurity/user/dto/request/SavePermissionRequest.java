package com.you.springsecurity.user.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SavePermissionRequest(

        @NotBlank(message = "Permission code is required")
        @Size(max = 100)
        String code,
        String name ,
        @Size(max = 255)
        String description

) {
}