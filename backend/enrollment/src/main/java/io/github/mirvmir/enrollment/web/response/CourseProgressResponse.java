package io.github.mirvmir.enrollment.web.response;

import java.math.BigDecimal;

public record CourseProgressResponse(
        Long courseEnrollmentId,
        Long courseId,
        Long courseModuleId,
        Long courseLessonId,
        BigDecimal lessonProgressPercent,
        BigDecimal moduleProgressPercent,
        BigDecimal courseProgressPercent,
        String enrollmentStatus
) {
}