package com.cts.adstudio.creative_delivery.creative.dto;

import jakarta.validation.constraints.*;

public record LinkRequest(
        @NotNull Long assetId,
        @NotNull Long lineItemId
) {}