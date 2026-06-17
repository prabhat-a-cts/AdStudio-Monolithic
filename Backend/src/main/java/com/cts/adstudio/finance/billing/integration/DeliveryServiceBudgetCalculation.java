package com.cts.adstudio.finance.billing.integration;

import com.cts.adstudio.finance.shared.BudgetCalculationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

/**
 * Sources delivered figures from the Delivery service over HTTP (this is the
 * cross-service call in the microservice architecture). The service boots
 * regardless of whether Delivery is up; the call only happens when an invoice is
 * generated or reconciled.
 *
 * Assumed Delivery API (align with Dev 4's service), returning the standard
 * ApiResponse envelope with a numeric `data`:
 *   GET {delivery}/api/delivery/campaigns/{briefId}/delivered-spend
 *   GET {delivery}/api/delivery/insertion-orders/{ioId}/delivered-value
 */
@Service
public class DeliveryServiceBudgetCalculation implements BudgetCalculationService {

    private final RestClient restClient;

    public DeliveryServiceBudgetCalculation(
            @Value("${adstudio.services.delivery.base-url}") String deliveryBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(deliveryBaseUrl).build();
    }

    /** Minimal view of the ApiResponse envelope (unknown fields are ignored). */
    private record Envelope(BigDecimal data) {}

    @Override
    public BigDecimal deliveredSpendForCampaign(Long campaignBriefId) {
        Envelope body = restClient.get()
                .uri("/api/delivery/campaigns/{id}/delivered-spend", campaignBriefId)
                .retrieve()
                .body(Envelope.class);
        return (body == null || body.data() == null) ? BigDecimal.ZERO : body.data();
    }

    @Override
    public BigDecimal deliveredValueForInsertionOrder(Long ioId) {
        Envelope body = restClient.get()
                .uri("/api/delivery/insertion-orders/{id}/delivered-value", ioId)
                .retrieve()
                .body(Envelope.class);
        return (body == null || body.data() == null) ? BigDecimal.ZERO : body.data();
    }
}
