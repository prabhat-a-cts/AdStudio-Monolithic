package com.cts.adstudio.finance.billing.config;

import com.cts.adstudio.finance.billing.enums.ClientInvoiceStatus;
import com.cts.adstudio.finance.billing.enums.PublisherInvoiceStatus;
import com.cts.adstudio.finance.shared.StatusTransitionValidator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Registers the billing state machines with the shared StatusTransitionValidator
 * on startup. Keeps the transition rules declarative and in one place, and means
 * the services never implement inline status checks (Backend Plan §7 / Risk Register).
 *
 * ClientInvoice:    DRAFT -> ISSUED -> {PAID | DISPUTED | OVERDUE};
 *                   OVERDUE -> {PAID | DISPUTED}; DISPUTED -> {ISSUED | PAID}; PAID terminal.
 * PublisherInvoice: RECEIVED -> {RECONCILED | DISCREPANCY};
 *                   DISCREPANCY -> RECONCILED; RECONCILED -> PAID; PAID terminal.
 */
@Component
@RequiredArgsConstructor
public class BillingStatusTransitions {

    private final StatusTransitionValidator validator;

    @PostConstruct
    public void register() {
        validator.register(ClientInvoiceStatus.class, Map.of(
                ClientInvoiceStatus.DRAFT, Set.of(ClientInvoiceStatus.ISSUED),
                ClientInvoiceStatus.ISSUED, Set.of(ClientInvoiceStatus.PAID,
                        ClientInvoiceStatus.DISPUTED, ClientInvoiceStatus.OVERDUE),
                ClientInvoiceStatus.OVERDUE, Set.of(ClientInvoiceStatus.PAID,
                        ClientInvoiceStatus.DISPUTED),
                ClientInvoiceStatus.DISPUTED, Set.of(ClientInvoiceStatus.ISSUED,
                        ClientInvoiceStatus.PAID)
        ));

        validator.register(PublisherInvoiceStatus.class, Map.of(
                PublisherInvoiceStatus.RECEIVED, Set.of(PublisherInvoiceStatus.RECONCILED,
                        PublisherInvoiceStatus.DISCREPANCY),
                PublisherInvoiceStatus.DISCREPANCY, Set.of(PublisherInvoiceStatus.RECONCILED),
                PublisherInvoiceStatus.RECONCILED, Set.of(PublisherInvoiceStatus.PAID)
        ));
    }
}
