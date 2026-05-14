package io.github.mirvmir.course.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record SaveDraftModuleItemRequest(
        @Positive
        Long id,
        String uiId,
        @NotBlank
        String title,
        @NotNull
        @PositiveOrZero
        Integer sortOrder
) {
}