package io.github.mirvmir.enrollment.application.service.implementation;

import io.github.mirvmir.activity.api.ActivityApi;
import io.github.mirvmir.activity.api.dto.ActivityPurchaseInfoResponse;
import io.github.mirvmir.activity.api.dto.ActivitySlotPurchaseInfoResponse;
import io.github.mirvmir.activity.api.dto.CreateIndividualActivitySlotRequest;
import io.github.mirvmir.activity.api.dto.CreatedActivitySlotResponse;
import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.common.exception.UnauthorizedException;
import io.github.mirvmir.course.api.CourseApi;
import io.github.mirvmir.course.api.dto.CoursePurchaseInfoResponse;
import io.github.mirvmir.enrollment.application.properties.BookingProperties;
import io.github.mirvmir.enrollment.application.service.interfaces.EnrollmentBookingService;
import io.github.mirvmir.enrollment.application.service.port.repository.ActivityEnrollmentRepository;
import io.github.mirvmir.enrollment.application.service.port.repository.CourseEnrollmentRepository;
import io.github.mirvmir.enrollment.application.service.port.repository.OrderRepository;
import io.github.mirvmir.enrollment.domain.ActivityEnrollment;
import io.github.mirvmir.enrollment.domain.CourseEnrollment;
import io.github.mirvmir.enrollment.domain.order.Order;
import io.github.mirvmir.enrollment.exception.EnrollmentErrorCode;
import io.github.mirvmir.enrollment.web.request.BookIndividualActivityRequest;
import io.github.mirvmir.enrollment.web.response.BookingResponse;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.payment.api.PaymentApi;
import io.github.mirvmir.payment.api.dto.CreatePaymentRequest;
import io.github.mirvmir.payment.api.dto.CreatePaymentResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

@AllArgsConstructor
@Slf4j
@Service
public class DefaultEnrollmentBookingService implements EnrollmentBookingService {

    private final IdentityApi identityApi;

    private final CourseApi courseApi;
    private final ActivityApi activityApi;
    private final PaymentApi paymentApi;

    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final ActivityEnrollmentRepository activityEnrollmentRepository;
    private final OrderRepository orderRepository;

    private final Clock clock;

    private final BookingProperties bookingProperties;

    @Override
    @Transactional
    public BookingResponse bookCourse(Long courseId) {
        Long userId = identityApi.getCurrentUserId();
        Instant now = Instant.now(clock);

        if (userId == null) {
            log.error("Unauthorized book course request");

            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        log.debug("Course booking requested: userId={}, courseId={}",
                userId,
                courseId);

        CoursePurchaseInfoResponse course = courseApi.getInfo(courseId);

        if (course == null) {
            log.error("Course booking stopped because course purchase info was not found: userId={}, courseId={}",
                    userId,
                    courseId);
            throw new NotFoundException(EnrollmentErrorCode.COURSE_NOT_FOUND);
        }

        if (courseEnrollmentRepository.existsActiveByUserIdAndCourseId(
                userId,
                courseId,
                now
        )) {
            log.error("Course booking rejected because student already has active enrollment: userId={}, courseId={}",
                    userId,
                    courseId);
            throw new BusinessException(EnrollmentErrorCode.COURSE_ALREADY_PAYED);
        }

        CourseEnrollment enrollment = CourseEnrollment.create(
                now,
                course.courseId(),
                course.publishedVersionId(),
                userId
        );

        CourseEnrollment savedEnrollment =
                courseEnrollmentRepository.saveOrUpdate(enrollment);

        Order order = Order.createForCourse(
                userId,
                savedEnrollment.getId(),
                course.courseId(),
                course.publishedVersionId(),
                course.priceAmount(),
                course.priceCurrency(),
                now,
                bookingProperties.getBookingExpiresMinutes()
        );

        if (isFree(course.priceAmount())) {
            savedEnrollment.confirmPayment(now, bookingProperties.getBookingExpiresMinutes());
            order.markPayed(now);

            courseEnrollmentRepository.saveOrUpdate(savedEnrollment);

            Order savedOrder = orderRepository.saveOrUpdate(order);

            return new BookingResponse(
                    savedOrder.getId(),
                    null,
                    "COURSE",
                    course.courseId(),
                    course.title(),
                    course.priceAmount(),
                    course.priceCurrency(),
                    savedOrder.getStatus().name(),
                    null
            );
        }

        Order savedOrder = orderRepository.saveOrUpdate(order);

        CreatePaymentResponse payment = createPayment(
                userId,
                course.priceAmount(),
                course.priceCurrency(),
                "Оплата курса: " + course.title(),
                savedOrder.getId(),
                "course",
                course.courseId()
        );

        log.info("Course booking completed: userId={}, courseId={}",
                userId,
                courseId);
        return new BookingResponse(
                savedOrder.getId(),
                payment.paymentId(),
                "COURSE",
                course.courseId(),
                course.title(),
                course.priceAmount(),
                course.priceCurrency(),
                savedOrder.getStatus().name(),
                payment.status()
        );
    }

    @Override
    @Transactional
    public BookingResponse bookGroupActivitySlot(Long activitySlotId) {
        Long userId = identityApi.getCurrentUserId();
        Instant now = Instant.now(clock);

        if (userId == null) {
            log.error("Unauthorized book group activity request");
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        log.debug("Group activity slot booking requested: userId={}, activitySlotId={}",
                userId,
                activitySlotId);

        ActivitySlotPurchaseInfoResponse slot =
                activityApi.getSlotPurchaseInfo(activitySlotId);

        if (slot == null) {
            log.error("Group activity booking stopped because slot purchase info was not found: userId={}, activitySlotId={}",
                    userId,
                    activitySlotId);
            throw new NotFoundException(
                    EnrollmentErrorCode.ACTIVITY_SLOT_NOT_FOUND
            );
        }

        if (activityEnrollmentRepository.existsActiveByUserIdAndActivitySlotId(
                userId,
                activitySlotId,
                now
        )) {
            log.error("Group activity booking rejected because student already holds this slot: userId={}, activitySlotId={}",
                    userId,
                    activitySlotId);
            throw new BusinessException(
                    EnrollmentErrorCode.ACTIVITY_ALREADY_BOOKED
            );
        }

        ActivityEnrollment enrollment =
                activityEnrollmentRepository.tryEnroll(
                        slot.activityId(),
                        activitySlotId,
                        userId,
                        now
                );

        if (enrollment == null) {
            log.error("Group activity booking rejected because slot capacity is exhausted: userId={}, activitySlotId={}",
                    userId,
                    activitySlotId);
            throw new BusinessException(
                    EnrollmentErrorCode.ACTIVITY_SLOT_FULL
            );
        }

        Order order = Order.createForActivitySlot(
                userId,
                enrollment.getId(),
                activitySlotId,
                slot.priceAmount(),
                slot.priceCurrency(),
                now,
                bookingProperties.getBookingExpiresMinutes()
        );

        if (isFree(slot.priceAmount())) {
            enrollment.confirmPayment(now, bookingProperties.getBookingExpiresMinutes());
            order.markPayed(now);

            activityEnrollmentRepository.saveOrUpdate(enrollment);
            Order savedOrder = orderRepository.saveOrUpdate(order);

            return new BookingResponse(
                    savedOrder.getId(),
                    null,
                    "ACTIVITY_SLOT",
                    activitySlotId,
                    slot.title(),
                    slot.priceAmount(),
                    slot.priceCurrency(),
                    savedOrder.getStatus().name(),
                    null
            );
        }

        Order savedOrder = orderRepository.saveOrUpdate(order);

        CreatePaymentResponse payment = createPayment(
                userId,
                slot.priceAmount(),
                slot.priceCurrency(),
                "Оплата занятия: " + slot.title(),
                savedOrder.getId(),
                "activity slot",
                activitySlotId
        );

        return new BookingResponse(
                savedOrder.getId(),
                payment.paymentId(),
                "ACTIVITY_SLOT",
                activitySlotId,
                slot.title(),
                slot.priceAmount(),
                slot.priceCurrency(),
                savedOrder.getStatus().name(),
                payment.status()
        );
    }

    @Override
    @Transactional
    public BookingResponse bookIndividualActivity(Long activityId,
                                                  BookIndividualActivityRequest request) {
        Long userId = identityApi.getCurrentUserId();
        Instant now = Instant.now(clock);

        if (userId == null) {
            log.info("Unauthorized book individual activity request");
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        log.info("Individual activity booking requested: userId={}, activityId={}, activityTimeId={}, startAt={}",
                userId,
                activityId,
                request.activityTimeId(),
                request.startAt());

        ActivityPurchaseInfoResponse activity =
                activityApi.getPurchaseInfo(activityId);

        if (activity == null) {
            log.error("Individual activity booking stopped because activity purchase info was not found: userId={}, activityId={}",
                    userId,
                    activityId);
            throw new NotFoundException(EnrollmentErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + activityId + " not found");
        }

        CreatedActivitySlotResponse createdSlot;
        try {
            createdSlot = activityApi.createIndividualSlot(
                    new CreateIndividualActivitySlotRequest(
                            activityId,
                            request.activityTimeId(),
                            request.startAt(),
                            userId
                    )
            );
        } catch (RuntimeException exception) {
            log.error("Individual activity slot creation failed before enrollment was saved: userId={}, activityId={}, activityTimeId={}, startAt={}",
                    userId,
                    activityId,
                    request.activityTimeId(),
                    request.startAt(),
                    exception);
            throw exception;
        }

        if (createdSlot == null) {
            log.error("Individual activity slot creation returned empty response: userId={}, activityId={}, activityTimeId={}",
                    userId,
                    activityId,
                    request.activityTimeId());
            throw new BusinessException(EnrollmentErrorCode.ACTIVITY_SLOT_NOT_FOUND);
        }

        ActivityEnrollment enrollment = ActivityEnrollment.create(
                now,
                activityId,
                createdSlot.activitySlotId(),
                userId
        );

        ActivityEnrollment savedEnrollment =
                activityEnrollmentRepository.saveOrUpdate(enrollment);

        Order order = Order.createForActivitySlot(
                userId,
                savedEnrollment.getId(),
                createdSlot.activitySlotId(),
                activity.priceAmount(),
                activity.priceCurrency(),
                now,
                bookingProperties.getBookingExpiresMinutes()
        );

        if (isFree(activity.priceAmount())) {
            savedEnrollment.confirmPayment(now, bookingProperties.getBookingExpiresMinutes());
            order.markPayed(now);

            activityEnrollmentRepository.saveOrUpdate(savedEnrollment);
            Order savedOrder = orderRepository.saveOrUpdate(order);

            return new BookingResponse(
                    savedOrder.getId(),
                    null,
                    "INDIVIDUAL_ACTIVITY",
                    activityId,
                    activity.title(),
                    activity.priceAmount(),
                    activity.priceCurrency(),
                    savedOrder.getStatus().name(),
                    null
            );
        }

        Order savedOrder = orderRepository.saveOrUpdate(order);

        CreatePaymentResponse payment = createPayment(
                userId,
                activity.priceAmount(),
                activity.priceCurrency(),
                "Оплата индивидуального занятия: " + activity.title(),
                savedOrder.getId(),
                "individual activity",
                activityId
        );

        return new BookingResponse(
                savedOrder.getId(),
                payment.paymentId(),
                "INDIVIDUAL_ACTIVITY",
                activityId,
                activity.title(),
                activity.priceAmount(),
                activity.priceCurrency(),
                savedOrder.getStatus().name(),
                payment.status()
        );
    }

    private CreatePaymentResponse createPayment(Long userId,
                                                BigDecimal amount,
                                                java.util.Currency currency,
                                                String description,
                                                Long orderId,
                                                String targetName,
                                                Long targetId) {
        log.debug("Payment creation requested: orderId={}, targetName={}, targetId={}, userId={}, amount={}, currency={}",
                orderId,
                targetName,
                targetId,
                userId,
                amount,
                currency);
        try {
            CreatePaymentResponse payment = paymentApi.createPayment(
                    new CreatePaymentRequest(
                            userId,
                            amount,
                            currency,
                            description,
                            orderId
                    )
            );

            if (payment == null) {
                log.error("Payment service returned empty response: orderId={}, targetName={}, targetId={}, userId={}",
                        orderId,
                        targetName,
                        targetId,
                        userId);
                throw new IllegalStateException("Payment response is null");
            }

            log.info("Payment creation completed for booking: orderId={}, targetName={}, targetId={}, userId={}, amount={}, currency={}",
                    orderId,
                    targetName,
                    targetId,
                    userId,
                    amount,
                    currency);
            return payment;
        } catch (RuntimeException exception) {
            log.error("Payment creation failed for booking: orderId={}, targetName={}, targetId={}, userId={}, amount={}, currency={}",
                    orderId,
                    targetName,
                    targetId,
                    userId,
                    amount,
                    currency,
                    exception);
            throw exception;
        }
    }

    private boolean isFree(BigDecimal amount) {
        return amount == null || BigDecimal.ZERO.compareTo(amount) == 0;
    }
}
