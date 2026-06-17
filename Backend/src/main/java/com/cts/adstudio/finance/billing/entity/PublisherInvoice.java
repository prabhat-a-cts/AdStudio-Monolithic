package com.cts.adstudio.finance.billing.entity;

import com.cts.adstudio.finance.billing.enums.PublisherInvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * PublisherInvoice (Backend Plan §4). publisherId references a User with the
 * Publisher role (the plan models publishers as users, not a separate table).
 */
@Entity
@Table(name = "publisher_invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublisherInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long publisherId;           // FK -> app_user.id (Publisher role)

    @Column(nullable = false)
    private Long ioId;                  // FK -> insertion_order.id

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal invoiceAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal deliveredValue = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal varianceAmount = BigDecimal.ZERO;

    private LocalDate receivedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PublisherInvoiceStatus status = PublisherInvoiceStatus.RECEIVED;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
