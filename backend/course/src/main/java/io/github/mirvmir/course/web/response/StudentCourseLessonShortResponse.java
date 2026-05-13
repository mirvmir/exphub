package io.github.mirvmir.course.web.response;

import io.github.mirvmir.course.domain.LessonType;

import java.time.Instant;
import java.util.UUID;

public record StudentCourseLessonShortResponse(
        UUID stableLessonId,
        String title,
        LessonType type,
        Integer sortOrder,
        Instant opensAt,
        Boolean opened
) {
}