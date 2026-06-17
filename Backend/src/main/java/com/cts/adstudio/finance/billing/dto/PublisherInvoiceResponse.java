package com.cts.adstudio.finance.billing.dto;

import com.cts.adstudio.finance.billing.entity.PublisherInvoice;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PublisherInvoiceResponse(
        Long id,
        Long publisherId,
        Long ioId,
        BigDecimal invoiceAmount,
        BigDecimal deliveredValue,
        BigDecimal varianceAmount,
        LocalDate receivedDate,
        String status,
        Instant createdAt,
        Instant updatedAt) {

    public static PublisherInvoiceResponse from(PublisherInvoice p) {
        return new PublisherInvoiceResponse(
                p.getId(), p.getPublisherId(), p.getIoId(), p.getInvoiceAmount(),
                p.getDeliveredValue(), p.getVarianceAmount(), p.getReceivedDate(),
                p.getStatus().name(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
