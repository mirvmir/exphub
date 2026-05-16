package io.github.mirvmir.taxonomy.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTopicSuggestionRequest(
        @NotBlank
        @Size(max = 255)
        String name,
        @Size(max = 255)
        String description
) {
}