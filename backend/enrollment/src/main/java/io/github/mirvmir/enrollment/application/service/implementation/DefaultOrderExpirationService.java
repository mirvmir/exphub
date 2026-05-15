package io.github.mirvmir.enrollment.application.service.implementation;

import io.github.mirvmir.enrollment.application.properties.BookingProperties;
import io.github.mirvmir.enrollment.application.service.interfaces.OrderExpirationService;
import io.github.mirvmir.enrollment.application.service.port.repository.ActivityEnrollmentRepository;
import io.github.mirvmir.enrollment.application.service.port.repository.CourseEnrollmentRepository;
import io.github.mirvmir.enrollment.application.service.port.repository.OrderRepository;
import io.github.mirvmir.enrollment.domain.ActivityEnrollment;
import io.github.mirvmir.enrollment.domain.CourseEnrollment;
import io.github.mirvmir.enrollment.domain.order.Order;
import io.github.mirvmir.enrollment.domain.order.OrderTargetType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Slf4j
@Service
public class DefaultOrderExpirationService
        implements OrderExpirationService {

    private final OrderRepository orderRepository;
    private final ActivityEnrollmentRepository activityEnrollmentRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;

    private final Clock clock;

    private final BookingProperties bookingProperties;

    @Override
    @Transactional
    public void deleteExpiredOrders() {
        Instant now = Instant.now(clock);
        log.info("Start expiring overdue orders: now={}",
                now);

        List<Order> orders = orderRepository.findExpiredForUpdate(now);

        if (orders.isEmpty()) {
            log.info("No overdue orders found for expiration: now={}", now);
            return;
        }

        for (Order order : orders) {
            try {
                expireOrder(order, now);
            } catch (RuntimeException exception) {
                log.error("Failed to expire overdue order: orderId={}, targetType={}, enrollmentId={}",
                        order.getId(),
                        order.getTargetType(),
                        order.getEnrollmentId(),
                        exception);
                throw exception;
            }
        }
    }

    private void expireOrder(Order order, Instant now) {
        log.info("Expiring overdue order: orderId={}, targetType={}, expiresAt={}",
                order.getId(),
                order.getTargetType(),
                order.getExpiresAt());

        order.expire(now);
        orderRepository.saveOrUpdate(order);

        if (OrderTargetType.ACTIVITY_SLOT == order.getTargetType()) {
            ActivityEnrollment activity =
                    activityEnrollmentRepository.findById(order.getEnrollmentId());

            if (activity == null) {
                log.error("Activity enrollment for expired order was not found: orderId={}, enrollmentId={}",
                        order.getId(),
                        order.getEnrollmentId());
                return;
            }

            activity.expire(now, bookingProperties.getBookingExpiresMinutes());
            activityEnrollmentRepository.saveOrUpdate(activity);
            return;
        }

        if (OrderTargetType.COURSE == order.getTargetType()) {
            CourseEnrollment course =
                    courseEnrollmentRepository.findById(order.getEnrollmentId());

            if (course == null) {
                log.error("Course enrollment for expired order was not found: orderId={}, enrollmentId={}",
                        order.getId(),
                        order.getEnrollmentId());
                return;
            }

            course.expire(now, bookingProperties.getBookingExpiresMinutes());
            courseEnrollmentRepository.saveOrUpdate(course);
            return;
        }

        log.error("Expired order has unsupported target type: orderId={}, targetType={}",
                order.getId(),
                order.getTargetType());
        throw new IllegalStateException("Unsupported order target type: " + order.getTargetType());
    }
}
