package com.example.taskandexpensemanager.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for expected, user-facing API failures.
 *
 * <p>
 * Services throw these instead of {@code ResponseStatusException} so that the
 * HTTP status, machine-readable code, and user-facing message are all carried
 * together and rendered by {@link GlobalExceptionHandler}.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
