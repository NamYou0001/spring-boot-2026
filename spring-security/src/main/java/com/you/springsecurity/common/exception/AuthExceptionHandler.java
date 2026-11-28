package com.you.springsecurity.common.exception;

import com.you.springsecurity.auth.exception.InvalidRefreshTokenException;
import com.you.springsecurity.auth.exception.TokenReuseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(AuthenticationException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password, or account is disabled."
        );
        problem.setTitle("Authentication Failed");
        problem.setType(URI.create("https://errors.security-lab.io/auth-failed"));
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ProblemDetail> handleRefreshTokenException(InvalidRefreshTokenException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage() != null ? exception.getMessage() : "Refresh token is invalid or expired. Please login again."
        );
        problem.setTitle("Invalid Refresh Token");
        problem.setType(URI.create("https://errors.security-lab.io/invalid-refresh-token"));
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(TokenReuseException.class)
    public ResponseEntity<ProblemDetail> handleTokenReuseException(TokenReuseException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Security Breach Detected: An attempt was made to reuse a revoked refresh token. All active sessions in this token family have been immediately revoked."
        );
        problem.setTitle("Token Reuse Detected");
        problem.setType(URI.create("https://errors.security-lab.io/token-reuse-detected"));
        problem.setProperty("timestamp", Instant.now());
        if (exception.getFamilyId() != null) {
            problem.setProperty("compromisedFamilyId", exception.getFamilyId());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource."
        );
        problem.setTitle("Access Denied");
        problem.setType(URI.create("https://errors.security-lab.io/forbidden"));
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }
}
