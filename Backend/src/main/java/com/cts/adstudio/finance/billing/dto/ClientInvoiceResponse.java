package com.cts.adstudio.finance.billing.dto;

import com.cts.adstudio.finance.billing.entity.ClientInvoice;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ClientInvoiceResponse(
        Long id,
        Long advertiserId,
        Long campaignBriefId,
        String billingPeriod,
        BigDecimal invoiceAmount,
        BigDecimal agencyCommission,
        BigDecimal netBillable,
        LocalDate issuedDate,
        String status,
        Instant createdAt,
        Instant updatedAt) {

    public static ClientInvoiceResponse from(ClientInvoice c) {
        return new ClientInvoiceResponse(
                c.getId(), c.getAdvertiserId(), c.getCampaignBriefId(), c.getBillingPeriod(),
                c.getInvoiceAmount(), c.getAgencyCommission(), c.getNetBillable(),
                c.getIssuedDate(), c.getStatus().name(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
