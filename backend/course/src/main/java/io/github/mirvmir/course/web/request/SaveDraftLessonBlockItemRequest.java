package io.github.mirvmir.course.web.request;

import io.github.mirvmir.course.domain.content.LessonContentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record SaveDraftLessonBlockItemRequest(
        @Positive
        Long id,
        String uiId,
        @NotNull
        LessonContentType type,
        String html,
        @Positive
        Long fileAssetId,
        @Positive
        Long videoAssetId,
        @NotNull
        @PositiveOrZero
        Integer sortOrder
) {
}