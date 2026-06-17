package com.cts.adstudio.finance.billing.dto;

import java.math.BigDecimal;

/** Payment tracker rollup for an advertiser (Finance Dashboard). */
public record PaymentSummaryResponse(
        Long advertiserId,
        BigDecimal totalBilled,
        BigDecimal totalPaid,
        BigDecimal totalOutstanding,
        BigDecimal totalOverdue) {}
