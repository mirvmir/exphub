package io.github.mirvmir.payment.domain;

public enum PaymentStatus {
    CREATED,
    CANCELLED,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    EXPIRED
}