package com.cts.adstudio.creative_delivery.creative.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.adstudio.creative_delivery.creative.entity.CreativeApproval;

public interface CreativeApprovalRepository extends JpaRepository<CreativeApproval, Long> {
}