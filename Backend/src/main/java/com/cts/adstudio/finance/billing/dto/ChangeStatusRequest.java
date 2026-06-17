package com.cts.adstudio.finance.billing.dto;

import jakarta.validation.constraints.NotBlank;

/** Status-change body, e.g. { "status": "ISSUED" }  (Backend Plan §9 API conventions). */
public record ChangeStatusRequest(@NotBlank String status) {}
