package io.github.mirvmir.course.web.response;

import java.util.List;
import java.util.UUID;

public record StudentCourseModuleResponse(
        UUID stableModuleId,
        String title,
        Integer sortOrder,
        List<StudentCourseLessonShortResponse> lessons
) {
}