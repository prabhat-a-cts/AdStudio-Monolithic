package com.cts.adstudio.finance.billing.enums;

/**
 * Client invoice lifecycle. Stored as STRING.
 *
 * NOTE (Dev 5 / Backend Plan Day 1-2): these values belong in the shared Status
 * constants file Dev 5 maintains; the team standard is UPPER_SNAKE_CASE stored
 * verbatim via @Enumerated(STRING).
 */
public enum ClientInvoiceStatus {
    DRAFT, ISSUED, PAID, DISPUTED, OVERDUE
}
