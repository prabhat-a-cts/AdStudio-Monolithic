package com.cts.adstudio.finance.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Base for all application exceptions carrying an HTTP status, mapped to the
 * ApiResponse error envelope by GlobalExceptionHandler.
 *
 * Part of this service's common layer.
 */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
