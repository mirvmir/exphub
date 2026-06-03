package io.github.mirvmir.course.web.request;

import io.github.mirvmir.course.domain.LessonType;
import jakarta.validation.constraints.*;

public record SaveDraftLessonItemRequest(
        @Positive
        Long id,
        String uiId,
        @NotBlank
        @Size(max = 250)
        String title,
        @NotNull
        LessonType type,
        @NotNull
        @PositiveOrZero
        Integer sortOrder
) {
}
