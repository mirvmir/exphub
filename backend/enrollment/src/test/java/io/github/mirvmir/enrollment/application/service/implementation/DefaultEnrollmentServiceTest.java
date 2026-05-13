package io.github.mirvmir.enrollment.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.enrollment.application.properties.BookingProperties;
import io.github.mirvmir.enrollment.application.service.port.repository.ActivityEnrollmentRepository;
import io.github.mirvmir.enrollment.application.service.port.repository.CourseEnrollmentRepository;
import io.github.mirvmir.enrollment.application.service.port.repository.OrderRepository;
import io.github.mirvmir.enrollment.domain.ActivityEnrollment;
import io.github.mirvmir.enrollment.domain.ActivityEnrollmentStatus;
import io.github.mirvmir.enrollment.domain.CourseEnrollment;
import io.github.mirvmir.enrollment.domain.CourseEnrollmentStatus;
import io.github.mirvmir.enrollment.domain.order.Order;
import io.github.mirvmir.enrollment.domain.order.OrderStatus;
import io.github.mirvmir.enrollment.domain.order.OrderTargetType;
import io.github.mirvmir.payment.api.PaymentApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultEnrollmentServiceTest {

    private ActivityEnrollmentRepository activityEnrollmentRepository;
    private CourseEnrollmentRepository courseEnrollmentRepository;
    private OrderRepository orderRepository;
    private PaymentApi paymentApi;
    private BookingProperties bookingProperties;

    private DefaultEnrollmentService service;

    private final Instant now = Instant.parse("2026-05-13T10:00:00Z");

    @BeforeEach
    void setUp() {
        activityEnrollmentRepository = mock(ActivityEnrollmentRepository.class);
        courseEnrollmentRepository = mock(CourseEnrollmentRepository.class);
        orderRepository = mock(OrderRepository.class);
        paymentApi = mock(PaymentApi.class);
        bookingProperties = mock(BookingProperties.class);

        service = new DefaultEnrollmentService(
                activityEnrollmentRepository,
                courseEnrollmentRepository,
                orderRepository,
                bookingProperties,
                paymentApi
        );
    }

    @Test
    void markPayed_shouldUseLockedOrderAndConfirmCourseEnrollment() {
        Order order = courseOrder(OrderStatus.CREATED);
        CourseEnrollment enrollment = bookedCourseEnrollment();

        when(orderRepository.findByIdForUpdate(20L)).thenReturn(order);
        when(courseEnrollmentRepository.findById(10L)).thenReturn(enrollment);
        when(bookingProperties.getBookingExpiresMinutes()).thenReturn(15L);

        service.markPayed(20L, now);

        assertEquals(OrderStatus.PAYED, order.getStatus());
        assertEquals(CourseEnrollmentStatus.PAYED, enrollment.getStatus());

        verify(orderRepository).findByIdForUpdate(20L);
        verify(orderRepository, never()).findById(20L);
        verify(courseEnrollmentRepository).saveOrUpdate(enrollment);
        verify(orderRepository).saveOrUpdate(order);
        verifyNoInteractions(paymentApi);
    }

    @Test
    void markPayed_shouldUseLockedOrderAndConfirmActivityEnrollment() {
        Order order = activityOrder(OrderStatus.PAYMENT_PROCESSING);
        ActivityEnrollment enrollment = bookedActivityEnrollment();

        when(orderRepository.findByIdForUpdate(20L)).thenReturn(order);
        when(activityEnrollmentRepository.findById(10L)).thenReturn(enrollment);
        when(bookingProperties.getBookingExpiresMinutes()).thenReturn(15L);

        service.markPayed(20L, now);

        assertEquals(OrderStatus.PAYED, order.getStatus());
        assertEquals(ActivityEnrollmentStatus.PAYED, enrollment.getStatus());

        verify(orderRepository).findByIdForUpdate(20L);
        verify(activityEnrollmentRepository).saveOrUpdate(enrollment);
        verify(orderRepository).saveOrUpdate(order);
        verifyNoInteractions(paymentApi);
    }

    @Test
    void markPayed_shouldRequestRefund_whenPaymentSucceededAfterOrderCancellation() {
        Order order = courseOrder(OrderStatus.CANCELLED);

        when(orderRepository.findByIdForUpdate(20L)).thenReturn(order);

        service.markPayed(20L, now);

        assertEquals(OrderStatus.REFUND_REQUIRED, order.getStatus());

        verify(paymentApi).refundPayment(argThat(actual ->
                actual.orderId().equals(20L)
                        && actual.amount().equals(new BigDecimal("3000"))
                        && actual.currency().equals(Currency.getInstance("RUB"))
                        && actual.reason().equals("Заказ был отменен")
        ));
        verify(orderRepository).saveOrUpdate(order);
        verifyNoInteractions(courseEnrollmentRepository, activityEnrollmentRepository);
    }

    @Test
    void markPayed_shouldRequestRefund_whenPaymentSucceededAfterCourseBookingExpired() {
        Order order = expiredCourseOrder(OrderStatus.CREATED);
        CourseEnrollment enrollment = bookedExpiredCourseEnrollment();

        when(orderRepository.findByIdForUpdate(20L)).thenReturn(order);
        when(courseEnrollmentRepository.findById(10L)).thenReturn(enrollment);
        when(bookingProperties.getBookingExpiresMinutes()).thenReturn(15L);

        service.markPayed(20L, now);

        assertEquals(OrderStatus.REFUND_REQUIRED, order.getStatus());
        assertEquals(CourseEnrollmentStatus.EXPIRED, enrollment.getStatus());

        verify(paymentApi).refundPayment(argThat(actual ->
                actual.orderId().equals(20L)
                        && actual.reason().equals("Бронирование истекло до подтверждения оплаты")
        ));
        verify(courseEnrollmentRepository).saveOrUpdate(enrollment);
        verify(orderRepository).saveOrUpdate(order);
    }

    @Test
    void markPayed_shouldRequestRefund_whenPaymentSucceededForCancelledCourseEnrollment() {
        Order order = courseOrder(OrderStatus.CREATED);
        CourseEnrollment enrollment = CourseEnrollment.load(
                10L,
                100L,
                1000L,
                1L,
                CourseEnrollmentStatus.CANCELLED,
                BigDecimal.ZERO,
                now.minusSeconds(60)
        );

        when(orderRepository.findByIdForUpdate(20L)).thenReturn(order);
        when(courseEnrollmentRepository.findById(10L)).thenReturn(enrollment);

        service.markPayed(20L, now);

        assertEquals(OrderStatus.REFUND_REQUIRED, order.getStatus());

        verify(paymentApi).refundPayment(argThat(actual ->
                actual.orderId().equals(20L)
                        && actual.reason().equals("Заказ был отменен")
        ));
        verify(courseEnrollmentRepository, never()).saveOrUpdate(any());
        verify(orderRepository).saveOrUpdate(order);
    }

    @Test
    void markPayed_shouldIgnoreAlreadyPayedOrder() {
        Order order = courseOrder(OrderStatus.PAYED);

        when(orderRepository.findByIdForUpdate(20L)).thenReturn(order);

        service.markPayed(20L, now);

        assertEquals(OrderStatus.PAYED, order.getStatus());
        verify(orderRepository, never()).saveOrUpdate(any());
        verifyNoInteractions(courseEnrollmentRepository, activityEnrollmentRepository, paymentApi);
    }

    @Test
    void markPayed_shouldIgnoreAlreadyRefundRequiredOrder() {
        Order order = courseOrder(OrderStatus.REFUND_REQUIRED);

        when(orderRepository.findByIdForUpdate(20L)).thenReturn(order);

        service.markPayed(20L, now);

        assertEquals(OrderStatus.REFUND_REQUIRED, order.getStatus());
        verify(orderRepository, never()).saveOrUpdate(any());
        verifyNoInteractions(courseEnrollmentRepository, activityEnrollmentRepository, paymentApi);
    }

    @Test
    void markPayed_shouldThrowNotFound_whenOrderNotFound() {
        when(orderRepository.findByIdForUpdate(20L)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> service.markPayed(20L, now));

        verify(orderRepository).findByIdForUpdate(20L);
        verifyNoInteractions(paymentApi);
    }

    @Test
    void markPayed_shouldThrowNotFound_whenEnrollmentNotFound() {
        Order order = courseOrder(OrderStatus.CREATED);

        when(orderRepository.findByIdForUpdate(20L)).thenReturn(order);
        when(courseEnrollmentRepository.findById(10L)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> service.markPayed(20L, now));

        verify(orderRepository, never()).saveOrUpdate(any());
        verifyNoInteractions(paymentApi);
    }

    @Test
    void markRefunded_shouldUseLockedOrderAndMarkOrderRefunded() {
        Order order = courseOrder(OrderStatus.REFUND_REQUIRED);

        when(orderRepository.findByIdForUpdate(20L)).thenReturn(order);

        service.markRefunded(20L);

        assertEquals(OrderStatus.REFUNDED, order.getStatus());
        verify(orderRepository).findByIdForUpdate(20L);
        verify(orderRepository).saveOrUpdate(order);
    }

    @Test
    void markRefunded_shouldBeIdempotent_whenOrderAlreadyRefunded() {
        Order order = courseOrder(OrderStatus.REFUNDED);

        when(orderRepository.findByIdForUpdate(20L)).thenReturn(order);

        service.markRefunded(20L);

        assertEquals(OrderStatus.REFUNDED, order.getStatus());
        verify(orderRepository).saveOrUpdate(order);
    }

    private CourseEnrollment bookedCourseEnrollment() {
        return CourseEnrollment.load(
                10L,
                100L,
                1000L,
                1L,
                CourseEnrollmentStatus.BOOKED,
                BigDecimal.ZERO,
                now.minusSeconds(60)
        );
    }

    private CourseEnrollment bookedExpiredCourseEnrollment() {
        return CourseEnrollment.load(
                10L,
                100L,
                1000L,
                1L,
                CourseEnrollmentStatus.BOOKED,
                BigDecimal.ZERO,
                now.minusSeconds(3600)
        );
    }

    private ActivityEnrollment bookedActivityEnrollment() {
        return ActivityEnrollment.load(
                10L,
                200L,
                1L,
                ActivityEnrollmentStatus.BOOKED,
                now.minusSeconds(60)
        );
    }

    private Order courseOrder(OrderStatus status) {
        return Order.load(
                20L,
                1L,
                10L,
                OrderTargetType.COURSE,
                100L,
                1000L,
                new BigDecimal("3000"),
                Currency.getInstance("RUB"),
                status,
                now.minusSeconds(60),
                now.plusSeconds(600)
        );
    }

    private Order expiredCourseOrder(OrderStatus status) {
        return Order.load(
                20L,
                1L,
                10L,
                OrderTargetType.COURSE,
                100L,
                1000L,
                new BigDecimal("3000"),
                Currency.getInstance("RUB"),
                status,
                now.minusSeconds(3600),
                now.minusSeconds(60)
        );
    }

    private Order activityOrder(OrderStatus status) {
        return Order.load(
                20L,
                1L,
                10L,
                OrderTargetType.ACTIVITY_SLOT,
                200L,
                null,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                status,
                now.minusSeconds(60),
                now.plusSeconds(600)
        );
    }
}
