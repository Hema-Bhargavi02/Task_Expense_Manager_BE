package com.example.taskandexpensemanager.datatransferobjects;

import java.time.Instant;
import java.util.Map;

/**
 * Single error contract returned by every endpoint.
 *
 * <p>
 * Example:
 * <pre>
 * {
 *   "timestamp": "2026-11-09T10:15:30Z",
 *   "status": 409,
 *   "code": "USER_ALREADY_EXISTS",
 *   "message": "That username or email is already registered.",
 *   "path": "/auth/register",
 *   "fieldErrors": null
 * }
 * </pre>
 */
public record ApiError(Instant timestamp, int status, String code, String message, String path, Map<String, String> fieldError) {

    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(
                Instant.now(),
                status,
                code,
                message,
                path,
                null
        );
    }

    public static ApiError of(
            int status,
            String code,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        return new ApiError(
                Instant.now(),
                status,
                code,
                message,
                path,
                fieldErrors
        );
    }
}
