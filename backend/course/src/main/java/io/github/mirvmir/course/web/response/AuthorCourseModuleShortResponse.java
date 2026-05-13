package io.github.mirvmir.course.web.response;

import java.util.UUID;

public record AuthorCourseModuleShortResponse(
        Long id,
        UUID stableModuleId,
        String title,
        Integer sortOrder
) {
}