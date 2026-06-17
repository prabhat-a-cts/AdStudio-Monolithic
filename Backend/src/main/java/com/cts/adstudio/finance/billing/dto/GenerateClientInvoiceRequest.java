package com.cts.adstudio.finance.billing.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Generate a client invoice from approved delivery data. The media amount is
 * computed via BudgetCalculationService rather than supplied.
 */
public record GenerateClientInvoiceRequest(
        @NotNull Long advertiserId,
        @NotNull Long campaignBriefId,
        String billingPeriod,
        @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal commissionRate) {}
