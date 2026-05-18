package io.github.mirvmir.enrollment.api;

import io.github.mirvmir.enrollment.api.dto.StudentCourseEnrollmentResponse;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public interface EnrollmentApi {
    boolean isStudentOfActivity(Long userId, Long activityId);
    boolean isStudentOfCourse(Long userId, Long courseId);
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

    boolean isOrderExpired(Long orderId, Instant now);
    void markPaymentProcessing(Long orderId, Instant now);
    boolean isOrderCancelled(Long orderId);
    Long getTeacherIdByOrderId(Long orderId);

    void expireOrder(Long orderId, Instant now);

    boolean canUserAccessFile(Long userId,
                              Long fileId);
    boolean canUserAccessVideo(Long userId,
                               Long videoId);
}
