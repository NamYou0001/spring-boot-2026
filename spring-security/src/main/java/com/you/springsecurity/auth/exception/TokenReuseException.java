package com.you.springsecurity.auth.exception;

public class TokenReuseException extends RuntimeException {

    private final String familyId;

    public TokenReuseException(String message) {
        super(message);
        this.familyId = null;
    }

    public TokenReuseException(String message, String familyId) {
        super(message);
        this.familyId = familyId;
    }

    public String getFamilyId() {
        return familyId;
    }
}
