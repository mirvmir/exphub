package io.github.mirvmir.enrollment.application.service;

import io.github.mirvmir.activity.api.ActivityApi;
import io.github.mirvmir.activity.api.dto.ActivitySlotPurchaseInfoResponse;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.course.api.CourseApi;
import io.github.mirvmir.course.api.dto.CoursePurchaseInfoResponse;
import io.github.mirvmir.course.api.dto.CourseTeacherResponse;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.enrollment.api.dto.OrderPaymentInfoResponse;
import io.github.mirvmir.enrollment.api.dto.StudentCourseEnrollmentResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Slf4j
@Service
public class DefaultEnrollmentApi implements EnrollmentApi {

    private final ActivityApi activityApi;
    private final CourseApi courseApi;
    private final PaymentApi paymentApi;

    private final ActivityEnrollmentRepository activityEnrollmentRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final OrderRepository orderRepository;

    private final BookingProperties bookingProperties;

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentOfActivity(Long userId,
                                       Long activityId) {
        log.info("Start checking paid activity access: userId={}, activityId={}",
                userId,
                activityId);
        return activityEnrollmentRepository.existsPayedByUserIdAndActivityId(
                userId,
                activityId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentOfCourse(Long userId,
                                     Long courseId) {
        log.info("Start checking paid course access: userId={}, courseId={}",
                userId,
                courseId);
        return courseEnrollmentRepository.existsPayedByUserIdAndCourseId(
                userId,
                courseId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentOfCourseEnrollment(Long userId,
                                               Long courseEnrollmentId) {
        log.info("Start checking course enrollment ownership: userId={}, courseEnrollmentId={}",
                userId,
                courseEnrollmentId);

        CourseEnrollment enrollment =
                courseEnrollmentRepository.findById(courseEnrollmentId);

        if (enrollment == null) {
            log.error("Course enrollment ownership check failed because enrollment was not found: userId={}, courseEnrollmentId={}",
                    userId,
                    courseEnrollmentId);
            return false;
        }

        return enrollment.getUserId().equals(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTeacherOfCourseEnrollment(Long teacherId,
                                               Long courseEnrollmentId) {
        log.info("Start checking teacher access to course enrollment: teacherId={}, courseEnrollmentId={}",
                teacherId,
                courseEnrollmentId);

        CourseEnrollment enrollment =
                courseEnrollmentRepository.findById(courseEnrollmentId);

        if (enrollment == null) {
            log.error("Teacher access check failed because course enrollment was not found: teacherId={}, courseEnrollmentId={}",
                    teacherId,
                    courseEnrollmentId);
            return false;
        }

        CourseTeacherResponse teacherResponse =
                courseApi.getCourseTeacher(enrollment.getCourseId());

        if (teacherResponse == null) {
            log.error("Teacher access check failed because course teacher was not resolved: teacherId={}, courseId={}",
                    teacherId,
                    enrollment.getCourseId());
            return false;
        }

        return teacherId.equals(teacherResponse.teacherId());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> countBookedByActivitySlotIds(Set<Long> activitySlotIds,
                                                           Instant now) {
        log.info("Start counting active bookings for activity slots: slotCount={}, now={}",
                activitySlotIds == null ? 0 : activitySlotIds.size(),
                now);
        return activityEnrollmentRepository.countBookedByActivitySlotIds(
                activitySlotIds,
                now
        );
    }

    @Override
    @Transactional(readOnly = true)
    public StudentCourseEnrollmentResponse getStudentCourseEnrollment(Long studentId,
                                                                      Long courseId) {
        log.info("Start loading paid course enrollment for student: studentId={}, courseId={}",
                studentId,
                courseId);

        CourseEnrollment enrollment =
                courseEnrollmentRepository.findPayedByUserIdAndCourseId(
                        studentId,
                        courseId
                );

        if (enrollment == null) {
            log.error("Paid course enrollment was not found for student: studentId={}, courseId={}",
                    studentId,
                    courseId);
            throw new NotFoundException(
                    EnrollmentErrorCode.ENROLLMENT_NOT_FOUND,
                    "Enrollment for studentId=" + studentId
                            + " and courseId=" + courseId + " not found"
            );
        }

        return new StudentCourseEnrollmentResponse(
                enrollment.getId(),
                enrollment.getUserId(),
                enrollment.getCourseId(),
                enrollment.getPublishedVersionId()
        );
    }

    @Override
    @Transactional
    public void cancelByActivitySlotIdAndStudentId(Long activitySlotId,
                                                   Long studentId,
                                                   String reason) {
        log.info("Start student activity cancellation: activitySlotId={}, studentId={}, reason={}",
                activitySlotId,
                studentId,
                reason);

        ActivityEnrollment enrollment =
                activityEnrollmentRepository.findActiveByActivitySlotIdAndStudentId(
                        activitySlotId,
                        studentId
                );

        if (enrollment == null) {
            log.error("Student activity cancellation failed because active enrollment was not found: activitySlotId={}, studentId={}",
                    activitySlotId,
                    studentId);
            throw new NotFoundException(
                    EnrollmentErrorCode.ENROLLMENT_NOT_FOUND,
                    "Enrollment for activitySlotId=" + activitySlotId
                            + " and studentId=" + studentId + " not found"
            );
        }

        Order order = orderRepository.findByEnrollmentIdForUpdate(
                enrollment.getId(),
                OrderTargetType.ACTIVITY_SLOT
        );

        if (order == null) {
            log.error("Student activity cancellation failed because order was not found: enrollmentId={}, activitySlotId={}, studentId={}",
                    enrollment.getId(),
                    activitySlotId,
                    studentId);
            throw new NotFoundException(
                    EnrollmentErrorCode.ORDER_NOT_FOUND,
                    "Order for enrollmentId=" + enrollment.getId() + " not found"
            );
        }

        enrollment.cancelByStudent();
        cancelOrder(order, reason);

        activityEnrollmentRepository.saveOrUpdate(enrollment);
        orderRepository.saveOrUpdate(order);
    }

    @Override
    @Transactional
    public void cancelAllByActivitySlotId(Long activitySlotId,
                                          String reason) {
        log.info("Start cancelling all active activity enrollments by slot: activitySlotId={}, reason={}",
                activitySlotId,
                reason);

        List<ActivityEnrollment> enrollments =
                activityEnrollmentRepository.findActiveByActivitySlotId(activitySlotId);

        if (enrollments.isEmpty()) {
            log.info("No active activity enrollments found for bulk cancellation: activitySlotId={}",
                    activitySlotId);
            return;
        }

        for (ActivityEnrollment enrollment : enrollments) {
            Order order = orderRepository.findByEnrollmentIdForUpdate(
                    enrollment.getId(),
                    OrderTargetType.ACTIVITY_SLOT
            );

            enrollment.cancel();

            if (order != null) {
                cancelOrder(order, reason);
                orderRepository.saveOrUpdate(order);
            } else {
                log.error("Active activity enrollment was cancelled without linked order: enrollmentId={}, activitySlotId={}",
                        enrollment.getId(),
                        activitySlotId);
            }

            activityEnrollmentRepository.saveOrUpdate(enrollment);
        }
    }

    @Override
    @Transactional
    public void refundPayedCourseEnrollments(Long courseId,
                                             String reason) {
        log.info("Start refunding paid course enrollments: courseId={}, reason={}",
                courseId,
                reason);

        List<CourseEnrollment> enrollments =
                courseEnrollmentRepository.findPayedByCourseId(courseId);

        if (enrollments.isEmpty()) {
            log.info("No paid course enrollments found for refund: courseId={}",
                    courseId);
            return;
        }

        for (CourseEnrollment enrollment : enrollments) {
            Order order = orderRepository.findByEnrollmentIdForUpdate(
                    enrollment.getId(),
                    OrderTargetType.COURSE
            );

            if (order == null) {
                log.error("Cannot refund paid course enrollment because order was not found: enrollmentId={}, courseId={}",
                        enrollment.getId(),
                        courseId);
                throw new NotFoundException(EnrollmentErrorCode.ORDER_NOT_FOUND);
            }

            enrollment.cancel();
            cancelOrder(order, reason);

            orderRepository.saveOrUpdate(order);
            courseEnrollmentRepository.saveOrUpdate(enrollment);
        }
    }

    @Override
    @Transactional
    public void refundPayedActivityEnrollments(Long activitySlotId,
                                               String reason) {
        log.info("Start refunding paid activity enrollments: activitySlotId={}, reason={}",
                activitySlotId,
                reason);

        List<ActivityEnrollment> enrollments =
                activityEnrollmentRepository.findPayedByActivitySlotId(activitySlotId);

        if (enrollments.isEmpty()) {
            log.info("No paid activity enrollments found for refund: activitySlotId={}",
                    activitySlotId);
            return;
        }

        for (ActivityEnrollment enrollment : enrollments) {
            Order order = orderRepository.findByEnrollmentIdForUpdate(
                    enrollment.getId(),
                    OrderTargetType.ACTIVITY_SLOT
            );

            if (order == null) {
                log.error("Cannot refund paid activity enrollment because order was not found: enrollmentId={}, activitySlotId={}",
                        enrollment.getId(),
                        activitySlotId);
                throw new NotFoundException(EnrollmentErrorCode.ORDER_NOT_FOUND);
            }

            enrollment.cancel();
            cancelOrder(order, reason);

            orderRepository.saveOrUpdate(order);
            activityEnrollmentRepository.saveOrUpdate(enrollment);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderPaymentInfoResponse getPaymentInfo(Long orderId,
                                                   Instant now) {
        log.info("Start loading payment info: orderId={}, now={}",
                orderId,
                now);
        Order order = getExistingOrder(orderId);

        return toPaymentInfo(order, now);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrderExpired(Long orderId,
                                  Instant now) {
        log.info("Start checking order expiration: orderId={}, now={}",
                orderId,
                now);
        Order order = getExistingOrder(orderId);

        return order.isExpired(now);
    }

    @Override
    @Transactional
    public void markPaymentProcessing(Long orderId,
                                      Instant now) {
        log.info("Start marking order as payment processing: orderId={}, now={}",
                orderId,
                now);
        Order order = getExistingOrderForUpdate(orderId);

        try {
            order.markPaymentProcessing(now);
        } catch (RuntimeException exception) {
            log.error("Order cannot be moved to payment processing: orderId={}, status={}, expiresAt={}, now={}",
                    orderId,
                    order.getStatus(),
                    order.getExpiresAt(),
                    now,
                    exception);
            throw exception;
        }

        orderRepository.saveOrUpdate(order);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrderCancelled(Long orderId) {
        log.info("Start checking order cancellation: orderId={}",
                orderId);
        Order order = getExistingOrder(orderId);
        return order.isCancelled();
    }

    @Override
    @Transactional
    public void expireOrder(Long orderId,
                            Instant now) {
        log.info("Start expiring order from API: orderId={}, now={}",
                orderId,
                now);
        Order order = getExistingOrderForUpdate(orderId);

        try {
            order.expire(now);
        } catch (RuntimeException exception) {
            log.error("Manual order expiration failed: orderId={}, status={}, expiresAt={}, now={}",
                    orderId,
                    order.getStatus(),
                    order.getExpiresAt(),
                    now,
                    exception);
            throw exception;
        }

        if (OrderTargetType.COURSE == order.getTargetType()) {
            CourseEnrollment enrollment =
                    courseEnrollmentRepository.findById(order.getEnrollmentId());

            if (enrollment != null) {
                enrollment.expire(now, bookingProperties.getBookingExpiresMinutes());
                courseEnrollmentRepository.saveOrUpdate(enrollment);
            } else {
                log.error("Course enrollment was not found while expiring order: orderId={}, enrollmentId={}",
                        orderId,
                        order.getEnrollmentId());
            }
        }

        if (OrderTargetType.ACTIVITY_SLOT == order.getTargetType()) {
            ActivityEnrollment enrollment =
                    activityEnrollmentRepository.findById(order.getEnrollmentId());

            if (enrollment != null) {
                enrollment.expire(now, bookingProperties.getBookingExpiresMinutes());
                activityEnrollmentRepository.saveOrUpdate(enrollment);
            } else {
                log.error("Activity enrollment was not found while expiring order: orderId={}, enrollmentId={}",
                        orderId,
                        order.getEnrollmentId());
            }
        }

        orderRepository.saveOrUpdate(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTeacherIdByOrderId(Long orderId) {
        log.info("Start resolving teacher by order: orderId={}",
                orderId);
        Order order = orderRepository.findById(orderId);

        if (order == null) {
            log.error("Teacher resolving failed because order was not found: orderId={}",
                    orderId);
            throw new NotFoundException(
                    EnrollmentErrorCode.ORDER_NOT_FOUND,
                    "Order with id=" + orderId + " not found"
            );
        }

        if (OrderTargetType.ACTIVITY_SLOT == order.getTargetType()) {
            ActivityEnrollment enrollment =
                    activityEnrollmentRepository.findById(order.getEnrollmentId());

            if (enrollment == null) {
                log.error("Teacher resolving failed because activity enrollment was not found: orderId={}, enrollmentId={}",
                        orderId,
                        order.getEnrollmentId());
                throw new NotFoundException(
                        EnrollmentErrorCode.ENROLLMENT_NOT_FOUND,
                        "Enrollment with id=" + order.getEnrollmentId() + " not found"
                );
            }

            ActivitySlotPurchaseInfoResponse activitySlot =
                    activityApi.getSlotPurchaseInfo(enrollment.getActivitySlotId());

            if (activitySlot == null) {
                log.error("Teacher resolving failed because activity slot info was not found: orderId={}, activitySlotId={}",
                        orderId,
                        enrollment.getActivitySlotId());
                throw new NotFoundException(EnrollmentErrorCode.ACTIVITY_SLOT_NOT_FOUND);
            }

            return activitySlot.authorId();
        }

        if (OrderTargetType.COURSE == order.getTargetType()) {
            CourseEnrollment enrollment =
                    courseEnrollmentRepository.findById(order.getEnrollmentId());

            if (enrollment == null) {
                log.error("Teacher resolving failed because course enrollment was not found: orderId={}, enrollmentId={}",
                        orderId,
                        order.getEnrollmentId());
                throw new NotFoundException(
                        EnrollmentErrorCode.ENROLLMENT_NOT_FOUND,
                        "Enrollment with id=" + order.getEnrollmentId() + " not found"
                );
            }

            CoursePurchaseInfoResponse course =
                    courseApi.getInfo(enrollment.getCourseId());

            if (course == null) {
                log.error("Teacher resolving failed because course info was not found: orderId={}, courseId={}",
                        orderId,
                        enrollment.getCourseId());
                throw new NotFoundException(EnrollmentErrorCode.COURSE_NOT_FOUND);
            }

            return course.authorId();
        }

        log.error("Teacher resolving failed because order target type is unsupported: orderId={}, targetType={}",
                orderId,
                order.getTargetType());
        throw new IllegalStateException(
                "Unsupported order type: " + order.getTargetType()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserAccessFile(Long userId,
                                     Long fileId) {
        log.info("Start checking file access by paid course enrollment: userId={}, fileId={}",
                userId,
                fileId);
        if (userId == null || fileId == null) {
            log.error("File access check skipped because required identifiers are missing: userId={}, fileId={}",
                    userId,
                    fileId);
            return false;
        }

        Set<Long> courseIds = courseApi.findCourseIdsByFileId(fileId);

        if (courseIds == null || courseIds.isEmpty()) {
            log.error("File access denied because file is not linked to courses: userId={}, fileId={}",
                    userId,
                    fileId);
            return false;
        }

        return courseEnrollmentRepository.existsPayedByUserIdAndCourseIds(
                userId,
                courseIds
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserAccessVideo(Long userId, Long videoId) {
        log.info("Start checking video access by paid course enrollment: userId={}, videoId={}",
                userId,
                videoId);
        if (userId == null || videoId == null) {
            log.error("Video access check skipped because required identifiers are missing: userId={}, videoId={}",
                    userId,
                    videoId);
            return false;
        }

        Set<Long> courseIds = courseApi.findCourseIdsByVideoId(videoId);

        if (courseIds == null || courseIds.isEmpty()) {
            log.error("Video access denied because video is not linked to courses: userId={}, videoId={}",
                    userId,
                    videoId);
            return false;
        }

        return courseEnrollmentRepository.existsPayedByUserIdAndCourseIds(
                userId,
                courseIds
        );
    }

    private void cancelOrder(Order order, String reason) {
        boolean wasPayed = order.isPayed();
        boolean wasPaymentProcessing = order.isPaymentProcessing();

        order.cancel();

        if (wasPayed) {
            requestRefund(order, reason);
            return;
        }

        if (wasPaymentProcessing) {
            log.info("Payment-processing order was cancelled without immediate refund; refund will be requested if payment succeeds later: orderId={}",
                    order.getId());
        }
    }

    private void requestRefund(Order order, String reason) {
        log.info("Requesting refund during cancellation: orderId={}, amount={}, currency={}, reason={}",
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
            log.error("Refund request during cancellation failed: orderId={}, targetType={}, targetId={}",
                    order.getId(),
                    order.getTargetType(),
                    order.getTargetId(),
                    exception);
            throw exception;
        }
    }

    private Order getExistingOrder(Long orderId) {
        Order order = orderRepository.findById(orderId);

        if (order == null) {
            log.error("Order lookup failed: orderId={}",
                    orderId);
            throw new NotFoundException(
                    EnrollmentErrorCode.ORDER_NOT_FOUND,
                    "Order with id=" + orderId + " not found"
            );
        }

        return order;
    }

    private Order getExistingOrderForUpdate(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId);

        if (order == null) {
            log.error("Order lookup for update failed: orderId={}",
                    orderId);
            throw new NotFoundException(
                    EnrollmentErrorCode.ORDER_NOT_FOUND,
                    "Order with id=" + orderId + " not found"
            );
        }

        return order;
    }

    private OrderPaymentInfoResponse toPaymentInfo(Order order,
                                                   Instant now) {
        return new OrderPaymentInfoResponse(
                order.getId(),
                order.getUserId(),
                order.getEnrollmentId(),
                order.getTargetType().name(),
                order.getTargetId(),
                order.getTargetVersionId(),
                order.getAmount(),
                order.getCurrency(),
                order.getExpiresAt(),
                order.isExpired(now),
                order.isPaymentProcessing(),
                order.isPayed(),
                order.isFinalStatus()
        );
    }
}
