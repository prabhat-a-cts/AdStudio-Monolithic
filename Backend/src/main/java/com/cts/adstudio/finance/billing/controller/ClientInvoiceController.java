package com.cts.adstudio.finance.billing.controller;

import com.cts.adstudio.finance.billing.dto.*;
import com.cts.adstudio.finance.billing.enums.ClientInvoiceStatus;
import com.cts.adstudio.finance.billing.service.ClientInvoiceService;
import com.cts.adstudio.finance.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Finance Dashboard — client billing.
 *
 * RBAC (Backend Plan §9): Finance + Admin have full access; AdvertiserBrand is
 * read-only. Roles map to ROLE_FINANCE / ROLE_ADMIN / ROLE_ADVERTISER_BRAND
 * authorities (set by Dev 1's JwtTokenProvider / UserDetailsService).
 *
 * actingUserId is taken from the X-User-Id header as a seam for audit logging;
 * in the integrated app, resolve it from the JWT principal (SecurityContext).
 */
@RestController
@RequestMapping("/api/client-invoices")
@RequiredArgsConstructor
public class ClientInvoiceController {

    private final ClientInvoiceService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('FINANCE_EXECUTIVE','ADMIN')")
    public ApiResponse<ClientInvoiceResponse> create(
            @Valid @RequestBody CreateClientInvoiceRequest req,
            @RequestHeader(value = "X-User-Id", required = false) Long actingUserId) {
        return ApiResponse.ok(service.create(req, actingUserId), "Client invoice created");
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('FINANCE_EXECUTIVE','ADMIN')")
    public ApiResponse<ClientInvoiceResponse> generate(
            @Valid @RequestBody GenerateClientInvoiceRequest req,
            @RequestHeader(value = "X-User-Id", required = false) Long actingUserId) {
        return ApiResponse.ok(service.generate(req, actingUserId),
                "Client invoice generated from delivery");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FINANCE_EXECUTIVE','ADMIN','BRAND_ADVERTISER')")
    public ApiResponse<ClientInvoiceResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FINANCE_EXECUTIVE','ADMIN','BRAND_ADVERTISER')")
    public ApiResponse<List<ClientInvoiceResponse>> list(
            @RequestParam(required = false) ClientInvoiceStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.page(service.list(status, pageable), "Client invoices");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FINANCE_EXECUTIVE','ADMIN')")
    public ApiResponse<ClientInvoiceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClientInvoiceRequest req,
            @RequestHeader(value = "X-User-Id", required = false) Long actingUserId) {
        return ApiResponse.ok(service.update(id, req, actingUserId), "Client invoice updated");
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('FINANCE_EXECUTIVE','ADMIN')")
    public ApiResponse<ClientInvoiceResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest req,
            @RequestHeader(value = "X-User-Id", required = false) Long actingUserId) {
        return ApiResponse.ok(service.changeStatus(id, req.status(), actingUserId),
                "Client invoice status updated");
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('FINANCE_EXECUTIVE','ADMIN','BRAND_ADVERTISER')")
    public ApiResponse<PaymentSummaryResponse> paymentSummary(@RequestParam Long advertiserId) {
        return ApiResponse.ok(service.paymentSummary(advertiserId), "Payment summary");
    }
}
