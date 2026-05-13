package io.github.mirvmir.enrollment.api.dto;

public record StudentCourseEnrollmentResponse(
        Long enrollmentId,
        Long studentId,
        Long courseId,
        Long courseVersionId
) {
}