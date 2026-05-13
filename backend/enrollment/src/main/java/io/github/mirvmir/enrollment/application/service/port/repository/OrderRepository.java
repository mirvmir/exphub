package io.github.mirvmir.enrollment.application.service.port.repository;

import io.github.mirvmir.enrollment.domain.order.Order;
import io.github.mirvmir.enrollment.domain.order.OrderTargetType;

import java.time.Instant;
import java.util.List;

public interface OrderRepository {
   Order saveOrUpdate(Order order);
   Order findById(Long id);
    Order findByIdForUpdate(Long id);
    List<Order> findExpiredForUpdate(Instant now, int limit);
   Order findByEnrollmentId(Long enrollmentId,
                            OrderTargetType type);
    Order findByEnrollmentIdForUpdate(Long enrollmentId, OrderTargetType type);
}
