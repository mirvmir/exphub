package io.github.mirvmir.course.web.response;

import io.github.mirvmir.course.domain.LessonType;

import java.util.UUID;

public record AuthorCourseLessonShortResponse(
        Long id,
        UUID stableLessonId,
        String title,
        LessonType type,
        Integer sortOrder
) {
}