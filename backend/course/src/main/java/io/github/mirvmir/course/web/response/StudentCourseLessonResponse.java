package io.github.mirvmir.course.web.response;

import io.github.mirvmir.course.domain.LessonType;

import java.util.List;
import java.util.UUID;

public record StudentCourseLessonResponse(
        UUID stableLessonId,
        String title,
        LessonType type,
        Integer sortOrder,
        List<StudentLessonBlockResponse> blocks
) {
}