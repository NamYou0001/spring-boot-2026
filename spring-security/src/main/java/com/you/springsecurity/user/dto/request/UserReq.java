package com.you.springsecurity.user.dto.request;

public record UserReq(
        String username ,
        String password ,
        String email  ,
        Long roleId) {

}
