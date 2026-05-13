package io.github.mirvmir.course.web.response;

import java.util.List;

public record StudentCourseResponse(
        Long courseId,
        Long courseVersionId,
        Long enrollmentId,
        String title,
        List<StudentCourseModuleShortResponse> modules
) {
}