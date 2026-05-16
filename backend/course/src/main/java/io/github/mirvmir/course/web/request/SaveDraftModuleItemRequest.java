package io.github.mirvmir.course.web.request;

import jakarta.validation.constraints.*;

public record SaveDraftModuleItemRequest(
        @Positive
        Long id,
        String uiId,
        @NotBlank
        @Size(max = 250)
        String title,
        @NotNull
        @PositiveOrZero
        Integer sortOrder
) {
}