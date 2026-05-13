package io.github.mirvmir.payment.application.service.port.repository;

import io.github.mirvmir.payment.domain.Payment;

import java.util.List;

public interface PaymentRepository {
    Payment save(Payment payment);
    Payment findById(Long id);
    Payment findByExternalPaymentId(String externalPaymentId);
    List<Payment> findByUserId(Long userId);
    Payment findByOrderId(Long orderId);
}
