package com.cts.adstudio.creative_delivery.delivery.dto;

public record DeliveryRequest(Long lineItemId, Integer targetImpressions, Integer deliveredImpressions) {
    
}