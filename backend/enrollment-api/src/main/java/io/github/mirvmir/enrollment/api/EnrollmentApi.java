package io.github.mirvmir.enrollment.api;

import io.github.mirvmir.enrollment.api.dto.OrderPaymentInfoResponse;
import io.github.mirvmir.enrollment.api.dto.StudentCourseEnrollmentResponse;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public interface EnrollmentApi {
    boolean isStudentOfActivity(Long userId, Long activityId);
    boolean isStudentOfCourse(Long userId, Long courseId);
    boolean isStudentOfCourseEnrollment(Long userId, Long courseEnrollmentId);
    boolean isTeacherOfCourseEnrollment(Long teacherId, Long courseEnrollmentId);
    Map<Long, Integer> countBookedByActivitySlotIds(Set<Long> activitySlotIds,
                                                    Instant now);
    StudentCourseEnrollmentResponse getStudentCourseEnrollment(Long studentId,
                                                               Long courseId);

    void cancelByActivitySlotIdAndStudentId(Long activitySlotId,
                                            Long studentId,
                                            String reason);
    void cancelAllByActivitySlotId(Long activitySlotId,
                                   String reason);

    void refundPayedCourseEnrollments(Long courseId,
                                      String reason);
    void refundPayedActivityEnrollments(Long activitySlotId,
                                        String reason);

    OrderPaymentInfoResponse getPaymentInfo(Long orderId, Instant now);
    boolean isOrderExpired(Long orderId, Instant now);
    void markPaymentProcessing(Long orderId, Instant now);
    boolean isOrderCancelled(Long orderId);

    void expireOrder(Long orderId, Instant now);

    Long getTeacherIdByOrderId(Long orderId);

    boolean canUserAccessFile(Long userId,
                              Long fileId);
    boolean canUserAccessVideo(Long userId,
                               Long videoId);
}
