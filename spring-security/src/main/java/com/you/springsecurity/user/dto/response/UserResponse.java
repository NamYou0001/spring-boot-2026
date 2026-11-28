package com.you.springsecurity.user.dto.response;

import lombok.Builder;

@Builder
public record UserResponse(
        Long userId  ,
        String username ,
        String password ,
        String email ,
        Long roleId  ,
        String roleName
) {

}
