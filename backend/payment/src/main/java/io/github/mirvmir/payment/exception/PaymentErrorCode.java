package io.github.mirvmir.payment.exception;

import io.github.mirvmir.common.exception.ErrorCode;

public enum PaymentErrorCode implements ErrorCode {
    CARD_NOT_FOUND("CARD_NOT_FOUND",
            "Card not found"),
    DEFAULT_CARD_NOT_FOUND("DEFAULT_CARD_NOT_FOUND",
            "Default card not found"),
    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND",
            "Payment card not found"),
    PAYOUT_NOT_FOUND("PAYOUT_NOT_FOUND",
            "Payout not found"),
    REFUND_NOT_FOUND("REFUND_NOT_FOUND",
            "Refund not found");

    private final String code;
    private final String message;

    PaymentErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
