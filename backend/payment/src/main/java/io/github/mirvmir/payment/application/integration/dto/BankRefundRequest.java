package io.github.mirvmir.payment.application.integration.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record BankRefundRequest(
        String externalPaymentId,
        String refundId,
        BigDecimal amount,
        Currency currency,
        String reason,
        String webhookUrl
) {
}