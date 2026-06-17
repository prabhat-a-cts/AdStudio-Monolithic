package com.cts.adstudio.finance.billing.entity;

import com.cts.adstudio.finance.billing.enums.ClientInvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * ClientInvoice (Backend Plan §4). Cross-aggregate references (advertiser,
 * campaign brief) are held as ids rather than @ManyToOne so the billing module
 * compiles and unit-tests independently of the other devs' modules and avoids
 * cross-module entity coupling; the columns still map 1:1 to the FK columns.
 */
@Entity
@Table(name = "client_invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long advertiserId;          // FK -> advertiser.id

    private Long campaignBriefId;       // FK -> campaign_brief.id

    @Column(length = 20)
    private String billingPeriod;       // e.g. "2026-05"

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal invoiceAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal agencyCommission = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netBillable = BigDecimal.ZERO;

    private LocalDate issuedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClientInvoiceStatus status = ClientInvoiceStatus.DRAFT;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
