package com.you.springsecurity.user.dto.request;


import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record AssignRolesRequest(

        @NotEmpty(message = "At least one role is required")
        Set<Long> roleIds

) {
}