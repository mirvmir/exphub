package io.github.mirvmir.taxonomy.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSubjectRequest(
        @NotBlank
        @Size(max = 255)
        String name
) {
}
