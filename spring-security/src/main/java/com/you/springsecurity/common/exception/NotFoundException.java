package com.you.springsecurity.common.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message, Long id) {
        super(message);
    }
}