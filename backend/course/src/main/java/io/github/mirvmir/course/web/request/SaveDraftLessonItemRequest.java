package io.github.mirvmir.course.web.request;

import io.github.mirvmir.course.domain.LessonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record SaveDraftLessonItemRequest(
        @Positive
        Long id,
        String uiId,
        @NotBlank
        String title,
        LessonType type,
        @NotNull
        @PositiveOrZero
        Integer sortOrder
) {
}
