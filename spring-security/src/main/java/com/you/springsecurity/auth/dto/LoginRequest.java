package com.you.springsecurity.auth.dto;

public record LoginRequest
        (
                String username ,
                String password
        ){

}
