package io.github.mirvmir.enrollment.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.enrollment.application.properties.BookingProperties;
import io.github.mirvmir.enrollment.application.service.port.repository.ActivityEnrollmentRepository;
import io.github.mirvmir.enrollment.application.service.port.repository.CourseEnrollmentRepository;
import io.github.mirvmir.enrollment.application.service.port.repository.OrderRepository;
import io.github.mirvmir.enrollment.domain.ActivityEnrollment;
import io.github.mirvmir.enrollment.domain.CourseEnrollment;
import io.github.mirvmir.enrollment.domain.order.Order;
import io.github.mirvmir.enrollment.domain.order.OrderTargetType;
import io.github.mirvmir.enrollment.exception.EnrollmentErrorCode;
import io.github.mirvmir.payment.api.PaymentApi;
import io.github.mirvmir.payment.api.dto.CreateRefundRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

@Slf4j
@Service
@AllArgsConstructor
public class RefundOrderService {

    private final ActivityEnrollmentRepository activityEnrollmentRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final OrderRepository orderRepository;

    private final BookingProperties bookingProperties;

    private final PaymentApi paymentApi;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRefundRequired(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId);

        if (order == null) {
            throw new NotFoundException(EnrollmentErrorCode.ORDER_NOT_FOUND);
        }

        if (order.isRefundRequired()) {
            log.info("Order already has REFUND_REQUIRED status: orderId={}", orderId);
            return;
        }

        if (order.isRefunded()) {
            log.info("Order already refunded, REFUND_REQUIRED skipped: orderId={}", orderId);
            return;
        }

        order.markRefundRequired();
        orderRepository.saveOrUpdate(order);

        log.info("Order marked as REFUND_REQUIRED: orderId={}", orderId);
    }

    public void requestRefund(Long orderId,
                              BigDecimal amount,
                              Currency currency,
                              String reason) {
        log.info("Requesting payment refund: orderId={}, amount={}, currency={}, reason={}",
                orderId,
                amount,
                currency,
                reason);

        paymentApi.refundPayment(
                new CreateRefundRequest(
                        orderId,
                        amount,
                        currency,
                        reason
                )
        );
    }

    public void markExpiredAndRefundRequired(Long orderId, Instant now) {
        Order order = orderRepository.findByIdForUpdate(orderId);

        if (order == null) {
            throw new NotFoundException(EnrollmentErrorCode.ORDER_NOT_FOUND);
        }

        order.expire(now);

        if (OrderTargetType.COURSE == order.getTargetType()) {
            CourseEnrollment enrollment =
                    courseEnrollmentRepository.findById(order.getEnrollmentId());

            if (enrollment == null) {
                log.error("Course enrollment referenced by order was not found: orderId={}, enrollmentId={}",
                        order.getId(),
                        order.getEnrollmentId());
                throw new NotFoundException(EnrollmentErrorCode.ENROLLMENT_NOT_FOUND);
            }

            enrollment.expire(now, bookingProperties.getBookingExpiresMinutes());
            courseEnrollmentRepository.saveOrUpdate(enrollment);
            return;
        }

        if (OrderTargetType.ACTIVITY_SLOT == order.getTargetType()) {
            ActivityEnrollment enrollment =
                    activityEnrollmentRepository.findById(order.getEnrollmentId());

            if (enrollment == null) {
                log.error("Activity enrollment referenced by order was not found: orderId={}, enrollmentId={}",
                        order.getId(),
                        order.getEnrollmentId());
                throw new NotFoundException(EnrollmentErrorCode.ENROLLMENT_NOT_FOUND);
            }

            enrollment.expire(now, bookingProperties.getBookingExpiresMinutes());
            activityEnrollmentRepository.saveOrUpdate(enrollment);
            return;
        }

        log.error("Enrollment expiration skipped because order target type is unsupported: orderId={}, targetType={}",
                order.getId(),
                order.getTargetType());
        throw new IllegalStateException("Unsupported order target type: " + order.getTargetType());
    }
}