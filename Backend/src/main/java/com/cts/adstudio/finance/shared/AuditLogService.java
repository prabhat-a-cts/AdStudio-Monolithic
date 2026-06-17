package com.cts.adstudio.finance.shared;

/**
 * Shared audit hook (Backend Plan §7 Day 3). Every financial / state-changing
 * action records an entry. Billing depends only on this interface.
 *
 * Implemented here by LoggingAuditLogService; could instead publish to a central
 * audit service. Mocked in unit tests.
 */
public interface AuditLogService {
    void log(Long userId, String action, String entityType, Long entityId);
}
