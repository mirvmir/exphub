package io.github.mirvmir.taxonomy.web.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSectionRequest(
        @NotBlank
        String name
) {
}
