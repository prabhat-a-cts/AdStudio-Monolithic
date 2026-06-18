package com.cts.adstudio.finance.billing.service;

import com.cts.adstudio.finance.billing.dto.*;
import com.cts.adstudio.finance.billing.entity.ClientInvoice;
import com.cts.adstudio.finance.billing.enums.ClientInvoiceStatus;
import com.cts.adstudio.finance.billing.exception.BillingRuleException;
import com.cts.adstudio.finance.billing.exception.InvoiceNotFoundException;
import com.cts.adstudio.finance.billing.repository.ClientInvoiceRepository;
import com.cts.adstudio.finance.shared.AuditLogService;
import com.cts.adstudio.finance.shared.BudgetCalculationService;
import com.cts.adstudio.finance.shared.StatusTransitionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Client billing (Backend Plan §4.7, Days 4-13).
 *  adding this to get contrib
 * COMMISSION MODEL (the one business rule to confirm with Finance/BA):
 *   agencyCommission = invoiceAmount * commissionRate
 *   netBillable      = invoiceAmount + agencyCommission     (commission billed on top)
 * If the agency works on a commission-inclusive basis instead, change
 * {@link #computeCommercials} to subtract. Default rate is {@link #DEFAULT_COMMISSION_RATE}.
 */
@Service
@RequiredArgsConstructor
public class ClientInvoiceService {

    /** Default agency commission when the request omits a rate (15%). */
    public static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.15");

    private final ClientInvoiceRepository repository;
    private final StatusTransitionValidator statusValidator;
    private final AuditLogService auditLog;
    private final BudgetCalculationService budgetCalc;

    // ---- reads ---------------------------------------------------------------

    public ClientInvoice getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException("ClientInvoice", id));
    }

    public ClientInvoiceResponse get(Long id) {
        return ClientInvoiceResponse.from(getEntity(id));
    }

    public Page<ClientInvoiceResponse> list(ClientInvoiceStatus status, Pageable pageable) {
        Page<ClientInvoice> page = (status == null)
                ? repository.findAll(pageable)
                : repository.findByStatus(status, pageable);
        return page.map(ClientInvoiceResponse::from);
    }

    // ---- create / generate ---------------------------------------------------

    /** Manual creation: Finance supplies the media amount. */
    @Transactional
    public ClientInvoiceResponse create(CreateClientInvoiceRequest req, Long actingUserId) {
        BigDecimal[] c = computeCommercials(req.invoiceAmount(), req.commissionRate());
        ClientInvoice invoice = ClientInvoice.builder()
                .advertiserId(req.advertiserId())
                .campaignBriefId(req.campaignBriefId())
                .billingPeriod(req.billingPeriod())
                .invoiceAmount(req.invoiceAmount())
                .agencyCommission(c[0])
                .netBillable(c[1])
                .status(ClientInvoiceStatus.DRAFT)
                .build();
        return saveNew(invoice, actingUserId);
    }

    /** Generate from approved delivery: media amount comes from BudgetCalculationService. */
    @Transactional
    public ClientInvoiceResponse generate(GenerateClientInvoiceRequest req, Long actingUserId) {
        BigDecimal mediaAmount = budgetCalc.deliveredSpendForCampaign(req.campaignBriefId());
        if (mediaAmount == null || mediaAmount.signum() <= 0) {
            throw new BillingRuleException(
                    "No approved delivery to invoice for campaign " + req.campaignBriefId());
        }
        BigDecimal[] c = computeCommercials(mediaAmount, req.commissionRate());
        ClientInvoice invoice = ClientInvoice.builder()
                .advertiserId(req.advertiserId())
                .campaignBriefId(req.campaignBriefId())
                .billingPeriod(req.billingPeriod())
                .invoiceAmount(mediaAmount)
                .agencyCommission(c[0])
                .netBillable(c[1])
                .status(ClientInvoiceStatus.DRAFT)
                .build();
        return saveNew(invoice, actingUserId);
    }

    private ClientInvoiceResponse saveNew(ClientInvoice invoice, Long actingUserId) {
        ClientInvoice saved = repository.save(invoice);
        auditLog.log(actingUserId, "CLIENT_INVOICE_CREATED", "ClientInvoice", saved.getId());
        return ClientInvoiceResponse.from(saved);
    }

    // ---- update --------------------------------------------------------------

    @Transactional
    public ClientInvoiceResponse update(Long id, UpdateClientInvoiceRequest req, Long actingUserId) {
        ClientInvoice invoice = getEntity(id);
        if (invoice.getStatus() != ClientInvoiceStatus.DRAFT) {
            throw new BillingRuleException("Only DRAFT invoices can be edited");
        }
        BigDecimal[] c = computeCommercials(req.invoiceAmount(), req.commissionRate());
        invoice.setCampaignBriefId(req.campaignBriefId());
        invoice.setBillingPeriod(req.billingPeriod());
        invoice.setInvoiceAmount(req.invoiceAmount());
        invoice.setAgencyCommission(c[0]);
        invoice.setNetBillable(c[1]);
        ClientInvoice saved = repository.save(invoice);
        auditLog.log(actingUserId, "CLIENT_INVOICE_UPDATED", "ClientInvoice", id);
        return ClientInvoiceResponse.from(saved);
    }

    // ---- status flow ---------------------------------------------------------

    @Transactional
    public ClientInvoiceResponse changeStatus(Long id, String targetStatus, Long actingUserId) {
        ClientInvoice invoice = getEntity(id);
        ClientInvoiceStatus target = parseStatus(targetStatus);
        statusValidator.validate(invoice.getStatus(), target);   // throws 422 if illegal
        invoice.setStatus(target);
        if (target == ClientInvoiceStatus.ISSUED && invoice.getIssuedDate() == null) {
            invoice.setIssuedDate(LocalDate.now());
        }
        ClientInvoice saved = repository.save(invoice);
        auditLog.log(actingUserId, "CLIENT_INVOICE_STATUS_" + target.name(), "ClientInvoice", id);
        return ClientInvoiceResponse.from(saved);
    }

    // ---- payment tracker -----------------------------------------------------

    /**
     * Payment tracker rollup. With no Payment table in the 17-table schema,
     * "paid" is derived from invoice status: PAID = collected, ISSUED/OVERDUE =
     * outstanding, OVERDUE = the overdue slice of that.
     */
    public PaymentSummaryResponse paymentSummary(Long advertiserId) {
        BigDecimal billed = repository.sumNetBillableByAdvertiser(advertiserId);
        BigDecimal paid = repository.sumNetBillableByAdvertiserAndStatus(advertiserId, ClientInvoiceStatus.PAID);
        BigDecimal issued = repository.sumNetBillableByAdvertiserAndStatus(advertiserId, ClientInvoiceStatus.ISSUED);
        BigDecimal overdue = repository.sumNetBillableByAdvertiserAndStatus(advertiserId, ClientInvoiceStatus.OVERDUE);
        BigDecimal outstanding = issued.add(overdue);
        return new PaymentSummaryResponse(advertiserId, billed, paid, outstanding, overdue);
    }

    // ---- billing calendar ----------------------------------------------------

    /** Invoices issued within the given month, e.g. month = "2026-05". */
    public List<BillingCalendarEntryResponse> calendar(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return repository.findByIssuedDateBetween(start, end).stream()
                .map(BillingCalendarEntryResponse::from)
                .toList();
    }

    // ---- helpers -------------------------------------------------------------

    /** Returns [agencyCommission, netBillable]. */
    private BigDecimal[] computeCommercials(BigDecimal invoiceAmount, BigDecimal commissionRate) {
        BigDecimal rate = (commissionRate == null) ? DEFAULT_COMMISSION_RATE : commissionRate;
        BigDecimal commission = invoiceAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netBillable = invoiceAmount.add(commission).setScale(2, RoundingMode.HALF_UP);
        return new BigDecimal[]{commission, netBillable};
    }

    private ClientInvoiceStatus parseStatus(String raw) {
        try {
            return ClientInvoiceStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BillingRuleException("Unknown client invoice status: " + raw);
        }
    }
}
