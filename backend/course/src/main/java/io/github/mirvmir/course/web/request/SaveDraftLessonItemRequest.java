package io.github.mirvmir.course.web.request;

import io.github.mirvmir.course.domain.LessonType;

public record SaveDraftLessonItemRequest(
        Long id,
        String uiId,
        String title,
        LessonType type,
        Integer sortOrder
) {
}
