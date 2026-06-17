package com.cts.adstudio.finance.billing.exception;

import com.cts.adstudio.finance.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/** Domain-rule violation in billing (e.g. nothing to invoice, bad commission rate). HTTP 422. */
public class BillingRuleException extends ApiException {
    public BillingRuleException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
