package io.github.mirvmir.enrollment.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.enrollment.application.properties.BookingProperties;
import io.github.mirvmir.enrollment.application.service.interfaces.EnrollmentService;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@AllArgsConstructor
@Slf4j
@Service
public class DefaultEnrollmentService implements EnrollmentService {

    private final ActivityEnrollmentRepository activityEnrollmentRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final OrderRepository orderRepository;

    private final BookingProperties bookingProperties;

    private final PaymentApi paymentApi;

    @Override
    @Transactional
    public void markPayed(Long orderId, Instant now) {
        log.info("Start payment success handling: orderId={}, paidAt={}",
                orderId,
                now);

        Order order = getExistingOrderForUpdate(orderId);

        if (order.isRefunded()) {
            log.info("Payment success event ignored because order is already refunded: orderId={}",
                    orderId);
            return;
        }

        if (order.isRefundRequired()) {
            log.info("Payment success event ignored because refund is already requested: orderId={}",
                    orderId);
            return;
        }

        if (order.isPayed()) {
            log.info("Payment success event ignored because order is already paid: orderId={}",
                    orderId);
            return;
        }

        if (order.isCancelled()) {
            log.info("Payment succeeded after order cancellation, refund will be requested: orderId={}, targetType={}",
                    orderId,
                    order.getTargetType());
            refundOrder(order, "Заказ был отменен");
            orderRepository.saveOrUpdate(order);
            return;
        }

        if (order.isExpiredStatus() || order.isExpired(now)) {
            log.error("Payment succeeded after booking expiration, order will be refunded: orderId={}, expiresAt={}, paidAt={}",
                    orderId,
                    order.getExpiresAt(),
                    now);

            if (!order.isExpiredStatus()) {
                order.expire(now);
                expireEnrollment(order, now);
            }

            refundOrder(order, "Бронирование истекло до подтверждения оплаты");
            orderRepository.saveOrUpdate(order);
            return;
        }

        order.markPayed(now);

        if (OrderTargetType.COURSE == order.getTargetType()) {
            CourseEnrollment enrollment = getCourseEnrollment(order);

            if (enrollment.isCancelled()) {
                log.info("Payment succeeded for cancelled course enrollment, refund will be requested: orderId={}, enrollmentId={}",
                        orderId,
                        enrollment.getId());
                refundOrder(order, "Заказ был отменен");
                orderRepository.saveOrUpdate(order);
                return;
            }

            enrollment.confirmPayment(now, bookingProperties.getBookingExpiresMinutes());
            courseEnrollmentRepository.saveOrUpdate(enrollment);
            orderRepository.saveOrUpdate(order);
            return;
        }

        if (OrderTargetType.ACTIVITY_SLOT == order.getTargetType()) {
            ActivityEnrollment enrollment = getActivityEnrollment(order);

            if (enrollment.isCancelled()) {
                log.info("Payment succeeded for cancelled activity enrollment, refund will be requested: orderId={}, enrollmentId={}",
                        orderId,
                        enrollment.getId());
                refundOrder(order, "Заказ был отменен");
                orderRepository.saveOrUpdate(order);
                return;
            }

            enrollment.confirmPayment(now, bookingProperties.getBookingExpiresMinutes());
            activityEnrollmentRepository.saveOrUpdate(enrollment);
            orderRepository.saveOrUpdate(order);
            return;
        }

        log.error("Payment success cannot be applied because order target type is unsupported: orderId={}, targetType={}",
                orderId,
                order.getTargetType());
        throw new IllegalStateException("Unsupported order target type: " + order.getTargetType());
    }

    @Override
    @Transactional
    public void markRefunded(Long orderId) {
        log.info("Start refund confirmation handling: orderId={}",
                orderId);

        Order order = getExistingOrderForUpdate(orderId);

        try {
            order.markRefunded();
        } catch (RuntimeException exception) {
            log.error("Refund confirmation rejected by order state: orderId={}, status={}",
                    orderId,
                    order.getStatus(),
                    exception);
            throw exception;
        }

        orderRepository.saveOrUpdate(order);
    }

    private void expireEnrollment(Order order, Instant now) {
        if (OrderTargetType.COURSE == order.getTargetType()) {
            CourseEnrollment enrollment = getCourseEnrollment(order);
            enrollment.expire(now, bookingProperties.getBookingExpiresMinutes());
            courseEnrollmentRepository.saveOrUpdate(enrollment);
            return;
        }

        if (OrderTargetType.ACTIVITY_SLOT == order.getTargetType()) {
            ActivityEnrollment enrollment = getActivityEnrollment(order);
            enrollment.expire(now, bookingProperties.getBookingExpiresMinutes());
            activityEnrollmentRepository.saveOrUpdate(enrollment);
            return;
        }

        log.error("Enrollment expiration skipped because order target type is unsupported: orderId={}, targetType={}",
                order.getId(),
                order.getTargetType());
        throw new IllegalStateException("Unsupported order target type: " + order.getTargetType());
    }

    private void refundOrder(Order order, String reason) {
        log.info("Requesting payment refund: orderId={}, amount={}, currency={}, reason={}",
                order.getId(),
                order.getAmount(),
                order.getCurrency(),
                reason);

        try {
            paymentApi.refundPayment(
                    new CreateRefundRequest(
                            order.getId(),
                            order.getAmount(),
                            order.getCurrency(),
                            reason
                    )
            );
        } catch (RuntimeException exception) {
            log.error("Payment refund request failed: orderId={}, targetType={}, targetId={}",
                    order.getId(),
                    order.getTargetType(),
                    order.getTargetId(),
                    exception);
            throw exception;
        }

        order.markRefundRequired();
    }

    private CourseEnrollment getCourseEnrollment(Order order) {
        CourseEnrollment enrollment =
                courseEnrollmentRepository.findById(order.getEnrollmentId());

        if (enrollment == null) {
            log.error("Course enrollment referenced by order was not found: orderId={}, enrollmentId={}",
                    order.getId(),
                    order.getEnrollmentId());
            throw new NotFoundException(EnrollmentErrorCode.ENROLLMENT_NOT_FOUND);
        }

        return enrollment;
    }

    private ActivityEnrollment getActivityEnrollment(Order order) {
        ActivityEnrollment enrollment =
                activityEnrollmentRepository.findById(order.getEnrollmentId());

        if (enrollment == null) {
            log.error("Activity enrollment referenced by order was not found: orderId={}, enrollmentId={}",
                    order.getId(),
                    order.getEnrollmentId());
            throw new NotFoundException(EnrollmentErrorCode.ENROLLMENT_NOT_FOUND);
        }

        return enrollment;
    }

    private Order getExistingOrderForUpdate(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId);

        if (order == null) {
            log.error("Payment event references unknown order: orderId={}",
                    orderId);
            throw new NotFoundException(
                    EnrollmentErrorCode.ORDER_NOT_FOUND
            );
        }

        return order;
    }
}
