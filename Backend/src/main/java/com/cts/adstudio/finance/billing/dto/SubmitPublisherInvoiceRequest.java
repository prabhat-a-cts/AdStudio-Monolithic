package com.cts.adstudio.finance.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A publisher submits an invoice against one of their insertion orders. */
public record SubmitPublisherInvoiceRequest(
        @NotNull Long publisherId,
        @NotNull Long ioId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal invoiceAmount,
        LocalDate receivedDate) {}
