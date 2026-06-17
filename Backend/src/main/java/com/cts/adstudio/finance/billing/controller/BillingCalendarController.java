package com.cts.adstudio.finance.billing.controller;

import com.cts.adstudio.finance.billing.dto.BillingCalendarEntryResponse;
import com.cts.adstudio.finance.billing.exception.BillingRuleException;
import com.cts.adstudio.finance.billing.service.ClientInvoiceService;
import com.cts.adstudio.finance.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Finance billing calendar: GET /api/invoices/calendar?month=YYYY-MM */
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class BillingCalendarController {

    private final ClientInvoiceService clientInvoiceService;

    @GetMapping("/calendar")
    @PreAuthorize("hasAnyRole('FINANCE_EXECUTIVE','ADMIN','BRAND_ADVERTISER')")
    public ApiResponse<List<BillingCalendarEntryResponse>> calendar(@RequestParam String month) {
        YearMonth ym;
        try {
            ym = YearMonth.parse(month);   // expects "YYYY-MM"
        } catch (DateTimeParseException e) {
            throw new BillingRuleException("Invalid month '" + month + "', expected format YYYY-MM");
        }
        return ApiResponse.ok(clientInvoiceService.calendar(ym), "Billing calendar for " + month);
    }
}
