package io.github.mirvmir.course.api.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CourseLessonInfoResponse(
        Long courseId,
        Long publishedVersionId,
        Long courseModuleId,
        Long courseLessonId,
        UUID stableLessonId,
        Instant opensAt,
        boolean isPractice,
        Set<Long> moduleLessonIds,
        Set<Long> courseLessonIds
) {
}