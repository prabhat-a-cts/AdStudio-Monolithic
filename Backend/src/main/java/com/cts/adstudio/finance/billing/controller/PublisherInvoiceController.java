package com.cts.adstudio.finance.billing.controller;

import com.cts.adstudio.finance.billing.dto.ChangeStatusRequest;
import com.cts.adstudio.finance.billing.dto.PublisherInvoiceResponse;
import com.cts.adstudio.finance.billing.dto.ReconciliationResultResponse;
import com.cts.adstudio.finance.billing.dto.SubmitPublisherInvoiceRequest;
import com.cts.adstudio.finance.billing.enums.PublisherInvoiceStatus;
import com.cts.adstudio.finance.billing.service.PublisherInvoiceService;
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
 * Publisher invoice submission (Publisher Portal) + reconciliation (Finance Dashboard).
 *
 * RBAC (Backend Plan §9): Publishers submit/read their own invoices; Finance +
 * Admin reconcile and progress status. Submission is guarded for PUBLISHER/ADMIN;
 * reconciliation/status for FINANCE/ADMIN.
 */
@RestController
@RequestMapping("/api/publisher-invoices")
@RequiredArgsConstructor
public class PublisherInvoiceController {

    private final PublisherInvoiceService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('DELIVERY_PUBLISHER','ADMIN')")
    public ApiResponse<PublisherInvoiceResponse> submit(
            @Valid @RequestBody SubmitPublisherInvoiceRequest req,
            @RequestHeader(value = "X-User-Id", required = false) Long actingUserId) {
        return ApiResponse.ok(service.submit(req, actingUserId), "Publisher invoice received");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DELIVERY_PUBLISHER','FINANCE_EXECUTIVE','ADMIN')")
    public ApiResponse<PublisherInvoiceResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DELIVERY_PUBLISHER','FINANCE_EXECUTIVE','ADMIN')")
    public ApiResponse<List<PublisherInvoiceResponse>> list(
            @RequestParam(required = false) Long publisherId,
            @RequestParam(required = false) PublisherInvoiceStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.page(service.list(publisherId, status, pageable), "Publisher invoices");
    }

    @PutMapping("/{id}/reconcile")
    @PreAuthorize("hasAnyRole('FINANCE_EXECUTIVE','ADMIN')")
    public ApiResponse<ReconciliationResultResponse> reconcile(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long actingUserId) {
        return ApiResponse.ok(service.reconcile(id, actingUserId), "Publisher invoice reconciled");
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('FINANCE_EXECUTIVE','ADMIN')")
    public ApiResponse<PublisherInvoiceResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest req,
            @RequestHeader(value = "X-User-Id", required = false) Long actingUserId) {
        return ApiResponse.ok(service.changeStatus(id, req.status(), actingUserId),
                "Publisher invoice status updated");
    }
}
