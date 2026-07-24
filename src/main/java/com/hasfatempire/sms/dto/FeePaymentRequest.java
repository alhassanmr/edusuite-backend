package com.hasfatempire.sms.dto;

import java.math.BigDecimal;

public record FeePaymentRequest(
        BigDecimal amount,
        String method,
        String reference
) {}
