package io.github.mirvmir.enrollment.domain.order;

public enum OrderStatus {
    CREATED,
    PAYMENT_PROCESSING,
    PAYED,
    REFUND_REQUIRED,
    REFUNDED,
    CANCELLED,
    EXPIRED
}