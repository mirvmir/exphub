package io.github.mirvmir.taxonomy.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSectionRequest(
        @Size(max = 255)
        @NotBlank
        String name
) {
}
