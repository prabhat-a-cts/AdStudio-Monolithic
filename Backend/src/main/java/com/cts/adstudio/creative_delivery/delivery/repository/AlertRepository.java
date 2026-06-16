package com.cts.adstudio.creative_delivery.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.adstudio.creative_delivery.delivery.entity.PacingAlert;

import java.util.List;

public interface AlertRepository extends JpaRepository<PacingAlert, Long> {

    // ✅ Optional: fetch alerts by line item
    List<PacingAlert> findByLineItemId(Long lineItemId);
}