package com.cts.adstudio.finance.billing.dto;

import com.cts.adstudio.finance.billing.entity.PublisherInvoice;

import java.math.BigDecimal;

/** Outcome of reconciling a publisher invoice against delivered value. */
public record ReconciliationResultResponse(
        Long pubInvoiceId,
        BigDecimal invoiceAmount,
        BigDecimal deliveredValue,
        BigDecimal varianceAmount,
        String status) {

    public static ReconciliationResultResponse from(PublisherInvoice p) {
        return new ReconciliationResultResponse(
                p.getId(), p.getInvoiceAmount(), p.getDeliveredValue(),
                p.getVarianceAmount(), p.getStatus().name());
    }
}
