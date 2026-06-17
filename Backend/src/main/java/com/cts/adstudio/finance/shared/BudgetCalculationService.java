package com.cts.adstudio.finance.shared;

import java.math.BigDecimal;

/**
 * Shared spend/delivery math used by Delivery, Billing, and Analytics
 * (Backend Plan §7 Day 3). Billing uses it to source the delivered figures for
 * client-invoice generation and publisher-invoice reconciliation, so the
 * aggregation lives in one place (no duplicated JPQL, no N+1).
 *
 * Implemented here by DeliveryServiceBudgetCalculation (an HTTP client to the
 * Delivery service). Mocked in unit tests.
 */
public interface BudgetCalculationService {

    /** Total accepted delivered spend for a campaign brief (basis for the client invoice). */
    BigDecimal deliveredSpendForCampaign(Long campaignBriefId);

    /** Accepted delivered value for an insertion order (basis for publisher reconciliation). */
    BigDecimal deliveredValueForInsertionOrder(Long ioId);
}
