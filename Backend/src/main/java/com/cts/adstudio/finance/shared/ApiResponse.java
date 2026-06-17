package com.cts.adstudio.finance.shared;

import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

/**
 * Standard response envelope used by every endpoint (Backend Plan §8).
 * Never return raw entities or naked lists.
 *
 * Part of this service's common layer. In a larger estate this kind of infra is
 * typically published as a shared 'adstudio-common' library each service depends on.
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        Instant timestamp,
        Pagination pagination) {

    public record Pagination(int page, int size, long total) {}

    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, "Operation successful");
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, Instant.now(), null);
    }

    /** Wraps a Spring Data Page's content into the envelope with pagination metadata. */
    public static <T> ApiResponse<List<T>> page(Page<T> page, String message) {
        return new ApiResponse<>(true, page.getContent(), message, Instant.now(),
                new Pagination(page.getNumber(), page.getSize(), page.getTotalElements()));
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, Instant.now(), null);
    }
}
