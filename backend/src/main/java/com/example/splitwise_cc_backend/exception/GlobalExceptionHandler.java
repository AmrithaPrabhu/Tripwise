package com.example.splitwise_cc_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> handleConflict(ConflictException ex) {
        return buildResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAnyException(Exception ex) {
        return buildResponse(
                ex.getMessage(),   // 👈 YOUR message
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<?> buildResponse(
            String message,
            HttpStatus status
    ) {
        return ResponseEntity
                .status(status)
                .body(Map.of(
                        "message", message,
                        "timestamp", Instant.now()
                ));
    }
}
