package com.cts.adstudio.creative_delivery.creative.dto;

import jakarta.validation.constraints.*;

public record ApprovalRequest(
        @NotNull Long assetId,
        @NotBlank String decision
) {}