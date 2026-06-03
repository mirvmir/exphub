package io.github.mirvmir.enrollment.application.service.implementation;

import io.github.mirvmir.activity.api.ActivityApi;
import io.github.mirvmir.activity.api.dto.ActivityBookingInfoResponse;
import io.github.mirvmir.activity.api.dto.ActivitySlotBookingInfoResponse;
import io.github.mirvmir.activity.api.dto.CreatedActivitySlotResponse;
import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.course.api.CourseApi;
import io.github.mirvmir.course.api.dto.CourseBookingInfoResponse;
import io.github.mirvmir.enrollment.application.properties.BookingProperties;
import io.github.mirvmir.enrollment.application.service.port.repository.ActivityEnrollmentRepository;
import io.github.mirvmir.enrollment.application.service.port.repository.CourseEnrollmentRepository;
import io.github.mirvmir.enrollment.application.service.port.repository.OrderRepository;
import io.github.mirvmir.enrollment.domain.ActivityEnrollment;
import io.github.mirvmir.enrollment.domain.CourseEnrollment;
import io.github.mirvmir.enrollment.domain.order.Order;
import io.github.mirvmir.enrollment.domain.order.OrderStatus;
import io.github.mirvmir.enrollment.domain.order.OrderTargetType;
import io.github.mirvmir.enrollment.web.request.BookIndividualActivityRequest;
import io.github.mirvmir.enrollment.web.response.BookingResponse;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.payment.api.PaymentApi;
import io.github.mirvmir.payment.api.dto.CreatePaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultEnrollmentBookingServiceTest {

    private IdentityApi identityApi;
    private CourseApi courseApi;
    private ActivityApi activityApi;
    private PaymentApi paymentApi;
    private CourseEnrollmentRepository courseEnrollmentRepository;
    private ActivityEnrollmentRepository activityEnrollmentRepository;
    private OrderRepository orderRepository;
    private BookingProperties bookingProperties;

    private DefaultEnrollmentBookingService service;

    private final Instant now = Instant.parse("2026-05-13T10:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        identityApi = mock(IdentityApi.class);
        courseApi = mock(CourseApi.class);
        activityApi = mock(ActivityApi.class);
        paymentApi = mock(PaymentApi.class);
        courseEnrollmentRepository = mock(CourseEnrollmentRepository.class);
        activityEnrollmentRepository = mock(ActivityEnrollmentRepository.class);
        orderRepository = mock(OrderRepository.class);
        bookingProperties = mock(BookingProperties.class);

        service = new DefaultEnrollmentBookingService(
                identityApi,
                courseApi,
                activityApi,
                paymentApi,
                courseEnrollmentRepository,
                activityEnrollmentRepository,
                orderRepository,
                clock,
                bookingProperties
        );
    }

    @Test
    void bookCourse_shouldCreateEnrollmentOrderAndPayment() {
        CourseBookingInfoResponse course = new CourseBookingInfoResponse(
                100L,
                1000L,
                2L,
                "Курс",
                new BigDecimal("3000"),
                Currency.getInstance("RUB"),
                true
        );

        CourseEnrollment savedEnrollment = CourseEnrollment.create(
                now,
                100L,
                1000L,
                1L
        );
        savedEnrollment.assignId(10L);

        Order savedOrder = Order.createForCourse(
                1L,
                10L,
                100L,
                1000L,
                new BigDecimal("3000"),
                Currency.getInstance("RUB"),
                now,
                15
        );

        savedOrder = Order.load(
                20L,
                savedOrder.getUserId(),
                savedOrder.getEnrollmentId(),
                savedOrder.getTargetType(),
                savedOrder.getTargetId(),
                savedOrder.getTargetVersionId(),
                savedOrder.getAmount(),
                savedOrder.getCurrency(),
                savedOrder.getStatus(),
                savedOrder.getCreatedAt(),
                savedOrder.getExpiresAt()
        );

        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(courseApi.getInfo(100L)).thenReturn(course);
        when(courseEnrollmentRepository.existsActiveByUserIdAndCourseId(1L, 100L, now))
                .thenReturn(false);
        when(courseEnrollmentRepository.saveOrUpdate(any(CourseEnrollment.class)))
                .thenReturn(savedEnrollment);
        when(orderRepository.saveOrUpdate(any(Order.class))).thenReturn(savedOrder);
        when(paymentApi.createPayment(any()))
                .thenReturn(new CreatePaymentResponse(30L, "CREATED"));

        BookingResponse result = service.bookCourse(100L);

        assertEquals(20L, result.orderId());
        assertEquals(30L, result.paymentId());
        assertEquals("COURSE", result.targetType());
        assertEquals("CREATED", result.orderStatus());

        verify(courseEnrollmentRepository).saveOrUpdate(any(CourseEnrollment.class));
        verify(orderRepository).saveOrUpdate(any(Order.class));
        verify(paymentApi).createPayment(argThat(actual ->
                actual.userId().equals(1L)
                && actual.amount().equals(new BigDecimal("3000"))
                && actual.description().equals("Оплата курса: Курс")
                && actual.orderId().equals(20L)
        ));
    }

    @Test
    void bookCourse_shouldConfirmEnrollmentWithoutPayment_whenCourseIsFree() {
        CourseBookingInfoResponse course = new CourseBookingInfoResponse(
                100L,
                1000L,
                2L,
                "Бесплатный курс",
                BigDecimal.ZERO,
                Currency.getInstance("RUB"),
                true
        );

        CourseEnrollment enrollment = CourseEnrollment.create(
                now,
                100L,
                1000L,
                1L
        );
        enrollment.assignId(10L);

        Order savedOrder = Order.load(
                20L,
                1L,
                10L,
                OrderTargetType.COURSE,
                100L,
                1000L,
                BigDecimal.ZERO,
                Currency.getInstance("RUB"),
                OrderStatus.PAYED,
                now,
                now.plusSeconds(900)
        );

        when(bookingProperties.getBookingExpiresMinutes()).thenReturn(15L);
        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(courseApi.getInfo(100L)).thenReturn(course);
        when(courseEnrollmentRepository.saveOrUpdate(any(CourseEnrollment.class)))
                .thenReturn(enrollment);
        when(orderRepository.saveOrUpdate(any(Order.class))).thenReturn(savedOrder);

        BookingResponse result = service.bookCourse(100L);

        assertEquals("PAYED", result.orderStatus());
        assertNull(result.paymentId());
        assertNull(result.paymentStatus());

        verify(paymentApi, never()).createPayment(any());
        verify(courseEnrollmentRepository, times(2))
                .saveOrUpdate(any(CourseEnrollment.class));
    }

    @Test
    void bookCourse_shouldThrowNotFound_whenCourseNotFound() {
        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(courseApi.getInfo(100L)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> service.bookCourse(100L));

        verifyNoInteractions(paymentApi);
        verify(orderRepository, never()).saveOrUpdate(any());
    }

    @Test
    void bookCourse_shouldThrowBusinessException_whenAlreadyActive() {
        CourseBookingInfoResponse course = new CourseBookingInfoResponse(
                100L,
                1000L,
                2L,
                "Курс",
                new BigDecimal("3000"),
                Currency.getInstance("RUB"),
                true
        );

        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(courseApi.getInfo(100L)).thenReturn(course);
        when(courseEnrollmentRepository.existsActiveByUserIdAndCourseId(1L, 100L, now))
                .thenReturn(true);

        assertThrows(BusinessException.class,
                () -> service.bookCourse(100L));

        verify(orderRepository, never()).saveOrUpdate(any());
        verifyNoInteractions(paymentApi);
    }

    @Test
    void bookGroupActivitySlot_shouldCreateEnrollmentOrderAndPayment() {
        ActivitySlotBookingInfoResponse slot = new ActivitySlotBookingInfoResponse(
                200L,
                2L,
                3L,
                "Групповое занятие",
                Instant.parse("2026-05-15T10:00:00Z"),
                Instant.parse("2026-05-15T11:30:00Z"),
                3,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                true
        );

        ActivityEnrollment enrollment = ActivityEnrollment.create(now, 2L, 200L, 1L);
        enrollment.assignId(10L);

        Order savedOrder = Order.load(
                20L,
                1L,
                10L,
                OrderTargetType.ACTIVITY_SLOT,
                200L,
                null,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                OrderStatus.CREATED,
                now,
                now.plusSeconds(600)
        );

        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(activityApi.getSlotBookingInfo(200L)).thenReturn(slot);
        when(activityEnrollmentRepository.existsActiveByUserIdAndActivitySlotId(1L, 200L, now))
                .thenReturn(false);
        when(activityEnrollmentRepository.tryEnroll(2L, 200L, 1L, now))
                .thenReturn(enrollment);
        when(orderRepository.saveOrUpdate(any(Order.class))).thenReturn(savedOrder);
        when(paymentApi.createPayment(any()))
                .thenReturn(new CreatePaymentResponse(30L, "CREATED"));

        BookingResponse result = service.bookGroupActivitySlot(200L);

        assertEquals("ACTIVITY_SLOT", result.targetType());
        assertEquals(200L, result.targetId());
        assertEquals(30L, result.paymentId());

        verify(activityEnrollmentRepository).tryEnroll(2L, 200L, 1L, now);
        verify(paymentApi).createPayment(any());
    }

    @Test
    void bookGroupActivitySlot_shouldThrowBusinessException_whenSlotFull() {
        ActivitySlotBookingInfoResponse slot = new ActivitySlotBookingInfoResponse(
                200L,
                2L,
                3L,
                "Групповое занятие",
                Instant.parse("2026-05-15T10:00:00Z"),
                Instant.parse("2026-05-15T11:30:00Z"),
                3,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                true
        );

        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(activityApi.getSlotBookingInfo(200L)).thenReturn(slot);
        when(activityEnrollmentRepository.tryEnroll(2L, 200L, 1L, now))
                .thenReturn(null);

        assertThrows(BusinessException.class,
                () -> service.bookGroupActivitySlot(200L));

        verify(orderRepository, never()).saveOrUpdate(any());
    }

    @Test
    void bookIndividualActivity_shouldCreateSlotEnrollmentOrderAndPayment() {
        Instant startAt = Instant.parse("2026-05-14T10:00:00Z");

        ActivityBookingInfoResponse activity = new ActivityBookingInfoResponse(
                200L,
                2L,
                "Индивидуальное занятие",
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                true
        );

        CreatedActivitySlotResponse createdSlot =
                new CreatedActivitySlotResponse(
                        500L,
                        200L,
                        Instant.parse("2026-05-15T10:00:00Z"),
                        Instant.parse("2026-05-15T11:30:00Z")
                        );

        ActivityEnrollment enrollment = ActivityEnrollment.create(now, 2L, 500L, 1L);
        enrollment.assignId(10L);

        Order savedOrder = Order.load(
                20L,
                1L,
                10L,
                OrderTargetType.ACTIVITY_SLOT,
                500L,
                null,
                new BigDecimal("2000"),
                Currency.getInstance("RUB"),
                OrderStatus.CREATED,
                now,
                now.plusSeconds(600)
        );

        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(activityApi.getBookingInfo(400L)).thenReturn(activity);
        when(activityApi.createIndividualSlot(any())).thenReturn(createdSlot);
        when(activityEnrollmentRepository.saveOrUpdate(any(ActivityEnrollment.class)))
                .thenReturn(enrollment);
        when(orderRepository.saveOrUpdate(any(Order.class))).thenReturn(savedOrder);
        when(paymentApi.createPayment(any()))
                .thenReturn(new CreatePaymentResponse(30L, "CREATED"));

        BookingResponse result = service.bookIndividualActivity(
                400L,
                new BookIndividualActivityRequest(300L, startAt)
        );

        assertEquals("INDIVIDUAL_ACTIVITY", result.targetType());
        assertEquals(400L, result.targetId());
        assertEquals(30L, result.paymentId());

        verify(activityApi).createIndividualSlot(argThat(actual ->
                actual.activityId().equals(400L)
                && actual.activityTimeId().equals(300L)
                && actual.startAt().equals(startAt)
                && actual.studentId().equals(1L)
        ));
    }
}