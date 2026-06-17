package com.cts.adstudio.finance.shared.exception;

import org.springframework.http.HttpStatus;

/** Thrown by StatusTransitionValidator for a disallowed status change (HTTP 422). */
public class IllegalStatusTransitionException extends ApiException {
    public IllegalStatusTransitionException(String entity, String from, String to) {
        super(HttpStatus.UNPROCESSABLE_ENTITY,
                "Illegal " + entity + " status transition: " + from + " -> " + to);
    }
}
