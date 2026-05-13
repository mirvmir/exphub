package io.github.mirvmir.course.web.response;

import java.util.List;
import java.util.UUID;

public record AuthorCourseModuleResponse(
        Long id,
        UUID stableModuleId,
        String title,
        Integer sortOrder,
        Boolean canEdit,
        List<AuthorCourseLessonShortResponse> lessons
) {
}