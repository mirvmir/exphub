package io.github.mirvmir.payment.application.service.port.repository;

import io.github.mirvmir.payment.domain.Refund;
import io.github.mirvmir.payment.domain.RefundStatus;

import java.util.Collection;

public interface RefundRepository {
    Refund saveOrUpdate(Refund refund);
    Refund findById(Long id);
    Refund findByExternalRefundId(String externalRefundId);
    boolean existsByPaymentIdAndStatusIn(Long paymentId,
                                         Collection<RefundStatus> statuses);
}
