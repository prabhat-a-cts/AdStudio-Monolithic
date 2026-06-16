package com.cts.adstudio.creative_delivery.creative.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.adstudio.creative_delivery.creative.entity.*;

public interface AssetLineItemLinkRepository extends JpaRepository<AssetLineItemLink, Long> {

    boolean existsByAssetAndLineItemId(CreativeAsset asset, Long lineItemId);
}