package com.cts.adstudio.finance.billing.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Manual client-invoice creation (Finance enters the media amount directly). */
public record CreateClientInvoiceRequest(
        @NotNull Long advertiserId,
        Long campaignBriefId,
        String billingPeriod,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal invoiceAmount,
        @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal commissionRate) {}
