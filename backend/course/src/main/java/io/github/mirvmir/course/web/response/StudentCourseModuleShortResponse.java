package io.github.mirvmir.course.web.response;

import java.util.UUID;

public record StudentCourseModuleShortResponse(
        UUID stableModuleId,
        String title,
        Integer sortOrder
) {
}