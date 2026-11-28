package com.you.springsecurity.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record SaveRoleRequest(

        @NotBlank(message = "Role name is required")
        @Size(max = 50)
        String name,

        @Size(max = 255)
        String description,

        Set<Long> permissionIds

) {
}