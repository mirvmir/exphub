package io.github.mirvmir.enrollment.api.dto;

public record StudentActivityEnrollmentResponse(
        Long enrollmentId,
        Long studentId,
        Long activityId,
        Long activitySlotId
) {
}