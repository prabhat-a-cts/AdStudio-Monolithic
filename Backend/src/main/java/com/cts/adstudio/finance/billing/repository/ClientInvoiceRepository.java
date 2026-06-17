package com.cts.adstudio.finance.billing.repository;

import com.cts.adstudio.finance.billing.entity.ClientInvoice;
import com.cts.adstudio.finance.billing.enums.ClientInvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ClientInvoiceRepository extends JpaRepository<ClientInvoice, Long> {

    Page<ClientInvoice> findByStatus(ClientInvoiceStatus status, Pageable pageable);

    Page<ClientInvoice> findByAdvertiserId(Long advertiserId, Pageable pageable);

    /** Billing calendar: invoices issued within a date window. */
    List<ClientInvoice> findByIssuedDateBetween(LocalDate start, LocalDate end);

    /** Payment-tracking aggregate: total net billable for an advertiser in a given status. */
    @Query("""
            select coalesce(sum(c.netBillable), 0)
            from ClientInvoice c
            where c.advertiserId = :advertiserId and c.status = :status
            """)
    BigDecimal sumNetBillableByAdvertiserAndStatus(@Param("advertiserId") Long advertiserId,
                                                   @Param("status") ClientInvoiceStatus status);

    /** Payment-tracking aggregate: total net billable across all statuses for an advertiser. */
    @Query("""
            select coalesce(sum(c.netBillable), 0)
            from ClientInvoice c
            where c.advertiserId = :advertiserId
            """)
    BigDecimal sumNetBillableByAdvertiser(@Param("advertiserId") Long advertiserId);
}
