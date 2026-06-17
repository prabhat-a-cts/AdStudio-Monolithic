package com.cts.adstudio.finance.billing.dto;

import com.cts.adstudio.finance.billing.entity.ClientInvoice;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One entry on the Finance billing calendar. */
public record BillingCalendarEntryResponse(
        Long invoiceId,
        Long advertiserId,
        Long campaignBriefId,
        String billingPeriod,
        BigDecimal netBillable,
        LocalDate issuedDate,
        String status) {

    public static BillingCalendarEntryResponse from(ClientInvoice c) {
        return new BillingCalendarEntryResponse(
                c.getId(), c.getAdvertiserId(), c.getCampaignBriefId(), c.getBillingPeriod(),
                c.getNetBillable(), c.getIssuedDate(), c.getStatus().name());
    }
}
