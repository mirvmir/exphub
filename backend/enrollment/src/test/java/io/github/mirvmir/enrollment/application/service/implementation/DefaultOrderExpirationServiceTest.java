package io.github.mirvmir.enrollment.application.service.implementation;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DefaultOrderExpirationServiceTest {

    private OrderRepository orderRepository;
    private ActivityEnrollmentRepository activityEnrollmentRepository;
    private CourseEnrollmentRepository courseEnrollmentRepository;
    private BookingProperties bookingProperties;

    private DefaultOrderExpirationService service;

    private final Instant now = Instant.parse("2026-05-13T10:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        activityEnrollmentRepository = mock(ActivityEnrollmentRepository.class);
        courseEnrollmentRepository = mock(CourseEnrollmentRepository.class);
        bookingProperties = mock(BookingProperties.class);

        service = new DefaultOrderExpirationService(
                orderRepository,
                activityEnrollmentRepository,
                courseEnrollmentRepository,
                clock,
                bookingProperties
        );
    }

    @Test
    void expireOrders_shouldDoNothing_whenNoExpiredOrdersFound() {
        when(orderRepository.findExpiredForUpdate(now)).thenReturn(List.of());

        service.deleteExpiredOrders();

        verify(orderRepository).findExpiredForUpdate(now);
        verify(orderRepository, never()).saveOrUpdate(any());
        verifyNoInteractions(activityEnrollmentRepository, courseEnrollmentRepository);
    }

    @Test
    void expireOrders_shouldExpireCourseOrderAndEnrollment() {
        Order order = expiredCourseOrder();
        CourseEnrollment enrollment = bookedExpiredCourseEnrollment();

        when(orderRepository.findExpiredForUpdate(now)).thenReturn(List.of(order));
        when(courseEnrollmentRepository.findById(10L)).thenReturn(enrollment);
        when(bookingProperties.getBookingExpiresMinutes()).thenReturn(15L);

        service.deleteExpiredOrders();

        assertEquals(OrderStatus.EXPIRED, order.getStatus());
        assertEquals(CourseEnrollmentStatus.EXPIRED, enrollment.getStatus());

        verify(orderRepository).saveOrUpdate(order);
        verify(courseEnrollmentRepository).saveOrUpdate(enrollment);
        verifyNoInteractions(activityEnrollmentRepository);
    }

    @Test
    void expireOrders_shouldExpireActivityOrderAndEnrollment() {
        Order order = expiredActivityOrder();
        ActivityEnrollment enrollment = bookedExpiredActivityEnrollment();

        when(orderRepository.findExpiredForUpdate(now)).thenReturn(List.of(order));
        when(activityEnrollmentRepository.findById(10L)).thenReturn(enrollment);
        when(bookingProperties.getBookingExpiresMinutes()).thenReturn(15L);

        service.deleteExpiredOrders();

        assertEquals(OrderStatus.EXPIRED, order.getStatus());
        assertEquals(ActivityEnrollmentStatus.EXPIRED, enrollment.getStatus());

        verify(orderRepository).saveOrUpdate(order);
        verify(activityEnrollmentRepository).saveOrUpdate(enrollment);
        verifyNoInteractions(courseEnrollmentRepository);
    }

    @Test
    void expireOrders_shouldKeepOrderExpired_whenCourseEnrollmentIsMissing() {
        Order order = expiredCourseOrder();

        when(orderRepository.findExpiredForUpdate(now)).thenReturn(List.of(order));
        when(courseEnrollmentRepository.findById(10L)).thenReturn(null);

        service.deleteExpiredOrders();

        assertEquals(OrderStatus.EXPIRED, order.getStatus());
        verify(orderRepository).saveOrUpdate(order);
        verify(courseEnrollmentRepository, never()).saveOrUpdate(any());
    }

    private Order expiredCourseOrder() {
        return Order.load(
                20L,
                1L,
                10L,
                OrderTargetType.COURSE,
                100L,
                1000L,
                new BigDecimal("3000"),
                Currency.getInstance("RUB"),
                OrderStatus.CREATED,
                now.minusSeconds(3600),
                now.minusSeconds(60)
        );
    }

    private Order expiredActivityOrder() {
        return Order.load(
                21L,
                1L,
                10L,
                OrderTargetType.ACTIVITY_SLOT,
                200L,
                null,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                OrderStatus.PAYMENT_PROCESSING,
                now.minusSeconds(3600),
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

    private ActivityEnrollment bookedExpiredActivityEnrollment() {
        return ActivityEnrollment.load(
                10L,
                100L,
                200L,
                1L,
                ActivityEnrollmentStatus.BOOKED,
                now.minusSeconds(3600)
        );
    }
}
