package com.cts.adstudio.finance.billing.service;

import com.cts.adstudio.finance.billing.dto.PublisherInvoiceResponse;
import com.cts.adstudio.finance.billing.dto.ReconciliationResultResponse;
import com.cts.adstudio.finance.billing.dto.SubmitPublisherInvoiceRequest;
import com.cts.adstudio.finance.billing.entity.PublisherInvoice;
import com.cts.adstudio.finance.billing.enums.PublisherInvoiceStatus;
import com.cts.adstudio.finance.billing.exception.InvoiceNotFoundException;
import com.cts.adstudio.finance.billing.exception.BillingRuleException;
import com.cts.adstudio.finance.billing.repository.PublisherInvoiceRepository;
import com.cts.adstudio.finance.shared.AuditLogService;
import com.cts.adstudio.finance.shared.BudgetCalculationService;
import com.cts.adstudio.finance.shared.StatusTransitionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Publisher invoice submission + reconciliation (Backend Plan §4.7, Days 4-13).
 *
 * Reconciliation: VarianceAmount = InvoiceAmount - DeliveredValue, where
 * DeliveredValue is the accepted delivered value for the IO (BudgetCalculationService).
 * Within {@link #RECONCILE_TOLERANCE} the invoice is RECONCILED; otherwise DISCREPANCY.
 */
@Service
@RequiredArgsConstructor
public class PublisherInvoiceService {

    /** Absolute variance treated as a clean match during reconciliation. */
    public static final BigDecimal RECONCILE_TOLERANCE = new BigDecimal("0.01");

    private final PublisherInvoiceRepository repository;
    private final StatusTransitionValidator statusValidator;
    private final AuditLogService auditLog;
    private final BudgetCalculationService budgetCalc;

    public PublisherInvoice getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException("PublisherInvoice", id));
    }

    public PublisherInvoiceResponse get(Long id) {
        return PublisherInvoiceResponse.from(getEntity(id));
    }

    public Page<PublisherInvoiceResponse> list(Long publisherId, PublisherInvoiceStatus status,
                                               Pageable pageable) {
        Page<PublisherInvoice> page;
        if (publisherId != null) {
            page = repository.findByPublisherId(publisherId, pageable);
        } else if (status != null) {
            page = repository.findByStatus(status, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.map(PublisherInvoiceResponse::from);
    }

    @Transactional
    public PublisherInvoiceResponse submit(SubmitPublisherInvoiceRequest req, Long actingUserId) {
        PublisherInvoice invoice = PublisherInvoice.builder()
                .publisherId(req.publisherId())
                .ioId(req.ioId())
                .invoiceAmount(req.invoiceAmount())
                .deliveredValue(BigDecimal.ZERO)
                .varianceAmount(BigDecimal.ZERO)
                .receivedDate(req.receivedDate() != null ? req.receivedDate() : LocalDate.now())
                .status(PublisherInvoiceStatus.RECEIVED)
                .build();
        PublisherInvoice saved = repository.save(invoice);
        auditLog.log(actingUserId, "PUBLISHER_INVOICE_RECEIVED", "PublisherInvoice", saved.getId());
        return PublisherInvoiceResponse.from(saved);
    }

    /** Reconcile against delivered value; sets variance and RECONCILED / DISCREPANCY. */
    @Transactional
    public ReconciliationResultResponse reconcile(Long id, Long actingUserId) {
        PublisherInvoice invoice = getEntity(id);

        BigDecimal deliveredValue = budgetCalc.deliveredValueForInsertionOrder(invoice.getIoId());
        if (deliveredValue == null) deliveredValue = BigDecimal.ZERO;
        BigDecimal variance = invoice.getInvoiceAmount().subtract(deliveredValue);

        PublisherInvoiceStatus target = variance.abs().compareTo(RECONCILE_TOLERANCE) <= 0
                ? PublisherInvoiceStatus.RECONCILED
                : PublisherInvoiceStatus.DISCREPANCY;
        statusValidator.validate(invoice.getStatus(), target);   // throws 422 if illegal

        invoice.setDeliveredValue(deliveredValue);
        invoice.setVarianceAmount(variance);
        invoice.setStatus(target);
        PublisherInvoice saved = repository.save(invoice);
        auditLog.log(actingUserId, "PUBLISHER_INVOICE_RECONCILED", "PublisherInvoice", id);
        return ReconciliationResultResponse.from(saved);
    }

    @Transactional
    public PublisherInvoiceResponse changeStatus(Long id, String targetStatus, Long actingUserId) {
        PublisherInvoice invoice = getEntity(id);
        PublisherInvoiceStatus target = parseStatus(targetStatus);
        statusValidator.validate(invoice.getStatus(), target);
        invoice.setStatus(target);
        PublisherInvoice saved = repository.save(invoice);
        auditLog.log(actingUserId, "PUBLISHER_INVOICE_STATUS_" + target.name(), "PublisherInvoice", id);
        return PublisherInvoiceResponse.from(saved);
    }

    private PublisherInvoiceStatus parseStatus(String raw) {
        try {
            return PublisherInvoiceStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BillingRuleException("Unknown publisher invoice status: " + raw);
        }
    }
}
