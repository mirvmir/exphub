package io.github.mirvmir.course.web.request;

import io.github.mirvmir.course.domain.content.LessonContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record SaveDraftLessonBlockItemRequest(
        @Positive
        Long id,
        String uiId,
        LessonContentType type,
        String html,
        @Positive
        Long fileAssetId,
        @Positive
        Long videoAssetId,
        @NotBlank
        @PositiveOrZero
        Integer sortOrder
) {
}