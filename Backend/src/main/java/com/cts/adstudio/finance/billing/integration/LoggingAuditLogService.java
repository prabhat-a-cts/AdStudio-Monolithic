package com.cts.adstudio.finance.billing.integration;

import com.cts.adstudio.finance.shared.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Concrete AuditLogService for this microservice. Writes a structured audit line
 * locally. In the wider platform this could publish an event to the audit/IAM
 * service (message broker or REST) instead — the service depends only on the
 * AuditLogService interface, so swapping this bean changes nothing else.
 */
@Service
@Slf4j
public class LoggingAuditLogService implements AuditLogService {

    @Override
    public void log(Long userId, String action, String entityType, Long entityId) {
        log.info("AUDIT user={} action={} entity={}#{}", userId, action, entityType, entityId);
    }
}
